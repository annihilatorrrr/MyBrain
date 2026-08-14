package com.mhss.app.presentation.backup

import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.ui.Res
import com.mhss.app.ui.export_format_json
import com.mhss.app.ui.export_format_markdown
import com.mhss.app.ui.ic_json
import com.mhss.app.ui.ic_markdown
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class UiExportFormat(
    val format: BackupFormat,
    val labelRes: StringResource,
    val iconRes: DrawableResource,
) {
    JSON(
        format = BackupFormat.JSON,
        labelRes = Res.string.export_format_json,
        iconRes = Res.drawable.ic_json,
    ),
    MARKDOWN(
        format = BackupFormat.MARKDOWN,
        labelRes = Res.string.export_format_markdown,
        iconRes = Res.drawable.ic_markdown
    )
}