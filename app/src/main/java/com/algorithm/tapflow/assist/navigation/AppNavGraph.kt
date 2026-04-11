package com.algorithm.tapflow.assist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algorithm.tapflow.assist.ui.screens.editor.EditorScreen
import com.algorithm.tapflow.assist.ui.screens.home.HomeScreen
import com.algorithm.tapflow.assist.ui.screens.permissions.PermissionsScreen
import com.algorithm.tapflow.assist.ui.screens.presets.PresetsScreen
import com.algorithm.tapflow.assist.ui.screens.settings.SettingsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(
                onCreateNew = { navController.navigate(Routes.Editor.route) },
                onOpenPermissions = { navController.navigate(Routes.Permissions.route) },
                onOpenPresets = { navController.navigate(Routes.Presets.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(Routes.Editor.route) {
            EditorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Permissions.route) {
            PermissionsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Presets.route) {
            PresetsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}