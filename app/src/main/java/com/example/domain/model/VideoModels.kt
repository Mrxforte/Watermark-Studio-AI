package com.example.domain.model

data class VideoInfo(
    val uriString: String,
    val displayName: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val fps: Float = 30f,
    val bitrate: Long = 0L,
    val hasAudio: Boolean = true,
    val rotationDegrees: Int = 0,
    val mimeType: String = "video/mp4",
    val fileSizeBytes: Long = 0L
) {
    val aspectRatio: Float get() = if (height > 0) width.toFloat() / height.toFloat() else 16f / 9f
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val m = totalSeconds / 60
            val s = totalSeconds % 60
            return String.format("%02d:%02d", m, s)
        }
    val resolutionFormatted: String get() = "${width}x${height}"
}

enum class ExportResolution(val label: String, val targetHeight: Int) {
    ORIGINAL("Original Resolution", -1),
    HD_720P("720p HD", 720),
    FHD_1080P("1080p Full HD", 1080)
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.ORIGINAL,
    val fps: Int = 30,
    val bitrateMbps: Float = 6.0f,
    val includeAudio: Boolean = true,
    val removalMethod: RemovalMethod = RemovalMethod.TELEA,
    val mask: WatermarkMask = WatermarkMask(),
    val watermarkDesign: WatermarkDesign? = null,
    val isWatermarkCreationMode: Boolean = false
)

data class MethodRecommendation(
    val recommendedMethod: RemovalMethod,
    val confidenceScore: Float, // 0.0 .. 1.0
    val explanation: String,
    val qualityRisks: List<String>,
    val alternativeMethods: List<RemovalMethod>
)
