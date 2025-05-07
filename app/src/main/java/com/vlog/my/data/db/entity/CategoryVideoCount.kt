package com.vlog.my.data.db.entity

import androidx.room.ColumnInfo

/**
 * 分类视频数量统计
 */
data class CategoryVideoCount(
    @ColumnInfo(name = "type_id")
    val typeId: Long,
    
    @ColumnInfo(name = "count")
    val count: Int
)
