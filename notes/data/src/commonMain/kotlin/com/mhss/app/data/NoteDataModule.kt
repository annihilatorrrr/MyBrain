package com.mhss.app.data

import com.mhss.app.data.impl.MarkdownNoteRepositoryImpl
import com.mhss.app.data.impl.RoomNoteRepositoryImpl
import com.mhss.app.database.dao.NoteDao
import com.mhss.app.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.qualifier.named
import org.koin.dsl.module

@Module
@ComponentScan("com.mhss.app.data")
class NoteDataModule {

    @Factory
    fun defaultNoteRepository(
        noteDao: NoteDao,
        @Named("ioDispatcher") ioDispatcher: CoroutineDispatcher
    ): NoteRepository {
        return RoomNoteRepositoryImpl(noteDao, ioDispatcher)
    }
}

val noteRoomModule = module {
    factory<NoteRepository> {
        RoomNoteRepositoryImpl(get(), get(named("ioDispatcher")))
    }
}

fun noteMarkdownModule(rootId: String) = module {
    factory<NoteRepository> {
        MarkdownNoteRepositoryImpl(get(), rootId)
    }
}
