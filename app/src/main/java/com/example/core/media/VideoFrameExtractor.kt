package com.example.core.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoFrameExtractor {

    suspend fun extractFrame(
        context: Context,
        uri: Uri,
        timeMs: Long
    ): Bitmap? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            if (uri.scheme == "file") {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }
            val timeUs = timeMs * 1000L
            retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    suspend fun extractTimelineThumbnails(
        context: Context,
        uri: Uri,
        durationMs: Long,
        thumbnailCount: Int = 10,
        thumbWidth: Int = 120,
        thumbHeight: Int = 68
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val list = mutableListOf<Bitmap>()
        try {
            if (uri.scheme == "file") {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }

            val stepMs = if (durationMs > 0 && thumbnailCount > 1) {
                durationMs / thumbnailCount
            } else 1000L

            for (i in 0 until thumbnailCount) {
                val tMs = i * stepMs
                val frame = retriever.getFrameAtTime(tMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    val scaled = Bitmap.createScaledBitmap(frame, thumbWidth, thumbHeight, true)
                    list.add(scaled)
                    if (scaled != frame) frame.recycle()
                }
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        list
    }

    suspend fun extractNeighborFrames(
        context: Context,
        uri: Uri,
        centerTimeMs: Long,
        durationMs: Long,
        count: Int = 4
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        val neighbors = mutableListOf<Bitmap>()
        try {
            if (uri.scheme == "file") {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }

            val offsets = listOf(-600L, -300L, 300L, 600L)
            for (offset in offsets) {
                val t = (centerTimeMs + offset).coerceIn(0L, durationMs)
                if (t != centerTimeMs) {
                    val frame = retriever.getFrameAtTime(t * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
                    if (frame != null) neighbors.add(frame)
                }
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        neighbors
    }
}
