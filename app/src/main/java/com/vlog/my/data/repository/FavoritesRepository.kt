package com.vlog.my.data.repository

import com.vlog.my.data.api.ApiResponse
import com.vlog.my.data.api.FavoritesService
import com.vlog.my.data.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 喜爱/收藏相关的仓库
 */
@Singleton
class FavoritesRepository @Inject constructor(
    private val favoritesService: FavoritesService,
    private val userDataRepository: UserDataRepository
) {
    /**
     * 添加喜爱/收藏
     * @param contentId 内容ID
     * @return 操作结果流
     */
    fun addFavorite(contentId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        val currentUser = userDataRepository.getCurrentUser()
        if (currentUser == null) {
            emit(Resource.Error("请先登录"))
            return@flow
        }
        
        val name = currentUser.name ?: ""
        val token = currentUser.accessToken ?: ""
        
        try {
            val response = favoritesService.addFavorite(name, contentId, token)
            if (response.code == 0 || response.code == 200) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message ?: "添加喜爱失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "添加喜爱失败"))
        }
    }
    
    /**
     * 移除喜爱/收藏
     * @param contentId 内容ID
     * @return 操作结果流
     */
    fun removeFavorite(contentId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        val currentUser = userDataRepository.getCurrentUser()
        if (currentUser == null) {
            emit(Resource.Error("请先登录"))
            return@flow
        }
        
        val name = currentUser.name ?: ""
        val token = currentUser.accessToken ?: ""
        
        try {
            val response = favoritesService.removeFavorite(name, contentId, token)
            if (response.code == 0 || response.code == 200) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message ?: "移除喜爱失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "移除喜爱失败"))
        }
    }
    
    /**
     * 检查是否已喜爱/收藏
     * @param contentId 内容ID
     * @return 是否已喜爱流
     */
    fun checkFavorite(contentId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        
        val currentUser = userDataRepository.getCurrentUser()
        if (currentUser == null) {
            emit(Resource.Error("请先登录"))
            return@flow
        }
        
        val name = currentUser.name ?: ""
        val token = currentUser.accessToken ?: ""
        
        try {
            // 由于API中没有提供检查是否已喜爱的接口，这里我们假设有这个接口
            // 如果实际上没有这个接口，可以在ViewModel中维护一个状态来记录
            val response = favoritesService.checkFavorite(name, contentId, token)
            if (response.code == 0 || response.code == 200) {
                emit(Resource.Success(response.data ?: false))
            } else {
                emit(Resource.Error(response.message ?: "检查喜爱状态失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "检查喜爱状态失败"))
        }
    }
    
    /**
     * 切换喜爱/收藏状态
     * @param contentId 内容ID
     * @param isFavorite 当前是否已喜爱
     * @return 操作结果流
     */
    fun toggleFavorite(contentId: String, isFavorite: Boolean): Flow<Resource<Boolean>> {
        return if (isFavorite) {
            removeFavorite(contentId)
        } else {
            addFavorite(contentId)
        }
    }
}
