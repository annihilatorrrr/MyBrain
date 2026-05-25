package com.mhss.app.presentation.integrations.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mhss.app.presentation.components.ExperimentalBadge
import com.mhss.app.ui.Res
import com.mhss.app.ui.enable_ai_tools
import com.mhss.app.ui.ic_tools
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiToolsSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheck: (Boolean) -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable { onCheck(!checked) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_tools),
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.enable_ai_tools),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(8.dp))
            ExperimentalBadge()
        }
        Switch(checked = checked, onCheckedChange = { onCheck(it) })
    }
}