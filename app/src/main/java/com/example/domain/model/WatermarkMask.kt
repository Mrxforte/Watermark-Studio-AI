package com.example.domain.model

import android.graphics.RectF

/**
 * Normalized coordinates (0.0f .. 1.0f) representing the watermark region
 * to be independent of screen display scaling, aspect ratios, or video resolution.
 */
data class NormalizedRect(
    val left: Float = 0.75f,
    val top: Float = 0.05f,
    val right: Float = 0.95f,
    val bottom: Float = 0.18f
) {
    fun toAbsolute(width: Int, height: Int): RectF {
        val l = (left * width).coerceIn(0f, width.toFloat())
        val t = (top * height).coerceIn(0f, height.toFloat())
        val r = (right * width).coerceIn(l + 1f, width.toFloat())
        val b = (bottom * height).coerceIn(t + 1f, height.toFloat())
        return RectF(l, t, r, b)
    }

    val width: Float get() = (right - left).coerceAtLeast(0.01f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.01f)
    val area: Float get() = width * height
}

data class NormalizedPoint(
    val x: Float,
    val y: Float
)

data class MaskKeyframe(
    val timestampMs: Long,
    val rect: NormalizedRect
)

data class WatermarkMask(
    val rect: NormalizedRect = NormalizedRect(),
    val freeformPoints: List<NormalizedPoint> = emptyList(),
    val isBrushMode: Boolean = false,
    val brushRadius: Float = 24f,
    val dilationRadius: Int = 3,       // pixels to expand mask boundary
    val featherRadius: Int = 2,        // pixels for edge blending
    val inpaintRadius: Int = 5,        // FMM / PDE propagation radius
    val keyframes: List<MaskKeyframe> = emptyList(),
    val trackingEnabled: Boolean = true
) {
    /**
     * Interpolates or returns the mask bounding box at given timestamp.
     */
    fun getRectAt(timestampMs: Long): NormalizedRect {
        if (keyframes.isEmpty()) return rect
        if (keyframes.size == 1) return keyframes.first().rect

        val sorted = keyframes.sortedBy { it.timestampMs }
        if (timestampMs <= sorted.first().timestampMs) return sorted.first().rect
        if (timestampMs >= sorted.last().timestampMs) return sorted.last().rect

        for (i in 0 until sorted.size - 1) {
            val kf1 = sorted[i]
            val kf2 = sorted[i + 1]
            if (timestampMs in kf1.timestampMs..kf2.timestampMs) {
                val span = (kf2.timestampMs - kf1.timestampMs).coerceAtLeast(1)
                val alpha = (timestampMs - kf1.timestampMs).toFloat() / span
                return NormalizedRect(
                    left = kf1.rect.left + alpha * (kf2.rect.left - kf1.rect.left),
                    top = kf1.rect.top + alpha * (kf2.rect.top - kf1.rect.top),
                    right = kf1.rect.right + alpha * (kf2.rect.right - kf1.rect.right),
                    bottom = kf1.rect.bottom + alpha * (kf2.rect.bottom - kf1.rect.bottom)
                )
            }
        }
        return rect
    }
}
