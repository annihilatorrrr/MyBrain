package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.BackupFormat
import com.mhss.app.domain.use_case.`interface`.ExportJsonDataUseCase
import org.koin.core.annotation.Factory

@Factory
class ExportDataUseCase(
    private val exportJsonData: ExportJsonDataUseCase,
) {
    suspend operator fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean ,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        format: BackupFormat,
        encrypted: Boolean,
        password: String?,
    ) = when(format) {
        BackupFormat.JSON -> exportJsonData(
            directoryUri,
            exportNotes,
            exportTasks,
            exportDiary,
            exportBookmarks,
            encrypted,
            password
        )
    }

}