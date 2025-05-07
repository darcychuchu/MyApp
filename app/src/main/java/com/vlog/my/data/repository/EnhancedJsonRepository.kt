package com.vlog.my.data.repository

import com.vlog.my.data.local.subscript.EnhancedJsonConfigDao
import com.vlog.my.data.model.EnhancedJsonConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 增强型JSON配置仓库
 */
@Singleton
class EnhancedJsonRepository @Inject constructor(
    private val enhancedJsonConfigDao: EnhancedJsonConfigDao
) {
    /**
     * 获取所有配置
     */
    fun getAllConfigs(): Flow<List<EnhancedJsonConfig>> {
        return enhancedJsonConfigDao.getAllConfigs()
    }



    /**
     * 根据ID获取配置
     */
    suspend fun getConfigById(id: String): EnhancedJsonConfig? {
        return enhancedJsonConfigDao.getConfigById(id)
    }

    /**
     * 保存配置
     */
    suspend fun saveConfig(config: EnhancedJsonConfig) {
        enhancedJsonConfigDao.insert(config)
    }

    /**
     * 更新配置
     */
    suspend fun updateConfig(config: EnhancedJsonConfig) {
        enhancedJsonConfigDao.update(config)
    }

    /**
     * 删除配置
     */
    suspend fun deleteConfig(config: EnhancedJsonConfig) {
        enhancedJsonConfigDao.delete(config)
    }
}
