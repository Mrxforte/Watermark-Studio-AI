package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import com.example.core.cv.InpaintingEngine
import com.example.domain.model.AnimationType
import com.example.domain.model.ExportConfig
import com.example.domain.model.ExportResolution
import com.example.domain.model.VideoInfo
import com.example.domain.model.WatermarkDesign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

object VideoExportPipeline {

    data class ExportResult(
        val success: Boolean,
        val outputFile: File?,
        val totalFramesProcessed: Int,
        val outputSizeBytes: Long,
        val errorMessage: String? = null
    )

    /**
     * Executes real video frame-by-frame rendering and encoding.
     */
    suspend fun exportVideo(
        context: Context,
        videoInfo: VideoInfo,
        config: ExportConfig,
        onProgress: (Float, String) -> Unit,
        isCancelled: () -> Boolean
    ): ExportResult = withContext(Dispatchers.IO) {
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val outputFile = File(exportDir, "Studio_Export_${System.currentTimeMillis()}.mp4")

        val retriever = MediaMetadataRetriever()
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var muxerStarted = false
        var videoTrackIndex = -1
        var audioTrackIndex = -1

        try {
            val sourceUri = Uri.parse(videoInfo.uriString)
            if (sourceUri.scheme == "file") {
                retriever.setDataSource(sourceUri.path)
            } else {
                retriever.setDataSource(context, sourceUri)
            }

            // Determine dimensions
            val targetHeight = when (config.resolution) {
                ExportResolution.ORIGINAL -> (videoInfo.height / 2) * 2 // ensure even
                ExportResolution.HD_720P -> 720
                ExportResolution.FHD_1080P -> 1080
            }
            val targetWidth = (((targetHeight * videoInfo.aspectRatio).toInt()) / 2) * 2

            val fps = config.fps.coerceIn(15, 60)
            val durationMs = videoInfo.durationMs.coerceAtLeast(1000L)
            val totalFrames = ((durationMs / 1000.0) * fps).toInt().coerceAtLeast(1)

            onProgress(0.05f, "Configuring hardware encoder ($targetWidth x $targetHeight @ ${fps}fps)...")

            val bitrate = (config.bitrateMbps * 1_000_000).toInt()
            val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, targetWidth, targetHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // Setup audio passthrough extractor if audio is requested and present
            var audioExtractor: MediaExtractor? = null
            var sourceAudioTrack = -1
            if (config.includeAudio && videoInfo.hasAudio) {
                try {
                    audioExtractor = MediaExtractor()
                    if (sourceUri.scheme == "file") {
                        audioExtractor.setDataSource(sourceUri.path ?: "")
                    } else {
                        audioExtractor.setDataSource(context, sourceUri, null)
                    }
                    for (i in 0 until audioExtractor.trackCount) {
                        val trackFormat = audioExtractor.getTrackFormat(i)
                        val mime = trackFormat.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("audio/")) {
                            sourceAudioTrack = i
                            audioExtractor.selectTrack(i)
                            audioTrackIndex = muxer.addTrack(trackFormat)
                            break
                        }
                    }
                } catch (_: Exception) {}
            }

            val bufferInfo = MediaCodec.BufferInfo()
            val yuvBuffer = ByteArray(targetWidth * targetHeight * 3 / 2)
            val frameDurationUs = 1_000_000L / fps

            var processedCount = 0

            for (frameIdx in 0 until totalFrames) {
                if (isCancelled()) {
                    outputFile.delete()
                    return@withContext ExportResult(false, null, processedCount, 0L, "Export cancelled by user")
                }

                val currentPtsUs = frameIdx * frameDurationUs
                val currentTimestampMs = currentPtsUs / 1000L

                // Extract source frame
                val rawFrame = retriever.getFrameAtTime(currentPtsUs, MediaMetadataRetriever.OPTION_CLOSEST)

                val scaledFrame = if (rawFrame != null) {
                    if (rawFrame.width != targetWidth || rawFrame.height != targetHeight) {
                        val s = Bitmap.createScaledBitmap(rawFrame, targetWidth, targetHeight, true)
                        if (s != rawFrame) rawFrame.recycle()
                        s
                    } else {
                        rawFrame
                    }
                } else {
                    // Fallback black frame if frame extractor drops sync
                    Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                }

                // Process frame: removal or watermark design
                val finalFrame = if (config.isWatermarkCreationMode && config.watermarkDesign != null) {
                    renderWatermarkOverlay(scaledFrame, config.watermarkDesign, currentTimestampMs, durationMs)
                } else {
                    InpaintingEngine.processFrame(
                        sourceBitmap = scaledFrame,
                        mask = config.mask,
                        method = config.removalMethod,
                        timestampMs = currentTimestampMs
                    )
                }

                // Convert to YUV420
                bitmapToYuv420(finalFrame, yuvBuffer, targetWidth, targetHeight)
                if (finalFrame != scaledFrame) finalFrame.recycle()
                scaledFrame.recycle()

                // Feed into encoder
                val inputIndex = encoder.dequeueInputBuffer(50_000L)
                if (inputIndex >= 0) {
                    val inputBuf = encoder.getInputBuffer(inputIndex)
                    if (inputBuf != null) {
                        inputBuf.clear()
                        inputBuf.put(yuvBuffer)
                        val flags = if (frameIdx == totalFrames - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        encoder.queueInputBuffer(inputIndex, 0, yuvBuffer.size, currentPtsUs, flags)
                    }
                }

                // Drain encoder
                drainEncoderToMuxer(encoder, muxer, bufferInfo) { trackIdx ->
                    videoTrackIndex = trackIdx
                    muxerStarted = true
                }

                processedCount++
                val p = 0.1f + 0.8f * (processedCount.toFloat() / totalFrames)
                if (frameIdx % 10 == 0 || frameIdx == totalFrames - 1) {
                    onProgress(p, "Rendering frame $processedCount / $totalFrames...")
                }
            }

            // Final drain
            while (true) {
                val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 20_000L)
                if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break
                if (outIndex >= 0) {
                    val buf = encoder.getOutputBuffer(outIndex)
                    if (buf != null && muxerStarted && bufferInfo.size > 0) {
                        buf.position(bufferInfo.offset)
                        buf.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, buf, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                }
            }

            // Copy audio samples if enabled and track is selected
            if (audioExtractor != null && audioTrackIndex >= 0 && muxerStarted) {
                onProgress(0.95f, "Preserving original audio track...")
                val audioBuf = ByteBuffer.allocate(128 * 1024)
                val audioBufferInfo = MediaCodec.BufferInfo()
                while (true) {
                    val sampleSize = audioExtractor.readSampleData(audioBuf, 0)
                    if (sampleSize < 0) break
                    audioBufferInfo.offset = 0
                    audioBufferInfo.size = sampleSize
                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    audioBufferInfo.flags = audioExtractor.sampleFlags
                    try {
                        muxer.writeSampleData(audioTrackIndex, audioBuf, audioBufferInfo)
                    } catch (_: Exception) {}
                    audioExtractor.advance()
                }
                audioExtractor.release()
            }

            onProgress(1.0f, "Finalizing MP4 container...")
            return@withContext ExportResult(
                success = true,
                outputFile = outputFile,
                totalFramesProcessed = processedCount,
                outputSizeBytes = outputFile.length()
            )

        } catch (e: Exception) {
            outputFile.delete()
            return@withContext ExportResult(false, null, 0, 0L, "Export error: ${e.message}")
        } finally {
            try { encoder?.stop() } catch (_: Exception) {}
            try { encoder?.release() } catch (_: Exception) {}
            if (muxerStarted) {
                try { muxer?.stop() } catch (_: Exception) {}
            }
            try { muxer?.release() } catch (_: Exception) {}
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun renderWatermarkOverlay(
        source: Bitmap,
        design: WatermarkDesign,
        timestampMs: Long,
        totalDurationMs: Long
    ): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(out)
        val w = out.width.toFloat()
        val h = out.height.toFloat()

        // Calculate animation factor (0.0 .. 1.0)
        val animProgress = ((timestampMs % (design.animationDurationSec * 1000).toLong()).toFloat() /
                (design.animationDurationSec * 1000).toFloat()).coerceIn(0f, 1f)

        var animAlpha = 1.0f
        var animScale = 1.0f
        var animOffsetX = 0f

        when (design.animation) {
            AnimationType.FADE -> {
                animAlpha = (sin(animProgress * Math.PI).toFloat()).coerceIn(0.2f, 1.0f)
            }
            AnimationType.PULSE -> {
                animScale = 1.0f + sin(animProgress * Math.PI * 2).toFloat() * 0.12f
            }
            AnimationType.SLIDE_HORIZONTAL -> {
                animOffsetX = (sin(animProgress * Math.PI * 2).toFloat()) * 30f
            }
            AnimationType.ROTATING -> {}
            AnimationType.NONE -> {}
        }

        // 1. Repeat diagonal grid pattern
        if (design.repeatGrid.enabled) {
            val gridPaint = Paint().apply {
                color = design.repeatGrid.textColor.toInt()
                alpha = ((design.repeatGrid.opacity * animAlpha) * 255).toInt().coerceIn(0, 255)
                textSize = design.repeatGrid.fontSizeSp * 1.5f
                isAntiAlias = true
                isFakeBoldText = true
            }

            canvas.save()
            canvas.rotate(design.repeatGrid.angleDeg, w / 2, h / 2)
            val spacing = design.repeatGrid.spacingDp * 1.8f
            var y = -h * 0.5f
            while (y < h * 1.5f) {
                var x = -w * 0.5f
                while (x < w * 1.5f) {
                    canvas.drawText(design.repeatGrid.text, x, y, gridPaint)
                    x += spacing
                }
                y += spacing * 0.6f
            }
            canvas.restore()
        }

        // 2. Text Watermark Layer
        val tc = design.textConfig
        if (tc.text.isNotBlank()) {
            val px = tc.position.x * w + animOffsetX
            val py = tc.position.y * h

            val paint = Paint().apply {
                color = tc.textColor.toInt()
                alpha = ((tc.opacity * animAlpha) * 255).toInt().coerceIn(0, 255)
                textSize = tc.fontSizeSp * (w / 400f).coerceIn(1.0f, 2.8f) * animScale
                isAntiAlias = true
                isFakeBoldText = tc.isBold
            }

            val textWidth = paint.measureText(tc.text)
            val fontMetrics = paint.fontMetrics
            val textHeight = fontMetrics.bottom - fontMetrics.top

            canvas.save()
            canvas.rotate(tc.rotationDeg, px, py)

            // Optional background plate
            if (tc.hasBackgroundPlate) {
                val platePaint = Paint().apply {
                    color = tc.plateColor.toInt()
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val pad = 16f
                val plateRect = RectF(
                    px - textWidth / 2 - pad,
                    py + fontMetrics.top - pad / 2,
                    px + textWidth / 2 + pad,
                    py + fontMetrics.bottom + pad / 2
                )
                canvas.drawRoundRect(plateRect, tc.plateCornerRadius, tc.plateCornerRadius, platePaint)
            }

            // Outline stroke
            if (tc.hasOutline) {
                val strokePaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = tc.outlineWidth * 2f
                    color = tc.outlineColor.toInt()
                    alpha = ((tc.opacity * animAlpha) * 255).toInt().coerceIn(0, 255)
                }
                canvas.drawText(tc.text, px - textWidth / 2, py, strokePaint)
            }

            // Text fill
            canvas.drawText(tc.text, px - textWidth / 2, py, paint)
            canvas.restore()
        }

        return out
    }

    private fun drainEncoderToMuxer(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        onMuxerStarted: (Int) -> Unit
    ) {
        var outIndex = encoder.dequeueOutputBuffer(bufferInfo, 0L)
        while (outIndex >= 0 || outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                val track = muxer.addTrack(newFormat)
                muxer.start()
                onMuxerStarted(track)
            } else if (outIndex >= 0) {
                val encodedData = encoder.getOutputBuffer(outIndex)
                if (encodedData != null && bufferInfo.size > 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    try {
                        muxer.writeSampleData(0, encodedData, bufferInfo)
                    } catch (_: Exception) {}
                }
                encoder.releaseOutputBuffer(outIndex, false)
            }
            outIndex = encoder.dequeueOutputBuffer(bufferInfo, 0L)
        }
    }

    private fun bitmapToYuv420(bitmap: Bitmap, yuv: ByteArray, width: Int, height: Int) {
        val argb = IntArray(width * height)
        bitmap.getPixels(argb, 0, width, 0, 0, width, height)

        var yIndex = 0
        var uvIndex = width * height

        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[j * width + i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                var y = (66 * r + 129 * g + 25 * b + 128) shr 8
                y += 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    var u = (-38 * r - 74 * g + 112 * b + 128) shr 8
                    var v = (112 * r - 94 * g - 18 * b + 128) shr 8
                    u += 128
                    v += 128
                    yuv[uvIndex++] = u.coerceIn(0, 255).toByte()
                    yuv[uvIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }
}
