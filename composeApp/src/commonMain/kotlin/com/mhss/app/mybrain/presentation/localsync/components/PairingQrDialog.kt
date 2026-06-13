package com.mhss.app.mybrain.presentation.localsync.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mhss.app.mybrain.presentation.localsync.KmpBitmap
import com.mhss.app.mybrain.presentation.localsync.KmpImage
import com.mhss.app.ui.Res
import com.mhss.app.ui.close
import com.mhss.app.ui.pairing_code
import com.mhss.app.ui.pairing_qr_code_content_description
import com.mhss.app.ui.pairing_qr_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun PairingQrDialog(
    qrBitmap: KmpBitmap?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.pairing_code),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.pairing_qr_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        KmpImage(
                            bitmap = qrBitmap,
                            contentDescription = stringResource(Res.string.pairing_qr_code_content_description),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Button(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
            }
        }
    }
}
