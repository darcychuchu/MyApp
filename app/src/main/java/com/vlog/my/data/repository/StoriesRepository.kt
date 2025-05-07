package com.vlog.my.data.repository

import com.vlog.my.data.api.ApiResponse
import com.vlog.my.data.api.StoriesService
import com.vlog.my.data.model.Stories
import com.squareup.moshi.Moshi
import com.vlog.my.data.api.PaginatedResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoriesRepository @Inject constructor(
    private val storiesService: StoriesService,
    private val videoUploadService: com.vlog.my.data.api.VideoUploadService,
    private val moshi: Moshi
) {
    // 获取全局动态和作品列表
    suspend fun getGlobalStoriesList(
        typed: Int = -1,
        page: Int = 1,
        token: String? = null
    ): ApiResponse<PaginatedResponse<Stories>> {
        return storiesService.getGlobalStoriesList(typed, page, token)
    }

    // 获取用户动态列表
    suspend fun getStoriesList(name: String, token: String): ApiResponse<List<Stories>> {
        return storiesService.getStoriesList(name, token)
    }

    // 获取用户动态详情
    suspend fun getStoriesDetail(name: String, id: String, token: String?): ApiResponse<Stories> {
        return storiesService.getStoriesDetail(name, id, token)
    }

    // 发布用户动态
    suspend fun createStories(
        name: String,
        token: String,
        photoFiles: List<MultipartBody.Part>,
        title: String?,
        description: String?,
        tags: String?,
        shareContent: String? = null
    ): ApiResponse<Unit> {
        return storiesService.createStories(name, token, photoFiles, title, description, tags, shareContent)
    }

    // 分享内容到动态（无需上传图片）
    suspend fun shareStories(
        name: String,
        token: String,
        title: String?,
        description: String?,
        tags: String?,
        shareContent: String,
        shareTyped: Int = 0
    ): ApiResponse<Unit> {
        return storiesService.shareStories(name, token, title, description, tags, shareContent, shareTyped)
    }

    // 获取用户作品列表
    suspend fun getArtworksList(name: String, token: String): ApiResponse<List<Stories>> {
        return storiesService.getArtworksList(name, token)
    }

    // 获取用户作品详情
    suspend fun getArtworkDetail(name: String, id: String, token: String?): ApiResponse<Stories> {
        return storiesService.getArtworkDetail(name, id, token)
    }

    // 发布用户作品
    suspend fun createArtwork(
        name: String,
        token: String,
        videoFile: MultipartBody.Part,
        title: String?,
        description: String,
        tags: String?
    ): ApiResponse<Unit> {
        // 使用专用的视频上传服务，避免OOM问题
        return videoUploadService.createArtwork(name, token, videoFile, title, description, tags)
    }
}
