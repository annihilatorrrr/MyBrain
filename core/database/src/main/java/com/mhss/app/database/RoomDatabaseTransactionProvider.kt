package com.mhss.app.database

import androidx.room.withTransaction

class RoomDatabaseTransactionProvider(
    private val database: MyBrainDatabase
) : DatabaseTransactionProvider {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withTransaction { block() }
    }
}
