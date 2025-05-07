package com.vlog.my.data.repository

import com.vlog.my.data.db.dao.ReceivedSubScriptDao
import com.vlog.my.data.db.entity.ReceivedSubScriptEntity
import com.vlog.my.data.model.SubScriptPackage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 小程序包仓库
 * 管理接收到的小程序包的存储和检索
 */
@Singleton
class SubScriptPackageRepository @Inject constructor(
    private val receivedSubScriptDao: ReceivedSubScriptDao
) {
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * 保存小程序包
     * @param subScriptPackage 小程序包
     */
    suspend fun saveSubScriptPackage(subScriptPackage: SubScriptPackage) {
        val packageData = json.encodeToString(subScriptPackage)
        
        val entity = ReceivedSubScriptEntity(
            id = subScriptPackage.id,
            title = subScriptPackage.title,
            description = subScriptPackage.description,
            creatorId = subScriptPackage.creatorId,
            creatorName = subScriptPackage.creatorName,
            createdAt = subScriptPackage.createdAt,
            version = subScriptPackage.version,
            isEditable = subScriptPackage.isEditable,
            isShareable = subScriptPackage.isShareable,
            packageData = packageData
        )
        
        receivedSubScriptDao.insertReceivedSubScript(entity)
    }
    
    /**
     * 获取所有接收到的小程序包
     * @return 小程序包列表流
     */
    fun getAllReceivedSubScriptPackages(): Flow<List<SubScriptPackage>> {
        return receivedSubScriptDao.getAllReceivedSubScripts()
            .map { entities ->
                entities.mapNotNull { entity ->
                    try {
                        json.decodeFromString<SubScriptPackage>(entity.packageData)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }
    
    /**
     * 根据ID获取小程序包
     * @param id 小程序包ID
     * @return 小程序包，如果不存在则返回null
     */
    suspend fun getSubScriptPackageById(id: String): SubScriptPackage? {
        val entity = receivedSubScriptDao.getReceivedSubScriptById(id) ?: return null
        
        return try {
            json.decodeFromString<SubScriptPackage>(entity.packageData)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 删除小程序包
     * @param id 小程序包ID
     */
    suspend fun deleteSubScriptPackage(id: String) {
        receivedSubScriptDao.deleteReceivedSubScriptById(id)
    }
}
