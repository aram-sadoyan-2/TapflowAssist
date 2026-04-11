package com.algorithm.tapflow.assist.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Editor : Routes("editor")
    data object Permissions : Routes("permissions")
    data object Presets : Routes("presets")
    data object Settings : Routes("settings")
}