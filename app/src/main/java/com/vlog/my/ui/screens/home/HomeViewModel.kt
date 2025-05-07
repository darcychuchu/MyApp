package com.vlog.my.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.api.PaginatedResponse
import com.vlog.my.data.model.Followers
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
 * 首页视图模型
 * 负责加载全局动态和作品列表
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val followersRepository: FollowersRepository,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    // 全局动态和作品列表
    private val _storiesList = MutableStateFlow<List<Stories>>(emptyList())
    val storiesList: StateFlow<List<Stories>> = _storiesList

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 刷新状态
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 分页信息
    private val _pagination = MutableStateFlow<PaginationInfo?>(null)
    val pagination: StateFlow<PaginationInfo?> = _pagination

    // 当前页码
    private var currentPage = 1

    // 是否有更多数据
    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData

    // 关注的用户列表
    private val _followingUsers = MutableStateFlow<List<Followers>>(emptyList())
    val followingUsers: StateFlow<List<Followers>> = _followingUsers

    // 关注用户的内容列表
    private val _followingStoriesList = MutableStateFlow<List<Stories>>(emptyList())
    val followingStoriesList: StateFlow<List<Stories>> = _followingStoriesList

    // 关注标签页的加载状态
    private val _isFollowingLoading = MutableStateFlow(false)
    val isFollowingLoading: StateFlow<Boolean> = _isFollowingLoading

    // 关注标签页的刷新状态
    private val _isFollowingRefreshing = MutableStateFlow(false)
    val isFollowingRefreshing: StateFlow<Boolean> = _isFollowingRefreshing

    // 关注标签页的错误信息
    private val _followingError = MutableStateFlow<String?>(null)
    val followingError: StateFlow<String?> = _followingError

    // 关注标签页的分页信息
    private var followingCurrentPage = 1
    private val _followingHasMoreData = MutableStateFlow(true)
    val followingHasMoreData: StateFlow<Boolean> = _followingHasMoreData

    // 初始化时加载数据
    init {
        loadGlobalStoriesList(refresh = true)
        loadFollowingUsers()
    }

    /**
     * 加载全局动态和作品列表
     * @param refresh 是否刷新
     */
    fun loadGlobalStoriesList(refresh: Boolean = false) {
        if (_isLoading.value && !refresh) return

        if (refresh) {
            _isRefreshing.value = true
            currentPage = 1
            _hasMoreData.value = true
        } else {
            _isLoading.value = true
        }

        _error.value = null

        viewModelScope.launch {
            try {
                val token = userSessionManager.getAccessToken()
                Log.d("HomeViewModel", "开始加载全局列表，页码: $currentPage, token: $token")

                val response = storiesRepository.getGlobalStoriesList(
                    typed = -1, // 混合类型
                    page = currentPage,
                    token = token
                )

                Log.d("HomeViewModel", "API响应: code=${response.code}, message=${response.message}, data=${response.data != null}")

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    val paginatedResponse = response.data
                    val newStories = paginatedResponse.items ?: emptyList()

                    // 添加日志
                    Log.d("HomeViewModel", "获取到全局列表: ${newStories.size}个")
                    newStories.forEach { story ->
                        Log.d("HomeViewModel", "内容: id=${story.id}, title=${story.title}, isTyped=${story.isTyped}")
                    }

                    // 更新分页信息
                    _pagination.value = PaginationInfo(
                        currentPage = paginatedResponse.page,
                        totalPages = paginatedResponse.total,
                        pageSize = paginatedResponse.pageSize,
                        totalItems = paginatedResponse.total
                    )

                    // 判断是否有更多数据
                    _hasMoreData.value = currentPage < paginatedResponse.total

                    // 更新列表
                    if (refresh) {
                        _storiesList.value = newStories
                    } else {
                        _storiesList.value = _storiesList.value + newStories
                    }

                    // 更新当前页码
                    currentPage++
                } else {
                    _error.value = response.message ?: "加载失败"
                    Log.e("HomeViewModel", "加载全局列表失败: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = "加载失败: ${e.message}"
                Log.e("HomeViewModel", "加载全局列表失败", e)
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    /**
     * 加载更多数据
     */
    fun loadMore() {
        if (_isLoading.value || _isRefreshing.value || !_hasMoreData.value) return
        loadGlobalStoriesList(refresh = false)
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadGlobalStoriesList(refresh = true)
    }

    /**
     * 刷新关注标签页数据
     */
    fun refreshFollowing() {
        loadFollowingUsers()
        loadFollowingStoriesList(refresh = true)
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 清除关注标签页错误
     */
    fun clearFollowingError() {
        _followingError.value = null
    }

    /**
     * 加载关注的用户列表
     */
    private fun loadFollowingUsers() {
        viewModelScope.launch {
            try {
                val currentUser = userSessionManager.getUserName()
                val token = userSessionManager.getAccessToken()

                if (currentUser == null) {
                    _followingError.value = "请先登录"
                    return@launch
                }

                followersRepository.getFollowing(currentUser, token).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _followingUsers.value = result.data ?: emptyList()
                            Log.d("HomeViewModel", "获取到关注用户列表: ${_followingUsers.value.size}个")

                            // 加载关注用户的内容
                            loadFollowingStoriesList(refresh = true)
                        }
                        is Resource.Error -> {
                            _followingError.value = result.message ?: "获取关注用户列表失败"
                            Log.e("HomeViewModel", "获取关注用户列表失败: ${result.message}")
                        }
                        is Resource.Loading -> {
                            // 加载中
                        }
                    }
                }
            } catch (e: Exception) {
                _followingError.value = "获取关注用户列表失败: ${e.message}"
                Log.e("HomeViewModel", "获取关注用户列表失败", e)
            }
        }
    }

    /**
     * 加载关注用户的内容列表
     * @param refresh 是否刷新
     */
    fun loadFollowingStoriesList(refresh: Boolean = false) {
        if (_isFollowingLoading.value && !refresh) return
        if (_followingUsers.value.isEmpty()) return

        if (refresh) {
            _isFollowingRefreshing.value = true
            followingCurrentPage = 1
            _followingHasMoreData.value = true
        } else {
            _isFollowingLoading.value = true
        }

        _followingError.value = null

        viewModelScope.launch {
            try {
                val token = userSessionManager.getAccessToken()
                val userIds = _followingUsers.value.mapNotNull { it.userId }

                if (userIds.isEmpty()) {
                    _followingStoriesList.value = emptyList()
                    _isFollowingLoading.value = false
                    _isFollowingRefreshing.value = false
                    return@launch
                }

                Log.d("HomeViewModel", "开始加载关注用户的内容，页码: $followingCurrentPage, 用户数: ${userIds.size}")

                // 这里需要根据实际API调整，假设API支持按用户ID列表筛选
                // 如果API不支持，可能需要分别获取每个用户的内容，然后合并
                val response = storiesRepository.getGlobalStoriesList(
                    typed = -1, // 混合类型
                    page = followingCurrentPage,
                    token = token
                )

                Log.d("HomeViewModel", "API响应: code=${response.code}, message=${response.message}, data=${response.data != null}")

                if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                    val paginatedResponse = response.data
                    val newStories = paginatedResponse.items ?: emptyList()

                    // 筛选出关注用户的内容
                    val followingStories = newStories.filter { story ->
                        story.createdBy in _followingUsers.value.mapNotNull { it.userId }
                    }

                    Log.d("HomeViewModel", "获取到关注用户的内容: ${followingStories.size}个")

                    // 判断是否有更多数据
                    _followingHasMoreData.value = followingCurrentPage < paginatedResponse.total

                    // 更新列表
                    if (refresh) {
                        _followingStoriesList.value = followingStories
                    } else {
                        _followingStoriesList.value = _followingStoriesList.value + followingStories
                    }

                    // 更新当前页码
                    followingCurrentPage++
                } else {
                    _followingError.value = response.message ?: "加载失败"
                    Log.e("HomeViewModel", "加载关注用户的内容失败: ${response.message}")
                }
            } catch (e: Exception) {
                _followingError.value = "加载失败: ${e.message}"
                Log.e("HomeViewModel", "加载关注用户的内容失败", e)
            } finally {
                _isFollowingLoading.value = false
                _isFollowingRefreshing.value = false
            }
        }
    }

    /**
     * 加载更多关注用户的内容
     */
    fun loadMoreFollowing() {
        if (_isFollowingLoading.value || _isFollowingRefreshing.value || !_followingHasMoreData.value) return
        loadFollowingStoriesList(refresh = false)
    }
}

/**
 * 分页信息
 */
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val totalItems: Int
)
