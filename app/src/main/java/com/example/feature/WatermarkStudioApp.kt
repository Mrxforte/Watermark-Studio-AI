package com.example.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.assistant.AssistantScreen
import com.example.feature.create.CreateWatermarkScreen
import com.example.feature.exports.ExportQueueScreen
import com.example.feature.home.HomeScreen
import com.example.feature.projects.ProjectsScreen
import com.example.feature.remove.RemoveWatermarkScreen
import com.example.feature.settings.SettingsScreen
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPrimary
import com.example.ui.theme.StudioPrimaryContainer
import com.example.ui.theme.StudioSecondary
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

enum class Screen(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Studio", Icons.Default.Home, "nav_home"),
    REMOVE("Remove", Icons.Default.AutoAwesome, "nav_remove"),
    CREATE("Create", Icons.Default.TextFields, "nav_create"),
    ASSISTANT("AI Advisor", Icons.Default.Psychology, "nav_assistant"),
    EXPORTS("Exports", Icons.Default.Download, "nav_exports"),
    PROJECTS("Projects", Icons.Default.FolderOpen, "nav_projects"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun WatermarkStudioApp(
    viewModel: StudioViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioBackground)
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = StudioBackground,
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar")
            ) {
                for (screen in Screen.values()) {
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudioPrimary,
                            selectedTextColor = StudioPrimary,
                            indicatorColor = StudioPrimaryContainer,
                            unselectedIconColor = StudioTextSecondary,
                            unselectedTextColor = StudioTextSecondary
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
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
                    onNavigateToAssistant = { currentScreen = Screen.ASSISTANT }
                )
                Screen.REMOVE -> RemoveWatermarkScreen(
                    viewModel = viewModel,
                    onNavigateToExport = { currentScreen = Screen.EXPORTS },
                    onNavigateToAssistant = { currentScreen = Screen.ASSISTANT }
                )
                Screen.CREATE -> CreateWatermarkScreen(
                    viewModel = viewModel,
                    onNavigateToExport = { currentScreen = Screen.EXPORTS }
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
