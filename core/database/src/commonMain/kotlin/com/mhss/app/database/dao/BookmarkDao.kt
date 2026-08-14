package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import com.mhss.app.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllFullBookmarks(): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmark(id: String): BookmarkEntity?

    @Query("SELECT * FROM bookmarks WHERE id IN (:ids)")
    suspend fun getBookmarksByIds(ids: List<String>): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE updated_date > :timestamp")
    suspend fun getBookmarksUpdatedAfter(timestamp: Long): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getBookmarksAfterSeq(seq: Long, maxSeq: Long): List<BookmarkEntity>

    @Query("SELECT * FROM bookmarks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%'")
    suspend fun searchBookmarks(query: String): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Upsert
    suspend fun upsertBookmarks(bookmarks: List<BookmarkEntity>)

}
