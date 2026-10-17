package com.example.core.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.domain.model.VideoInfo
import java.io.File

object VideoMetadataHelper {

    fun extractMetadata(context: Context, uri: Uri): VideoInfo {
        val retriever = MediaMetadataRetriever()
        return try {
            if (uri.scheme == "file") {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 10000L

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = widthStr?.toIntOrNull() ?: 1280
            val height = heightStr?.toIntOrNull() ?: 720

            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val rotation = rotationStr?.toIntOrNull() ?: 0

            val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val bitrate = bitrateStr?.toLongOrNull() ?: 4_000_000L

            val hasAudioStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)
            val hasAudio = hasAudioStr != null && hasAudioStr == "yes"

            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "video/mp4"

            var fileSize = 0L
            if (uri.scheme == "file") {
                val f = File(uri.path ?: "")
                if (f.exists()) fileSize = f.length()
            }

            val displayName = uri.lastPathSegment ?: "Imported_Video.mp4"

            // Adjust width/height if rotated 90 or 270 degrees
            val finalWidth = if (rotation == 90 || rotation == 270) height else width
            val finalHeight = if (rotation == 90 || rotation == 270) width else height

            VideoInfo(
                uriString = uri.toString(),
                displayName = displayName,
                durationMs = durationMs,
                width = finalWidth,
                height = finalHeight,
                fps = 30f,
                bitrate = bitrate,
                hasAudio = hasAudio,
                rotationDegrees = rotation,
                mimeType = mimeType,
                fileSizeBytes = fileSize
            )
        } catch (e: Exception) {
            VideoInfo(
                uriString = uri.toString(),
                displayName = uri.lastPathSegment ?: "Video.mp4",
                durationMs = 10000L,
                width = 1280,
                height = 720,
                fps = 30f,
                hasAudio = false
            )
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }
}
