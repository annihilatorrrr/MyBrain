package com.mhss.app.domain.use_case

import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.alarm.use_case.UpsertAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import org.koin.core.annotation.Single
import kotlin.time.Clock.System.now

@Single
class UpsertTaskUseCase(
    private val tasksRepository: TaskRepository,
    private val upsertAlarm: UpsertAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(
        task: Task,
        previousTask: Task? = null,
        updateWidget: Boolean = true
    ): Boolean {
        val nowMillis = now().toEpochMilliseconds()
        val finalTask = when {
            task.dueDate != 0L && task.dueDate > nowMillis -> {
                // Valid future due date, create/update alarm
                val alarmId = upsertAlarm(Alarm(task.alarmId ?: 0, task.dueDate))
                task.copy(alarmId = alarmId?.toInt())
            }

            // Due date removed (=0) or past due, delete existing alarm if it exists
            task.dueDate <= nowMillis && previousTask?.alarmId != null -> {
                deleteAlarmUseCase(previousTask.alarmId)
                task.copy(alarmId = null)
            }

            else -> task
        }

        tasksRepository.upsertTask(finalTask)
        if (updateWidget) widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)

        // Return true (success) if the task alarm was set correctly or if it has no due date at all
        return finalTask.alarmId != null || finalTask.dueDate == 0L
    }
}