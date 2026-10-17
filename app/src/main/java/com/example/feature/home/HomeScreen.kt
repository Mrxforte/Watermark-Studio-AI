package com.example.feature.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.StudioViewModel
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    onNavigateToRemove: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val completedExports by viewModel.completedExports.collectAsState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importVideo(uri)
            onNavigateToRemove()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // App Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(listOf(StudioPrimary, StudioSecondary)),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Watermark Studio AI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextPrimary
                        )
                        Text(
                            text = "Professional Video Reconstruction & Creation",
                            fontSize = 11.sp,
                            color = StudioSecondary
                        )
                    }
                }
            }
        }

        // Hero Actions Row: Remove vs Create
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Remove Watermark
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPrimary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (currentVideo == null) {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        } else {
                            onNavigateToRemove()
                        }
                    }
                    .testTag("action_remove_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(StudioPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Remove",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Inpaint, Telea, Navier-Stokes, & Temporal",
                        fontSize = 11.sp,
                        color = StudioTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Card 2: Create Watermark
            Card(
                colors = CardDefaults.cardColors(containerColor = StudioElevated),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioSecondary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (currentVideo == null) {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        } else {
                            onNavigateToCreate()
                        }
                    }
                    .testTag("action_create_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(StudioSecondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Create",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = StudioTextPrimary
                    )
                    Text(
                        text = "Logos, Typography, Grids, & Animations",
                        fontSize = 11.sp,
                        color = StudioTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Video Import Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Source Video Input",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (currentVideo != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentVideo!!.displayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${currentVideo!!.resolutionFormatted} • ${currentVideo!!.durationFormatted}",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                        Button(
                            onClick = { onNavigateToRemove() },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open Editor", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("import_video_btn")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Video", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.loadSampleVideo()
                                onNavigateToRemove()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = StudioElevated),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sample_video_btn")
                        ) {
                            Text("Try Sample", fontSize = 12.sp, color = StudioSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Capabilities Card
        Card(
            colors = CardDefaults.cardColors(containerColor = StudioCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = StudioSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hardware & Engine Pipeline",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val statusItems = listOf(
                    "OpenCV Inpainting (Telea FMM & Navier-Stokes PDE)" to "Active (Native)",
                    "Temporal Reconstruction (Multi-frame median fusion)" to "Active (Native)",
                    "Optical Flow Motion Tracker (Lucas-Kanade)" to "Active (Native)",
                    "Video Encoder (Android MediaCodec H.264 AVC)" to "Hardware Ready",
                    "AI Intelligence (Gemini 4-Argon & 3.8-Flash)" to "Integrated"
                )

                for ((label, status) in statusItems) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 11.sp, color = StudioTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioSuccess, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(status, fontSize = 11.sp, color = StudioSuccess, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Projects Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Projects", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${savedProjects.size} saved", fontSize = 11.sp, color = StudioTextSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (savedProjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioElevated)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved projects yet. Import a video to begin!",
                    fontSize = 12.sp,
                    color = StudioTextSecondary
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedProjects) { project ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .clickable {
                                viewModel.loadProject(project)
                                onNavigateToRemove()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(project.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${project.width}x${project.height} • ${project.durationFormatted}", fontSize = 11.sp, color = StudioTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(project.removalMethod.title, fontSize = 10.sp, color = StudioPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Exports List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Exports", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${completedExports.size} videos", fontSize = 11.sp, color = StudioTextSecondary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (completedExports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioElevated)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No exports yet. Rendered videos will appear here.",
                    fontSize = 12.sp,
                    color = StudioTextSecondary
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (exp in completedExports.take(3)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudioElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = StudioSecondary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(exp.fileName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("${exp.resolution} • %.1f MB".format(exp.fileSizeBytes / (1024f * 1024f)), fontSize = 10.sp, color = StudioTextSecondary)
                                }
                            }
                            Text("Ready", fontSize = 11.sp, color = StudioSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
