package com.algorithm.tapflow.assist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.algorithm.tapflow.assist.navigation.AppNavGraph
import com.algorithm.tapflow.assist.ui.theme.TapFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TapFlowTheme {
                AppNavGraph()
            }
        }
    }
}