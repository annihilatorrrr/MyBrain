package com.mhss.app.mybrain.presentation.app_lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.Res
import com.mhss.app.ui.auth_button
import com.mhss.app.ui.auth_title
import com.mhss.app.ui.ic_lock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthScreen(
    onAuthClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_lock),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(Res.string.auth_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAuthClick,
            shape = RoundedCornerShape(99.dp),
        ) {
            Text(
                text = stringResource(Res.string.auth_button),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}