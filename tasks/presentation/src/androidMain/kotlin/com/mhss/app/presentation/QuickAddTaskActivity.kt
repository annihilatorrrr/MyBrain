package com.mhss.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.ui.theme.MyBrainTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class QuickAddTaskActivity : ComponentActivity() {

    private val viewModel: TasksViewModel by viewModel()
    private val dateTimeFormatter: DateTimeFormatter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(true)
        setContent {
            CompositionLocalProvider(
                LocalDateTimeFormatter provides dateTimeFormatter
            ) {
                MyBrainTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        AddTaskFloatingCard(
                            onAddTask = {
                                viewModel.onEvent(TaskEvent.AddTask(it))
                            },
                            onDismiss = ::finishAndRemoveTask
                        )
                    }
                }
            }
        }
    }
}
