package com.vlog.my.data.model.ebook

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 电子书章节实体
 */
@Entity(
    tableName = "ebook_chapters",
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
data class EbookChapterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ebookId: String,              // 电子书ID
    val chapterIndex: Int,            // 章节索引
    val title: String? = null,        // 章节标题
    val startPosition: Int,           // 章节开始位置
    val endPosition: Int,             // 章节结束位置
    val content: String               // 章节内容
)
