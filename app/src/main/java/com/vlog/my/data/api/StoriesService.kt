package com.vlog.my.data.api

import com.vlog.my.data.model.Stories
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface StoriesService {
    // 获取全局动态和作品列表
    @GET("stories/list")
    suspend fun getGlobalStoriesList(
        @Query("typed") typed: Int = -1,
        @Query("page") page: Int = 1,
        @Query("token") token: String? = null
    ): ApiResponse<PaginatedResponse<Stories>>

    // 获取用户动态列表
    @GET("{name}/stories/list")
    suspend fun getStoriesList(
        @Path("name") name: String,
        @Query("token") token: String
    ): ApiResponse<List<Stories>>

    // 获取用户动态详情
    @GET("{name}/stories/{id}")
    suspend fun getStoriesDetail(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String?
    ): ApiResponse<Stories>

    // 发布用户动态
    @Multipart
    @POST("{name}/stories-created")
    suspend fun createStories(
        @Path("name") name: String,
        @Query("token") token: String,
        @Part photoFile: List<MultipartBody.Part>,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("tags") tags: String?,
        @Query("shareContent") shareContent: String?
    ): ApiResponse<Unit>

    // 分享内容到动态（无需上传图片）
    @POST("{name}/stories-shared")
    suspend fun shareStories(
        @Path("name") name: String,
        @Query("token") token: String,
        @Query("title") title: String?,
        @Query("description") description: String?,
        @Query("tags") tags: String?,
        @Query("shareContent") shareContent: String,
        @Query("shareTyped") shareTyped: Int = 0
    ): ApiResponse<Unit>

    // 获取用户作品列表
    @GET("{name}/artworks/list")
    suspend fun getArtworksList(
        @Path("name") name: String,
        @Query("token") token: String
    ): ApiResponse<List<Stories>>

    // 获取用户作品详情
    @GET("{name}/artworks/{id}")
    suspend fun getArtworkDetail(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String?
    ): ApiResponse<Stories>

    // 发布用户作品
    @Multipart
    @POST("{name}/artworks-created")
    suspend fun createArtwork(
        @Path("name") name: String,
        @Query("token") token: String,
        @Part videoFile: MultipartBody.Part,
        @Query("title") title: String?,
        @Query("description") description: String,
        @Query("tags") tags: String?
    ): ApiResponse<Unit>
}
