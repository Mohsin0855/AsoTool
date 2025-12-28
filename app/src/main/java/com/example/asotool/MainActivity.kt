package com.example.asotool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.asotool.presentation.screen.ASOAnalyzerScreen
import com.example.asotool.ui.theme.AsoToolTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AsoToolTheme {
                ASOAnalyzerScreen()
            }
        }
    }
}