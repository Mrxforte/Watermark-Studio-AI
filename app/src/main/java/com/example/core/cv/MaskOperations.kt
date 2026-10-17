package com.example.core.cv

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.domain.model.NormalizedPoint
import com.example.domain.model.NormalizedRect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object MaskOperations {

    /**
     * Creates a binary mask bitmap (8-bit grayscale in ARGB_8888, where 255 = masked/to inpaint, 0 = known background)
     */
    fun createMaskBitmap(
        width: Int,
        height: Int,
        rect: NormalizedRect,
        freeformPoints: List<NormalizedPoint> = emptyList(),
        dilationRadius: Int = 3
    ): Bitmap {
        val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mask)
        canvas.drawColor(0xFF000000.toInt()) // unmasked background (black)

        val paint = Paint().apply {
            color = 0xFFFFFFFF.toInt() // masked pixels (white)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        if (freeformPoints.size > 2) {
            val path = Path()
            val first = freeformPoints.first()
            path.moveTo(first.x * width, first.y * height)
            for (i in 1 until freeformPoints.size) {
                val pt = freeformPoints[i]
                path.lineTo(pt.x * width, pt.y * height)
            }
            path.close()
            canvas.drawPath(path, paint)
        } else {
            val absRect = rect.toAbsolute(width, height)
            canvas.drawRect(absRect, paint)
        }

        return if (dilationRadius > 0) {
            dilateMask(mask, dilationRadius)
        } else {
            mask
        }
    }

    /**
     * Morphological Dilation on binary mask to expand boundary and cover semi-transparent edges.
     */
    fun dilateMask(srcMask: Bitmap, radius: Int): Bitmap {
        val w = srcMask.width
        val h = srcMask.height
        val pixels = IntArray(w * h)
        srcMask.getPixels(pixels, 0, w, 0, 0, w, h)

        val dilated = IntArray(w * h) { 0xFF000000.toInt() }
        val r = radius.coerceIn(1, 15)

        for (y in 0 until h) {
            for (x in 0 until w) {
                // If pixel is white (masked), dilate its neighborhood
                if ((pixels[y * w + x] and 0xFF) > 128) {
                    val yMin = max(0, y - r)
                    val yMax = min(h - 1, y + r)
                    val xMin = max(0, x - r)
                    val xMax = min(w - 1, x + r)

                    for (ny in yMin..yMax) {
                        val dy = ny - y
                        val dy2 = dy * dy
                        for (nx in xMin..xMax) {
                            val dx = nx - x
                            if (dx * dx + dy2 <= r * r) {
                                dilated[ny * w + nx] = 0xFFFFFFFF.toInt()
                            }
                        }
                    }
                }
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(dilated, 0, w, 0, 0, w, h)
        return result
    }

    /**
     * Computes boundary feathering (alpha blending weights 0.0 .. 1.0) for inpainting seams.
     */
    fun computeFeatherWeights(
        maskWidth: Int,
        maskHeight: Int,
        featherRadius: Int
    ): FloatArray {
        val weights = FloatArray(maskWidth * maskHeight) { 1.0f }
        if (featherRadius <= 0) return weights

        val r = featherRadius.toFloat()
        for (y in 0 until maskHeight) {
            val distY = min(y, maskHeight - 1 - y).toFloat()
            for (x in 0 until maskWidth) {
                val distX = min(x, maskWidth - 1 - x).toFloat()
                val minEdgeDist = min(distX, distY)
                val alpha = (minEdgeDist / r).coerceIn(0.0f, 1.0f)
                weights[y * maskWidth + x] = alpha
            }
        }
        return weights
    }
}
