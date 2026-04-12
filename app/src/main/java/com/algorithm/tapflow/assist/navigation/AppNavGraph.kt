package com.algorithm.tapflow.assist.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.algorithm.tapflow.assist.service.ServiceLocator
import com.algorithm.tapflow.assist.ui.screens.editor.EditorScreen
import com.algorithm.tapflow.assist.ui.screens.home.HomeScreen
import com.algorithm.tapflow.assist.ui.screens.permissions.PermissionsScreen
import com.algorithm.tapflow.assist.ui.screens.presets.PresetsScreen
import com.algorithm.tapflow.assist.ui.screens.settings.SettingsScreen
import com.algorithm.tapflow.assist.ui.viewmodel.EditorViewModel
import com.algorithm.tapflow.assist.ui.viewmodel.HomeViewModel
import com.algorithm.tapflow.assist.ui.viewmodel.PermissionsViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = ServiceLocator.providePresetRepository(context)

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
                onCreateNew = { navController.navigate(Routes.Editor.route) },
                onOpenPermissions = { navController.navigate(Routes.Permissions.route) },
                onOpenPresets = { navController.navigate(Routes.Presets.route) },
                onOpenSettings = { navController.navigate(Routes.Settings.route) }
            )
        }

        composable(Routes.Editor.route) {
            val vm: EditorViewModel = viewModel(
                factory = EditorViewModel.factory(repository)
            )

            EditorScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Permissions.route) {
            val vm: PermissionsViewModel = viewModel()

            PermissionsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Presets.route) {
            PresetsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}