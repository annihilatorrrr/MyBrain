package com.mhss.app.presentation.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.model.BackupFrequency
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.PrefsKey.BooleanKey
import com.mhss.app.preferences.domain.model.PrefsKey.IntKey
import com.mhss.app.ui.R
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.components.common.NumberPicker
import com.mhss.app.ui.titleRes
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

    // Auto backup state
    val isAutoBackupEnabled by viewModel.getSettings(
        BooleanKey(PrefsConstants.AUTO_BACKUP_ENABLED),
        false
    ).collectAsStateWithLifecycle(false)

    val autoBackupFrequencyValue by viewModel.getSettings(
        IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY),
        BackupFrequency.DAILY.value
    ).collectAsStateWithLifecycle(BackupFrequency.DAILY.value)
    val autoBackupFrequencyAmount by viewModel.getSettings(
        IntKey(PrefsConstants.AUTO_BACKUP_FREQUENCY_AMOUNT),
        1
    ).collectAsStateWithLifecycle(1)

    val autoBackupFrequency =
        BackupFrequency.entries.firstOrNull { it.value == autoBackupFrequencyValue }
            ?: BackupFrequency.DAILY

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
        }
    }

    val chooseAutoBackupDirectoryLauncher = rememberFilePickerLauncher(
        FilePickerFileType.Folder,
        selectionMode = FilePickerSelectionMode.Single
    ) { files ->
        files.firstOrNull()?.let { file ->
            viewModel.onEvent(BackupEvent.SelectAutoBackupFolder(folderUri = file.uri.toString()))
        }
    }

    Scaffold(
        topBar = { MyBrainAppBar(stringResource(R.string.export_import)) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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


            Spacer(Modifier.height(24.dp))

            // Auto Backup Section
            val selectedBackupFolder by viewModel.getAutoBackupFolderPath()
                .collectAsStateWithLifecycle(null)
            AutoBackupCard(
                isEnabled = isAutoBackupEnabled,
                selectedFolder = selectedBackupFolder,
                frequency = autoBackupFrequency,
                frequencyAmount = autoBackupFrequencyAmount,
                onSwitchToggled = {
                    viewModel.onEvent(BackupEvent.SetAutoBackupEnabled(it))
                },
                onFolderSelected = { uri ->
                    viewModel.onEvent(BackupEvent.SelectAutoBackupFolder(uri))
                },
                onSaveFrequencies = { frequency, amount ->
                    viewModel.onEvent(BackupEvent.SaveFrequenciesAndReschedule(frequency, amount))
                },
                chooseDirectoryLauncher = chooseAutoBackupDirectoryLauncher
            )
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

@Composable
fun AutoBackupCard(
    isEnabled: Boolean,
    selectedFolder: String?,
    frequency: BackupFrequency,
    frequencyAmount: Int,
    onSwitchToggled: (Boolean) -> Unit,
    onFolderSelected: (String) -> Unit,
    onSaveFrequencies: (BackupFrequency, Int) -> Unit,
    chooseDirectoryLauncher: com.mohamedrejeb.calf.picker.FilePickerLauncher,
    modifier: Modifier = Modifier
) {
    var localFrequency by remember(frequency) { mutableStateOf(frequency) }
    var localFrequencyAmount by remember(frequencyAmount) { mutableIntStateOf(frequencyAmount) }

    val hasUnsavedChanges = localFrequency != frequency || localFrequencyAmount != frequencyAmount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(25.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.auto_backup),
                    style = MaterialTheme.typography.titleLarge
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onSwitchToggled(it) }
                )
            }
            Text(
                text = stringResource(R.string.auto_backup_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            AnimatedVisibility(isEnabled) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    AutoBackupFolderCard(
                        selectedFolder = selectedFolder,
                        onFolderSelected = onFolderSelected,
                        chooseDirectoryLauncher = chooseDirectoryLauncher
                    )
                    Spacer(Modifier.height(12.dp))
                    var frequencyMenuVisible by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DropDownItem(
                            title = stringResource(R.string.backup_frequency),
                            expanded = frequencyMenuVisible,
                            items = BackupFrequency.entries,
                            selectedItem = localFrequency,
                            getText = {
                                stringResource(it.titleRes)
                            },
                            onItemSelected = {
                                frequencyMenuVisible = false
                                localFrequency = it
                            },
                            onDismissRequest = {
                                frequencyMenuVisible = false
                            },
                            onClick = {
                                frequencyMenuVisible = true
                            }
                        )
                        NumberPicker(
                            stringResource(R.string.repeats_every),
                            localFrequencyAmount
                        ) {
                            if (it > 0) localFrequencyAmount = it
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    AnimatedVisibility(hasUnsavedChanges) {
                        Button(
                            onClick = {
                                onSaveFrequencies(localFrequency, localFrequencyAmount)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoBackupFolderCard(
    selectedFolder: String?,
    onFolderSelected: (String) -> Unit,
    chooseDirectoryLauncher: com.mohamedrejeb.calf.picker.FilePickerLauncher,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = stringResource(R.string.backup_folder),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(0.1f).compositeOver(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            ),
            onClick = { chooseDirectoryLauncher.launch() }
        ) {
            Text(
                text = selectedFolder ?: stringResource(R.string.select_backup_folder),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun <T> DropDownItem(
    modifier: Modifier = Modifier,
    title: String,
    expanded: Boolean,
    items: Iterable<T>,
    selectedItem: T,
    getText: @Composable (T) -> String,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    onClick: () -> Unit,
) {
    Box(modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onItemSelected(item)
                    },
                    text = {
                        Text(text = getText(item))
                    }
                )
            }
        }
        Row(
            Modifier
                .clickable { onClick() }
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getText(selectedItem)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = title,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}