package com.vlog.my.ui.screens.users

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 用户动态详情视图模型
 */
@HiltViewModel
class UserStoryDetailViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取用户名和动态ID
    val username: String = checkNotNull(savedStateHandle["username"])
    private val storyId: String = checkNotNull(savedStateHandle["storyId"])

    // 动态详情
    private val _story = MutableStateFlow<Stories?>(null)
    val story: StateFlow<Stories?> = _story

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
                val response = storiesRepository.getStoriesDetail(username, storyId, token)

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    _story.value = response.data
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("UserStoryDetailViewModel", "加载动态详情失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("UserStoryDetailViewModel", "加载动态详情失败", e)
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
