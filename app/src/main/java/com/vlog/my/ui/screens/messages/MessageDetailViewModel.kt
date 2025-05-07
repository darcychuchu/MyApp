package com.vlog.my.ui.screens.messages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.Messages
import com.vlog.my.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 消息详情视图模型
 */
@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    // 消息详情
    private val _message = MutableStateFlow<Messages?>(null)
    val message: StateFlow<Messages?> = _message.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * 加载消息详情
     * @param messageId 消息ID
     */
    fun loadMessageDetail(messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                Log.d("MessageDetailViewModel", "开始加载消息详情: messageId=$messageId")
                val messageDetail = messageRepository.getMessageDetail(messageId)

                if (messageDetail != null) {
                    Log.d("MessageDetailViewModel", "获取到消息详情: id=${messageDetail.id}, subject=${messageDetail.subject}")
                    _message.value = messageDetail

                    // 尝试标记消息为已读，但不影响显示
                    try {
                        messageRepository.markMessageAsRead(messageId)
                    } catch (e: Exception) {
                        Log.e("MessageDetailViewModel", "标记消息为已读失败", e)
                        // 不影响消息显示，所以不设置错误状态
                    }
                } else {
                    Log.e("MessageDetailViewModel", "消息不存在: messageId=$messageId")
                    _error.value = "消息不存在或已被删除"
                }
            } catch (e: Exception) {
                Log.e("MessageDetailViewModel", "加载消息详情失败", e)
                _error.value = "加载失败: ${e.message}"
            } finally {
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
}
