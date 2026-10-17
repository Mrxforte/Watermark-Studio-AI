package com.example.feature.remove

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.StudioWarning

@Composable
fun RemoveWatermarkScreen(
    viewModel: StudioViewModel,
    onNavigateToExport: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateBack: () -> Unit = {},
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

    val methodList = listOf(
        RemovalMethod.TELEA,
        RemovalMethod.NAVIER_STOKES,
        RemovalMethod.TEMPORAL,
        RemovalMethod.GAUSSIAN_BLUR
    )
    val methodTitles = methodList.map { it.title.substringBefore(" ") }
    val currentMethodIndex = methodList.indexOf(selectedMethod).coerceAtLeast(0)

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
                            .background(Color(0xFFE0F2FE))
                            .border(1.5.dp, StudioPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "No Video",
                            tint = StudioPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Video Loaded",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please load a video from the Studio Home or tap below to test with our built-in sample footage.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StudioTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NavyPillButton(
                        text = "Load Sample Video",
                        onClick = { viewModel.loadSampleVideo() },
                        modifier = Modifier.testTag("load_sample_btn")
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
        // Top Bar: < Back, Centered Title, Right Action Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                text = "Remove Watermark",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StudioTextPrimary,
                letterSpacing = 0.5.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, if (isSplitCompare) StudioPrimary else Color(0xFFE2E8F0), CircleShape)
                        .clickable { viewModel.toggleSplitCompare() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = "Split",
                        tint = if (isSplitCompare) StudioPrimary else StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, if (showTuningSheet) StudioPrimary else Color(0xFFE2E8F0), CircleShape)
                        .clickable { showTuningSheet = !showTuningSheet },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Tune",
                        tint = if (showTuningSheet) StudioPrimary else StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xF0FFFFFF))
                    .border(1.dp, Color(0x400284C7), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isSplitCompare) "Drag center divider to compare" else "Drag mask box to align with logo",
                    fontSize = 11.sp,
                    color = StudioPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Pill Switcher (Telea | Navier | Temporal | Blur)
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SegmentedPillSwitcher(
                options = methodTitles,
                selectedIndex = currentMethodIndex,
                onSelectIndex = { idx ->
                    if (idx in methodList.indices) {
                        viewModel.selectMethod(methodList[idx])
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Timeline Scrubber
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Timecode: ${String.format("%02d:%02d", (currentTimeMs / 1000) / 60, (currentTimeMs / 1000) % 60)}",
                    fontSize = 12.sp,
                    color = StudioPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentVideo?.durationFormatted ?: "00:00",
                    fontSize = 12.sp,
                    color = StudioTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thumbnail strip
            if (thumbnails.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9))
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
                    inactiveTrackColor = Color(0xFFE2E8F0)
                ),
                modifier = Modifier.testTag("timeline_slider")
            )
        }

        // Data Insights Waveform Card (PSNR & Quality Metrics)
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            val psnrVal = qualityReport?.let { "%.1f dB".format(it.psnrDb) } ?: "99.4%"
            DataInsightsWaveform(
                title = "Quality & Isophote Telemetry",
                subtitle = "Reconstruction accuracy: ${selectedMethod.title}",
                psnrText = psnrVal
            )
        }

        // Mask Tuning Panel (if expanded)
        if (showTuningSheet) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Mask Boundary Calibration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioTextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Boundary Dilation: ${mask.dilationRadius} px", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = mask.dilationRadius.toFloat(),
                            onValueChange = { viewModel.updateMaskSettings(it.toInt(), mask.featherRadius, mask.inpaintRadius) },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary, inactiveTrackColor = Color(0xFFE2E8F0))
                        )

                        Text("Edge Feathering: ${mask.featherRadius} px", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = mask.featherRadius.toFloat(),
                            onValueChange = { viewModel.updateMaskSettings(mask.dilationRadius, it.toInt(), mask.inpaintRadius) },
                            valueRange = 0f..8f,
                            steps = 7,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8), inactiveTrackColor = Color(0xFFE2E8F0))
                        )

                        Text("Inpaint Radius: ${mask.inpaintRadius} px", fontSize = 11.sp, color = StudioTextSecondary)
                        Slider(
                            value = mask.inpaintRadius.toFloat(),
                            onValueChange = { viewModel.updateMaskSettings(mask.dilationRadius, mask.featherRadius, it.toInt()) },
                            valueRange = 2f..9f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = StudioPrimary, activeTrackColor = StudioPrimary, inactiveTrackColor = Color(0xFFE2E8F0))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Actions Row matching reference pill buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NavyPillButton(
                text = "Gemini 4 AI",
                onClick = { onNavigateToAssistant() },
                modifier = Modifier.weight(1f).testTag("ai_advisor_btn"),
                leadingIcon = {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(16.dp))
                }
            )

            NavyPillButton(
                text = "Save Project",
                onClick = { viewModel.saveCurrentProject(currentVideo?.displayName ?: "Project") },
                modifier = Modifier.weight(1f).testTag("save_project_btn"),
                leadingIcon = {
                    Icon(Icons.Default.Save, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(16.dp))
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Floating Bottom Primary Action Pill Button
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NavyPillButton(
                text = "Render & Export Final MP4",
                onClick = { onNavigateToExport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("proceed_export_btn"),
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}
