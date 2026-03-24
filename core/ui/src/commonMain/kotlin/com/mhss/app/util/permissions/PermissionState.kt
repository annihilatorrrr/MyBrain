package com.mhss.app.util.permissions

import androidx.compose.runtime.Composable

interface PermissionState {
    var isGranted: Boolean
    var shouldShowRationale: Boolean
    fun launchRequest()
    fun refresh()
    fun openAppSettings()
}

enum class Permission {
    READ_CALENDAR,
    WRITE_CALENDAR,
    SCHEDULE_ALARMS,
    NOTIFICATIONS
}

@Composable
expect fun rememberPermissionState(
    permission: Permission,
): PermissionState
