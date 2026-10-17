package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.feature.StudioViewModel
import com.example.feature.WatermarkStudioApp
import com.example.ui.theme.WatermarkStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WatermarkStudioTheme {
                val studioViewModel: StudioViewModel = viewModel()
                WatermarkStudioApp(viewModel = studioViewModel)
            }
        }
    }
}
