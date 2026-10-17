package com.example.feature.remove

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RemovalMethod
import com.example.feature.StudioViewModel
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioError
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioWarning

@Composable
fun RemoveWatermarkScreen(
    viewModel: StudioViewModel,
    onNavigateToExport: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val originalBitmap by viewModel.currentFrameBitmap.collectAsState()
    val processedBitmap by viewModel.processedPreviewBitmap.collectAsState()
    val mask by viewModel.currentMask.collectAsState()
    val selectedMethod by viewModel.selectedMethod.collectAsState()
    val recommendation by viewModel.methodRecommendation.collectAsState()
    val qualityReport by viewModel.qualityReport.collectAsState()
    val isSplitCompare by viewModel.isSplitCompareEnabled.collectAsState()
    val splitRatio by viewModel.compareSplitRatio.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val thumbnails by viewModel.timelineThumbnails.collectAsState()

    var showTuningSheet by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "No Video",
                        tint = StudioPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Video Loaded",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please load a video from the Studio Home or tap below to test with a built-in sample video.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioTextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.loadSampleVideo() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                        modifier = Modifier.testTag("load_sample_btn")
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
        // Video Header Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentVideo?.displayName ?: "Video",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${currentVideo?.resolutionFormatted} • ${currentVideo?.durationFormatted} • ${currentVideo?.fps?.toInt()} fps",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Split Compare Button
                OutlinedButton(
                    onClick = { viewModel.toggleSplitCompare() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSplitCompare) StudioPrimary.copy(alpha = 0.2f) else Color.Transparent
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isSplitCompare) StudioPrimary else StudioBorder)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("compare_split_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = "Compare",
                        tint = if (isSplitCompare) StudioPrimary else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSplitCompare) "Split ON" else "Compare",
                        fontSize = 11.sp,
                        color = if (isSplitCompare) StudioPrimary else Color.White
                    )
                }

                // Mask Fine-tune Button
                IconButton(
                    onClick = { showTuningSheet = !showTuningSheet },
                    modifier = Modifier
                        .size(36.dp)
                        .background(StudioElevated, CircleShape)
                        .testTag("mask_tuning_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tuning",
                        tint = if (showTuningSheet) StudioSecondary else StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live Interactive Preview & Mask Editor Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(currentVideo?.aspectRatio ?: (16f / 9f))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
        ) {
            MaskEditorCanvas(
                originalBitmap = originalBitmap,
                processedBitmap = processedBitmap,
                currentMaskRect = mask.rect,
                isSplitCompare = isSplitCompare,
                splitRatio = splitRatio,
                onMaskRectChanged = { viewModel.updateMaskRect(it) },
                onSplitRatioChanged = { viewModel.setCompareSplitRatio(it) }
            )

            // Touch instruction badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color(0xBB101114), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isSplitCompare) "Drag center divider to compare" else "Drag box or corners to position mask",
                    fontSize = 10.sp,
                    color = StudioSecondary
                )
            }
        }

        // Timeline Scrubber
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Timeline: ${String.format("%02d:%02d", (currentTimeMs / 1000) / 60, (currentTimeMs / 1000) % 60)}",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = currentVideo?.durationFormatted ?: "00:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioTextSecondary
                )
            }

            // Thumbnail strip
            if (thumbnails.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioElevated)
                ) {
                    for (thumb in thumbnails) {
                        androidx.compose.foundation.Image(
                            bitmap = thumb.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            Slider(
                value = currentTimeMs.toFloat(),
                onValueChange = { viewModel.seekToTime(it.toLong()) },
                valueRange = 0f..(currentVideo?.durationMs?.toFloat() ?: 10000f),
                colors = SliderDefaults.colors(
                    thumbColor = StudioPrimary,
                    activeTrackColor = StudioPrimary,
                    inactiveTrackColor = StudioElevated
                ),
                modifier = Modifier.testTag("timeline_slider")
            )
        }

        // Quality HUD Card
        if (qualityReport != null) {
            val qr = qualityReport!!
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PSNR", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                        Text("%.1f dB".format(qr.psnrDb), fontWeight = FontWeight.Bold, color = StudioSuccess)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Seam Secrecy", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                        val seamPct = ((1f - qr.boundaryDiscontinuityScore) * 100).toInt()
                        Text("$seamPct%", fontWeight = FontWeight.Bold, color = if (seamPct > 70) StudioSuccess else StudioWarning)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Blur Factor", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                        Text("${qr.estimatedBlurPercent.toInt()}%", fontWeight = FontWeight.Bold, color = StudioPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Flicker Risk", style = MaterialTheme.typography.labelSmall, color = StudioTextSecondary)
                        Text(qr.flickeringRisk, fontWeight = FontWeight.Bold, color = StudioSecondary)
                    }
                }
            }
        }

        // Mask Tuning Panel (if expanded)
        if (showTuningSheet) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mask Boundaries & Blending", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dilation
                    Text("Boundary Dilation: ${mask.dilationRadius} px", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = mask.dilationRadius.toFloat(),
                        onValueChange = { viewModel.updateMaskSettings(it.toInt(), mask.featherRadius, mask.inpaintRadius) },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = StudioSecondary, activeTrackColor = StudioSecondary)
                    )

                    // Feathering
                    Text("Edge Feathering: ${mask.featherRadius} px", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = mask.featherRadius.toFloat(),
                        onValueChange = { viewModel.updateMaskSettings(mask.dilationRadius, it.toInt(), mask.inpaintRadius) },
                        valueRange = 0f..8f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                    )

                    // Radius
                    Text("Inpaint Radius: ${mask.inpaintRadius} px", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = mask.inpaintRadius.toFloat(),
                        onValueChange = { viewModel.updateMaskSettings(mask.dilationRadius, mask.featherRadius, it.toInt()) },
                        valueRange = 2f..9f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary)
                    )
                }
            }
        }

        // Automatic Recommendation Card
        if (recommendation != null) {
            val rec = recommendation!!
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Recommended",
                        tint = StudioSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Auto Recommendation: ${rec.recommendedMethod.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StudioSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${(rec.confidenceScore * 100).toInt()}% match)",
                                fontSize = 11.sp,
                                color = StudioSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }

        // Method Selector Tabs
        Text(
            text = "Reconstruction Engine",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (m in RemovalMethod.values()) {
                val isSelected = selectedMethod == m
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioPrimary.copy(alpha = 0.2f) else StudioElevated
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) StudioPrimary else StudioBorder
                    ),
                    modifier = Modifier
                        .clickable { viewModel.selectMethod(m) }
                        .testTag("method_${m.name.lowercase()}")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            text = m.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isSelected) StudioPrimary else Color.White
                        )
                        Text(
                            text = m.category.name.replace("_", " "),
                            fontSize = 9.sp,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        }

        // Method Explanation & Limitations Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = selectedMethod.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Strengths: ${selectedMethod.strengths}",
                    fontSize = 11.sp,
                    color = StudioSuccess
                )
                Text(
                    text = "• Limitation: ${selectedMethod.limitations}",
                    fontSize = 11.sp,
                    color = StudioWarning
                )
            }
        }

        // Actions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onNavigateToAssistant() },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_advisor_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = StudioSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gemini 4 AI", fontSize = 12.sp, color = Color.White)
            }

            Button(
                onClick = {
                    viewModel.saveCurrentProject(currentVideo?.displayName ?: "Project")
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_project_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
            }

            Button(
                onClick = { onNavigateToExport() },
                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("proceed_export_btn")
            ) {
                Text("Render & Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
        }
    }
}
