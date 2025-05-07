package com.vlog.my.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 视频分享内容数据类
 * 用于序列化和反序列化分享到动态的视频内容
 */
@JsonClass(generateAdapter = true)
data class VideoShareContent(
    // 使用驼峰命名法，与JSON字段名匹配
    val subScriptId: String,       // 小程序ID
    val videoId: String,           // 视频ID
    val position: Long = 0,        // 播放位置（毫秒）
    val episodeIndex: Int? = null, // 集数（如果适用）
    val title: String,             // 视频标题
    val coverUrl: String,          // 视频封面URL
    val description: String,       // 视频描述
    val playerApiUrl: String,      // 播放器API路径
    val sharedBy: String? = null,  // 分享者ID
    val sharedAt: Long = System.currentTimeMillis() // 分享时间
)
