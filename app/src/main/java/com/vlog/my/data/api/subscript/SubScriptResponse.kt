package com.vlog.my.data.api.subscript

/**
 * SubScript 模块的 API 响应类
 * 独立于主体功能模块，避免相互影响
 */
data class SubScriptResponse<T>(
    val code: Int = 200,
    val message: String? = null,
    val data: T? = null
)

/**
 * SubScript 模块的状态响应类
 * 用于不需要返回数据的 API 请求
 */
data class SubScriptStatusResponse(
    val code: Int = 200,
    val message: String? = null
)

/**
 * SubScript 模块的分页信息类
 */
data class SubScriptPage<T>(
    val data: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

/**
 * SubScript 模块的分页响应类
 */
data class SubScriptPaginatedResponse<T>(
    val items: List<T>?,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
