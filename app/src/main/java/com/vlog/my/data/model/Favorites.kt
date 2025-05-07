package com.vlog.my.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 喜爱/收藏数据模型
 */
@JsonClass(generateAdapter = true)
data class Favorites(
    val id: String? = null,
    val createdAt: Long? = null,
    
    @Json(name = "isTyped")
    val isTyped: Int? = null,
    
    val version: Int? = null,
    val createdBy: String? = null,
    val quoteId: String? = null,  // 引用的内容ID（如故事ID）
    val quoteType: Int? = null    // 引用的内容类型
)
