package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.db.entity.ReceivedSubScriptEntity
import kotlinx.coroutines.flow.Flow

/**
 * 接收到的小程序DAO
 * 用于访问接收到的小程序数据
 */
@Dao
interface ReceivedSubScriptDao {
    @Query("SELECT * FROM received_subscripts ORDER BY receivedAt DESC")
    fun getAllReceivedSubScripts(): Flow<List<ReceivedSubScriptEntity>>
    
    @Query("SELECT * FROM received_subscripts WHERE id = :id")
    suspend fun getReceivedSubScriptById(id: String): ReceivedSubScriptEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceivedSubScript(receivedSubScript: ReceivedSubScriptEntity)
    
    @Delete
    suspend fun deleteReceivedSubScript(receivedSubScript: ReceivedSubScriptEntity)
    
    @Query("DELETE FROM received_subscripts WHERE id = :id")
    suspend fun deleteReceivedSubScriptById(id: String)
}
