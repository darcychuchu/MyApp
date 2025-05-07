package com.vlog.my.data.model

import com.vlog.my.data.model.SubScripts
import java.util.UUID

/**
 * 小程序包数据模型
 * 用于封装小程序及其数据，支持分享和加密
 */
data class SubScriptPackage(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val creatorId: String,  // 创作者ID
    val creatorName: String,  // 创作者名称
    val createdAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val isEditable: Boolean = false,  // 接收者是否可编辑
    val isShareable: Boolean = false,  // 接收者是否可分享
    val subScript: SubScripts,  // 小程序配置
    val dataItems: List<Any> = emptyList()  // 小程序数据，可以为空
)
