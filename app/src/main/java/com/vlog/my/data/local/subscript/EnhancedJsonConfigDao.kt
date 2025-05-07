package com.vlog.my.data.local.subscript

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vlog.my.data.model.EnhancedJsonConfig
import kotlinx.coroutines.flow.Flow

/**
 * 增强型JSON配置DAO
 */
@Dao
interface EnhancedJsonConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: EnhancedJsonConfig): Long

    @Update
    suspend fun update(config: EnhancedJsonConfig)

    @Delete
    suspend fun delete(config: EnhancedJsonConfig)

    @Query("SELECT * FROM enhanced_json_configs WHERE id = :id")
    suspend fun getConfigById(id: String): EnhancedJsonConfig?

    @Query("SELECT * FROM enhanced_json_configs ORDER BY createdAt DESC")
    fun getAllConfigs(): Flow<List<EnhancedJsonConfig>>
}
