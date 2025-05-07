package com.vlog.my.ui.screens.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 发送消息视图模型
 */
@HiltViewModel
class ComposeMessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    // 接收者ID
    private val _recipientId = MutableStateFlow<String?>(null)

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 消息是否已发送
    private val _messageSent = MutableStateFlow(false)
    val messageSent: StateFlow<Boolean> = _messageSent

    /**
     * 设置接收者ID
     * @param recipientId 接收者ID
     */
    fun setRecipientId(recipientId: String) {
        _recipientId.value = recipientId
    }

    /**
     * 发送消息
     * @param subject 主题
     * @param content 内容
     */
    fun sendMessage(subject: String, content: String) {
        val recipientId = _recipientId.value ?: return

        Log.d("ComposeMessageViewModel", "===== 用户点击发送消息 =====")
        Log.d("ComposeMessageViewModel", "接收者ID: $recipientId")
        Log.d("ComposeMessageViewModel", "主题: $subject")
        Log.d("ComposeMessageViewModel", "内容长度: ${content.length}字符")

        if (_isLoading.value) {
            Log.d("ComposeMessageViewModel", "正在加载中，忽略发送请求")
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                Log.d("ComposeMessageViewModel", "===== 开始发送消息 =====")
                Log.d("ComposeMessageViewModel", "调用 messageRepository.sendMessage")
                Log.d("ComposeMessageViewModel", "参数: recipientId=$recipientId, subject=$subject, content长度=${content.length}")
                val success = messageRepository.sendMessage(
                    recipientId = recipientId,
                    subject = subject,
                    description = content
                )

                Log.d("ComposeMessageViewModel", "===== 发送完成 =====")
                Log.d("ComposeMessageViewModel", "发送结果: success=$success")

                if (success) {
                    Log.d("ComposeMessageViewModel", "发送成功，设置 _messageSent = true")
                    _messageSent.value = true
                } else {
                    Log.e("ComposeMessageViewModel", "发送失败，设置错误信息")
                    _error.value = "发送失败，请重试"
                }
            } catch (e: Exception) {
                Log.e("ComposeMessageViewModel", "发送异常: ${e.message}", e)
                // 提供更友好的错误消息
                val errorMessage = when {
                    e.message?.contains("timeout") == true -> "连接超时，请检查网络"
                    e.message?.contains("Connection") == true -> "连接失败，请检查网络"
                    else -> "发送失败，请稍后重试"
                }
                _error.value = errorMessage
            } finally {
                Log.d("ComposeMessageViewModel", "完成发送，设置 _isLoading = false")
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 重置消息发送状态
     */
    fun resetMessageSent() {
        _messageSent.value = false
    }
}
