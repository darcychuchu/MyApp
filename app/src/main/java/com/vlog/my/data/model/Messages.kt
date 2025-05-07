package com.vlog.my.data.model

/**
 * 消息模型
 */
data class Messages(
    var id: String? = null,
    var createdAt: Long? = null,
    var isTyped: Int? = null,      // 消息类型
    var version: Int? = null,
    var createdBy: String? = null, // 发送者
    var createdRead: Int? = null,  // 发送者是否已读
    var recipientBy: String? = null, // 接收者
    var recipientRead: Int? = null,  // 接收者是否已读
    var parentId: String? = null,    // 父消息ID（用于回复）
    var subject: String? = null,     // 主题
    var description: String? = null  // 内容
)

/**
 * 消息类型
 */
object MessageTypes {
    const val NORMAL = 0       // 普通消息
    const val SUBSCRIPT = 1    // 小程序分享消息
    // 其他类型...
}
