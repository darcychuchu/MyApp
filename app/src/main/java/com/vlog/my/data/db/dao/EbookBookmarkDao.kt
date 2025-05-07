package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * 电子书书签DAO
 */
@Dao
interface EbookBookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: EbookBookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: EbookBookmarkEntity)

    @Query("SELECT * FROM ebook_bookmarks WHERE ebookId = :ebookId ORDER BY chapterIndex, position")
    fun getBookmarksByEbookId(ebookId: String): Flow<List<EbookBookmarkEntity>>
}
