package com.vlog.my.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 电子书分享内容模型
 * 用于将电子书信息序列化为JSON，存储在Stories的shareContent字段中
 */
@JsonClass(generateAdapter = true)
data class EbookShareContent(
    // 使用下划线命名法，与JSON字段名匹配
    @Json(name = "ebook_id")
    val ebookId: String,                // 电子书ID

    val title: String,                  // 电子书标题
    val author: String? = null,         // 作者
    val description: String? = null,    // 简介

    @Json(name = "total_chapters")
    val totalChapters: Int = 0,         // 总章节数

    @Json(name = "cover_url")
    val coverUrl: String? = null,       // 封面图片URL

    @Json(name = "shared_by")
    val sharedBy: String? = null,       // 分享者ID

    @Json(name = "shared_at")
    val sharedAt: Long = System.currentTimeMillis() // 分享时间
)
