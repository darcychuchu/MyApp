package com.vlog.my.data.model

import com.vlog.my.data.model.json.ContentTypeConfig

/**
 * 小程序配置模型
 * 包含小程序的基本信息和内容类型配置
 */
data class SubScripts(
    var id: String,
    var title: String,
    var isTyped: Int? = null,  // 0 = edge模式，1= json模式
    var createdBy: String? = null,  // 主要字段 存储Users 的name字段
    var subUrl: String,
    var subKey: String,
    var contentTypeConfig: ContentTypeConfig? = null // 内容类型配置，用于自定义解析
)
