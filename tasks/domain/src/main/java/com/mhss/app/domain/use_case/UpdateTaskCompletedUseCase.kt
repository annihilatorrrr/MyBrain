package com.mhss.app.domain.use_case

import com.mhss.app.alarm.use_case.DeleteAlarmUseCase
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.widget.WidgetUpdater
import org.koin.core.annotation.Single

@Single
class UpdateTaskCompletedUseCase(
    private val tasksRepository: TaskRepository,
    private val deleteAlarm: DeleteAlarmUseCase,
    private val widgetUpdater: WidgetUpdater
) {
    suspend operator fun invoke(task: Task, completed: Boolean) {
        tasksRepository.completeTask(task.id, completed)
        if (completed && task.alarmId != null) {
            deleteAlarm(task.alarmId)
        }
        widgetUpdater.updateAll(WidgetUpdater.WidgetType.Tasks)
    }
}