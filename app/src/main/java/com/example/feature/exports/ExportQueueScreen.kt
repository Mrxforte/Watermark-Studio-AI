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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
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
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "Render & Export Pipeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_progress_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                            Text(
                                text = "${(state.progress * 100).toInt()}%",
                                fontWeight = FontWeight.Bold,
                                color = StudioPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StudioPrimary,
                            trackColor = StudioCard
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = state.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.cancelExport() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StudioError),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            is ExportState.Success -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Completed Successfully!", fontWeight = FontWeight.Bold, color = StudioSuccess)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Output File: ${state.file.name}", fontSize = 12.sp)
                        Text("• File Size: %.1f MB".format(state.bytes / (1024f * 1024f)), fontSize = 12.sp)
                        Text("• Processed: ${state.frames} frames", fontSize = 12.sp)
                        Text("• Location: ${state.file.absolutePath}", fontSize = 10.sp, color = StudioTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.resetExportState() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSuccess)
                        ) {
                            Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            is ExportState.Failed -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudioElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Export Encountered an Error", fontWeight = FontWeight.Bold, color = StudioError)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(state.error, fontSize = 12.sp, color = StudioTextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.resetExportState() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioError)
                        ) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            ExportState.Idle -> {}
        }

        // Export Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export Profile Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Resolution
                Text("Output Resolution", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (res in ExportResolution.values()) {
                        val isSel = selectedResolution == res
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) StudioPrimary.copy(alpha = 0.2f) else StudioElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (isSel) StudioPrimary else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedResolution = res }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = res.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) StudioPrimary else StudioTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // FPS
                Text("Framerate: $selectedFps FPS", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (fps in listOf(24, 30, 60)) {
                        val isSel = selectedFps == fps
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) StudioSecondary.copy(alpha = 0.2f) else StudioElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (isSel) StudioSecondary else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFps = fps }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$fps FPS",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) StudioSecondary else StudioTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bitrate
                Text("Bitrate: ${selectedBitrate.toInt()} Mbps", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (br in listOf(4.0f, 6.0f, 8.0f, 12.0f)) {
                        val isSel = selectedBitrate == br
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) StudioPrimary.copy(alpha = 0.2f) else StudioElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (isSel) StudioPrimary else Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedBitrate = br }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${br.toInt()}M",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) StudioPrimary else StudioTextSecondary
                                )
                            }
                        }
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
                        Text("Preserve Audio Track", style = MaterialTheme.typography.bodySmall)
                        Text("Passthrough original audio muxing", fontSize = 10.sp, color = StudioTextSecondary)
                    }
                    Switch(
                        checked = includeAudio,
                        onCheckedChange = { includeAudio = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioSecondary)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Start Export Button
                Button(
                    onClick = {
                        viewModel.startExport(
                            resolution = selectedResolution,
                            fps = selectedFps,
                            bitrateMbps = selectedBitrate,
                            isWatermarkCreation = false
                        )
                    },
                    enabled = currentVideo != null && exportState !is ExportState.InProgress,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_render_button")
                ) {
                    Text(
                        text = if (currentVideo != null) "Start Render & Export MP4" else "Import Video First",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Completed Exports List
        Text(
            text = "Completed Exports (${completedExports.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (completedExports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioElevated)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No exported files yet. Rendered MP4 files will appear here.", fontSize = 12.sp, color = StudioTextSecondary)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (exp in completedExports) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioElevated),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = StudioSecondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(exp.fileName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
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
