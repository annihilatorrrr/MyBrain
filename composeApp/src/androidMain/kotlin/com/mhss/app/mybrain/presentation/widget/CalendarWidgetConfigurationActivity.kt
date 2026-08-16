package com.mhss.app.mybrain.presentation.widget

import com.mhss.app.ui.R
import com.mhss.app.widget.calendar.CalendarWidget

class CalendarWidgetConfigurationActivity : WidgetConfigurationActivity() {
    override val widget = CalendarWidget()
    override val titleResource = R.string.calendar_widget_settings
}
