package com.mhss.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.alarm.use_case.UpsertAlarmUseCase
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.use_case.GetTaskByAlarmUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val deleteAlarmUseCase: DeleteAlarmUseCase by inject()
    private val upsertAlarmUseCase: UpsertAlarmUseCase by inject()
    private val getTaskByAlarm: GetTaskByAlarmUseCase by inject()
    private val upsertTask: UpsertTaskUseCase by inject()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()

        scope.launch {
            val task =
                intent?.getIntExtra(Constants.ALARM_ID_EXTRA, -1)?.let { getTaskByAlarm(it) }
                    ?: run {
                        pendingResult.finish()
                        return@launch
                    }
            val notificationJob = launch {
                val manager =
                    context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.sendNotification(task, context, task.alarmId ?: return@launch)
                if (!task.recurring) deleteAlarmUseCase(task.alarmId ?: return@launch)
            }
            val recurrenceJob = launch {
                if (task.recurring) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                    when (task.frequency) {
                        TaskFrequency.EVERY_MINUTES -> calendar.add(Calendar.MINUTE, task.frequencyAmount)
                        TaskFrequency.HOURLY -> calendar.add(Calendar.HOUR, task.frequencyAmount)
                        TaskFrequency.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, task.frequencyAmount)
                        TaskFrequency.MONTHLY -> calendar.add(Calendar.MONTH, task.frequencyAmount)
                        TaskFrequency.ANNUAL -> calendar.add(Calendar.YEAR, task.frequencyAmount)
                    }
                    val newTask = task.copy(
                        dueDate = calendar.timeInMillis,
                    )
                    upsertTask(task = newTask, previousTask = task)
                    upsertAlarmUseCase(Alarm(newTask.alarmId ?: return@launch, newTask.dueDate))
                }
            }

            notificationJob.join()
            recurrenceJob.join()
            pendingResult.finish()
        }
    }
}