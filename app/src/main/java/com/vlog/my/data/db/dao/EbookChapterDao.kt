package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.model.ebook.EbookChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 电子书章节DAO
 */
@Dao
interface EbookChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<EbookChapterEntity>)

    @Query("SELECT * FROM ebook_chapters WHERE ebookId = :ebookId ORDER BY chapterIndex")
    fun getChaptersByEbookId(ebookId: String): Flow<List<EbookChapterEntity>>

    @Query("SELECT * FROM ebook_chapters WHERE ebookId = :ebookId AND chapterIndex = :chapterIndex")
    suspend fun getChapter(ebookId: String, chapterIndex: Int): EbookChapterEntity?
}
