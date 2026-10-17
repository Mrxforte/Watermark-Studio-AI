package com.example.feature.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonGlowBrush
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

/**
 * 1:1 Pixel-Perfect Glassmorphic Card from reference mockup.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    borderGlow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color(0x140F172A),
                spotColor = Color(0x100284C7)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = if (borderGlow) Brush.linearGradient(
                    listOf(
                        Color(0x400284C7),
                        Color(0x2638BDF8),
                        Color(0x20E2E8F0)
                    )
                ) else Brush.linearGradient(listOf(StudioBorder, StudioBorder)),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/**
 * Deep Navy / Black High-Contrast Primary Pill Button.
 */
@Composable
fun NavyPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color(0x260F172A),
                spotColor = Color(0x200284C7)
            )
            .clip(CircleShape)
            .background(
                if (enabled) Brush.horizontalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                ) else Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
            )
            .border(
                width = 1.dp,
                brush = if (enabled) Brush.horizontalGradient(
                    listOf(Color(0x660284C7), Color(0x3338BDF8))
                ) else Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFFCBD5E1))),
                shape = CircleShape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * High-Tech Pill Button (Selectable filter / tab style).
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(
                    Color(0xFFE0F2FE),
                    CircleShape
                ) else Modifier.background(Color(0xFFF1F5F9), CircleShape)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) StudioPrimary else StudioBorder,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.invoke()
            if (leadingIcon != null) Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = if (isSelected) StudioPrimary else StudioTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Solid Cyan Neon Action Button.
 */
@Composable
fun NeonActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color(0x330284C7),
                spotColor = Color(0x330284C7)
            )
            .clip(CircleShape)
            .background(
                if (enabled) Brush.horizontalGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
                ) else Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Status indicator pill with glowing LED dot.
 */
@Composable
fun LuminousStatusPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = StudioPrimary
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFE0F2FE))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Segmented Pill Tab Switcher (Light Theme: clean light gray background with white elevated active pill).
 */
@Composable
fun SegmentedPillSwitcher(
    options: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
            .border(1.dp, Color(0xFFE2E8F0), CircleShape)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (idx in options.indices) {
                val isSelected = selectedIndex == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier
                                .shadow(2.dp, CircleShape, ambientColor = Color(0x140F172A), spotColor = Color(0x100284C7))
                                .background(Color.White, CircleShape)
                            else Modifier
                        )
                        .then(
                            if (isSelected) Modifier.border(
                                1.dp,
                                Color(0x400284C7),
                                CircleShape
                            ) else Modifier
                        )
                        .clickable { onSelectIndex(idx) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = options[idx],
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StudioPrimary else Color(0xFF64748B),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Sleek List Row Item with trailing chevron (Clean White Card with Soft Slate Border).
 */
@Composable
fun GlassListRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailingText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x0F0F172A), spotColor = Color(0x0A0284C7))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE))
                            .border(1.dp, Color(0x330284C7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = StudioPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = StudioTextSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        fontSize = 12.sp,
                        color = StudioPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Data Insights Waveform Card (Clean Light High-Tech Graph).
 */
@Composable
fun DataInsightsWaveform(
    modifier: Modifier = Modifier,
    title: String = "Data Insights",
    subtitle: String = "Real-time Frame Stability & Seam Analysis",
    psnrText: String = "99.4%"
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = StudioTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .border(1.dp, Color(0x400284C7), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = psnrText,
                        color = StudioPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(0f, h * 0.70f)
                    cubicTo(w * 0.18f, h * 0.85f, w * 0.32f, h * 0.20f, w * 0.48f, h * 0.40f)
                    cubicTo(w * 0.65f, h * 0.60f, w * 0.82f, h * 0.15f, w, h * 0.30f)
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x260284C7),
                            Color(0x080284C7),
                            Color.Transparent
                        )
                    )
                )

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8))),
                    style = Stroke(width = 3.5f)
                )

                val keyPoints = listOf(
                    Offset(w * 0.48f, h * 0.40f),
                    Offset(w * 0.82f, h * 0.15f)
                )
                for (pt in keyPoints) {
                    drawCircle(Color(0x330284C7), radius = 10f, center = pt)
                    drawCircle(StudioPrimary, radius = 5f, center = pt)
                    drawCircle(Color.White, radius = 2.5f, center = pt)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00:00", "00:01", "00:02", "00:03", "00:04", "00:05").forEach { label ->
                    Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
