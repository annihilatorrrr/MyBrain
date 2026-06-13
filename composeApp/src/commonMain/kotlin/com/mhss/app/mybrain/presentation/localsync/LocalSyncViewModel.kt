package com.mhss.app.mybrain.presentation.localsync

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.sync.SyncOrchestrator
import com.mhss.app.mybrain.sync.domain.DeletePairedDeviceUseCase
import com.mhss.app.mybrain.sync.domain.GetOwnQrContentUseCase
import com.mhss.app.mybrain.sync.domain.GetPairedDevicesFlowUseCase
import com.mhss.app.mybrain.sync.domain.NetworkHelper
import com.mhss.app.mybrain.sync.domain.PairDeviceFromQrDataUseCase
import com.mhss.app.mybrain.sync.domain.PairDeviceUseCase
import com.mhss.app.mybrain.sync.domain.PairResult
import com.mhss.app.mybrain.sync.domain.UpdateDeviceNameUseCase
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.mybrain.sync.repository.DeviceKeyStore
import com.mhss.app.mybrain.sync.repository.PairedDevicesRepository
import com.mhss.app.mybrain.sync.util.SYNC_DEEP_LINK_BASE_URI
import com.mhss.app.ui.Res
import com.mhss.app.ui.clipboard_empty_or_invalid
import com.mhss.app.ui.encryption_key_reset_success
import com.mhss.app.ui.error_with_message
import com.mhss.app.ui.invalid_pairing_format
import com.mhss.app.ui.invalid_pairing_link_format
import com.mhss.app.ui.pairing_offline_success
import com.mhss.app.ui.pairing_success
import com.mhss.app.ui.qr_decode_failed
import com.mhss.app.ui.snackbar.showErrorSnackbar
import com.mhss.app.ui.snackbar.showSuccessSnackbar
import com.mhss.app.ui.sync_failed
import com.mhss.app.ui.sync_failed_with_error
import com.mhss.app.ui.sync_success_with_device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LocalSyncViewModel(
    private val deviceKeyStore: DeviceKeyStore,
    private val orchestrator: SyncOrchestrator,
    private val getPairedDevicesFlow: GetPairedDevicesFlowUseCase,
    private val deletePairedDeviceUseCase: DeletePairedDeviceUseCase,
    private val networkHelper: NetworkHelper,
    private val pairedDevicesRepository: PairedDevicesRepository,
    private val pairDeviceUseCase: PairDeviceUseCase,
    private val getOwnQrContentUseCase: GetOwnQrContentUseCase,
    private val updateDeviceNameUseCase: UpdateDeviceNameUseCase,
    private val pairDeviceFromQrData: PairDeviceFromQrDataUseCase,
    private val qrCodeUtils: QrCodeUtils
) : ViewModel() {

    private val _uiState = MutableStateFlow(PairedDevicesUiState())
    val uiState: StateFlow<PairedDevicesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val deviceId = deviceKeyStore.getCurrentDeviceId()
            val encKey = deviceKeyStore.getCurrentDeviceEncKey()
            getPairedDevicesFlow().collect { devices ->
                val deviceName = deviceKeyStore.getCurrentDeviceName()
                _uiState.update {
                    it.copy(
                        ownDeviceId = deviceId,
                        ownDeviceName = deviceName,
                        ownDeviceEncKey = encKey,
                        pairedDevices = devices
                    )
                }
            }
        }
        viewModelScope.launch {
            networkHelper.observeNetworkChanges().collect { ips ->
                val currentLocalIp = ips.firstOrNull() ?: "Unknown IP"
                val qrContent = getOwnQrContentUseCase()
                val qrBitmap = qrCodeUtils.generateQrCode(qrContent)
                _uiState.update {
                    it.copy(
                        ownIpAddress = currentLocalIp,
                        ownQrContent = qrContent,
                        ownQrBitmap = qrBitmap
                    )
                }
            }
        }
    }

    private var deepLinkHandled = false

    fun onEvent(event: PairedDevicesEvent) {
        when (event) {
            is PairedDevicesEvent.PairDirectly -> {
                if (deepLinkHandled) return
                deepLinkHandled = true
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    when (val result = pairDeviceUseCase(event.deviceId, event.ips, event.port, event.encKey)) {
                        is PairResult.Success -> {
                            showSuccessSnackbar(Res.string.pairing_success)
                        }
                        is PairResult.OfflineSuccess -> {
                            showSuccessSnackbar(Res.string.pairing_offline_success)
                        }
                        is PairResult.Error -> {
                            showErrorSnackbar(
                                Res.string.error_with_message,
                                formatArgs = listOf(result.message)
                            )
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

            is PairedDevicesEvent.DecodeAndPair -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    val qrText = qrCodeUtils.decodeQrFromBitmap(event.bitmap)
                    if (qrText == null) {
                        showErrorSnackbar(Res.string.qr_decode_failed)
                        _uiState.update { it.copy(isLoading = false) }
                        return@launch
                    }

                    when (val result = pairDeviceFromQrData(qrText)) {
                        is PairResult.Success -> {
                            showSuccessSnackbar(Res.string.pairing_success)
                        }
                        is PairResult.OfflineSuccess -> {
                            showSuccessSnackbar(Res.string.pairing_offline_success)
                        }
                        is PairResult.Error -> {
                            showErrorSnackbar(
                                Res.string.invalid_pairing_format,
                                formatArgs = listOf(result.message)
                            )
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

            is PairedDevicesEvent.PingDevice -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        val name = event.device.deviceName.ifBlank { event.device.deviceId }
                        val success = orchestrator.manualSync(event.device.deviceId)
                        if (success) {
                            showSuccessSnackbar(
                                Res.string.sync_success_with_device,
                                formatArgs = listOf(name)
                            )
                        } else {
                            showErrorSnackbar(Res.string.sync_failed)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorSnackbar(
                            Res.string.sync_failed_with_error,
                            formatArgs = listOf(e.message ?: "")
                        )
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }

            is PairedDevicesEvent.DeleteDevice -> {
                orchestrator.disconnectDevice(event.deviceId)
                viewModelScope.launch {
                    deletePairedDeviceUseCase(event.deviceId)
                }
            }

            is PairedDevicesEvent.UpdateDeviceName -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    updateDeviceNameUseCase(event.name, _uiState.value.pairedDevices)
                    val deviceId = deviceKeyStore.getCurrentDeviceId()
                    val encKey = deviceKeyStore.getCurrentDeviceEncKey()
                    val localIp = networkHelper.getPrimaryLocalIpAddress() ?: "Unknown IP"
                    _uiState.update {
                        it.copy(
                            ownDeviceName = event.name,
                            ownDeviceId = deviceId,
                            ownDeviceEncKey = encKey,
                            ownIpAddress = localIp,
                            isLoading = false
                        )
                    }
                }
            }

            is PairedDevicesEvent.SetCustomIp -> {
                val trimmed = event.ipAddress?.trim()?.takeIf { it.isNotBlank() }
                viewModelScope.launch {
                    val device = _uiState.value.pairedDevices.firstOrNull { it.deviceId == event.deviceId }
                        ?: return@launch
                    pairedDevicesRepository.updateCustomIpAddress(event.deviceId, trimmed)

                    if (trimmed != null) {
                        pairedDevicesRepository.updateIpAddresses(
                            event.deviceId,
                            trimmed,
                            device.candidateIpAddresses
                        )
                    } else if (device.ipAddress == device.customIpAddress) {
                        val fallbackIp = device.candidateIpAddresses.firstOrNull() ?: device.ipAddress
                        pairedDevicesRepository.updateIpAddresses(
                            event.deviceId,
                            fallbackIp,
                            device.candidateIpAddresses
                        )
                    }

                    orchestrator.connectWebSocket(event.deviceId)
                    if (trimmed != null) {
                        orchestrator.syncDevice(event.deviceId)
                    }
                }
            }

            is PairedDevicesEvent.PairFromClipboard -> {
                viewModelScope.launch {
                    val link = event.pairingLink?.trim()
                    if (link.isNullOrBlank()) {
                        showErrorSnackbar(Res.string.clipboard_empty_or_invalid)
                        return@launch
                    }
                    if (!link.startsWith(SYNC_DEEP_LINK_BASE_URI)) {
                        showErrorSnackbar(Res.string.invalid_pairing_link_format)
                        return@launch
                    }
                    _uiState.update { it.copy(isLoading = true) }
                    when (val result = pairDeviceFromQrData(link)) {
                        is PairResult.Success -> {
                            showSuccessSnackbar(Res.string.pairing_success)
                        }
                        is PairResult.OfflineSuccess -> {
                            showSuccessSnackbar(Res.string.pairing_offline_success)
                        }
                        is PairResult.Error -> {
                            showErrorSnackbar(
                                Res.string.invalid_pairing_format,
                                formatArgs = listOf(result.message)
                            )
                        }
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

            PairedDevicesEvent.ResetEncryptionKey -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    try {
                        _uiState.value.pairedDevices.forEach { device ->
                            orchestrator.disconnectDevice(device.deviceId)
                            deletePairedDeviceUseCase(device.deviceId)
                        }
                        val encKey = deviceKeyStore.resetCurrentDeviceEncKey()
                        val qrContent = getOwnQrContentUseCase()
                        val qrBitmap = qrCodeUtils.generateQrCode(qrContent)
                        _uiState.update {
                            it.copy(
                                ownDeviceEncKey = encKey,
                                ownQrContent = qrContent,
                                ownQrBitmap = qrBitmap
                            )
                        }
                        showSuccessSnackbar(Res.string.encryption_key_reset_success)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorSnackbar(
                            Res.string.error_with_message,
                            formatArgs = listOf(e.message ?: "")
                        )
                    } finally {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    private fun showSuccessSnackbar(
        stringRes: StringResource,
        actionLabelRes: StringResource? = null,
        formatArgs: List<Any> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value.snackbarHostState.showSuccessSnackbar(stringRes, actionLabelRes, formatArgs)
        }
    }

    private fun showErrorSnackbar(
        stringRes: StringResource,
        actionLabelRes: StringResource? = null,
        formatArgs: List<Any> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value.snackbarHostState.showErrorSnackbar(stringRes, actionLabelRes, formatArgs)
        }
    }
}

data class PairedDevicesUiState(
    val ownDeviceId: String = "",
    val ownDeviceName: String = "",
    val ownDeviceEncKey: String = "",
    val ownIpAddress: String = "",
    val ownQrContent: String = "",
    val ownQrBitmap: KmpBitmap? = null,
    val pairedDevices: List<PairedDevice> = emptyList(),
    val isLoading: Boolean = false,
    val snackbarHostState: SnackbarHostState = SnackbarHostState()
)
