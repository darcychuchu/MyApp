package com.vlog.my.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.Followers
import com.vlog.my.data.model.Resource
import com.vlog.my.data.repository.FollowersRepository
import com.vlog.my.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 关注相关的ViewModel
 */
@HiltViewModel
class FollowersViewModel @Inject constructor(
    private val followersRepository: FollowersRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // 我关注的用户列表状态
    private val _followingState = MutableStateFlow<FollowersState>(FollowersState.Initial)
    val followingState: StateFlow<FollowersState> = _followingState.asStateFlow()

    // 关注我的用户列表状态（粉丝）
    private val _followersState = MutableStateFlow<FollowersState>(FollowersState.Initial)
    val followersState: StateFlow<FollowersState> = _followersState.asStateFlow()

    // 关注/取消关注操作状态
    private val _followActionState = MutableStateFlow<FollowActionState>(FollowActionState.Initial)
    val followActionState: StateFlow<FollowActionState> = _followActionState.asStateFlow()

    /**
     * 获取用户关注的列表
     * @param username 要查看的用户名，如果为null则查看当前登录用户
     */
    fun getFollowing(username: String? = null) {
        viewModelScope.launch {
            val currentUser = userDataRepository.getCurrentUser()
            if (currentUser == null) {
                _followingState.value = FollowersState.Error("请先登录")
                return@launch
            }

            val token = currentUser.accessToken ?: ""
            val name = username ?: currentUser.name ?: ""

            followersRepository.getFollowing(name, token)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _followingState.value = FollowersState.Loading
                        }
                        is Resource.Success -> {
                            _followingState.value = if (result.data.isNullOrEmpty()) {
                                FollowersState.Empty
                            } else {
                                FollowersState.Success(result.data)
                            }
                        }
                        is Resource.Error -> {
                            _followingState.value = FollowersState.Error(result.message ?: "获取关注列表失败")
                        }
                    }
                }
        }
    }

    /**
     * 获取用户的粉丝列表
     * @param username 要查看的用户名，如果为null则查看当前登录用户
     */
    fun getFollowers(username: String? = null) {
        viewModelScope.launch {
            val currentUser = userDataRepository.getCurrentUser()
            if (currentUser == null) {
                _followersState.value = FollowersState.Error("请先登录")
                return@launch
            }

            val token = currentUser.accessToken ?: ""
            val name = username ?: currentUser.name ?: ""

            followersRepository.getFollowers(name, token)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _followersState.value = FollowersState.Loading
                        }
                        is Resource.Success -> {
                            _followersState.value = if (result.data.isNullOrEmpty()) {
                                FollowersState.Empty
                            } else {
                                FollowersState.Success(result.data)
                            }
                        }
                        is Resource.Error -> {
                            _followersState.value = FollowersState.Error(result.message ?: "获取粉丝列表失败")
                        }
                    }
                }
        }
    }

    /**
     * 关注用户
     */
    fun followUser(userId: String) {
        viewModelScope.launch {
            val currentUser = userDataRepository.getCurrentUser()
            if (currentUser == null) {
                _followActionState.value = FollowActionState.Error("请先登录")
                return@launch
            }

            _followActionState.value = FollowActionState.Loading

            val name = currentUser.name ?: ""
            val token = currentUser.accessToken ?: ""

            followersRepository.followUser(name, token, userId)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // 已经在上面设置了Loading状态
                        }
                        is Resource.Success -> {
                            _followActionState.value = FollowActionState.Success("关注成功")
                            // 刷新关注列表
                            getFollowing()
                        }
                        is Resource.Error -> {
                            _followActionState.value = FollowActionState.Error(result.message ?: "关注失败")
                        }
                    }
                }
        }
    }

    /**
     * 取消关注用户
     */
    fun unfollowUser(userId: String) {
        viewModelScope.launch {
            val currentUser = userDataRepository.getCurrentUser()
            if (currentUser == null) {
                _followActionState.value = FollowActionState.Error("请先登录")
                return@launch
            }

            _followActionState.value = FollowActionState.Loading

            val name = currentUser.name ?: ""
            val token = currentUser.accessToken ?: ""

            followersRepository.unfollowUser(name, token, userId)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // 已经在上面设置了Loading状态
                        }
                        is Resource.Success -> {
                            _followActionState.value = FollowActionState.Success("取消关注成功")
                            // 刷新关注列表
                            getFollowing()
                        }
                        is Resource.Error -> {
                            _followActionState.value = FollowActionState.Error(result.message ?: "取消关注失败")
                        }
                    }
                }
        }
    }

    /**
     * 重置关注/取消关注操作状态
     */
    fun resetFollowActionState() {
        _followActionState.value = FollowActionState.Initial
    }

    /**
     * 获取当前登录用户的用户名
     */
    fun getCurrentUserName(): String {
        return userDataRepository.getCurrentUser()?.name ?: ""
    }

    /**
     * 关注列表状态
     */
    sealed class FollowersState {
        object Initial : FollowersState()
        object Loading : FollowersState()
        object Empty : FollowersState()
        data class Success(val followers: List<Followers>) : FollowersState()
        data class Error(val message: String) : FollowersState()
    }

    /**
     * 关注/取消关注操作状态
     */
    sealed class FollowActionState {
        object Initial : FollowActionState()
        object Loading : FollowActionState()
        data class Success(val message: String) : FollowActionState()
        data class Error(val message: String) : FollowActionState()
    }
}
