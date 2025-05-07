package com.vlog.my.ui.screens.users

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.model.Resource
import com.vlog.my.data.model.Stories
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.data.repository.FollowersRepository
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 用户主页视图模型
 * 负责加载指定用户的动态和作品列表
 */
@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val followersRepository: FollowersRepository,
    private val userSessionManager: UserSessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取用户名
    val username: String = checkNotNull(savedStateHandle["username"])

    // 用户动态列表
    private val _storiesList = MutableStateFlow<List<Stories>>(emptyList())
    val storiesList: StateFlow<List<Stories>> = _storiesList

    // 用户作品列表
    private val _artworksList = MutableStateFlow<List<Stories>>(emptyList())
    val artworksList: StateFlow<List<Stories>> = _artworksList

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 关注状态
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    // 关注操作状态
    sealed class FollowActionState {
        object Idle : FollowActionState()
        object Loading : FollowActionState()
        data class Success(val message: String) : FollowActionState()
        data class Error(val message: String) : FollowActionState()
    }

    private val _followActionState = MutableStateFlow<FollowActionState>(FollowActionState.Idle)
    val followActionState: StateFlow<FollowActionState> = _followActionState

    // 粉丝数和关注数
    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = _followingCount

    // 初始化时加载数据
    init {
        Log.d("UserHomeViewModel", "初始化，加载用户 $username 的数据")
        loadStoriesList(refresh = true)
        loadArtworksList(refresh = true)
        checkFollowStatus()
        loadFollowCounts()
    }

    /**
     * 加载用户动态列表
     * @param refresh 是否刷新
     */
    fun loadStoriesList(refresh: Boolean = false) {
        val token = userSessionManager.getAccessToken() ?: ""

        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = storiesRepository.getStoriesList(
                    name = username,
                    token = token
                )

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    val newStories = response.data as List<Stories>

                    // 添加日志
                    Log.d("UserHomeViewModel", "获取到用户 $username 的动态列表: ${newStories.size}个")
                    newStories.forEach { story ->
                        Log.d("UserHomeViewModel", "动态: id=${story.id}, title=${story.title}, isTyped=${story.isTyped}")
                    }

                    // 更新列表
                    _storiesList.value = newStories
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("UserHomeViewModel", "加载用户 $username 的动态列表失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("UserHomeViewModel", "加载用户 $username 的动态列表失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载用户作品列表
     * @param refresh 是否刷新
     */
    fun loadArtworksList(refresh: Boolean = false) {
        val token = userSessionManager.getAccessToken() ?: ""

        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val response = storiesRepository.getArtworksList(
                    name = username,
                    token = token
                )

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    val newArtworks = response.data as List<Stories>

                    // 添加日志
                    Log.d("UserHomeViewModel", "获取到用户 $username 的作品列表: ${newArtworks.size}个")
                    newArtworks.forEach { artwork ->
                        Log.d("UserHomeViewModel", "作品: id=${artwork.id}, title=${artwork.title}, isTyped=${artwork.isTyped}")
                    }

                    // 更新列表
                    _artworksList.value = newArtworks
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("UserHomeViewModel", "加载用户 $username 的作品列表失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("UserHomeViewModel", "加载用户 $username 的作品列表失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadStoriesList(refresh = true)
        loadArtworksList(refresh = true)
        checkFollowStatus()
        loadFollowCounts()
    }

    /**
     * 根据选中的 Tab 加载数据
     * @param tabIndex Tab 索引（0=动态，1=作品）
     */
    fun loadDataByTab(tabIndex: Int) {
        Log.d("UserHomeViewModel", "加载用户 $username 的 Tab $tabIndex 的数据")
        when (tabIndex) {
            0 -> {
                Log.d("UserHomeViewModel", "加载用户 $username 的动态列表")
                loadStoriesList(refresh = true)
            }
            1 -> {
                Log.d("UserHomeViewModel", "加载用户 $username 的作品列表")
                loadArtworksList(refresh = true)
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
     * 检查当前查看的用户是否是当前登录用户
     * @return 是否是当前登录用户
     */
    fun isCurrentUser(): Boolean {
        val currentUserName = userSessionManager.getUserName()
        return currentUserName == username
    }

    /**
     * 检查关注状态
     */
    fun checkFollowStatus() {
        if (isCurrentUser()) {
            // 如果是当前用户，不需要检查关注状态
            return
        }

        viewModelScope.launch {
            try {
                val currentUserName = userSessionManager.getUserName() ?: return@launch
                val token = userSessionManager.getAccessToken() ?: return@launch

                // 获取关注列表
                followersRepository.getFollowing(currentUserName, token).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            // 检查是否已关注该用户
                            val isFollowing = result.data?.any { it.userId == username || it.alternateId == username } ?: false
                            _isFollowing.value = isFollowing
                            Log.d("UserHomeViewModel", "检查关注状态: $isFollowing")
                        }
                        is Resource.Error -> {
                            Log.e("UserHomeViewModel", "检查关注状态失败: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UserHomeViewModel", "检查关注状态失败", e)
            }
        }
    }

    /**
     * 加载关注数和粉丝数
     */
    fun loadFollowCounts() {
        viewModelScope.launch {
            try {
                val token = userSessionManager.getAccessToken() ?: return@launch

                // 获取粉丝列表
                followersRepository.getFollowers(username, token).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _followersCount.value = result.data?.size ?: 0
                            Log.d("UserHomeViewModel", "粉丝数: ${_followersCount.value}")
                        }
                        is Resource.Error -> {
                            Log.e("UserHomeViewModel", "获取粉丝数失败: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }

                // 获取关注列表
                followersRepository.getFollowing(username, token).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _followingCount.value = result.data?.size ?: 0
                            Log.d("UserHomeViewModel", "关注数: ${_followingCount.value}")
                        }
                        is Resource.Error -> {
                            Log.e("UserHomeViewModel", "获取关注数失败: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UserHomeViewModel", "加载关注数和粉丝数失败", e)
            }
        }
    }

    /**
     * 关注用户
     */
    fun followUser() {
        if (isCurrentUser()) {
            // 不能关注自己
            _followActionState.value = FollowActionState.Error("不能关注自己")
            return
        }

        viewModelScope.launch {
            _followActionState.value = FollowActionState.Loading

            try {
                val currentUserName = userSessionManager.getUserName() ?: return@launch
                val token = userSessionManager.getAccessToken() ?: return@launch

                followersRepository.followUser(currentUserName, token, username).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _isFollowing.value = true
                            _followActionState.value = FollowActionState.Success("已关注")
                            // 刷新粉丝数
                            loadFollowCounts()
                        }
                        is Resource.Error -> {
                            _followActionState.value = FollowActionState.Error(result.message ?: "关注失败")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }
            } catch (e: Exception) {
                _followActionState.value = FollowActionState.Error("关注失败: ${e.message}")
                Log.e("UserHomeViewModel", "关注用户失败", e)
            }
        }
    }

    /**
     * 取消关注用户
     */
    fun unfollowUser() {
        if (isCurrentUser()) {
            // 不能取消关注自己
            _followActionState.value = FollowActionState.Error("不能取消关注自己")
            return
        }

        viewModelScope.launch {
            _followActionState.value = FollowActionState.Loading

            try {
                val currentUserName = userSessionManager.getUserName() ?: return@launch
                val token = userSessionManager.getAccessToken() ?: return@launch

                followersRepository.unfollowUser(currentUserName, token, username).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _isFollowing.value = false
                            _followActionState.value = FollowActionState.Success("已取消关注")
                            // 刷新粉丝数
                            loadFollowCounts()
                        }
                        is Resource.Error -> {
                            _followActionState.value = FollowActionState.Error(result.message ?: "取消关注失败")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }
            } catch (e: Exception) {
                _followActionState.value = FollowActionState.Error("取消关注失败: ${e.message}")
                Log.e("UserHomeViewModel", "取消关注用户失败", e)
            }
        }
    }

    /**
     * 重置关注操作状态
     */
    fun resetFollowActionState() {
        _followActionState.value = FollowActionState.Idle
    }
}
