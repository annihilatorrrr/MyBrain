package com.mhss.app.mybrain.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.mybrain.sync.domain.GetPairedDevicesFlowUseCase
import com.mhss.app.mybrain.sync.model.PairedDevice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SpacesViewModel(
    getPairedDevicesFlow: GetPairedDevicesFlowUseCase
) : ViewModel() {

    val pairedDevices: StateFlow<List<PairedDevice>> = getPairedDevicesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
