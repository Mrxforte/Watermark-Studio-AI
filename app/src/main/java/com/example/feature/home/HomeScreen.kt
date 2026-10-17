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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.StudioViewModel
import com.example.feature.components.DataInsightsWaveform
import com.example.feature.components.GlassCard
import com.example.feature.components.GlassListRow
import com.example.feature.components.NavyPillButton
import com.example.feature.components.NeonActionButton
import com.example.feature.components.SegmentedPillSwitcher
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun HomeScreen(
    viewModel: StudioViewModel,
    onNavigateToRemove: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentVideo by viewModel.currentVideo.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val completedExports by viewModel.completedExports.collectAsState()

    var activePillTab by remember { mutableStateOf(0) }

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
            .padding(bottom = 95.dp)
    ) {
        // 1:1 Top Bar: < Back Arrow, Centered App Title, Right Settings Gear
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
                    .clickable { /* back / refresh */ },
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
                text = "Watermark Studio",
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
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { onNavigateToSettings() },
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

        Spacer(modifier = Modifier.height(6.dp))

        // Hero Profile / Engine Card
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                borderGlow = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circle with luminous cyan ring
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(8.dp, CircleShape, ambientColor = Color(0x330284C7), spotColor = Color(0x260284C7))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                )
                            )
                            .border(2.dp, StudioPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = StudioPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Watermark Studio AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = StudioTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stats indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(StudioPrimary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${savedProjects.size.coerceAtLeast(1)} Projects",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF38BDF8), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${completedExports.size.coerceAtLeast(1)} Exports",
                                fontSize = 11.sp,
                                color = StudioTextSecondary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(StudioSuccess, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ultra 4K",
                                fontSize = 11.sp,
                                color = StudioSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dual Pill Sub-Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NavyPillButton(
                            text = "Import Video",
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("import_video_btn"),
                            leadingIcon = {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(16.dp))
                            }
                        )

                        NavyPillButton(
                            text = "Try Demo",
                            onClick = {
                                viewModel.loadSampleVideo()
                                onNavigateToRemove()
                            },
                            modifier = Modifier.weight(1f).testTag("sample_video_btn")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Recent Activity" 4-Icon Action Row (1:1 with reference image layout)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Recent Activity",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StudioTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1: Remove Watermark
                SquareActionButton(
                    icon = Icons.Default.AutoAwesome,
                    label = "Remove",
                    color = Color(0xFF00E5FF),
                    onClick = {
                        if (currentVideo == null) viewModel.loadSampleVideo()
                        onNavigateToRemove()
                    }
                )

                // 2: Create Watermark
                SquareActionButton(
                    icon = Icons.Default.TextFields,
                    label = "Create",
                    color = Color(0xFF38BDF8),
                    onClick = {
                        if (currentVideo == null) viewModel.loadSampleVideo()
                        onNavigateToCreate()
                    }
                )

                // 3: Gemini AI Vision
                SquareActionButton(
                    icon = Icons.Default.Psychology,
                    label = "AI Advisor",
                    color = Color(0xFF818CF8),
                    onClick = { onNavigateToAssistant() }
                )

                // 4: Video Export
                SquareActionButton(
                    icon = Icons.Default.Download,
                    label = "Export",
                    color = Color(0xFF00E676),
                    onClick = { onNavigateToRemove() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // "Data Insights" Waveform Card (1:1 matching reference image graph)
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            DataInsightsWaveform(
                title = "Data Insights",
                subtitle = "Reconstruction Stability & Isophote Continuity",
                psnrText = if (currentVideo != null) "99.4%" else "Ready"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Segmented Pill Switcher (from reference image)
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SegmentedPillSwitcher(
                options = listOf("Overview", "Inpaint", "Temporal", "AI Vision"),
                selectedIndex = activePillTab,
                onSelectIndex = { activePillTab = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List Menu Rows (1:1 with rounded row items in reference image)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassListRow(
                title = "Video Processing Mode",
                subtitle = "Telea FMM & Navier-Stokes Fluid Inpainting",
                icon = Icons.Default.Tune,
                trailingText = "Active",
                onClick = { onNavigateToRemove() }
            )

            GlassListRow(
                title = "Gemini 4 Argon Intelligence",
                subtitle = "Deep Thinking Multimodal Frame Understanding",
                icon = Icons.Default.Psychology,
                trailingText = "Online",
                onClick = { onNavigateToAssistant() }
            )

            GlassListRow(
                title = "Hardware Encoder",
                subtitle = "Android MediaCodec H.264 AVC (GPU Accelerated)",
                icon = Icons.Default.Memory,
                trailingText = "Ready",
                onClick = { onNavigateToRemove() }
            )

            GlassListRow(
                title = "Privacy & Local Storage",
                subtitle = "Zero-cloud transfer • 100% on-device reconstruction",
                icon = Icons.Default.Security,
                trailingText = "Locked",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Floating Bottom Primary Action Pill Button
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            NavyPillButton(
                text = if (currentVideo != null) "Open Watermark Studio" else "Start Video Inpainting",
                onClick = {
                    if (currentVideo == null) viewModel.loadSampleVideo()
                    onNavigateToRemove()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

/**
 * 1:1 Rounded Square Icon Action Button from reference mockup
 */
@Composable
private fun SquareActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = color.copy(alpha = 0.2f), spotColor = Color(0x0D000000))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(color.copy(alpha = 0.45f), Color(0xFFE2E8F0))),
                    shape = RoundedCornerShape(18.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = StudioTextPrimary
        )
    }
}
