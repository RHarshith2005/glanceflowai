package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.GlanceMainApp
import com.example.ui.theme.GlanceFlowTheme
import com.example.ui.viewmodel.GlanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlanceFlowTheme {
                val viewModel: GlanceViewModel = viewModel()
                GlanceMainApp(viewModel = viewModel)
            }
        }
    }
}
