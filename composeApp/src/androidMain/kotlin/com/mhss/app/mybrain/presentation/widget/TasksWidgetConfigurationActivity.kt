package com.mhss.app.mybrain.presentation.widget

import com.mhss.app.ui.R
import com.mhss.app.widget.tasks.TasksWidget

class TasksWidgetConfigurationActivity : WidgetConfigurationActivity() {
    override val widget = TasksWidget()
    override val titleResource = R.string.tasks_widget_settings
}
