package com.example.feature.create

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun CreateWatermarkScreen(
    viewModel: StudioViewModel,
    onNavigateToExport: () -> Unit,
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
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = "No Video",
                        tint = StudioSecondary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Watermark Creator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please import a video from Home or load the sample video to begin designing custom watermarks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioTextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadSampleVideo() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary),
                        modifier = Modifier.testTag("load_sample_creator_btn")
                    ) {
                        Text("Load Sample Video", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Video Preview with Live Watermark Overlay & Drag Gestures
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .aspectRatio(currentVideo?.aspectRatio ?: (16f / 9f))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
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

                // Draw base frame
                if (frameBitmap != null) {
                    drawImage(
                        image = frameBitmap!!.asImageBitmap(),
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(cw.toInt(), ch.toInt())
                    )
                }

                // Draw Repeat Grid Pattern if enabled
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

                // Draw Text Layer
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

                        // Optional plate
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

                        // Outline
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

            // Drag hint badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xBB101114), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Drag watermark directly on video to position",
                    fontSize = 10.sp,
                    color = StudioSecondary
                )
            }
        }

        // Feature Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Text Layer", "Positions", "Repeat Grid", "Animations")
            for (idx in tabs.indices) {
                val isSelected = activeTab == idx
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) StudioPrimary else Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = idx }
                        .testTag("tab_$idx")
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = tabs[idx],
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioPrimary else StudioTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Text Layer Customization
        if (activeTab == 0) {
            val tc = watermarkDesign.textConfig
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Text Content & Styling", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

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
                            focusedContainerColor = StudioElevated,
                            unfocusedContainerColor = StudioElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("watermark_text_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Font Size Slider
                    Text("Font Size: ${tc.fontSizeSp.toInt()} sp", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = tc.fontSizeSp,
                        onValueChange = {
                            viewModel.updateWatermarkDesign(
                                watermarkDesign.copy(textConfig = tc.copy(fontSizeSp = it))
                            )
                        },
                        valueRange = 12f..60f,
                        colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                    )

                    // Opacity Slider
                    Text("Opacity: ${(tc.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = tc.opacity,
                        onValueChange = {
                            viewModel.updateWatermarkDesign(
                                watermarkDesign.copy(textConfig = tc.copy(opacity = it))
                            )
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = StudioSecondary, activeTrackColor = StudioSecondary)
                    )

                    // Rotation Slider
                    Text("Rotation: ${tc.rotationDeg.toInt()}°", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = tc.rotationDeg,
                        onValueChange = {
                            viewModel.updateWatermarkDesign(
                                watermarkDesign.copy(textConfig = tc.copy(rotationDeg = it))
                            )
                        },
                        valueRange = -90f..90f,
                        colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color swatches
                    Text("Text Color", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val colors = listOf(
                            0xFFFFFFFF to "White",
                            0xFF5EEAD4 to "Cyan",
                            0xFFA78BFA to "Violet",
                            0xFFFBBF24 to "Gold",
                            0xFFFF6B75 to "Coral"
                        )
                        for ((cVal, label) in colors) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
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

                    // Background Plate Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Background Plate", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = tc.hasBackgroundPlate,
                            onCheckedChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(hasBackgroundPlate = it))
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioSecondary)
                        )
                    }

                    // Stroke Outline Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("High-Contrast Outline Stroke", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = tc.hasOutline,
                            onCheckedChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(textConfig = tc.copy(hasOutline = it))
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioPrimary)
                        )
                    }
                }
            }
        }

        // TAB 1: Position Presets
        if (activeTab == 1) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Position Presets & Anchors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val presets = PositionPreset.values()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (p in presets) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StudioElevated),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateWatermarkDesign(
                                            watermarkDesign.copy(
                                                textConfig = watermarkDesign.textConfig.copy(
                                                    position = NormalizedPoint(p.normX, p.normY)
                                                )
                                            )
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = p.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
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
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tiled Diagonal Grid Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = rg.enabled,
                            onCheckedChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(repeatGrid = rg.copy(enabled = it))
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StudioSecondary)
                        )
                    }

                    if (rg.enabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = rg.text,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(repeatGrid = rg.copy(text = it))
                                )
                            },
                            label = { Text("Grid Text") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioSecondary,
                                focusedContainerColor = StudioElevated,
                                unfocusedContainerColor = StudioElevated
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Grid Angle: ${rg.angleDeg.toInt()}°", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = rg.angleDeg,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(repeatGrid = rg.copy(angleDeg = it))
                                )
                            },
                            valueRange = -60f..60f,
                            colors = SliderDefaults.colors(thumbColor = StudioSecondary, activeTrackColor = StudioSecondary)
                        )

                        Text("Grid Opacity: ${(rg.opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = rg.opacity,
                            onValueChange = {
                                viewModel.updateWatermarkDesign(
                                    watermarkDesign.copy(repeatGrid = rg.copy(opacity = it))
                                )
                            },
                            valueRange = 0.05f..0.7f,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                        )
                    }
                }
            }
        }

        // TAB 3: Animations
        if (activeTab == 3) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Watermark Motion & Animation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val animations = AnimationType.values()
                    for (anim in animations) {
                        val isSelected = watermarkDesign.animation == anim
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) StudioPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.updateWatermarkDesign(watermarkDesign.copy(animation = anim))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Animation,
                                    contentDescription = null,
                                    tint = if (isSelected) StudioPrimary else StudioTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = anim.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) StudioPrimary else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Render & Export Button
        Button(
            onClick = { onNavigateToExport() },
            colors = ButtonDefaults.buttonColors(containerColor = StudioSecondary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp)
                .testTag("apply_watermark_export_btn")
        ) {
            Text("Apply Watermark & Export Video", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
        }
    }
}
