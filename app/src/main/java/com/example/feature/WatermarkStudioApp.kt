package com.example.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.feature.assistant.AssistantScreen
import com.example.feature.create.CreateWatermarkScreen
import com.example.feature.exports.ExportQueueScreen
import com.example.feature.home.HomeScreen
import com.example.feature.projects.ProjectsScreen
import com.example.feature.remove.RemoveWatermarkScreen
import com.example.feature.settings.SettingsScreen
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.NeonGlowBrush
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Studio", Icons.Default.Home, "nav_home"),
    REMOVE("Remove", Icons.Default.AutoAwesome, "nav_remove"),
    CREATE("Create", Icons.Default.TextFields, "nav_create"),
    ASSISTANT("Advisor", Icons.Default.Psychology, "nav_assistant"),
    EXPORTS("Exports", Icons.Default.Download, "nav_exports"),
    PROJECTS("Projects", Icons.Default.FolderOpen, "nav_projects"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun WatermarkStudioApp(
    viewModel: StudioViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    BackHandler(enabled = currentScreen != Screen.HOME) {
        currentScreen = Screen.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = StudioBackground,
        bottomBar = {
            // Floating Frosted Glass Dock (matching the reference image's curved bottom bar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 14.dp,
                            shape = CircleShape,
                            ambientColor = Color(0x1F0F172A),
                            spotColor = Color(0x140284C7)
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = StudioBorder,
                            shape = CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("floating_glass_nav_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (screen in Screen.values()) {
                            val isSelected = currentScreen == screen
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .then(
                                        if (isSelected) Modifier.background(
                                            Color(0xFFE0F2FE),
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(1.dp, Color(0x400284C7), CircleShape)
                                        else Modifier
                                    )
                                    .clickable { currentScreen = screen }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag(screen.tag),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) StudioPrimary else Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = screen.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToRemove = { currentScreen = Screen.REMOVE },
                    onNavigateToCreate = { currentScreen = Screen.CREATE },
                    onNavigateToAssistant = { currentScreen = Screen.ASSISTANT },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
                Screen.REMOVE -> RemoveWatermarkScreen(
                    viewModel = viewModel,
                    onNavigateToExport = { currentScreen = Screen.EXPORTS },
                    onNavigateToAssistant = { currentScreen = Screen.ASSISTANT },
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
                Screen.CREATE -> CreateWatermarkScreen(
                    viewModel = viewModel,
                    onNavigateToExport = { currentScreen = Screen.EXPORTS },
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
                Screen.ASSISTANT -> AssistantScreen(
                    viewModel = viewModel
                )
                Screen.EXPORTS -> ExportQueueScreen(
                    viewModel = viewModel
                )
                Screen.PROJECTS -> ProjectsScreen(
                    viewModel = viewModel,
                    onOpenProject = { currentScreen = Screen.REMOVE }
                )
                Screen.SETTINGS -> SettingsScreen()
            }
        }
    }
}
