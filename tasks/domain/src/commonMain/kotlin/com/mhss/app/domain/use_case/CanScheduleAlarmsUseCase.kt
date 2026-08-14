package com.mhss.app.domain.use_case

import com.mhss.app.alarm.repository.AlarmScheduler
import org.koin.core.annotation.Single

@Single
class CanScheduleAlarmsUseCase(
    private val alarmScheduler: AlarmScheduler
) {
    operator fun invoke(): Boolean {
        return alarmScheduler.canScheduleExactAlarms()
    }
}
