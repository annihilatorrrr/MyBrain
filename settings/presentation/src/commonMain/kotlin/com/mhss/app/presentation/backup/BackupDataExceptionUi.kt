package com.mhss.app.presentation.backup

import androidx.compose.runtime.Composable
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.ui.Res
import com.mhss.app.ui.backup_error_create_directory
import com.mhss.app.ui.backup_error_create_file
import com.mhss.app.ui.backup_error_generic
import com.mhss.app.ui.backup_error_invalid_location
import com.mhss.app.ui.backup_error_read_file
import com.mhss.app.ui.backup_error_write_file
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackupDataException.toUiMessage(): String = when (this) {
    is BackupDataException.InvalidBackupLocation -> stringResource(
        Res.string.backup_error_invalid_location,
        uri
    )
    is BackupDataException.CouldNotCreateDirectory -> stringResource(
        Res.string.backup_error_create_directory,
        path(parent, directoryName)
    )
    is BackupDataException.CouldNotCreateFile -> stringResource(
        Res.string.backup_error_create_file,
        path(parent, fileName)
    )
    BackupDataException.CouldNotReadFile -> stringResource(Res.string.backup_error_read_file)
    is BackupDataException.CouldNotWriteFile -> stringResource(
        Res.string.backup_error_write_file,
        path(parent, fileName)
    )
    is BackupDataException.GenericError -> stringResource(Res.string.backup_error_generic)
}

private fun path(parent: String, child: String): String {
    val normalizedParent = parent.trim().trimEnd('/')
    val normalizedChild = child.trim().trimStart('/')

    return when {
        normalizedParent.isBlank() -> normalizedChild
        normalizedChild.isBlank() -> normalizedParent
        else -> "$normalizedParent/$normalizedChild"
    }
}
