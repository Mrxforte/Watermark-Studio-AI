package com.example.feature.exports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExportResolution
import com.example.feature.ExportState
import com.example.feature.StudioViewModel
import com.example.feature.components.GlassCard
import com.example.feature.components.GlassPillButton
import com.example.feature.components.LuminousStatusPill
import com.example.feature.components.NeonActionButton
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioError
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun ExportQueueScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val completedExports by viewModel.completedExports.collectAsState()

    var selectedResolution by remember { mutableStateOf(ExportResolution.ORIGINAL) }
    var selectedFps by remember { mutableStateOf(30) }
    var selectedBitrate by remember { mutableStateOf(6.0f) }
    var includeAudio by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 85.dp)
    ) {
        Text(
            text = "Render & Export Pipeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = StudioTextPrimary
        )
        Text(
            text = "Hardware-accelerated H.264 MediaCodec video encoder",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Export Progress Card
        when (val state = exportState) {
            is ExportState.InProgress -> {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_progress_card"),
                    cornerRadius = 22.dp,
                    borderGlow = true
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rendering in Progress",
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                            LuminousStatusPill(text = "${(state.progress * 100).toInt()}%", color = StudioPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StudioPrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassPillButton(
                            text = "Cancel Export",
                            onClick = { viewModel.cancelExport() },
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            is ExportState.Success -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    borderGlow = true
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Completed Successfully!", fontWeight = FontWeight.Bold, color = StudioSuccess)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• Output: ${state.file.name}", fontSize = 12.sp, color = StudioTextPrimary)
                        Text("• Size: %.1f MB".format(state.bytes / (1024f * 1024f)), fontSize = 12.sp, color = StudioTextPrimary)
                        Text("• Processed: ${state.frames} frames", fontSize = 12.sp, color = StudioTextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        NeonActionButton(
                            text = "Done",
                            onClick = { viewModel.resetExportState() },
                            modifier = Modifier.height(42.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            is ExportState.Failed -> {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Export Encountered an Error", fontWeight = FontWeight.Bold, color = StudioError)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(state.error, fontSize = 12.sp, color = StudioTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassPillButton(
                            text = "Dismiss",
                            onClick = { viewModel.resetExportState() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            ExportState.Idle -> {}
        }

        // Export Configuration Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Export Profile Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StudioTextPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                // Resolution
                Text("Output Resolution", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (res in ExportResolution.values()) {
                        val isSel = selectedResolution == res
                        GlassPillButton(
                            text = res.label,
                            onClick = { selectedResolution = res },
                            isSelected = isSel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FPS
                Text("Framerate: $selectedFps FPS", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (fps in listOf(24, 30, 60)) {
                        val isSel = selectedFps == fps
                        GlassPillButton(
                            text = "$fps FPS",
                            onClick = { selectedFps = fps },
                            isSelected = isSel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bitrate
                Text("Bitrate: ${selectedBitrate.toInt()} Mbps", style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (br in listOf(4.0f, 6.0f, 8.0f, 12.0f)) {
                        val isSel = selectedBitrate == br
                        GlassPillButton(
                            text = "${br.toInt()}M",
                            onClick = { selectedBitrate = br },
                            isSelected = isSel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Passthrough Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Preserve Audio Track", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                        Text("Passthrough original audio muxing", fontSize = 10.sp, color = StudioTextSecondary)
                    }
                    Switch(
                        checked = includeAudio,
                        onCheckedChange = { includeAudio = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Start Export Button
                NeonActionButton(
                    text = if (currentVideo != null) "Start Render & Export MP4" else "Import Video First",
                    onClick = {
                        viewModel.startExport(
                            resolution = selectedResolution,
                            fps = selectedFps,
                            bitrateMbps = selectedBitrate,
                            isWatermarkCreation = false
                        )
                    },
                    enabled = currentVideo != null && exportState !is ExportState.InProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_render_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Completed Exports List
        Text(
            text = "Completed Exports (${completedExports.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = StudioTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (completedExports.isEmpty()) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No exported files yet. Rendered MP4 files will appear here.", fontSize = 12.sp, color = StudioTextSecondary)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (exp in completedExports) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2FE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(exp.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, color = StudioTextPrimary)
                                    Text(
                                        text = "${exp.resolution} • %.1f MB".format(exp.fileSizeBytes / (1024f * 1024f)),
                                        fontSize = 11.sp,
                                        color = StudioTextSecondary
                                    )
                                    Text(
                                        text = exp.methodOrDesign,
                                        fontSize = 10.sp,
                                        color = StudioPrimary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteExport(exp.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StudioTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
