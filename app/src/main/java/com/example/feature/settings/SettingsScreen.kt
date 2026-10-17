package com.example.feature.settings

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.components.GlassCard
import com.example.feature.components.GlassPillButton
import com.example.feature.components.LuminousStatusPill
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSuccess
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember { mutableStateOf("English") }
    var autoDetectEnabled by remember { mutableStateOf(true) }
    var hardwareAccelEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 85.dp)
    ) {
        Text(
            text = "Studio Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = StudioTextPrimary
        )
        Text(
            text = "Configure engine performance, language, and privacy",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selection Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Interface Language", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioTextPrimary)
                }
                Spacer(modifier = Modifier.height(14.dp))

                val languages = listOf("English", "Русский", "O'zbek")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (lang in languages) {
                        val isSel = selectedLanguage == lang
                        GlassPillButton(
                            text = lang,
                            onClick = { selectedLanguage = lang },
                            isSelected = isSel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Engine Preferences Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = StudioPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Processing & AI Defaults", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioTextPrimary)
                }
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Watermark Suggestion", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                        Text("Run OpenCV contrast & corner heuristics automatically on import", fontSize = 10.sp, color = StudioTextSecondary)
                    }
                    Switch(
                        checked = autoDetectEnabled,
                        onCheckedChange = { autoDetectEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hardware Acceleration (MediaCodec)", style = MaterialTheme.typography.bodySmall, color = StudioTextPrimary)
                        Text("Utilize GPU / DSP hardware encoders for real-time MP4 rendering", fontSize = 10.sp, color = StudioTextSecondary)
                    }
                    Switch(
                        checked = hardwareAccelEnabled,
                        onCheckedChange = { hardwareAccelEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = StudioPrimary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy & Local-First Processing
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = StudioSuccess, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Zero-Cloud Video Privacy", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioTextPrimary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "All video frames, inpainting sweeps (Telea & Navier-Stokes), and MP4 encoding execute 100% locally on this device. Your source video files never leave your phone.",
                    fontSize = 11.sp,
                    color = StudioTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // About & Version
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("About Watermark Studio AI", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StudioTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Version 2.0.0 (Futuristic Glassmorphic Theme)", fontSize = 11.sp, color = StudioTextSecondary)
                Text("Computer Vision: Alexandru Telea FMM, Bertalmio Navier-Stokes PDE", fontSize = 10.sp, color = StudioTextSecondary)
                Text("Video Engine: Android MediaCodec AVC H.264 & AndroidX Media3", fontSize = 10.sp, color = StudioTextSecondary)
                Text("AI Intelligence: Gemini 4-Argon (High Thinking) & 3.8-Flash", fontSize = 10.sp, color = StudioTextSecondary)
            }
        }
    }
}
