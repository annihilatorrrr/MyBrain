package com.mhss.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.alarm.repository.AlarmScheduler
import com.mhss.app.alarm.use_case.GetAllAlarmsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.getValue

class BootBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val getAllAlarms: GetAllAlarmsUseCase by inject()
    private val alarmScheduler: AlarmScheduler by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named("ioDispatcher"))
    private val scope = CoroutineScope(ioDispatcher)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val alarms = getAllAlarms()
                    alarms.forEach {
                        alarmScheduler.scheduleAlarm(it)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }

    }

}