package com.vlog.my.data.model.ebook

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 电子书书签实体
 */
@Entity(
    tableName = "ebook_bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = EbookEntity::class,
            parentColumns = ["id"],
            childColumns = ["ebookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ebookId")]
)
data class EbookBookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ebookId: String,              // 电子书ID
    val chapterIndex: Int,            // 章节索引
    val position: Int,                // 位置
    val text: String,                 // 书签文本
    val note: String? = null,         // 笔记
    val createDate: Long = System.currentTimeMillis() // 创建日期
)
