package com.example.core.cv

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TemporalReconstruction {

    /**
     * Reconstructs patch at [rx, ry, rw, rh] using neighboring frames.
     */
    fun reconstructPatch(
        target: Bitmap,
        neighborFrames: List<Bitmap>,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int
    ) {
        if (neighborFrames.isEmpty()) return

        val w = target.width
        val h = target.height

        // Extract candidate patches from valid neighbors
        val validCandidates = mutableListOf<IntArray>()

        for (candidate in neighborFrames) {
            if (candidate.width == w && candidate.height == h) {
                // Find best local alignment offset (dx, dy) within search window [-8..8]
                val offset = findBestAlignment(target, candidate, rx, ry, rw, rh, searchRange = 6)
                val alignedX = (rx + offset.first).coerceIn(0, w - rw)
                val alignedY = (ry + offset.second).coerceIn(0, h - rh)

                val patch = IntArray(rw * rh)
                candidate.getPixels(patch, 0, rw, alignedX, alignedY, rw, rh)
                validCandidates.add(patch)
            }
        }

        if (validCandidates.isEmpty()) return

        // Temporal median filtering over candidates
        val targetPixels = IntArray(rw * rh)
        target.getPixels(targetPixels, 0, rw, rx, ry, rw, rh)

        val rValues = IntArray(validCandidates.size)
        val gValues = IntArray(validCandidates.size)
        val bValues = IntArray(validCandidates.size)

        for (i in 0 until (rw * rh)) {
            for (cIdx in validCandidates.indices) {
                val color = validCandidates[cIdx][i]
                rValues[cIdx] = (color shr 16) and 0xFF
                gValues[cIdx] = (color shr 8) and 0xFF
                bValues[cIdx] = color and 0xFF
            }

            rValues.sort()
            gValues.sort()
            bValues.sort()

            // Median value
            val mid = validCandidates.size / 2
            val medianR = rValues[mid]
            val medianG = gValues[mid]
            val medianB = bValues[mid]

            targetPixels[i] = (0xFF shl 24) or (medianR shl 16) or (medianG shl 8) or medianB
        }

        target.setPixels(targetPixels, 0, rw, rx, ry, rw, rh)
    }

    /**
     * Block-matching motion estimation around surrounding boundary of the watermark patch.
     * Returns best (dx, dy) displacement.
     */
    private fun findBestAlignment(
        target: Bitmap,
        candidate: Bitmap,
        rx: Int,
        ry: Int,
        rw: Int,
        rh: Int,
        searchRange: Int = 6
    ): Pair<Int, Int> {
        val w = target.width
        val h = target.height

        // Sample 4 corner test blocks outside the watermark
        val samplePoints = listOf(
            Pair(max(0, rx - 10), max(0, ry - 10)),
            Pair(min(w - 12, rx + rw + 2), max(0, ry - 10)),
            Pair(max(0, rx - 10), min(h - 12, ry + rh + 2)),
            Pair(min(w - 12, rx + rw + 2), min(h - 12, ry + rh + 2))
        )

        var bestDx = 0
        var bestDy = 0
        var minSad = Long.MAX_VALUE

        for (dy in -searchRange..searchRange) {
            for (dx in -searchRange..searchRange) {
                var totalSad = 0L

                for (pt in samplePoints) {
                    val tx = pt.first
                    val ty = pt.second
                    val cx = (tx + dx).coerceIn(0, w - 8)
                    val cy = (ty + dy).coerceIn(0, h - 8)

                    for (sy in 0 until 6) {
                        for (sx in 0 until 6) {
                            val targetColor = target.getPixel(tx + sx, ty + sy)
                            val candidateColor = candidate.getPixel(cx + sx, cy + sy)

                            val diffR = abs(((targetColor shr 16) and 0xFF) - ((candidateColor shr 16) and 0xFF))
                            val diffG = abs(((targetColor shr 8) and 0xFF) - ((candidateColor shr 8) and 0xFF))
                            val diffB = abs((targetColor and 0xFF) - (candidateColor and 0xFF))
                            totalSad += (diffR + diffG + diffB)
                        }
                    }
                }

                if (totalSad < minSad) {
                    minSad = totalSad
                    bestDx = dx
                    bestDy = dy
                }
            }
        }

        return Pair(bestDx, bestDy)
    }
}
