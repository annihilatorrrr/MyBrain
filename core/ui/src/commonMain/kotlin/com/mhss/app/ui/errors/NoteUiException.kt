package com.mhss.app.ui.errors

import com.mhss.app.domain.model.NoteException
import com.mhss.app.ui.Res
import com.mhss.app.ui.error_folder_exists
import com.mhss.app.ui.error_note_create_file_failed
import com.mhss.app.ui.error_note_create_folder_failed
import com.mhss.app.ui.error_note_delete_file_failed
import com.mhss.app.ui.error_note_delete_folder_failed
import com.mhss.app.ui.error_note_file_not_found
import com.mhss.app.ui.error_note_file_with_same_name_exists
import com.mhss.app.ui.error_note_invalid_file_name
import com.mhss.app.ui.error_note_invalid_uri
import com.mhss.app.ui.error_note_move_file_failed
import com.mhss.app.ui.error_note_permission_denied
import com.mhss.app.ui.error_note_rename_file_failed
import com.mhss.app.ui.error_note_rename_folder_failed
import com.mhss.app.ui.error_note_unknown_error
import com.mhss.app.ui.error_note_write_file_failed
import com.mhss.app.ui.snackbar.LocalisedSnackbarMessage
import org.jetbrains.compose.resources.StringResource

fun NoteException.toMessageResId(): StringResource {
    return when (this) {
        NoteException.FileNotFound -> Res.string.error_note_file_not_found
        NoteException.CreateFileFailed -> Res.string.error_note_create_file_failed
        NoteException.NoteWithSameNameAlreadyExists -> Res.string.error_note_file_with_same_name_exists
        NoteException.WriteFileFailed -> Res.string.error_note_write_file_failed
        NoteException.RenameFileFailed -> Res.string.error_note_rename_file_failed
        NoteException.MoveFileFailed -> Res.string.error_note_move_file_failed
        NoteException.DeleteFileFailed -> Res.string.error_note_delete_file_failed
        NoteException.CreateFolderFailed -> Res.string.error_note_create_folder_failed
        NoteException.RenameFolderFailed -> Res.string.error_note_rename_folder_failed
        NoteException.DeleteFolderFailed -> Res.string.error_note_delete_folder_failed
        NoteException.InvalidUri -> Res.string.error_note_invalid_uri
        NoteException.PermissionDenied -> Res.string.error_note_permission_denied
        NoteException.InvalidFileName -> Res.string.error_note_invalid_file_name
        NoteException.UnknownError -> Res.string.error_note_unknown_error
        NoteException.FolderWithSameNameExists -> Res.string.error_folder_exists
    }
}

fun NoteException.toSnackbarError() = LocalisedSnackbarMessage.Error(toMessageResId())

