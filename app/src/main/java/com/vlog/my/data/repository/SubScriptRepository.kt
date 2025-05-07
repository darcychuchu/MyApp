package com.vlog.my.data.repository

import com.vlog.my.data.api.SubScriptService
import com.vlog.my.data.api.subscript.SubScriptResponse
import com.vlog.my.data.api.subscript.SubScriptStatusResponse
import com.vlog.my.data.local.subscript.SubScriptDao
import com.vlog.my.data.local.entity.SubScriptEntity
import com.vlog.my.data.model.SubScripts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubScriptRepository @Inject constructor(
    private val subScriptDao: SubScriptDao,
    private val subScriptService: SubScriptService
) {
    // Local operations
    fun getAllSubScripts(): Flow<List<SubScripts>> {
        return subScriptDao.getAllSubScripts().map { entities ->
            entities.map { it.toSubScripts() }
        }
    }

    suspend fun getSubScriptById(id: String): SubScripts? {
        return subScriptDao.getSubScriptById(id)?.toSubScripts()
    }

    suspend fun insertSubScript(subScript: SubScripts) {
        subScriptDao.insertSubScript(SubScriptEntity.fromSubScripts(subScript))
    }

    suspend fun updateSubScript(subScript: SubScripts) {
        subScriptDao.updateSubScript(SubScriptEntity.fromSubScripts(subScript))
    }

    suspend fun deleteSubScript(subScript: SubScripts) {
        subScriptDao.deleteSubScript(SubScriptEntity.fromSubScripts(subScript))
    }

    // Remote operations
    suspend fun getRemoteSubScripts(token: String): SubScriptResponse<List<SubScripts>> {
        return subScriptService.getSubScriptsList(token)
    }

    suspend fun createRemoteSubScript(token: String, subScript: SubScripts): SubScriptStatusResponse {
        return subScriptService.createSubScript(token, subScript)
    }

    // Sync operation - for logged-in users
    suspend fun syncSubScript(token: String, subScript: SubScripts) {
        // First save locally
        insertSubScript(subScript)

        // Then try to sync with server if token is provided
        if (!token.isNullOrEmpty()) {
            try {
                createRemoteSubScript(token, subScript)
            } catch (e: Exception) {
                // Handle network errors, but keep local changes
            }
        }
    }
}
