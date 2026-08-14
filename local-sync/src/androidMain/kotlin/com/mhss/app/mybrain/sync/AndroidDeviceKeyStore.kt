package com.mhss.app.mybrain.sync

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.preferences.domain.use_case.SavePreferenceUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import java.security.SecureRandom
import java.util.UUID

@Single
class AndroidDeviceKeyStore(
    private val context: Context,
    private val getPreference: GetPreferenceUseCase,
    private val savePreference: SavePreferenceUseCase,
    private val pairedDevicesRepository: PairedDevicesRepository
) : DeviceKeyStore {

    private val deviceIdKey = stringPreferencesKey("sync_device_id")
    private val deviceCustomNameKey = stringPreferencesKey("sync_custom_device_name")
    private val deviceIdMutex = Mutex()

    override suspend fun getCurrentDeviceId(): String {
        val current = getPreference(deviceIdKey, "").first()
        if (current.isNotBlank()) return current
        return deviceIdMutex.withLock {
            val existing = getPreference(deviceIdKey, "").first()
            if (existing.isNotBlank()) {
                existing
            } else {
                UUID.randomUUID().toString().also {
                    savePreference(deviceIdKey, it)
                }
            }
        }
    }

    override suspend fun getCurrentDeviceName(): String {
        val customName = getPreference(deviceCustomNameKey, "").first()
        if (customName.isNotBlank()) return customName
        return try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
                ?: Settings.Secure.getString(context.contentResolver, "bluetooth_name")
                ?: Build.MODEL
                ?: "Android Device"
        } catch (_: Exception) {
            Build.MODEL ?: "Android Device"
        }
    }

    override suspend fun updateCurrentDeviceName(name: String) {
        savePreference(deviceCustomNameKey, name)
    }

    override fun generateEncryptionKey(): String {
        val bytes = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override suspend fun getDeviceKey(deviceId: String): String? {
        return pairedDevicesRepository.getPairedDevice(deviceId)?.encryptionKey
    }

    override suspend fun getCurrentDeviceVersion(): Int {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                (packageInfo.longVersionCode and 0xFFFFFFFFL).toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }
}
