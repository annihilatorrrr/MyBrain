package com.mhss.app.data

import androidx.core.net.toUri
import com.mhss.app.data.impl.MarkdownNoteRepositoryImpl
import com.mhss.app.data.impl.RoomNoteRepositoryImpl
import com.mhss.app.domain.di.NoteDomainModule
import com.mhss.app.domain.repository.NoteRepository
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ksp.generated.module

@Module
@ComponentScan("com.mhss.app.data")
internal class NoteDataModule

val noteDataModule = module {
    includes(NoteDataModule().module, NoteDomainModule().module)
}

val noteRoomModule = module {
    factory<NoteRepository> {
        RoomNoteRepositoryImpl(get(), get(named("ioDispatcher")))
    }
}

fun noteMarkdownModule(rootUri: String) = module {
    factory<NoteRepository> {
        MarkdownNoteRepositoryImpl(get(), rootUri.toUri())
    }
}
