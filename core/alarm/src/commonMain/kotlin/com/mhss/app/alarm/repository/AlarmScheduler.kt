package com.mhss.app.alarm.repository

import com.mhss.app.alarm.model.Alarm


interface AlarmScheduler {

    fun scheduleAlarm(alarm: Alarm)

    fun cancelAlarm(schedulerId: Int)

    fun canScheduleExactAlarms(): Boolean
}