package com.example.feature.remove

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.domain.model.NormalizedRect
import com.example.ui.theme.MaskBoundingCyan
import com.example.ui.theme.MaskHandleViolet
import com.example.ui.theme.MaskOverlayRed
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun MaskEditorCanvas(
    originalBitmap: Bitmap?,
    processedBitmap: Bitmap?,
    currentMaskRect: NormalizedRect,
    isSplitCompare: Boolean,
    splitRatio: Float,
    onMaskRectChanged: (NormalizedRect) -> Unit,
    onSplitRatioChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragMode by remember { mutableStateOf(0) } // 0 = none, 1 = move, 2 = resize-br, 3 = resize-tl, 4 = split-bar

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("mask_editor_canvas")
                .pointerInput(isSplitCompare, currentMaskRect) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            if (isSplitCompare) {
                                val splitX = canvasW * splitRatio
                                if (abs(offset.x - splitX) < 40f) {
                                    dragMode = 4
                                    return@detectDragGestures
                                }
                            }

                            // Check bounding box corners and center
                            val rectL = currentMaskRect.left * canvasW
                            val rectT = currentMaskRect.top * canvasH
                            val rectR = currentMaskRect.right * canvasW
                            val rectB = currentMaskRect.bottom * canvasH

                            val nearBR = abs(offset.x - rectR) < 48f && abs(offset.y - rectB) < 48f
                            val nearTL = abs(offset.x - rectL) < 48f && abs(offset.y - rectT) < 48f
                            val inside = offset.x in rectL..rectR && offset.y in rectT..rectB

                            dragMode = when {
                                nearBR -> 2
                                nearTL -> 3
                                inside -> 1
                                else -> {
                                    // Start a new rectangle from tap point
                                    val nx = (offset.x / canvasW).coerceIn(0f, 0.9f)
                                    val ny = (offset.y / canvasH).coerceIn(0f, 0.9f)
                                    onMaskRectChanged(NormalizedRect(nx, ny, nx + 0.15f, ny + 0.10f))
                                    2
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()

                            if (dragMode == 4) {
                                val newRatio = (change.position.x / canvasW).coerceIn(0.05f, 0.95f)
                                onSplitRatioChanged(newRatio)
                                return@detectDragGestures
                            }

                            val dxNorm = dragAmount.x / canvasW
                            val dyNorm = dragAmount.y / canvasH

                            when (dragMode) {
                                1 -> { // Move box
                                    val w = currentMaskRect.width
                                    val h = currentMaskRect.height
                                    val newL = (currentMaskRect.left + dxNorm).coerceIn(0f, 1f - w)
                                    val newT = (currentMaskRect.top + dyNorm).coerceIn(0f, 1f - h)
                                    onMaskRectChanged(NormalizedRect(newL, newT, newL + w, newT + h))
                                }
                                2 -> { // Resize bottom-right
                                    val newR = (currentMaskRect.right + dxNorm).coerceIn(currentMaskRect.left + 0.04f, 1f)
                                    val newB = (currentMaskRect.bottom + dyNorm).coerceIn(currentMaskRect.top + 0.04f, 1f)
                                    onMaskRectChanged(currentMaskRect.copy(right = newR, bottom = newB))
                                }
                                3 -> { // Resize top-left
                                    val newL = (currentMaskRect.left + dxNorm).coerceIn(0f, currentMaskRect.right - 0.04f)
                                    val newT = (currentMaskRect.top + dyNorm).coerceIn(0f, currentMaskRect.bottom - 0.04f)
                                    onMaskRectChanged(currentMaskRect.copy(left = newL, top = newT))
                                }
                            }
                        },
                        onDragEnd = { dragMode = 0 },
                        onDragCancel = { dragMode = 0 }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw video frame bitmap
            val activeBitmap = if (isSplitCompare && processedBitmap != null) processedBitmap else originalBitmap
            if (activeBitmap != null) {
                drawImage(
                    image = activeBitmap.asImageBitmap(),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(canvasW.toInt(), canvasH.toInt())
                )
            }

            // 2. If Split Compare mode is active, draw original on left half
            if (isSplitCompare && originalBitmap != null) {
                val splitX = canvasW * splitRatio
                val origImage = originalBitmap.asImageBitmap()

                // Source rect on original bitmap proportional to split
                val srcSplitW = (originalBitmap.width * splitRatio).toInt().coerceAtLeast(1)
                drawImage(
                    image = origImage,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(srcSplitW, originalBitmap.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(splitX.toInt(), canvasH.toInt())
                )

                // Split divider line
                drawLine(
                    color = Color.White,
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, canvasH),
                    strokeWidth = 4f
                )
                // Split circular handle
                drawCircle(
                    color = MaskBoundingCyan,
                    radius = 16f,
                    center = Offset(splitX, canvasH / 2)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(splitX, canvasH / 2)
                )
            }

            // 3. Draw Watermark Mask Overlay (when not in split compare)
            if (!isSplitCompare) {
                val rx = currentMaskRect.left * canvasW
                val ry = currentMaskRect.top * canvasH
                val rw = (currentMaskRect.right - currentMaskRect.left) * canvasW
                val rh = (currentMaskRect.bottom - currentMaskRect.top) * canvasH

                // Semi-transparent red highlight over watermark region
                drawRect(
                    color = MaskOverlayRed,
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh)
                )

                // High-visibility cyan contour
                drawRect(
                    color = MaskBoundingCyan,
                    topLeft = Offset(rx, ry),
                    size = Size(rw, rh),
                    style = Stroke(width = 3.5f)
                )

                // Corner resize handles
                val handleRadius = 14f
                drawCircle(
                    color = MaskHandleViolet,
                    radius = handleRadius,
                    center = Offset(rx, ry)
                )
                drawCircle(
                    color = MaskHandleViolet,
                    radius = handleRadius,
                    center = Offset(rx + rw, ry + rh)
                )

                // Corner bracket accents
                val bracketLen = min(24f, rw / 3)
                // Top-left
                drawLine(MaskBoundingCyan, Offset(rx, ry), Offset(rx + bracketLen, ry), 5f)
                drawLine(MaskBoundingCyan, Offset(rx, ry), Offset(rx, ry + bracketLen), 5f)
                // Bottom-right
                drawLine(MaskBoundingCyan, Offset(rx + rw, ry + rh), Offset(rx + rw - bracketLen, ry + rh), 5f)
                drawLine(MaskBoundingCyan, Offset(rx + rw, ry + rh), Offset(rx + rw, ry + rh - bracketLen), 5f)
            }
        }
    }
}
