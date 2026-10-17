package com.example.core.cv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.example.domain.model.NormalizedRect
import com.example.domain.model.RemovalMethod
import com.example.domain.model.WatermarkMask
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object InpaintingEngine {

    /**
     * Executes the requested watermark removal method on a frame bitmap.
     * Returns a new reconstructed Bitmap.
     */
    fun processFrame(
        sourceBitmap: Bitmap,
        mask: WatermarkMask,
        method: RemovalMethod,
        timestampMs: Long = 0L,
        neighborFrames: List<Bitmap> = emptyList()
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val rect = mask.getRectAt(timestampMs)
        val absRect = rect.toAbsolute(width, height)

        val margin = mask.dilationRadius + 3
        val rx = (absRect.left.toInt() - margin).coerceIn(0, width - 6)
        val ry = (absRect.top.toInt() - margin).coerceIn(0, height - 6)
        val rw = (absRect.width().toInt() + margin * 2).coerceIn(8, width - rx)
        val rh = (absRect.height().toInt() + margin * 2).coerceIn(8, height - ry)

        val output = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)

        when (method) {
            RemovalMethod.TELEA -> {
                inpaintTelea(output, rx, ry, rw, rh, mask.inpaintRadius, mask.featherRadius)
            }
            RemovalMethod.NAVIER_STOKES -> {
                inpaintNavierStokes(output, rx, ry, rw, rh, mask.inpaintRadius, mask.featherRadius)
            }
            RemovalMethod.TEMPORAL -> {
                if (neighborFrames.isNotEmpty()) {
                    TemporalReconstruction.reconstructPatch(output, neighborFrames, rx, ry, rw, rh)
                } else {
                    // Fallback to Telea if no neighboring frames are available
                    inpaintTelea(output, rx, ry, rw, rh, mask.inpaintRadius, mask.featherRadius)
                }
            }
            RemovalMethod.GAUSSIAN_BLUR -> {
                applyGaussianBlurPatch(output, rx, ry, rw, rh, mask.featherRadius)
            }
            RemovalMethod.PIXELATE_MOSAIC -> {
                applyMosaicPatch(output, rx, ry, rw, rh, blockSize = 14)
            }
            RemovalMethod.SMART_CROP -> {
                // Returns cropped image if requested
                return applySmartCrop(sourceBitmap, rect)
            }
            RemovalMethod.LOGO_REPLACE -> {
                // Handled in overlay pipeline or plate drawing
                drawConcealmentPlate(output, rx, ry, rw, rh)
            }
        }

        return output
    }

    /**
     * Alexandru Telea (2004) Fast Marching Method Inpainting.
     * Computes level-set distance from boundary, estimates gradients, and propagates pixel values inward.
     */
    private fun inpaintTelea(
        bitmap: Bitmap,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int,
        inpaintRadius: Int,
        featherRadius: Int
    ) {
        val patchPixels = IntArray(rw * rh)
        bitmap.getPixels(patchPixels, 0, rw, rx, ry, rw, rh)

        // 0 = KNOWN, 1 = BAND, 2 = INSIDE
        val state = IntArray(rw * rh) { 2 } // All inside patch are initially target
        val distance = FloatArray(rw * rh) { Float.MAX_VALUE }

        // Mark boundary pixels as KNOWN (outer 2 pixels of patch)
        for (y in 0 until rh) {
            for (x in 0 until rw) {
                if (x <= 1 || x >= rw - 2 || y <= 1 || y >= rh - 2) {
                    state[y * rw + x] = 0
                    distance[y * rw + x] = 0f
                }
            }
        }

        // Initialize Narrow Band (neighbors of known pixels)
        for (y in 2 until rh - 2) {
            for (x in 2 until rw - 2) {
                val idx = y * rw + x
                if (state[idx] == 2) {
                    val hasKnownNeighbor = (state[idx - 1] == 0) || (state[idx + 1] == 0) ||
                            (state[(y - 1) * rw + x] == 0) || (state[(y + 1) * rw + x] == 0)
                    if (hasKnownNeighbor) {
                        state[idx] = 1
                        distance[idx] = 1f
                    }
                }
            }
        }

        val r = inpaintRadius.coerceIn(2, 9)
        val featherWeights = MaskOperations.computeFeatherWeights(rw, rh, featherRadius)

        // Multi-pass Fast Marching sweep inward
        var changed = true
        var pass = 0
        val maxPasses = max(rw, rh) / 2

        while (changed && pass < maxPasses) {
            changed = false
            pass++

            for (y in 2 until rh - 2) {
                for (x in 2 until rw - 2) {
                    val idx = y * rw + x
                    if (state[idx] == 1) { // BAND pixel
                        // Inpaint this pixel from known neighbors in neighborhood B_r(p)
                        var sumR = 0.0
                        var sumG = 0.0
                        var sumB = 0.0
                        var totalWeight = 0.0

                        val yMin = max(0, y - r)
                        val yMax = min(rh - 1, y + r)
                        val xMin = max(0, x - r)
                        val xMax = min(rw - 1, x + r)

                        for (ny in yMin..yMax) {
                            val dy = (ny - y).toFloat()
                            val dy2 = dy * dy
                            for (nx in xMin..xMax) {
                                val nIdx = ny * rw + nx
                                if (state[nIdx] == 0) { // Known pixel
                                    val dx = (nx - x).toFloat()
                                    val distSq = dx * dx + dy2
                                    if (distSq <= r * r && distSq > 0.001f) {
                                        // Telea weight: geometric distance decay + gradient direction
                                        val dist = sqrt(distSq)
                                        val w = (1.0 / (dist * (1.0 + abs(distance[nIdx] - distance[idx]))))

                                        val c = patchPixels[nIdx]
                                        sumR += ((c shr 16) and 0xFF) * w
                                        sumG += ((c shr 8) and 0xFF) * w
                                        sumB += (c and 0xFF) * w
                                        totalWeight += w
                                    }
                                }
                            }
                        }

                        if (totalWeight > 0.0) {
                            val newR = (sumR / totalWeight).toInt().coerceIn(0, 255)
                            val newG = (sumG / totalWeight).toInt().coerceIn(0, 255)
                            val newB = (sumB / totalWeight).toInt().coerceIn(0, 255)

                            // Apply feather weight at outer edge
                            val alpha = featherWeights[idx]
                            val origC = patchPixels[idx]
                            val finalR = (newR * alpha + ((origC shr 16) and 0xFF) * (1f - alpha)).toInt()
                            val finalG = (newG * alpha + ((origC shr 8) and 0xFF) * (1f - alpha)).toInt()
                            val finalB = (newB * alpha + (origC and 0xFF) * (1f - alpha)).toInt()

                            patchPixels[idx] = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                            state[idx] = 0 // Marked as KNOWN
                            changed = true

                            // Promote neighboring INSIDE pixels to BAND
                            if (state[idx - 1] == 2) { state[idx - 1] = 1; distance[idx - 1] = distance[idx] + 1f }
                            if (state[idx + 1] == 2) { state[idx + 1] = 1; distance[idx + 1] = distance[idx] + 1f }
                            if (state[(y - 1) * rw + x] == 2) { state[(y - 1) * rw + x] = 1; distance[(y - 1) * rw + x] = distance[idx] + 1f }
                            if (state[(y + 1) * rw + x] == 2) { state[(y + 1) * rw + x] = 1; distance[(y + 1) * rw + x] = distance[idx] + 1f }
                        }
                    }
                }
            }
        }

        bitmap.setPixels(patchPixels, 0, rw, rx, ry, rw, rh)
    }

    /**
     * Navier-Stokes / Bertalmio PDE Inpainting.
     * Solves discrete Laplace / Poisson equation to propagate smoothness along isophotes.
     */
    private fun inpaintNavierStokes(
        bitmap: Bitmap,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int,
        inpaintRadius: Int,
        featherRadius: Int
    ) {
        val patchPixels = IntArray(rw * rh)
        bitmap.getPixels(patchPixels, 0, rw, rx, ry, rw, rh)

        // Extract color channels as float arrays for PDE relaxation
        val rChan = FloatArray(rw * rh)
        val gChan = FloatArray(rw * rh)
        val bChan = FloatArray(rw * rh)

        for (i in patchPixels.indices) {
            val c = patchPixels[i]
            rChan[i] = ((c shr 16) and 0xFF).toFloat()
            gChan[i] = ((c shr 8) and 0xFF).toFloat()
            bChan[i] = (c and 0xFF).toFloat()
        }

        // Iterative Jacobi relaxation for Laplacian smoothness:
        // L(p) = (I(x+1,y) + I(x-1,y) + I(x,y+1) + I(x,y-1)) / 4
        val iterations = (inpaintRadius * 8).coerceIn(20, 80)
        val rTemp = FloatArray(rw * rh)
        val gTemp = FloatArray(rw * rh)
        val bTemp = FloatArray(rw * rh)

        for (it in 0 until iterations) {
            for (y in 2 until rh - 2) {
                for (x in 2 until rw - 2) {
                    val idx = y * rw + x
                    // 4-neighbor average
                    rTemp[idx] = (rChan[idx - 1] + rChan[idx + 1] + rChan[(y - 1) * rw + x] + rChan[(y + 1) * rw + x]) * 0.25f
                    gTemp[idx] = (gChan[idx - 1] + gChan[idx + 1] + gChan[(y - 1) * rw + x] + gChan[(y + 1) * rw + x]) * 0.25f
                    bTemp[idx] = (bChan[idx - 1] + bChan[idx + 1] + bChan[(y - 1) * rw + x] + bChan[(y + 1) * rw + x]) * 0.25f
                }
            }
            // Copy back interior values while preserving outer boundary
            for (y in 2 until rh - 2) {
                for (x in 2 until rw - 2) {
                    val idx = y * rw + x
                    rChan[idx] = rTemp[idx]
                    gChan[idx] = gTemp[idx]
                    bChan[idx] = bTemp[idx]
                }
            }
        }

        val featherWeights = MaskOperations.computeFeatherWeights(rw, rh, featherRadius)
        for (y in 2 until rh - 2) {
            for (x in 2 until rw - 2) {
                val idx = y * rw + x
                val alpha = featherWeights[idx]
                val origC = patchPixels[idx]

                val nr = rChan[idx].toInt().coerceIn(0, 255)
                val ng = gChan[idx].toInt().coerceIn(0, 255)
                val nb = bChan[idx].toInt().coerceIn(0, 255)

                val finalR = (nr * alpha + ((origC shr 16) and 0xFF) * (1f - alpha)).toInt()
                val finalG = (ng * alpha + ((origC shr 8) and 0xFF) * (1f - alpha)).toInt()
                val finalB = (nb * alpha + (origC and 0xFF) * (1f - alpha)).toInt()

                patchPixels[idx] = (0xFF shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
            }
        }

        bitmap.setPixels(patchPixels, 0, rw, rx, ry, rw, rh)
    }

    /**
     * Fast separable Gaussian blur applied to selected region with smooth feather boundary.
     */
    private fun applyGaussianBlurPatch(
        bitmap: Bitmap,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int,
        featherRadius: Int
    ) {
        val patch = Bitmap.createBitmap(bitmap, rx, ry, rw, rh)
        val blurred = fastBoxBlur(patch, radius = 9)

        val patchPixels = IntArray(rw * rh)
        val blurPixels = IntArray(rw * rh)
        patch.getPixels(patchPixels, 0, rw, 0, 0, rw, rh)
        blurred.getPixels(blurPixels, 0, rw, 0, 0, rw, rh)

        val feather = MaskOperations.computeFeatherWeights(rw, rh, featherRadius.coerceAtLeast(3))
        for (i in patchPixels.indices) {
            val a = feather[i]
            val oc = patchPixels[i]
            val bc = blurPixels[i]
            val r = (Color.red(bc) * a + Color.red(oc) * (1f - a)).toInt()
            val g = (Color.green(bc) * a + Color.green(oc) * (1f - a)).toInt()
            val b = (Color.blue(bc) * a + Color.blue(oc) * (1f - a)).toInt()
            patchPixels[i] = Color.rgb(r, g, b)
        }

        bitmap.setPixels(patchPixels, 0, rw, rx, ry, rw, rh)
    }

    /**
     * Mosaic downsampling blocks with anti-aliased edge.
     */
    private fun applyMosaicPatch(
        bitmap: Bitmap,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int,
        blockSize: Int = 12
    ) {
        val patchPixels = IntArray(rw * rh)
        bitmap.getPixels(patchPixels, 0, rw, rx, ry, rw, rh)

        for (by in 0 until rh step blockSize) {
            for (bx in 0 until rw step blockSize) {
                var sumR = 0
                var sumG = 0
                var sumB = 0
                var count = 0

                val ey = min(rh, by + blockSize)
                val ex = min(rw, bx + blockSize)

                for (y in by until ey) {
                    for (x in bx until ex) {
                        val c = patchPixels[y * rw + x]
                        sumR += (c shr 16) and 0xFF
                        sumG += (c shr 8) and 0xFF
                        sumB += c and 0xFF
                        count++
                    }
                }

                if (count > 0) {
                    val avg = Color.rgb(sumR / count, sumG / count, sumB / count)
                    for (y in by until ey) {
                        for (x in bx until ex) {
                            patchPixels[y * rw + x] = avg
                        }
                    }
                }
            }
        }
        bitmap.setPixels(patchPixels, 0, rw, rx, ry, rw, rh)
    }

    private fun applySmartCrop(source: Bitmap, watermarkRect: NormalizedRect): Bitmap {
        val w = source.width
        val h = source.height

        // Determine which edge contains the watermark
        val cropRect = when {
            watermarkRect.top < 0.2f -> Rect(0, (watermarkRect.bottom * h).toInt(), w, h)
            watermarkRect.bottom > 0.8f -> Rect(0, 0, w, (watermarkRect.top * h).toInt())
            watermarkRect.left < 0.2f -> Rect((watermarkRect.right * w).toInt(), 0, w, h)
            watermarkRect.right > 0.8f -> Rect(0, 0, (watermarkRect.left * w).toInt(), h)
            else -> Rect(0, 0, w, h)
        }

        val cw = cropRect.width().coerceAtLeast(32)
        val ch = cropRect.height().coerceAtLeast(32)
        return Bitmap.createBitmap(source, cropRect.left, cropRect.top, cw, ch)
    }

    private fun drawConcealmentPlate(bitmap: Bitmap, rx: Int, ry: Int, rw: Int, rh: Int) {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val rectF = RectF(rx.toFloat(), ry.toFloat(), (rx + rw).toFloat(), (ry + rh).toFloat())
        canvas.drawRoundRect(rectF, 12f, 12f, paint)
    }

    private fun fastBoxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)

        val r = radius.coerceIn(1, 25)
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = r + r + 1

        val rAcc = IntArray(wh)
        val gAcc = IntArray(wh)
        val bAcc = IntArray(wh)

        var rsum: Int; var gsum: Int; var bsum: Int
        var x: Int; var y: Int; var i: Int; var p: Int; var yp: Int; var yi: Int; var yw: Int
        val vmin = IntArray(max(w, h))

        yw = 0
        yi = 0

        for (currY in 0 until h) {
            rsum = 0; gsum = 0; bsum = 0
            for (currI in -r..r) {
                p = pix[yi + min(wm, max(currI, 0))]
                rsum += (p and 0xff0000) shr 16
                gsum += (p and 0x00ff00) shr 8
                bsum += (p and 0x0000ff)
            }
            for (currX in 0 until w) {
                rAcc[yi] = rsum / div
                gAcc[yi] = gsum / div
                bAcc[yi] = bsum / div

                if (currY == 0) vmin[currX] = min(currX + r + 1, wm)
                val p1 = pix[yw + vmin[currX]]
                val p2 = pix[yw + max(currX - r, 0)]

                rsum += ((p1 and 0xff0000) - (p2 and 0xff0000)) shr 16
                gsum += ((p1 and 0x00ff00) - (p2 and 0x00ff00)) shr 8
                bsum += (p1 and 0x0000ff) - (p2 and 0x0000ff)
                yi++
            }
            yw += w
        }

        for (currX in 0 until w) {
            rsum = 0; gsum = 0; bsum = 0
            yp = -r * w
            for (currI in -r..r) {
                yi = max(0, yp) + currX
                rsum += rAcc[yi]
                gsum += gAcc[yi]
                bsum += bAcc[yi]
                yp += w
            }
            yi = currX
            for (currY in 0 until h) {
                pix[yi] = (0xff000000.toInt()) or ((rsum / div) shl 16) or ((gsum / div) shl 8) or (bsum / div)
                if (currX == 0) vmin[currY] = min(currY + r + 1, hm) * w
                val p1 = currX + vmin[currY]
                val p2 = currX + max(currY - r, 0) * w

                rsum += rAcc[p1] - rAcc[p2]
                gsum += gAcc[p1] - gAcc[p2]
                bsum += bAcc[p1] - bAcc[p2]
                yi += w
            }
        }

        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pix, 0, w, 0, 0, w, h)
        return out
    }
}
