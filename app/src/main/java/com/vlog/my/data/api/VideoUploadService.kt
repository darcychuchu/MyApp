package com.vlog.my.data.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 视频上传服务
 * 专用于视频上传，避免OOM问题
 */
interface VideoUploadService {
    /**
     * 发布用户作品
     * 专用于视频上传，使用不记录请求体的OkHttpClient
     */
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
