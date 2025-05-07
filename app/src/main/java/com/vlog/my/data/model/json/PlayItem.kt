package com.vlog.my.data.model.json

/**
 * 播放项
 * @param title 标题（如：第1集）
 * @param playUrl 播放URL
 */
data class PlayItem(
    val title: String,
    val playUrl: String
)

/**
 * 解析播放URL
 * 格式：vod_play_url = "第1集$http://example.com/1.m3u8#第2集$http://example.com/2.m3u8"
 * 或者：vod_play_url = "线路1$$$第1集$http://example.com/1.m3u8#第2集$http://example.com/2.m3u8"
 * @return 播放项列表
 */
fun String?.parsePlayUrl(): List<PlayItem> {
    if (this.isNullOrEmpty()) {
        return emptyList()
    }
    
    // 1. 先使用 $$$ 分割，取第一个元素
    val mainPart = this.split("$$$").firstOrNull() ?: return emptyList()
    
    // 2. 使用 # 分割获取每个播放项
    return mainPart.split("#").mapNotNull { item ->
        // 3. 使用 $ 分割获取标题和播放URL
        val parts = item.split("$", limit = 2)
        if (parts.size == 2) {
            PlayItem(
                title = parts[0].trim(),
                playUrl = parts[1].trim()
            )
        } else {
            null
        }
    }
}
