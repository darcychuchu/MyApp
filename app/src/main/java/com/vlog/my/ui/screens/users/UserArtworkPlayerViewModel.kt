package com.vlog.my.ui.screens.users

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.model.Stories
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户作品播放视图模型
 */
@HiltViewModel
class UserArtworkPlayerViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取用户名和作品ID
    val username: String = checkNotNull(savedStateHandle["username"])
    private val artworkId: String = checkNotNull(savedStateHandle["artworkId"])

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
                val response = storiesRepository.getArtworkDetail(username, artworkId, token)

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    _artwork.value = response.data
                    
                    // 设置视频URL
                    response.data.attachmentId?.let { attachmentId ->
                        _videoUrl.value = "${ApiConstants.VIDEO_HLS_BASE_URL}$attachmentId"
                    }
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("UserArtworkPlayerViewModel", "加载作品详情失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("UserArtworkPlayerViewModel", "加载作品详情失败", e)
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
}
