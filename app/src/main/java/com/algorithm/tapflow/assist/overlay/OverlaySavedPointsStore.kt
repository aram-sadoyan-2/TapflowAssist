package com.algorithm.tapflow.assist.overlay

import android.content.Context
import androidx.core.content.edit

object OverlaySavedPointsStore {

    fun clear(context: Context) {
        context.getSharedPreferences("overlay_setup_prefs", Context.MODE_PRIVATE)
            .edit {
                remove(OverlaySetupService.KEY_POINTS)
                    .remove(OverlaySetupService.KEY_LAST_SAVED_AT)
            }
    }
}