package com.mhss.app.mybrain.presentation.widget

import com.mhss.app.ui.R
import com.mhss.app.widget.notes.NotesWidget

class NotesWidgetConfigurationActivity : WidgetConfigurationActivity() {
    override val widget = NotesWidget()
    override val titleResource = R.string.notes_widget_settings
}
