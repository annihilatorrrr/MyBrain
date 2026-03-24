package com.mhss.app.mybrain.presentation.app_lock

import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data object NoneEnrolled : AuthResult
    data object HardwareUnavailable : AuthResult
    data object NoHardware : AuthResult
    data class Error(val message: String) : AuthResult
    data object Success : AuthResult
    data object Failed : AuthResult
}

expect class AppLockManager {
    val resultFlow: Flow<AuthResult>
    fun showAuthPrompt()
    fun canUseFeature(): Boolean
}
