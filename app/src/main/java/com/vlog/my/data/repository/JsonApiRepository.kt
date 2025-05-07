package com.vlog.my.data.repository

import com.vlog.my.data.api.JsonApiService
import com.vlog.my.data.model.json.JsonApiResponse
import com.vlog.my.data.parser.CustomParserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 小程序JSON API数据仓库
 */
@Singleton
class JsonApiRepository @Inject constructor(
    private val jsonApiService: JsonApiService,
    private val customParserService: CustomParserService
) {
    // 最后使用的小程序URL
    private var lastUsedSubUrl: String? = null

    /**
     * 获取视频列表
     * @param url API URL
     * @param subScriptId 小程序ID，用于获取自定义解析器
     * @return JsonApiResponse 响应数据
     */
    suspend fun getVideoList(url: String, subScriptId: String? = null): Result<JsonApiResponse> = withContext(Dispatchers.IO) {
        try {
            // 如果提供了小程序ID，尝试使用自定义解析器
            if (subScriptId != null) {
                val customParser = customParserService.getParser(subScriptId)

                if (customParser != null) {
                    // 使用自定义解析器
                    val rawResponse = jsonApiService.getVideoListRaw(url)
                    val parsedResponse = customParser.parseJsonResponse(rawResponse)
                    return@withContext Result.success(parsedResponse)
                }
            }

            // 使用默认解析器
            val response = jsonApiService.getVideoList(url)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取分页视频列表
     * @param url 基础URL
     * @param pg 页码
     * @param subScriptId 小程序ID，用于获取自定义解析器
     * @return JsonApiResponse 响应数据
     */
    suspend fun getVideoListByPage(url: String, pg: Int = 1, subScriptId: String? = null): Result<JsonApiResponse> = withContext(Dispatchers.IO) {
        try {
            // 保存最后使用的URL
            lastUsedSubUrl = url

            // 如果提供了小程序ID，尝试使用自定义解析器
            if (subScriptId != null) {
                val customParser = customParserService.getParser(subScriptId)

                if (customParser != null) {
                    // 使用自定义解析器
                    val rawResponse = jsonApiService.getVideoListByPageRaw(url, pg)
                    val parsedResponse = customParser.parseJsonResponse(rawResponse)
                    return@withContext Result.success(parsedResponse)
                }
            }

            // 使用默认解析器
            val response = jsonApiService.getVideoListByPage(url, pg)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取最后使用的小程序URL
     * @return 最后使用的URL，如果没有则返回空字符串
     */
    fun getLastUsedSubUrl(): String {
        return lastUsedSubUrl ?: ""
    }
}
