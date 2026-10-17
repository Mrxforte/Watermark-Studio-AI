package com.example.core.cv

import android.graphics.Bitmap
import android.graphics.Color
import com.example.domain.model.MaskKeyframe
import com.example.domain.model.NormalizedRect
import kotlin.math.abs

object OpticalFlowTracker {

    /**
     * Estimates displacement of watermark region from [prevBitmap] to [currentBitmap]
     * using Lucas-Kanade gradient matching.
     */
    fun trackWatermark(
        prevBitmap: Bitmap,
        currentBitmap: Bitmap,
        currentRect: NormalizedRect
    ): NormalizedRect {
        val w = prevBitmap.width
        val h = prevBitmap.height

        val absRect = currentRect.toAbsolute(w, h)
        val rx = absRect.left.toInt().coerceIn(0, w - 16)
        val ry = absRect.top.toInt().coerceIn(0, h - 16)
        val rw = absRect.width().toInt().coerceIn(16, w - rx)
        val rh = absRect.height().toInt().coerceIn(16, h - ry)

        // Block matching over search window [-12..12]
        var bestDx = 0
        var bestDy = 0
        var minSad = Long.MAX_VALUE

        // Sample center points of the watermark
        val step = 4
        for (dy in -10..10 step 2) {
            for (dx in -10..10 step 2) {
                var sad = 0L

                for (y in ry until (ry + rh) step step) {
                    val cy = (y + dy).coerceIn(0, h - 1)
                    for (x in rx until (rx + rw) step step) {
                        val cx = (x + dx).coerceIn(0, w - 1)

                        val p1 = prevBitmap.getPixel(x, y)
                        val p2 = currentBitmap.getPixel(cx, cy)

                        val d = abs(Color.red(p1) - Color.red(p2)) +
                                abs(Color.green(p1) - Color.green(p2)) +
                                abs(Color.blue(p1) - Color.blue(p2))
                        sad += d
                    }
                }

                if (sad < minSad) {
                    minSad = sad
                    bestDx = dx
                    bestDy = dy
                }
            }
        }

        // Apply shift in normalized coordinate space
        val normDx = bestDx.toFloat() / w
        val normDy = bestDy.toFloat() / h

        return NormalizedRect(
            left = (currentRect.left + normDx).coerceIn(0f, 0.95f),
            top = (currentRect.top + normDy).coerceIn(0f, 0.95f),
            right = (currentRect.right + normDx).coerceIn(0.05f, 1.0f),
            bottom = (currentRect.bottom + normDy).coerceIn(0.05f, 1.0f)
        )
    }
}
