package com.vlog.my.data.model.ebook

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 电子书实体
 */
@Entity(tableName = "ebooks")
data class EbookEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,                // 书名
    val author: String? = null,       // 作者
    val coverPath: String? = null,    // 封面图片路径
    val filePath: String,             // 文件路径
    val fileSize: Long,               // 文件大小
    val totalChapters: Int,           // 总章节数
    val lastReadChapter: Int = 0,     // 上次阅读的章节
    val lastReadPosition: Int = 0,    // 上次阅读的位置
    val importDate: Long = System.currentTimeMillis(), // 导入日期
    val lastReadDate: Long? = null,   // 上次阅读日期
    val createdBy: String? = null,    // 创建者
    val isShared: Boolean = false     // 是否是分享的电子书
)
