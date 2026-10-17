package com.example.domain.model

data class ProjectItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Untitled Video Project",
    val videoUri: String,
    val thumbnailUri: String? = null,
    val durationMs: Long = 0L,
    val width: Int = 1280,
    val height: Int = 720,
    val isWatermarkCreation: Boolean = false,
    val removalMethod: RemovalMethod = RemovalMethod.TELEA,
    val mask: WatermarkMask = WatermarkMask(),
    val watermarkText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val m = totalSeconds / 60
            val s = totalSeconds % 60
            return String.format("%02d:%02d", m, s)
        }
}
