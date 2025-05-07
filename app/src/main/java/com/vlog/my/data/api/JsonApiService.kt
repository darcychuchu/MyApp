package com.vlog.my.data.api

import com.vlog.my.data.model.json.JsonApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * 小程序JSON API服务接口
 */
interface JsonApiService {
    /**
     * 获取视频列表
     * @param url 完整的API URL
     * @return JsonApiResponse 响应数据
     */
    @GET
    suspend fun getVideoList(@Url url: String): JsonApiResponse

    /**
     * 获取视频列表（原始JSON）
     * @param url 完整的API URL
     * @return String 原始JSON字符串
     */
    @GET
    suspend fun getVideoListRaw(@Url url: String): String

    /**
     * 获取分页视频列表
     * @param url 基础URL
     * @param pg 页码
     * @return JsonApiResponse 响应数据
     */
    @GET
    suspend fun getVideoListByPage(
        @Url url: String,
        @Query("pg") pg: Int = 1
    ): JsonApiResponse

    /**
     * 获取分页视频列表（原始JSON）
     * @param url 基础URL
     * @param pg 页码
     * @return String 原始JSON字符串
     */
    @GET
    suspend fun getVideoListByPageRaw(
        @Url url: String,
        @Query("pg") pg: Int = 1
    ): String
}
