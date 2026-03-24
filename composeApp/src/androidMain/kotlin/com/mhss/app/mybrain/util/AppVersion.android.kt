package com.mhss.app.mybrain.util

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getAppVersion(): String {
    val context = LocalContext.current
    val pm = context.packageManager
    val packageName = context.packageName
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0)).versionName.orEmpty()
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(packageName, 0).versionName.orEmpty()
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual fun supportsMaterialYouTheme(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
