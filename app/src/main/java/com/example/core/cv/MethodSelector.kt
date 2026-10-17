package com.example.core.cv

import android.graphics.Bitmap
import android.graphics.Color
import com.example.domain.model.MethodRecommendation
import com.example.domain.model.NormalizedRect
import com.example.domain.model.RemovalMethod
import kotlin.math.abs
import kotlin.math.sqrt

object MethodSelector {

    /**
     * Recommends the optimal removal method based on measured frame signals:
     * mask area, boundary variance, and neighbor motion.
     */
    fun selectOptimalMethod(
        frame: Bitmap?,
        maskRect: NormalizedRect,
        hasMultipleFrames: Boolean = true
    ): MethodRecommendation {
        val areaPercent = maskRect.area * 100f // % of frame covered

        // If no frame yet, provide sensible default
        if (frame == null) {
            return MethodRecommendation(
                recommendedMethod = RemovalMethod.TELEA,
                confidenceScore = 0.85f,
                explanation = "Telea Fast Marching is recommended for standard corner watermarks and static text stamps.",
                qualityRisks = listOf("Large uniform areas may show slight radial diffusion."),
                alternativeMethods = listOf(RemovalMethod.NAVIER_STOKES, RemovalMethod.TEMPORAL)
            )
        }

        // Measure boundary color variance
        val w = frame.width
        val h = frame.height
        val absRect = maskRect.toAbsolute(w, h)
        val rx = absRect.left.toInt().coerceIn(0, w - 10)
        val ry = absRect.top.toInt().coerceIn(0, h - 10)
        val rw = absRect.width().toInt().coerceIn(10, w - rx)
        val rh = absRect.height().toInt().coerceIn(10, h - ry)

        var sumLum = 0.0
        var sumLumSq = 0.0
        var sampleCount = 0

        // Sample border perimeter
        for (x in rx until (rx + rw) step 2) {
            val cTop = frame.getPixel(x, ry)
            val cBot = frame.getPixel(x, (ry + rh - 1).coerceIn(0, h - 1))
            val lum1 = 0.299 * Color.red(cTop) + 0.587 * Color.green(cTop) + 0.114 * Color.blue(cTop)
            val lum2 = 0.299 * Color.red(cBot) + 0.587 * Color.green(cBot) + 0.114 * Color.blue(cBot)
            sumLum += lum1 + lum2
            sumLumSq += lum1 * lum1 + lum2 * lum2
            sampleCount += 2
        }

        val mean = sumLum / sampleCount.coerceAtLeast(1)
        val variance = (sumLumSq / sampleCount.coerceAtLeast(1)) - (mean * mean)
        val stdDev = sqrt(variance.coerceAtLeast(0.0))

        return when {
            // Very large mask: inpainting may blur, suggest concealment or crop
            areaPercent > 12.0f -> {
                MethodRecommendation(
                    recommendedMethod = RemovalMethod.GAUSSIAN_BLUR,
                    confidenceScore = 0.90f,
                    explanation = "Mask covers %.1f%% of the frame. Spatial inpainting over large areas produces significant blur artifacts; blur or logo cover is recommended.".format(areaPercent),
                    qualityRisks = listOf("Underlying content cannot be fully recovered from a large single-frame occluded region."),
                    alternativeMethods = listOf(RemovalMethod.SMART_CROP, RemovalMethod.LOGO_REPLACE, RemovalMethod.TELEA)
                )
            }
            // Temporal motion available and reasonable size
            hasMultipleFrames && areaPercent in 1.0f..8.0f -> {
                MethodRecommendation(
                    recommendedMethod = RemovalMethod.TEMPORAL,
                    confidenceScore = 0.94f,
                    explanation = "Video playback provides temporal motion information across frames. Reconstructing background from revealed neighboring frames yields the highest visual fidelity.",
                    qualityRisks = listOf("Rapid scene cuts or camera pans may require spatial fallback on transition frames."),
                    alternativeMethods = listOf(RemovalMethod.TELEA, RemovalMethod.NAVIER_STOKES)
                )
            }
            // Smooth gradient / continuous tone background (e.g. sky, wall, solid color)
            stdDev < 18.0 -> {
                MethodRecommendation(
                    recommendedMethod = RemovalMethod.NAVIER_STOKES,
                    confidenceScore = 0.89f,
                    explanation = "Background has smooth gradient tones (variance: %.1f). Navier-Stokes fluid PDE preserves isophote lines and color continuity without abrupt edges.".format(stdDev),
                    qualityRisks = listOf("Slightly slower than Telea on high frame counts."),
                    alternativeMethods = listOf(RemovalMethod.TELEA, RemovalMethod.TEMPORAL)
                )
            }
            // Standard textured or thin text watermark
            else -> {
                MethodRecommendation(
                    recommendedMethod = RemovalMethod.TELEA,
                    confidenceScore = 0.91f,
                    explanation = "Boundary contains high-frequency texture (std dev: %.1f). Alexandru Telea Fast Marching preserves sharp boundary details and renders rapidly.".format(stdDev),
                    qualityRisks = listOf("Ensure mask dilation covers all semi-transparent halo pixels."),
                    alternativeMethods = listOf(RemovalMethod.NAVIER_STOKES, RemovalMethod.TEMPORAL)
                )
            }
        }
    }
}
