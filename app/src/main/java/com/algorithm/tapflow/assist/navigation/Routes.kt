package com.algorithm.tapflow.assist.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Permissions : Routes("permissions")
    data object Presets : Routes("presets")
    data object Settings : Routes("settings")

    data object Editor : Routes("editor?presetId={presetId}") {
        fun createRoute(presetId: Long? = null): String {
            return if (presetId != null) {
                "editor?presetId=$presetId"
            } else {
                "editor"
            }
        }
    }
}