package com.algorithm.tapflow.assist.domain.session

import com.algorithm.tapflow.assist.data.model.TouchPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionController {

    private val _state = MutableStateFlow<SessionState>(SessionState.Idle)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private var currentPreset: TouchPreset? = null

    fun start(preset: TouchPreset) {
        currentPreset = preset
        _state.value = SessionState.Running
    }

    fun pause() {
        if (_state.value is SessionState.Running) {
            _state.value = SessionState.Paused
        }
    }

    fun resume() {
        if (_state.value is SessionState.Paused) {
            _state.value = SessionState.Running
        }
    }

    fun stop() {
        currentPreset = null
        _state.value = SessionState.Idle
    }

    fun currentPreset(): TouchPreset? = currentPreset
}