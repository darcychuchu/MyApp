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
 * 消息列表视图模型
 */
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {
    
    // 消息列表
    private val _messagesList = MutableStateFlow<List<Messages>>(emptyList())
    val messagesList: StateFlow<List<Messages>> = _messagesList.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadMessages()
    }
    
    /**
     * 加载消息列表
     */
    fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("MessagesViewModel", "开始加载消息列表")
                val messages = messageRepository.getReceivedMessages()
                
                Log.d("MessagesViewModel", "获取到消息列表: ${messages.size}个")
                messages.forEach { message ->
                    Log.d("MessagesViewModel", "消息: id=${message.id}, subject=${message.subject}, from=${message.createdBy}, to=${message.recipientBy}")
                }
                
                _messagesList.value = messages
            } catch (e: Exception) {
                Log.e("MessagesViewModel", "加载消息列表失败", e)
                _error.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 刷新消息列表
     */
    fun refresh() {
        loadMessages()
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
}
