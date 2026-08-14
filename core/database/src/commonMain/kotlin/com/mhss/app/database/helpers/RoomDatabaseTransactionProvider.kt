package com.mhss.app.database.helpers

import androidx.room3.withWriteTransaction
import com.mhss.app.database.MyBrainDatabase

class RoomDatabaseTransactionProvider(
    private val database: MyBrainDatabase
) : DatabaseTransactionProvider {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database.withWriteTransaction { block() }
    }
}