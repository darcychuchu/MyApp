package com.vlog.my.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 喜爱/收藏相关的API服务接口
 */
interface FavoritesService {
    /**
     * 添加喜爱/收藏
     * @param name 用户名
     * @param id 内容ID
     * @param token 用户令牌
     * @return 操作结果
     */
    @GET("{name}/favorites-created/{id}")
    suspend fun addFavorite(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): ApiResponse<Nothing>

    /**
     * 移除喜爱/收藏
     * @param name 用户名
     * @param id 内容ID
     * @param token 用户令牌
     * @return 操作结果
     */
    @GET("{name}/favorites-removed/{id}")
    suspend fun removeFavorite(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): ApiResponse<Nothing>

    /**
     * 检查是否已喜爱/收藏
     * @param name 用户名
     * @param id 内容ID
     * @param token 用户令牌
     * @return 是否已喜爱
     */
    @GET("{name}/favorites-check/{id}")
    suspend fun checkFavorite(
        @Path("name") name: String,
        @Path("id") id: String,
        @Query("token") token: String
    ): ApiResponse<Boolean>
}
