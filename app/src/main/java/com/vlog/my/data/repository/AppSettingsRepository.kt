package com.vlog.my.data.repository

import com.vlog.my.data.local.dao.AppSettingsDao
import com.vlog.my.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用设置仓库
 * 用于存储和获取应用设置
 */
@Singleton
class AppSettingsRepository @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) {
    /**
     * 获取设置值
     * @param key 设置键
     * @return 设置值的Flow
     */
    fun getSetting(key: String): Flow<AppSettingsEntity?> {
        return appSettingsDao.getSetting(key)
    }
    
    /**
     * 保存设置
     * @param key 设置键
     * @param value 设置值
     */
    suspend fun saveSetting(key: String, value: String) {
        val setting = AppSettingsEntity(
            settingKey = key,
            settingValue = value
        )
        appSettingsDao.saveSetting(setting)
    }
    
    /**
     * 删除设置
     * @param key 设置键
     */
    suspend fun deleteSetting(key: String) {
        appSettingsDao.deleteSetting(key)
    }
}
