package com.vlog.my.data.model.json

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 小程序JSON API响应模型
 */
@JsonClass(generateAdapter = true)
data class JsonApiResponse(
    val pageindex: Int,       // 当前页面
    val pagecount: Int,       // 总页码
    val pagesize: Int,        // 每页数据量
    val recordcount: Int?,     // 总数据量

    @Json(name = "class")  // JSON 中的 "list" 字段，可能会变成 "class"
    val categories: List<Category>, // 分类列表

    @Json(name = "list")  // JSON 中的 "data" 字段，可能会变成 "list"
    val videos: List<VideoItem> // 视频列表
)

/**
 * 分类模型
 */
@JsonClass(generateAdapter = true)
data class Category(
    val type_id: Long,        // 分类ID
    val type_name: String,    // 分类名称
    var parent_type_id: Long = 0 // 父分类ID，默认为0表示一级分类
)

/**
 * 视频项目模型
 */
@JsonClass(generateAdapter = true)
data class VideoItem(
    val vod_id: Long,         // 视频ID
    val vod_name: String,     // 视频名称
    val vod_sub: String?,     // 视频副标题
    val vod_remarks: String?, // 视频备注（如：连载中 连载到5集）
    val vod_serial: Int?,     // 连载集数
    val type_id: Long,        // 分类ID
    val vod_actor: String?,   // 演员
    val vod_director: String?, // 导演
    val vod_pic: String?,     // 封面图片URL
    val vod_content: String?, // 内容简介
    val vod_time: String?,    // 更新时间
    val vod_area: String?,    // 地区
    val vod_lang: String?,    // 语言
    val vod_year: Int?,       // 年份
    val type_name: String?,   // 分类名称
    val vod_tag: String?,     // 标签
    val vod_play_url: String?, // 播放URL
    val vod_duration: Int?    // 时长（分钟）
)
