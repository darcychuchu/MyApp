package com.vlog.my.data.repository

import com.vlog.my.data.api.FollowersService
import com.vlog.my.data.api.ApiResponse
import com.vlog.my.data.model.Followers
import com.vlog.my.data.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 关注相关的仓库
 */
@Singleton
class FollowersRepository @Inject constructor(
    private val followersService: FollowersService
) {
    /**
     * 获取我关注的用户列表
     */
    fun getFollowing(name: String, token: String?): Flow<Resource<List<Followers>>> = flow {
        emit(Resource.Loading())
        try {
            val response = followersService.getFollowing(name, token)
            if (response.code == 0) {
                emit(Resource.Success(response.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.message ?: "获取关注列表失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "获取关注列表失败"))
        }
    }

    /**
     * 获取关注我的用户列表（粉丝）
     */
    fun getFollowers(name: String, token: String?): Flow<Resource<List<Followers>>> = flow {
        emit(Resource.Loading())
        try {
            val response = followersService.getFollowers(name, token)
            if (response.code == 0) {
                emit(Resource.Success(response.data ?: emptyList()))
            } else {
                emit(Resource.Error(response.message ?: "获取粉丝列表失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "获取粉丝列表失败"))
        }
    }

    /**
     * 关注用户
     */
    fun followUser(name: String, token: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = followersService.followUser(name, token, userId)
            if (response.code == 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message ?: "关注用户失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "关注用户失败"))
        }
    }

    /**
     * 取消关注用户
     */
    fun unfollowUser(name: String, token: String, userId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = followersService.unfollowUser(name, token, userId)
            if (response.code == 0) {
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error(response.message ?: "取消关注失败"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "取消关注失败"))
        }
    }
}
