package com.vlog.my.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置DAO
 * 这是一个占位DAO，确保AppDatabase有至少一个DAO
 */
@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE settingKey = :key")
    fun getSetting(key: String): Flow<AppSettingsEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSettingsEntity)
    
    @Query("DELETE FROM app_settings WHERE settingKey = :key")
    suspend fun deleteSetting(key: String)
}
