package com.vlog.my.data.repository

import android.util.Log
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.api.MessageService
import com.vlog.my.data.model.Messages
import com.vlog.my.data.model.MessageTypes
import com.vlog.my.data.model.SubScriptMessage
import com.vlog.my.data.model.SubScriptPackage
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.utils.SubScriptPackager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 消息仓库
 * 管理小程序消息的发送和接收
 */
@Singleton
class MessageRepository @Inject constructor(
    private val messageService: MessageService,
    private val userSessionManager: UserSessionManager
) {
    // 使用内存中的消息列表缓存最近的消息
    private val _messages = MutableStateFlow<List<SubScriptMessage>>(emptyList())
    val messages: StateFlow<List<SubScriptMessage>> = _messages.asStateFlow()

    /**
     * 获取接收到的消息
     * @return 消息列表
     */
    suspend fun getReceivedMessages(): List<Messages> {
        val userName = userSessionManager.getUserName() ?: return emptyList()
        val token = userSessionManager.getAccessToken() ?: return emptyList()

        val response = messageService.getMessages(userName, token)
        return if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
            response.data
        } else {
            emptyList()
        }
    }

    /**
     * 获取接收到的小程序消息
     * @return 小程序消息列表
     */
    suspend fun getReceivedSubScriptMessages(): List<SubScriptMessage> {
        val messages = getReceivedMessages()

        // 过滤出小程序分享消息
        val subScriptMessages = messages.filter { it.isTyped == MessageTypes.SUBSCRIPT }
            .mapNotNull { message ->
                try {
                    // 解析消息内容
                    val content = JSONObject(message.description ?: "")
                    val subScriptInfo = content.getJSONObject("subScriptInfo")

                    SubScriptMessage(
                        id = message.id ?: "",
                        senderId = message.createdBy ?: "",
                        senderName = subScriptInfo.getString("creatorName"),
                        recipientId = message.recipientBy ?: "",
                        sentAt = message.createdAt ?: System.currentTimeMillis(),
                        title = subScriptInfo.getString("title"),
                        description = subScriptInfo.getString("description"),
                        encryptedPackage = content.getString("encryptedPackage"),
                        pointsCost = content.getInt("pointsCost")
                    )
                } catch (e: Exception) {
                    null
                }
            }

        // 更新内存缓存
        _messages.value = subScriptMessages

        return subScriptMessages
    }

    /**
     * 发送消息
     * @param recipientId 接收者ID
     * @param subject 主题
     * @param description 内容
     * @param isTyped 消息类型
     * @return 是否发送成功
     */
    suspend fun sendMessage(
        recipientId: String,
        subject: String,
        description: String,
        isTyped: Int = MessageTypes.NORMAL
    ): Boolean {
        Log.d("MessageRepository", "===== 开始发送消息 =====")
        Log.d("MessageRepository", "发送消息参数: recipientId=$recipientId, subject=$subject, isTyped=$isTyped")

        val userName = userSessionManager.getUserName()
        if (userName == null) {
            Log.e("MessageRepository", "发送失败: 用户未登录")
            return false
        }

        val token = userSessionManager.getAccessToken()
        if (token == null) {
            Log.e("MessageRepository", "发送失败: 无访问令牌")
            return false
        }

        Log.d("MessageRepository", "认证信息: userName=$userName, token=${token.take(15)}...")

        return try {
            Log.d("MessageRepository", "===== 发送API请求 =====")
            Log.d("MessageRepository", "API端点: ${userName}/messages-composed")
            Log.d("MessageRepository", "请求参数:")
            Log.d("MessageRepository", "- token: ${token.take(15)}...")
            Log.d("MessageRepository", "- recipientBy: $recipientId")
            Log.d("MessageRepository", "- subject: $subject")
            Log.d("MessageRepository", "- description: ${if (description.length > 50) description.take(50) + "..." else description}")

            val response = messageService.sendMessage(
                name = userName,
                token = token,
                recipientBy = recipientId,
                subject = subject,
                description = description
            )

            Log.d("MessageRepository", "===== 收到API响应 =====")
            Log.d("MessageRepository", "响应代码: ${response.code}")
            Log.d("MessageRepository", "响应消息: ${response.message ?: "无"}")
            Log.d("MessageRepository", "响应数据: ${response.data}")

            val success = response.code == ApiResponseCode.SUCCESS
            Log.d("MessageRepository", "发送结果: $success (code ${response.code} == ${ApiResponseCode.SUCCESS})")
            success
        } catch (e: Exception) {
            Log.e("MessageRepository", "===== 发送异常 =====")
            Log.e("MessageRepository", "异常类型: ${e.javaClass.simpleName}")
            Log.e("MessageRepository", "异常消息: ${e.message}")
            Log.e("MessageRepository", "异常堆栈:", e)
            false
        }
    }

    /**
     * 获取指定ID的消息
     * @param messageId 消息ID
     * @return 消息，如果不存在则返回null
     */
    fun getMessageById(messageId: String): SubScriptMessage? {
        return _messages.value.find { it.id == messageId }
    }

    /**
     * 获取消息详情
     * @param messageId 消息ID
     * @return 消息详情
     */
    suspend fun getMessageDetail(messageId: String): Messages? {
        val userName = userSessionManager.getUserName() ?: return null
        val token = userSessionManager.getAccessToken() ?: return null

        val response = messageService.getMessageDetail(userName, messageId, token)
        return if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
            response.data
        } else {
            null
        }
    }

    /**
     * 获取小程序消息详情
     * @param messageId 消息ID
     * @return 小程序消息详情
     */
    suspend fun getSubScriptMessageDetail(messageId: String): SubScriptMessage? {
        // 先从缓存中查找
        val cachedMessage = getMessageById(messageId)
        if (cachedMessage != null) {
            return cachedMessage
        }

        // 如果缓存中没有，则从API获取
        val message = getMessageDetail(messageId) ?: return null

        if (message.isTyped != MessageTypes.SUBSCRIPT) {
            return null
        }

        return try {
            // 解析消息内容
            val content = JSONObject(message.description ?: "")
            val subScriptInfo = content.getJSONObject("subScriptInfo")

            SubScriptMessage(
                id = message.id ?: "",
                senderId = message.createdBy ?: "",
                senderName = subScriptInfo.getString("creatorName"),
                recipientId = message.recipientBy ?: "",
                sentAt = message.createdAt ?: System.currentTimeMillis(),
                title = subScriptInfo.getString("title"),
                description = subScriptInfo.getString("description"),
                encryptedPackage = content.getString("encryptedPackage"),
                pointsCost = content.getInt("pointsCost")
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return 是否标记成功
     */
    suspend fun markMessageAsRead(messageId: String): Boolean {
        val userName = userSessionManager.getUserName() ?: return false
        val token = userSessionManager.getAccessToken() ?: return false

        val message = Messages(
            id = messageId,
            recipientRead = 1
        )

        val response = messageService.replyMessage(userName, messageId, token, message)
        return response.code == ApiResponseCode.SUCCESS
    }

    /**
     * 删除消息
     * @param messageId 消息ID
     * @return 是否删除成功
     */
    suspend fun deleteMessage(messageId: String): Boolean {
        val userName = userSessionManager.getUserName() ?: return false
        val token = userSessionManager.getAccessToken() ?: return false

        // 假设我们可以通过回复API来删除消息
        val message = Messages(
            id = messageId,
            isTyped = -1 // 假设-1表示删除
        )

        val response = messageService.replyMessage(userName, messageId, token, message)

        // 如果删除成功，也从内存缓存中删除
        if (response.code == ApiResponseCode.SUCCESS) {
            val currentMessages = _messages.value.toMutableList()
            currentMessages.removeIf { it.id == messageId }
            _messages.value = currentMessages
        }

        return response.code == ApiResponseCode.SUCCESS
    }

    /**
     * 创建并发送小程序消息
     * @param subScriptPackage 小程序包
     * @param recipientId 接收者ID
     * @param pointsCost 积分成本
     * @return 是否发送成功
     */
    suspend fun createAndSendMessage(
        subScriptPackage: SubScriptPackage,
        recipientId: String,
        pointsCost: Int = 0
    ): Boolean {
        try {
            // 加密小程序包
            val encryptedPackage = SubScriptPackager.packAndEncrypt(
                subScriptPackage,
                recipientId
            )

            // 创建消息内容
            val messageContent = JSONObject().apply {
                put("type", "subscript")
                put("encryptedPackage", encryptedPackage)
                put("pointsCost", pointsCost)
                put("subScriptInfo", JSONObject().apply {
                    put("title", subScriptPackage.title)
                    put("description", subScriptPackage.description)
                    put("creatorName", subScriptPackage.creatorName)
                })
            }.toString()

            // 发送消息
            return sendMessage(
                recipientId = recipientId,
                subject = "小程序分享: ${subScriptPackage.title}",
                description = messageContent,
                isTyped = MessageTypes.SUBSCRIPT
            )
        } catch (e: Exception) {
            return false
        }
    }
}
