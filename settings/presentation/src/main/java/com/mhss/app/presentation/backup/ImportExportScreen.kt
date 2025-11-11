package com.mhss.app.presentation.backup

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mohamedrejeb.calf.core.LocalPlatformContext
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import org.koin.androidx.compose.koinViewModel

@Composable
fun ImportExportScreen(
    viewModel: BackupViewModel = koinViewModel(),
) {
    val backupResult by viewModel.backupResult.collectAsStateWithLifecycle()

    val encrypted by remember { mutableStateOf(false) }
    val password by remember { mutableStateOf("") }
    var exportNotes by remember { mutableStateOf(true) }
    var exportTasks by remember { mutableStateOf(true) }
    var exportDiary by remember { mutableStateOf(true) }
    var exportBookmarks by remember { mutableStateOf(true) }
    var openImportDialog by rememberSaveable { mutableStateOf(false) }
    var pendingImportPath by remember { mutableStateOf<String?>(null) }

    val kmpContext = LocalPlatformContext.current
    val pickFileLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Document,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.getPath(kmpContext)?.let {
            pendingImportPath = it
            openImportDialog = true
        }
    }
    val chooseDirectoryLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.getPath(kmpContext)?.let {
            viewModel.onEvent(
                BackupEvent.ExportData(
                    directoryUri = it,
                    exportNotes = exportNotes,
                    exportTasks = exportTasks,
                    exportDiary = exportDiary,
                    exportBookmarks = exportBookmarks,
                    format = BackupFormat.JSON,
                    encrypted = encrypted,
                    password = password
                )
            )
            val contentResolver = kmpContext.contentResolver

            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            contentResolver.takePersistableUriPermission(it.toUri(), takeFlags)
        }
    }

    Scaffold(
        topBar = { MyBrainAppBar(stringResource(R.string.export_import)) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // encryption will be added in a future version
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Checkbox(checked = encrypted, onCheckedChange = { encrypted = it })
//                Text(
//                    text = stringResource(R.string.encrypted),
//                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                    modifier = Modifier.padding(12.dp)
//                )
//            }
//            AnimatedVisibility(encrypted) {
//                OutlinedTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    label = {
//                        Text(text = stringResource(R.string.password))
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(12.dp),
//                    shape = RoundedCornerShape(15.dp),
//                )
//            }
            Button(
                onClick = {
                    chooseDirectoryLauncher.launch()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_export),
                    null,
                    tint = Color.White
                )
                Text(
                    text = stringResource(R.string.export),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
            CheckBoxWithText(
                text = stringResource(R.string.notes),
                checked = exportNotes,
                onCheckedChange = { exportNotes = it },
            )
            CheckBoxWithText(
                text = stringResource(R.string.tasks),
                checked = exportTasks,
                onCheckedChange = { exportTasks = it },
            )
            CheckBoxWithText(
                text = stringResource(R.string.diary),
                checked = exportDiary,
                onCheckedChange = { exportDiary = it },
            )
            CheckBoxWithText(
                text = stringResource(R.string.bookmarks),
                checked = exportBookmarks,
                onCheckedChange = { exportBookmarks = it },
            )

            if (backupResult == BackupResult.ExportFailed) {
                Text(
                    text = stringResource(R.string.export_failed),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (backupResult == BackupResult.ExportSuccess) {
                Text(
                    text = stringResource(R.string.export_success),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    pickFileLauncher.launch()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_import),
                    null,
                    tint = Color.White
                )
                Text(
                    text = stringResource(R.string.import_data),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }

            if (backupResult == BackupResult.ImportFailed) {
                Text(
                    text = stringResource(R.string.import_failed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (backupResult == BackupResult.ImportSuccess) {
                Text(
                    text = stringResource(R.string.import_success),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
            if (backupResult == BackupResult.Loading) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(12.dp)
                )
            }
        }
        if (openImportDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { 
                    openImportDialog = false
                    pendingImportPath = null
                },
                title = { Text(stringResource(R.string.import_confirmation_title)) },
                text = {
                    Text(stringResource(R.string.import_confirmation_message))
                },
                confirmButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            pendingImportPath?.let { path ->
                                viewModel.onEvent(
                                    BackupEvent.ImportData(
                                        path,
                                        BackupFormat.JSON,
                                        encrypted,
                                        password
                                    )
                                )
                            }
                            openImportDialog = false
                            pendingImportPath = null
                        },
                    ) {
                        Text(stringResource(R.string.import_data), color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openImportDialog = false
                            pendingImportPath = null
                        }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
    }
}

@Composable
fun CheckBoxWithText(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}