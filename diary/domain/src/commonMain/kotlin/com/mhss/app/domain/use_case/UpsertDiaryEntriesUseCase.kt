package com.mhss.app.domain.use_case

import com.mhss.app.domain.model.DiaryEntry
import com.mhss.app.domain.repository.DiaryRepository
import org.koin.core.annotation.Factory

@Factory
class UpsertDiaryEntriesUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entries: List<DiaryEntry>, notifySyncChanges: Boolean = true) =
        diaryRepository.upsertEntries(entries, notifyChange = notifySyncChanges)
}
