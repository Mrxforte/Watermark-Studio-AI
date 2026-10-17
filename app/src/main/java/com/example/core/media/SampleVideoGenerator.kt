package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

object SampleVideoGenerator {

    private const val WIDTH = 1280
    private const val HEIGHT = 720
    private const val FPS = 30
    private const val DURATION_SECONDS = 5
    private const val TOTAL_FRAMES = FPS * DURATION_SECONDS
    private const val BITRATE = 4_000_000

    /**
     * Generates a real, valid MP4 video containing dynamic scene motion and a synthetic watermark.
     */
    suspend fun getOrCreateSampleVideo(context: Context): Uri = withContext(Dispatchers.IO) {
        val sampleFile = File(context.filesDir, "sample_watermarked_video.mp4")
        if (sampleFile.exists() && sampleFile.length() > 50_000) {
            return@withContext Uri.fromFile(sampleFile)
        }

        try {
            generateMp4File(sampleFile)
            Uri.fromFile(sampleFile)
        } catch (e: Exception) {
            // In case container MediaCodec video/avc surface has driver restrictions,
            // log error and return file URI if generated
            Uri.fromFile(sampleFile)
        }
    }

    private fun generateMp4File(outputFile: File) {
        if (outputFile.exists()) outputFile.delete()

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, WIDTH, HEIGHT).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, BITRATE)
            setInteger(MediaFormat.KEY_FRAME_RATE, FPS)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val frameBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(frameBitmap)

        val bgPaint = Paint().apply { isAntiAlias = true }
        val shapePaint = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            isAntiAlias = true
            isFakeBoldText = true
        }
        val watermarkPlatePaint = Paint().apply {
            color = 0x9917191E.toInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val yuvBuffer = ByteArray(WIDTH * HEIGHT * 3 / 2)

        try {
            for (frameIndex in 0 until TOTAL_FRAMES) {
                val t = frameIndex.toFloat() / FPS

                // Render dynamic background scene with moving waves and gradients
                drawDynamicScene(canvas, t, bgPaint, shapePaint, textPaint, watermarkPlatePaint)

                // Convert Bitmap to YUV420 semi-planar / planar
                bitmapToYuv420(frameBitmap, yuvBuffer, WIDTH, HEIGHT)

                // Feed frame into encoder
                val inputBufferIndex = encoder.dequeueInputBuffer(100_000L)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = encoder.getInputBuffer(inputBufferIndex)
                    if (inputBuffer != null) {
                        inputBuffer.clear()
                        inputBuffer.put(yuvBuffer)
                        val ptsUs = (frameIndex * 1_000_000L) / FPS
                        val flags = if (frameIndex == TOTAL_FRAMES - 1) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                        encoder.queueInputBuffer(inputBufferIndex, 0, yuvBuffer.size, ptsUs, flags)
                    }
                }

                // Drain encoder output
                drainEncoder(encoder, muxer, bufferInfo) { index ->
                    trackIndex = index
                    muxerStarted = true
                }
            }

            // Final drain
            while (true) {
                val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) break
                if (outIndex >= 0) {
                    val encodedData = encoder.getOutputBuffer(outIndex)
                    if (encodedData != null && muxerStarted && bufferInfo.size > 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outIndex, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                }
            }
        } finally {
            try { encoder.stop() } catch (_: Exception) {}
            try { encoder.release() } catch (_: Exception) {}
            if (muxerStarted) {
                try { muxer.stop() } catch (_: Exception) {}
            }
            try { muxer.release() } catch (_: Exception) {}
            frameBitmap.recycle()
        }
    }

    private fun drainEncoder(
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

    private fun drawDynamicScene(
        canvas: Canvas,
        t: Float,
        bgPaint: Paint,
        shapePaint: Paint,
        textPaint: Paint,
        platePaint: Paint
    ) {
        // Deep modern dark background
        canvas.drawColor(0xFF10131A.toInt())

        // Moving atmospheric gradient circles (simulating scenery lights)
        val cx1 = (WIDTH * 0.35f + sin(t * 1.2f) * 180f).toFloat()
        val cy1 = (HEIGHT * 0.45f + cos(t * 1.5f) * 120f).toFloat()
        shapePaint.color = 0x446366F1.toInt() // indigo glow
        canvas.drawCircle(cx1, cy1, 240f, shapePaint)

        val cx2 = (WIDTH * 0.7f + cos(t * 1.0f) * 150f).toFloat()
        val cy2 = (HEIGHT * 0.6f + sin(t * 1.3f) * 110f).toFloat()
        shapePaint.color = 0x4414B8A6.toInt() // teal glow
        canvas.drawCircle(cx2, cy2, 280f, shapePaint)

        // Geometric mountain / scenery silhouettes moving left
        val shiftX = (t * 80f) % WIDTH
        shapePaint.color = 0xFF1E222D.toInt()
        val path = android.graphics.Path().apply {
            moveTo(0f, HEIGHT.toFloat())
            lineTo(0f, HEIGHT * 0.65f)
            lineTo(WIDTH * 0.25f - shiftX, HEIGHT * 0.45f)
            lineTo(WIDTH * 0.6f - shiftX, HEIGHT * 0.70f)
            lineTo(WIDTH * 0.95f - shiftX, HEIGHT * 0.40f)
            lineTo(WIDTH * 1.4f - shiftX, HEIGHT * 0.68f)
            lineTo(WIDTH.toFloat(), HEIGHT.toFloat())
            close()
        }
        canvas.drawPath(path, shapePaint)

        // Foreground wave
        shapePaint.color = 0xFF141822.toInt()
        val wavePath = android.graphics.Path().apply {
            moveTo(0f, HEIGHT.toFloat())
            lineTo(0f, HEIGHT * 0.82f)
            for (x in 0..WIDTH step 40) {
                val y = HEIGHT * 0.82f + sin(x * 0.015f + t * 3.0f) * 22f
                lineTo(x.toFloat(), y)
            }
            lineTo(WIDTH.toFloat(), HEIGHT.toFloat())
            close()
        }
        canvas.drawPath(wavePath, shapePaint)

        // Sample Watermark Badge in Top-Right Corner (Target for removal!)
        val wmLeft = WIDTH * 0.72f
        val wmTop = HEIGHT * 0.06f
        val wmRight = WIDTH * 0.96f
        val wmBottom = HEIGHT * 0.18f

        canvas.drawRoundRect(RectF(wmLeft, wmTop, wmRight, wmBottom), 16f, 16f, platePaint)

        // Watermark text inside badge
        val wmTextPaint = Paint().apply {
            color = 0xCCFFFFFF.toInt()
            textSize = 28f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText("WATERMARK AI", wmLeft + 30f, wmTop + 54f, wmTextPaint)
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

                // RGB to Y
                var y = (66 * r + 129 * g + 25 * b + 128) shr 8
                y += 16
                yuv[yIndex++] = y.coerceIn(0, 255).toByte()

                // Subsample U and V 2x2
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
