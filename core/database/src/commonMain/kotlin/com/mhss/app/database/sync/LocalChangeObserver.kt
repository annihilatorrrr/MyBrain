package com.mhss.app.database.sync

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class LocalChangeObserver {
    private val _changes = Channel<Unit>(capacity = Channel.CONFLATED)
    val changes = _changes.receiveAsFlow()

    fun notifyChange() {
        _changes.trySend(Unit)
    }
}
