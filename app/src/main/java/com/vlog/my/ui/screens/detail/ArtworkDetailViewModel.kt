package com.vlog.my.ui.screens.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.model.Resource
import com.vlog.my.data.model.Stories
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.data.repository.FavoritesRepository
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 作品详情视图模型
 */
@HiltViewModel
class ArtworkDetailViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userSessionManager: UserSessionManager,
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取作品ID和用户名
    private val artworkId: String = checkNotNull(savedStateHandle["artworkId"])
    private val userName: String = checkNotNull(savedStateHandle["userName"])

    // 作品详情
    private val _artwork = MutableStateFlow<Stories?>(null)
    val artwork: StateFlow<Stories?> = _artwork

    // 视频URL
    private val _videoUrl = MutableStateFlow<String?>(null)
    val videoUrl: StateFlow<String?> = _videoUrl

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 喜爱状态
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // 喜爱操作状态
    private val _favoriteActionState = MutableStateFlow<FavoriteActionState>(FavoriteActionState.Initial)
    val favoriteActionState: StateFlow<FavoriteActionState> = _favoriteActionState.asStateFlow()

    // 初始化时加载数据
    init {
        loadArtworkDetail()
    }

    /**
     * 加载作品详情
     */
    fun loadArtworkDetail() {
        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val token = userSessionManager.getAccessToken()
                val response = storiesRepository.getArtworkDetail(userName, artworkId, token)

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    _artwork.value = response.data

                    // 设置视频URL
                    response.data.attachmentId?.let { attachmentId ->
                        _videoUrl.value = "${ApiConstants.VIDEO_HLS_BASE_URL}$attachmentId"
                    }

                    // 加载成功后检查喜爱状态
                    checkFavoriteStatus()
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("ArtworkDetailViewModel", "加载作品详情失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("ArtworkDetailViewModel", "加载作品详情失败", e)
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
                favoritesRepository.checkFavorite(artworkId)
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
                favoritesRepository.toggleFavorite(artworkId, _isFavorite.value)
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
