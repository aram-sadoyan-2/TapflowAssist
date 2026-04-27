package com.algorithm.tapflow.assist.navigation

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.algorithm.tapflow.assist.data.model.GestureType
import com.algorithm.tapflow.assist.service.AccessibilityHelper
import com.algorithm.tapflow.assist.service.FloatingOverlayStarter
import com.algorithm.tapflow.assist.service.ServiceLocator
import com.algorithm.tapflow.assist.ui.components.AccessibilityDisclosureDialog
import com.algorithm.tapflow.assist.ui.screens.editor.EditorScreen
import com.algorithm.tapflow.assist.ui.screens.home.HomeScreen
import com.algorithm.tapflow.assist.ui.screens.permissions.PermissionsScreen
import com.algorithm.tapflow.assist.ui.screens.presets.PresetsScreen
import com.algorithm.tapflow.assist.ui.screens.settings.SettingsScreen
import com.algorithm.tapflow.assist.ui.viewmodel.EditorViewModel
import com.algorithm.tapflow.assist.ui.viewmodel.HomeViewModel
import com.algorithm.tapflow.assist.ui.viewmodel.PermissionsViewModel
import com.algorithm.tapflow.assist.ui.viewmodel.PresetsViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = ServiceLocator.providePresetRepository(context)

    var showAccessibilityDisclosure by rememberSaveable {
        mutableStateOf(false)
    }

    var pendingPresetId by rememberSaveable {
        mutableLongStateOf(-1L)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(repository)
            )

            HomeScreen(
                viewModel = vm,
                onCreateNew = {
                    navController.navigate(Routes.Editor.createRoute())
                },
                onOpenPermissions = {
                    navController.navigate(Routes.Permissions.route)
                },
                onOpenPresets = {
                    navController.navigate(Routes.Presets.route)
                },
                onOpenSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onRecentPresetClick = { presetId ->
                    navController.navigate(Routes.Editor.createRoute(presetId))
                }
            )
        }

        composable(
            route = Routes.Editor.route,
            arguments = listOf(
                navArgument("presetId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val vm: EditorViewModel = viewModel(
                factory = EditorViewModel.factory(repository)
            )

            val presetId = backStackEntry.arguments
                ?.getLong("presetId")
                ?.takeIf { it > 0L }

            EditorScreen(
                viewModel = vm,
                presetId = presetId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Permissions.route) {
            val vm: PermissionsViewModel = viewModel()

            PermissionsScreen(
                viewModel = vm,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Presets.route) {
            val vm: PresetsViewModel = viewModel(
                factory = PresetsViewModel.factory(repository)
            )

            val uiState by vm.uiState.collectAsState()

            PresetsScreen(
                viewModel = vm,
                onBack = {
                    navController.popBackStack()
                },
                onPresetClick = { presetId ->
                    navController.navigate(Routes.Editor.createRoute(presetId))
                },
                onStartPreset = { presetId ->
                    val preset = uiState.presets.firstOrNull { it.id == presetId }
                        ?: return@PresetsScreen

                    if (!AccessibilityHelper.isTouchServiceEnabled(context)) {
                        pendingPresetId = presetId
                        showAccessibilityDisclosure = true
                        return@PresetsScreen
                    }

                    val isValid = when (preset.type) {
                        GestureType.SINGLE_TAP -> preset.points.size == 1
                        GestureType.LONG_PRESS -> preset.points.size == 1
                        GestureType.SWIPE -> preset.points.size == 2
                        GestureType.MULTI_TAP -> preset.points.isNotEmpty()
                    }

                    if (!isValid) return@PresetsScreen

                    FloatingOverlayStarter.start(context, preset)

                    Handler(Looper.getMainLooper()).postDelayed({
                        (context as? Activity)?.moveTaskToBack(true)
                    }, 200)
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            onAgree = {
                showAccessibilityDisclosure = false
                pendingPresetId = -1L

                AccessibilityHelper.openAccessibilitySettings(context)
            },
            onDecline = {
                showAccessibilityDisclosure = false
                pendingPresetId = -1L
            }
        )
    }
}