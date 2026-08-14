package com.mhss.app.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.mhss.app.ui.Res
import com.mhss.app.ui.add_note
import com.mhss.app.ui.add_task
import com.mhss.app.ui.calendar_events_next_7_days
import com.mhss.app.ui.components.common.frostedGlass
import com.mhss.app.ui.ic_add_note
import com.mhss.app.ui.ic_calendar
import com.mhss.app.ui.ic_check
import io.github.fletchmckee.liquid.LiquidState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun AttachmentDropDownMenu(
    expanded: Boolean,
    excludedItems: List<AttachmentMenuItem>,
    liquidState: LiquidState,
    onDismiss: () -> Unit,
    onItemClick: (AttachmentMenuItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember(excludedItems) {
        AttachmentMenuItem.entries.filterNot { it in excludedItems }
    }
    val shape = RoundedCornerShape(22.dp)
    DropdownMenu(
        modifier = modifier
            .frostedGlass(liquidState = liquidState, shape = shape, frost = 7.dp),
        expanded = expanded,
        shape = shape,
        onDismissRequest = onDismiss,
        shadowElevation = 4.dp,
        properties = PopupProperties(focusable = false)
    ) {
        items.forEach {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(it.titleRes),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(it.iconRes),
                        contentDescription = stringResource(it.titleRes),
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = { onItemClick(it) }
            )
        }
    }
}

enum class AttachmentMenuItem(val titleRes: StringResource, val iconRes: DrawableResource) {
    Note(Res.string.add_note, Res.drawable.ic_add_note),
    Task(Res.string.add_task, Res.drawable.ic_check),
    CalendarEvents(Res.string.calendar_events_next_7_days, Res.drawable.ic_calendar)
}