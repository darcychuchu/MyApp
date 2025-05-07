package com.vlog.my.ui.screens.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.model.Resource
import com.vlog.my.data.model.Stories
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.data.repository.FavoritesRepository
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 动态详情视图模型
 */
@HiltViewModel
class StoryDetailViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userSessionManager: UserSessionManager,
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取动态ID和用户名
    private val storyId: String = checkNotNull(savedStateHandle["storyId"])
    private val userName: String = checkNotNull(savedStateHandle["userName"])

    // 动态详情
    private val _story = MutableStateFlow<Stories?>(null)
    val story: StateFlow<Stories?> = _story

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 喜爱状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    // 喜爱操作状态
    private val _favoriteActionState = MutableStateFlow<FavoriteActionState>(FavoriteActionState.Initial)
    val favoriteActionState: StateFlow<FavoriteActionState> = _favoriteActionState

    // 初始化时加载数据
    init {
        loadStoryDetail()
    }

    /**
     * 加载动态详情
     */
    fun loadStoryDetail() {
        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val token = userSessionManager.getAccessToken()
                val response = storiesRepository.getStoriesDetail(userName, storyId, token)

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    _story.value = response.data

                    // 加载成功后检查喜爱状态
                    checkFavoriteStatus()
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("StoryDetailViewModel", "加载动态详情失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("StoryDetailViewModel", "加载动态详情失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 检查是否已喜爱
     */
    fun checkFavoriteStatus() {
        viewModelScope.launch {
            _favoriteActionState.value = FavoriteActionState.Loading

            try {
                favoritesRepository.checkFavorite(storyId)
                    .collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                // 已经设置了Loading状态
                            }
                            is Resource.Success -> {
                                _isFavorite.value = result.data ?: false
                                _favoriteActionState.value = FavoriteActionState.Success
                            }
                            is Resource.Error -> {
                                _favoriteActionState.value = FavoriteActionState.Error(result.message ?: "检查喜爱状态失败")
                            }
                        }
                    }
            } catch (e: Exception) {
                _favoriteActionState.value = FavoriteActionState.Error("检查喜爱状态失败: ${e.message}")
            }
        }
    }

    /**
     * 切换喜爱状态
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            _favoriteActionState.value = FavoriteActionState.Loading

            try {
                favoritesRepository.toggleFavorite(storyId, _isFavorite.value)
                    .collect { result ->
                        when (result) {
                            is Resource.Loading -> {
                                // 已经设置了Loading状态
                            }
                            is Resource.Success -> {
                                _isFavorite.value = !_isFavorite.value
                                _favoriteActionState.value = FavoriteActionState.Success
                            }
                            is Resource.Error -> {
                                _favoriteActionState.value = FavoriteActionState.Error(result.message ?: "操作失败")
                            }
                        }
                    }
            } catch (e: Exception) {
                _favoriteActionState.value = FavoriteActionState.Error("操作失败: ${e.message}")
            }
        }
    }

    /**
     * 重置喜爱操作状态
     */
    fun resetFavoriteActionState() {
        _favoriteActionState.value = FavoriteActionState.Initial
    }

    /**
     * 喜爱操作状态
     */
    sealed class FavoriteActionState {
        object Initial : FavoriteActionState()
        object Loading : FavoriteActionState()
        object Success : FavoriteActionState()
        data class Error(val message: String) : FavoriteActionState()
    }
}
