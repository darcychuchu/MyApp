package com.vlog.my.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 接收到的小程序实体
 * 用于在Room数据库中存储接收到的小程序包
 */
@Entity(tableName = "received_subscripts")
data class ReceivedSubScriptEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val createdAt: Long,
    val receivedAt: Long = System.currentTimeMillis(),
    val version: Int,
    val isEditable: Boolean,
    val isShareable: Boolean,
    val packageData: String  // 存储序列化的SubScriptPackage
)
