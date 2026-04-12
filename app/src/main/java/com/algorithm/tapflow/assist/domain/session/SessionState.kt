package com.algorithm.tapflow.assist.domain.session

sealed class SessionState {
    data object Idle : SessionState()
    data object Running : SessionState()
    data object Paused : SessionState()
    data class Error(val message: String) : SessionState()
}