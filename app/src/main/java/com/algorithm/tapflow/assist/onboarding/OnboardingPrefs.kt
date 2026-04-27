package com.algorithm.tapflow.assist.onboarding

import android.content.Context

class OnboardingPrefs(context: Context) {

    private val prefs = context.getSharedPreferences(
        "tapflow_onboarding_prefs",
        Context.MODE_PRIVATE
    )

    fun isOnboardingSeen(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
    }

    fun setOnboardingSeen() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .apply()
    }

    companion object {
        private const val KEY_ONBOARDING_SEEN = "key_onboarding_seen"
    }
}