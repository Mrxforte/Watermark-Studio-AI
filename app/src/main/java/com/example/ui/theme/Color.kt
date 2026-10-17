package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Futuristic Glassmorphism Palette (Clean Light High-Tech Edition: Pure white surfaces, crisp black/slate text, vibrant cyan/blue)
val StudioBackground = Color(0xFFF6F8FC) // Clean light pearl background
val StudioSurface = Color(0xFFFFFFFF) // Pure crisp white surface
val StudioElevated = Color(0xFFF1F5F9) // Clean elevated surface
val StudioCard = Color(0xFFFFFFFF) // White card
val StudioCardDark = Color(0xFFF8FAFC) // Very light subtle card
val StudioBorder = Color(0xFFE2E8F0) // Subtle crisp border
val StudioBorderGlow = Color(0x330284C7) // Soft electric cyan glow border

val StudioPrimary = Color(0xFF0284C7) // Electric Blue / Vivid Cyan - crisp & striking on white
val StudioPrimaryLight = Color(0xFF0EA5E9)
val StudioPrimaryGlow = Color(0x260284C7)
val StudioPrimaryContainer = Color(0xFFE0F2FE)
val StudioOnPrimaryContainer = Color(0xFF0369A1)

val StudioSecondary = Color(0xFF2563EB) // Royal Electric Blue
val StudioSecondaryGlow = Color(0x262563EB)
val StudioSecondaryContainer = Color(0xFFDBEAFE)
val StudioOnSecondaryContainer = Color(0xFF1E40AF)

val StudioAccentViolet = Color(0xFF6366F1)

val StudioTextPrimary = Color(0xFF0F172A) // Rich deep black / slate text
val StudioTextSecondary = Color(0xFF475569) // Dark secondary slate text
val StudioTextTertiary = Color(0xFF94A3B8) // Muted subtle text

val StudioError = Color(0xFFEF4444)
val StudioOnError = Color(0xFFFFFFFF)
val StudioSuccess = Color(0xFF10B981)
val StudioWarning = Color(0xFFF59E0B)

// Mask Editor Overlays
val MaskOverlayRed = Color(0x99EF4444)
val MaskBoundingCyan = Color(0xFF0284C7)
val MaskHandleViolet = Color(0xFF38BDF8)
val MaskGridSubtle = Color(0x1A0284C7)

// Glassmorphism Gradients (Clean light glass with soft sheen)
val GlassCardBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFBFDFF)
    )
)

val GlassElevatedBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF1F5F9)
    )
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x660EA5E9),
        Color(0x3338BDF8),
        Color(0x20CBD5E1)
    )
)

val NeonGlowBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF0284C7),
        Color(0xFF2563EB)
    )
)

