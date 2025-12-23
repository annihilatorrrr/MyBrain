package com.mhss.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.util.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class TaskActionButtonBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val updateTaskCompleted: UpdateTaskCompletedUseCase by inject()
    private val getTaskById: GetTaskByIdUseCase by inject()
    private val ioDispatcher: CoroutineDispatcher by inject(named("ioDispatcher"))
    private val scope = CoroutineScope(ioDispatcher)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Constants.ACTION_COMPLETE) {
            val pendingResult = goAsync()
            scope.launch(ioDispatcher) {
                try {
                    val taskId = intent.getStringExtra(Constants.TASK_ID_EXTRA) ?: return@launch
                    val task = getTaskById(taskId) ?: return@launch
                    updateTaskCompleted(task, true)
                    val manager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(task.alarmId ?: return@launch)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}