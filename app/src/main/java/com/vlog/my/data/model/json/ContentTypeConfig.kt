package com.vlog.my.data.model.json

import com.squareup.moshi.JsonClass

/**
 * 内容类型枚举
 * 定义支持的内容类型
 */
enum class ContentType(val typeName: String) {
    MOVIE("电影"),
    BOOK("图书"),
    MUSIC("音乐"),
    CUSTOM("自定义")
}

/**
 * 详情页面类型
 * 定义不同内容类型对应的详情页面
 */
enum class DetailScreenType {
    VIDEO_PLAYER,  // 视频播放器
    BOOK_READER,   // 图书阅读器
    AUDIO_PLAYER,  // 音频播放器
    WEB_VIEW       // 网页视图
}

/**
 * 字段映射模型
 * 用于定义外部API字段名与内部标准字段名的映射关系
 */
@JsonClass(generateAdapter = true)
data class FieldMapping(
    val sourceField: String,      // 外部API字段名
    val targetField: String,      // 内部标准字段名
    val isRequired: Boolean = false, // 是否必需字段
    val defaultValue: String? = null // 默认值，当字段不存在时使用
)

/**
 * 内容类型配置
 * 定义特定内容类型的字段映射和详情页面类型
 */
@JsonClass(generateAdapter = true)
data class ContentTypeConfig(
    val type: ContentType,
    val fieldMappings: List<FieldMapping>,
    val detailScreenType: DetailScreenType,
    val listResponsePath: String = "data", // JSON响应中列表数据的路径
    val categoryResponsePath: String = "list" // JSON响应中分类数据的路径
)
