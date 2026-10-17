package com.example.feature.create

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AnimationType
import com.example.domain.model.NormalizedPoint
import com.example.domain.model.PositionPreset
import com.example.feature.StudioViewModel
import com.example.feature.components.GlassCard
import com.example.feature.components.NavyPillButton
import com.example.feature.components.SegmentedPillSwitcher
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun CreateWatermarkScreen(
    viewModel: StudioViewModel,
    onNavigateToExport: () -> Unit,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val frameBitmap by viewModel.currentFrameBitmap.collectAsState()
    val watermarkDesign by viewModel.watermarkDesign.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Text, 1: Positions, 2: Repeat Grid, 3: Animations

    if (currentVideo == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F2644))
                            .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "No Video",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Watermark Creator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please import a video from Home or load the sample video to begin designing custom watermarks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NavyPillButton(
                        text = "Load Sample Video",
                        onClick = { viewModel.loadSampleVideo() },
                        modifier = Modifier.testTag("load_sample_creator_btn")
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 95.dp)
    ) {
        // Top Bar: < Back, Centered Title, Right Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StudioTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Watermark Designer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                letterSpacing = 0.5.sp
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = StudioPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Live Floating 3D Frosted Glass Video Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(currentVideo?.aspectRatio ?: (16f / 9f))
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black)
                .border(1.5.dp, GlassBorderBrush, RoundedCornerShape(26.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("watermark_preview_canvas")
                    .pointerInput(watermarkDesign) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val cw = size.width.toFloat()
                            val ch = size.height.toFloat()
                            val curPos = watermarkDesign.textConfig.position
                            val newX = (curPos.x + dragAmount.x / cw).coerceIn(0.05f, 0.95f)
                            val newY = (curPos.y + dragAmount.y / ch).coerceIn(0.05f, 0.95f)
                            viewModel.updateWatermarkDesign(
                                watermarkDesign.copy(
                                    textConfig = watermarkDesign.textConfig.copy(position = NormalizedPoint(newX, newY))
                                )
                            )
                        }
                    }
            ) {
                val cw = size.width
                val ch = size.height

                if (frameBitmap != null) {
                    drawImage(
                        image = frameBitmap!!.asImageBitmap(),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(cw.toInt(), ch.toInt())
                    )
                }

                // Repeat Grid Pattern
                val rg = watermarkDesign.repeatGrid
                if (rg.enabled) {
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate(rg.angleDeg, cw / 2, ch / 2)
                        val paint = android.graphics.Paint().apply {
                            color = rg.textColor.toInt()
                            alpha = (rg.opacity * 255).toInt().coerceIn(0, 255)
                            textSize = rg.fontSizeSp * (cw / 600f)
                            isAntiAlias = true
                            isFakeBoldText = true
                        }
                        val spacing = rg.spacingDp * 1.6f
                        var y = -ch * 0.5f
                        while (y < ch * 1.5f) {
                            var x = -cw * 0.5f
                            while (x < cw * 1.5f) {
                                drawText(rg.text, x, y, paint)
                                x += spacing
                            }
                            y += spacing * 0.6f
                        }
                        restore()
                    }
                }

                // Text Layer
                val tc = watermarkDesign.textConfig
                if (tc.text.isNotBlank()) {
                    val px = tc.position.x * cw
                    val py = tc.position.y * ch

                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        rotate(tc.rotationDeg, px, py)

                        val textPaint = android.graphics.Paint().apply {
                            color = tc.textColor.toInt()
                            alpha = (tc.opacity * 255).toInt().coerceIn(0, 255)
                            textSize = tc.fontSizeSp * (cw / 380f).coerceIn(1.2f, 2.5f)
                            isAntiAlias = true
                            isFakeBoldText = tc.isBold
                        }

                        val textWidth = textPaint.measureText(tc.text)
                        val metrics = textPaint.fontMetrics

                        if (tc.hasBackgroundPlate) {
                            val platePaint = android.graphics.Paint().apply {
                                color = tc.plateColor.toInt()
                                isAntiAlias = true
                            }
                            val pad = 12f
                            drawRoundRect(
                                android.graphics.RectF(
                                    px - textWidth / 2 - pad,
                                    py + metrics.top - pad / 2,
                                    px + textWidth / 2 + pad,
                                    py + metrics.bottom + pad / 2
                                ),
                                tc.plateCornerRadius,
                                tc.plateCornerRadius,
                                platePaint
                            )
                        }

                        if (tc.hasOutline) {
                            val strokePaint = android.graphics.Paint(textPaint).apply {
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = tc.outlineWidth * 2f
                                color = tc.outlineColor.toInt()
                                alpha = (tc.opacity * 255).toInt().coerceIn(0, 255)
                            }
                            drawText(tc.text, px - textWidth / 2, py, strokePaint)
                        }

                        drawText(tc.text, px - textWidth / 2, py, textPaint)
                        restore()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xF0FFFFFF))
                    .border(1.dp, Color(0x400284C7), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Touch and drag to position watermark directly",
                    fontSize = 11.sp,
                    color = StudioPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Pill Switcher (Text | Positions | Grid | Motion)
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SegmentedPillSwitcher(
                options = listOf("Text", "Positions", "Grid", "Motion"),
                selectedIndex = activeTab,
                onSelectIndex = { activeTab = it }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TAB 0: Text Layer Customization
        if (activeTab == 0) {
            val tc = watermarkDesign.textConfig
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Text Content & Typography", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudioTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = tc.text,
                            onValueChange = { newText ->
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(text = newText))
                                )
                            },
                            label = { Text("Watermark Text") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioPrimary,
                                unfocusedBorderColor = StudioBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("watermark_text_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Font Size: ${tc.fontSizeSp.toInt()} sp", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = tc.fontSizeSp,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(fontSizeSp = it))
                                )
                            },
                            valueRange = 12f..60f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary, inactiveTrackColor = Color(0xFFE2E8F0))
                        )

                        Text("Opacity: ${(tc.opacity * 100).toInt()}%", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = tc.opacity,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(opacity = it))
                                )
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8), inactiveTrackColor = Color(0xFFE2E8F0))
                        )

                        Text("Rotation: ${tc.rotationDeg.toInt()}°", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = tc.rotationDeg,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(rotationDeg = it))
                                )
                            },
                            valueRange = -90f..90f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary, inactiveTrackColor = Color(0xFFE2E8F0))
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Palette", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val colors = listOf(
                                0xFFFFFFFF to "White",
                                0xFF00E5FF to "Cyan",
                                0xFF38BDF8 to "Blue",
                                0xFFFFB74D to "Gold",
                                0xFFFF5252 to "Coral"
                            )
                            for ((cVal, label) in colors) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(cVal), CircleShape)
                                        .border(
                                            width = if (tc.textColor == cVal) 2.5.dp else 1.dp,
                                            color = if (tc.textColor == cVal) Color.White else StudioBorder,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateWatermarkDesign(
                                                watermarkDesign.copy(textConfig = tc.copy(textColor = cVal))
                                            )
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Background Plate", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                            Switch(
                                checked = tc.hasBackgroundPlate,
                                onCheckedChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(textConfig = tc.copy(hasBackgroundPlate = it))
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E5FF))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("High-Contrast Outline", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                            Switch(
                                checked = tc.hasOutline,
                                onCheckedChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(textConfig = tc.copy(hasOutline = it))
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: Position Presets
        if (activeTab == 1) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Position Anchors", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudioTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val presets = PositionPreset.values()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (p in presets) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.updateWatermarkDesign(
                                                watermarkDesign.copy(
                                                    textConfig = watermarkDesign.textConfig.copy(
                                                        position = NormalizedPoint(p.normX, p.normY)
                                                    )
                                                )
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = p.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: Repeat Grid Pattern
        if (activeTab == 2) {
            val rg = watermarkDesign.repeatGrid
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Diagonal Grid Matrix", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudioTextPrimary)
                            Switch(
                                checked = rg.enabled,
                                onCheckedChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(repeatGrid = rg.copy(enabled = it))
                                    )
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = StudioPrimary)
                            )
                        }

                        if (rg.enabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = rg.text,
                                onValueChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(repeatGrid = rg.copy(text = it))
                                    )
                                },
                                label = { Text("Grid Text") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioPrimary,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Grid Angle: ${rg.angleDeg.toInt()}°", fontSize = 11.sp, color = StudioTextSecondary)
                            Slider(
                                value = rg.angleDeg,
                                onValueChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(repeatGrid = rg.copy(angleDeg = it))
                                    )
                                },
                                valueRange = -60f..60f,
                                colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary, inactiveTrackColor = Color(0xFFE2E8F0))
                            )

                            Text("Grid Opacity: ${(rg.opacity * 100).toInt()}%", fontSize = 11.sp, color = StudioTextSecondary)
                            Slider(
                                value = rg.opacity,
                                onValueChange = {
                                    viewModel.updateWatermarkDesign(
                                        watermarkDesign.copy(repeatGrid = rg.copy(opacity = it))
                                    )
                                },
                                valueRange = 0.05f..0.7f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8), inactiveTrackColor = Color(0xFFE2E8F0))
                            )
                        }
                    }
                }
            }
        }

        // TAB 3: Animations
        if (activeTab == 3) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Motion Presets", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudioTextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val animations = AnimationType.values()
                        for (anim in animations) {
                            val isSelected = watermarkDesign.animation == anim
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isSelected) StudioPrimary else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.updateWatermarkDesign(watermarkDesign.copy(animation = anim))
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Animation,
                                        contentDescription = null,
                                        tint = if (isSelected) StudioPrimary else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = anim.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) StudioPrimary else StudioTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Floating Bottom Primary Action Pill Button
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NavyPillButton(
                text = "Apply Watermark & Export Final MP4",
                onClick = { onNavigateToExport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("apply_watermark_export_btn"),
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}
