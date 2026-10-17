package com.example

import android.graphics.Bitmap
import android.graphics.Color
import com.example.core.cv.InpaintingEngine
import com.example.core.cv.MaskOperations
import com.example.core.cv.MethodSelector
import com.example.core.cv.ProcessingQualityAnalyzer
import com.example.domain.model.NormalizedRect
import com.example.domain.model.RemovalMethod
import com.example.domain.model.VideoInfo
import com.example.domain.model.WatermarkMask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WatermarkStudioEngineTest {

    @Test
    fun testCoordinateConversion() {
        val norm = NormalizedRect(left = 0.5f, top = 0.25f, right = 0.75f, bottom = 0.5f)
        val abs = norm.toAbsolute(width = 1920, height = 1080)

        assertEquals(960f, abs.left, 0.1f)
        assertEquals(270f, abs.top, 0.1f)
        assertEquals(1440f, abs.right, 0.1f)
        assertEquals(540f, abs.bottom, 0.1f)
        assertEquals(480f, abs.width(), 0.1f)
        assertEquals(270f, abs.height(), 0.1f)
    }

    @Test
    fun testMaskDilationExpandsWhiteRegion() {
        // Create 20x20 black mask with a 4x4 white square in center
        val mask = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        for (y in 8..11) {
            for (x in 8..11) {
                mask.setPixel(x, y, 0xFFFFFFFF.toInt())
            }
        }

        val dilated = MaskOperations.dilateMask(mask, radius = 2)

        // Center pixel remains white
        assertEquals(0xFFFFFFFF.toInt(), dilated.getPixel(9, 9))
        // (6, 9) was black, should now be dilated to white
        assertEquals(0xFFFFFFFF.toInt(), dilated.getPixel(6, 9))
    }

    @Test
    fun testFeatherWeightsBoundaryFalloff() {
        val weights = MaskOperations.computeFeatherWeights(maskWidth = 20, maskHeight = 20, featherRadius = 4)
        assertEquals(400, weights.size)

        // Corner at (0, 0) should have zero weight
        assertEquals(0.0f, weights[0], 0.01f)
        // Center at (10, 10) should have full 1.0 weight
        assertEquals(1.0f, weights[10 * 20 + 10], 0.01f)
    }

    @Test
    fun testAutomaticMethodSelectorForSmoothBackground() {
        // Uniform color image (low variance)
        val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        for (y in 0 until 100) {
            for (x in 0 until 100) {
                bmp.setPixel(x, y, Color.rgb(120, 160, 220)) // smooth blue sky
            }
        }

        val mask = NormalizedRect(0.7f, 0.1f, 0.9f, 0.2f)
        val rec = MethodSelector.selectOptimalMethod(bmp, mask, hasMultipleFrames = false)

        // Smooth background should recommend Navier-Stokes PDE
        assertEquals(RemovalMethod.NAVIER_STOKES, rec.recommendedMethod)
        assertTrue(rec.confidenceScore >= 0.85f)
    }

    @Test
    fun testTeleaInpaintingRemovesCorruptedPixels() {
        // 50x50 green background
        val bmp = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        for (y in 0 until 50) {
            for (x in 0 until 50) {
                bmp.setPixel(x, y, Color.rgb(40, 180, 40))
            }
        }

        // Draw a red "watermark" rectangle in center
        for (y in 20..30) {
            for (x in 20..30) {
                bmp.setPixel(x, y, Color.RED)
            }
        }

        val mask = WatermarkMask(
            rect = NormalizedRect(left = 0.40f, top = 0.40f, right = 0.60f, bottom = 0.60f),
            dilationRadius = 2,
            inpaintRadius = 4,
            featherRadius = 2
        )

        val reconstructed = InpaintingEngine.processFrame(
            sourceBitmap = bmp,
            mask = mask,
            method = RemovalMethod.TELEA
        )

        assertNotNull(reconstructed)
        val centerColor = reconstructed.getPixel(25, 25)

        // Center pixel should no longer be pure Red, should be reconstructed green from surroundings!
        val greenComponent = Color.green(centerColor)
        val redComponent = Color.red(centerColor)
        assertTrue("Expected green component ($greenComponent) > red ($redComponent)", greenComponent > redComponent)
    }

    @Test
    fun testVideoInfoFormatting() {
        val info = VideoInfo(
            uriString = "file:///sample.mp4",
            displayName = "demo.mp4",
            durationMs = 125000L, // 2 minutes 5 seconds
            width = 1920,
            height = 1080
        )

        assertEquals("02:05", info.durationFormatted)
        assertEquals("1920x1080", info.resolutionFormatted)
        assertEquals(16f / 9f, info.aspectRatio, 0.05f)
    }
}
