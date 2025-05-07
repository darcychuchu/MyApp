package com.vlog.my.data.local.subscript

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vlog.my.data.local.entity.SubScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubScriptDao {
    @Query("SELECT * FROM subscripts")
    fun getAllSubScripts(): Flow<List<SubScriptEntity>>
    
    @Query("SELECT * FROM subscripts WHERE id = :id")
    suspend fun getSubScriptById(id: String): SubScriptEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubScript(subScript: SubScriptEntity)
    
    @Update
    suspend fun updateSubScript(subScript: SubScriptEntity)
    
    @Delete
    suspend fun deleteSubScript(subScript: SubScriptEntity)
}
