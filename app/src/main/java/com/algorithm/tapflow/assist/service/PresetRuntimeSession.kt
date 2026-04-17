package com.algorithm.tapflow.assist.service

import com.algorithm.tapflow.assist.data.model.TouchPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PresetRuntimeSession {

    private val _activePreset = MutableStateFlow<TouchPreset?>(null)
    val activePreset: StateFlow<TouchPreset?> = _activePreset.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun setPreset(preset: TouchPreset) {
        _activePreset.value = preset
    }

    fun start() {
        _isRunning.value = true
    }

    fun pause() {
        _isRunning.value = false
    }

    fun stop() {
        _isRunning.value = false
        _activePreset.value = null
    }
}