package com.example.domain.model

enum class PositionPreset(val title: String, val normX: Float, val normY: Float) {
    TOP_LEFT("Top-Left", 0.08f, 0.08f),
    TOP_CENTER("Top-Center", 0.5f, 0.08f),
    TOP_RIGHT("Top-Right", 0.92f, 0.08f),
    CENTER("Center", 0.5f, 0.5f),
    BOTTOM_LEFT("Bottom-Left", 0.08f, 0.92f),
    BOTTOM_CENTER("Bottom-Center", 0.5f, 0.92f),
    BOTTOM_RIGHT("Bottom-Right", 0.92f, 0.92f),
    CUSTOM("Custom Drag", 0.5f, 0.5f)
}

enum class AnimationType(val title: String) {
    NONE("Static"),
    FADE("Fade In / Out"),
    PULSE("Pulse / Breath"),
    SLIDE_HORIZONTAL("Horizontal Drift"),
    ROTATING("Subtle Spin")
}

data class TextWatermarkConfig(
    val text: String = "© Studio Creator",
    val fontSizeSp: Float = 24f,
    val textColor: Long = 0xFFFFFFFF,
    val opacity: Float = 0.85f,
    val rotationDeg: Float = 0f,
    val position: NormalizedPoint = NormalizedPoint(0.85f, 0.92f),
    val hasOutline: Boolean = true,
    val outlineColor: Long = 0xFF000000,
    val outlineWidth: Float = 2.5f,
    val hasShadow: Boolean = true,
    val shadowColor: Long = 0xAA000000,
    val shadowRadius: Float = 4f,
    val hasBackgroundPlate: Boolean = false,
    val plateColor: Long = 0x66000000,
    val plateCornerRadius: Float = 8f,
    val isBold: Boolean = true
)

data class LogoWatermarkConfig(
    val uriString: String? = null,
    val scale: Float = 0.2f,       // proportion of video width
    val opacity: Float = 0.9f,
    val rotationDeg: Float = 0f,
    val position: NormalizedPoint = NormalizedPoint(0.12f, 0.12f)
)

data class RepeatGridConfig(
    val enabled: Boolean = false,
    val text: String = "CONFIDENTIAL",
    val angleDeg: Float = -30f,
    val spacingDp: Float = 140f,
    val fontSizeSp: Float = 18f,
    val opacity: Float = 0.25f,
    val textColor: Long = 0xFFFFFFFF
)

data class WatermarkDesign(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Custom Watermark",
    val textConfig: TextWatermarkConfig = TextWatermarkConfig(),
    val logoConfig: LogoWatermarkConfig = LogoWatermarkConfig(),
    val repeatGrid: RepeatGridConfig = RepeatGridConfig(),
    val animation: AnimationType = AnimationType.NONE,
    val animationDurationSec: Float = 3.0f,
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = Long.MAX_VALUE
)
