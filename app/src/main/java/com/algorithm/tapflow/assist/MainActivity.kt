package com.algorithm.tapflow.assist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.algorithm.tapflow.assist.navigation.AppNavGraph
import com.algorithm.tapflow.assist.onboarding.OnboardingPrefs
import com.algorithm.tapflow.assist.onboarding.OnboardingScreen
import com.algorithm.tapflow.assist.ui.theme.TapFlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TapFlowTheme {
                val onboardingPrefs = remember {
                    OnboardingPrefs(this)
                }

//                val showOnboarding = remember {
//                    mutableStateOf(!onboardingPrefs.isOnboardingSeen())
//                }

                val showOnboarding = remember { // for testing
                    mutableStateOf(true)
                }

                if (showOnboarding.value) {
                    OnboardingScreen(
                        onFinish = {
                            onboardingPrefs.setOnboardingSeen()
                            showOnboarding.value = false
                        }
                    )
                } else {
                    AppNavGraph()
                }
            }
        }
    }
}