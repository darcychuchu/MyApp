package com.vlog.my.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vlog.my.data.model.json.VideoItem

/**
 * 视频数据库实体
 */
@Entity(
    tableName = "videos",
    primaryKeys = ["vod_id", "sub_url"] // 复合主键，确保每个小程序下的视频ID唯一
)
data class VideoEntity(
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
    val vod_duration: Int?,   // 时长（分钟）
    val sub_url: String       // 所属小程序URL，用于区分不同小程序的视频
) {
    /**
     * 转换为VideoItem模型
     */
    fun toVideoItem(): VideoItem {
        return VideoItem(
            vod_id = vod_id,
            vod_name = vod_name,
            vod_sub = vod_sub,
            vod_remarks = vod_remarks,
            vod_serial = vod_serial,
            type_id = type_id,
            vod_actor = vod_actor,
            vod_director = vod_director,
            vod_pic = vod_pic,
            vod_content = vod_content,
            vod_time = vod_time,
            vod_area = vod_area,
            vod_lang = vod_lang,
            vod_year = vod_year,
            type_name = type_name,
            vod_tag = vod_tag,
            vod_play_url = vod_play_url,
            vod_duration = vod_duration
        )
    }

    companion object {
        /**
         * 从VideoItem模型创建实体
         */
        fun fromVideoItem(videoItem: VideoItem, subUrl: String): VideoEntity {
            return VideoEntity(
                vod_id = videoItem.vod_id,
                vod_name = videoItem.vod_name,
                vod_sub = videoItem.vod_sub,
                vod_remarks = videoItem.vod_remarks,
                vod_serial = videoItem.vod_serial,
                type_id = videoItem.type_id,
                vod_actor = videoItem.vod_actor,
                vod_director = videoItem.vod_director,
                vod_pic = videoItem.vod_pic,
                vod_content = videoItem.vod_content,
                vod_time = videoItem.vod_time,
                vod_area = videoItem.vod_area,
                vod_lang = videoItem.vod_lang,
                vod_year = videoItem.vod_year,
                type_name = videoItem.type_name,
                vod_tag = videoItem.vod_tag,
                vod_play_url = videoItem.vod_play_url,
                vod_duration = videoItem.vod_duration,
                sub_url = subUrl
            )
        }
    }
}
