package com.algorithm.tapflow.assist

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.algorithm.tapflow.assist.navigation.AppNavGraph
import com.algorithm.tapflow.assist.ui.theme.AppBackground
import com.algorithm.tapflow.assist.ui.theme.TapFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TapFlowTheme {
                val view = LocalView.current

                SideEffect {
                    val window = (view.context as Activity).window
                    window.navigationBarColor = AppBackground.toArgb()
                    window.statusBarColor = AppBackground.toArgb()

                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightNavigationBars = false
                        isAppearanceLightStatusBars = false
                    }
                }

                AppNavGraph()
            }
        }
    }
}