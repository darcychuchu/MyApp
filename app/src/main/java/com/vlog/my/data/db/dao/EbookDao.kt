package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vlog.my.data.model.ebook.EbookEntity
import kotlinx.coroutines.flow.Flow

/**
 * 电子书DAO
 */
@Dao
interface EbookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEbook(ebook: EbookEntity): Long

    @Update
    suspend fun updateEbook(ebook: EbookEntity)

    @Delete
    suspend fun deleteEbook(ebook: EbookEntity)

    @Query("SELECT * FROM ebooks ORDER BY lastReadDate DESC")
    fun getAllEbooks(): Flow<List<EbookEntity>>

    @Query("SELECT * FROM ebooks WHERE id = :ebookId")
    suspend fun getEbookById(ebookId: String): EbookEntity?

    @Query("UPDATE ebooks SET lastReadChapter = :chapter, lastReadPosition = :position, lastReadDate = :date WHERE id = :ebookId")
    suspend fun updateReadProgress(ebookId: String, chapter: Int, position: Int, date: Long = System.currentTimeMillis())
}
