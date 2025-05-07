package com.vlog.my.data.model

import java.util.UUID

/**
 * 小程序消息数据模型
 * 用于在用户之间传递加密的小程序包
 */
data class SubScriptMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val sentAt: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val encryptedPackage: String,  // 加密后的小程序包
    val pointsCost: Int = 0  // 积分成本，0表示免费
)
