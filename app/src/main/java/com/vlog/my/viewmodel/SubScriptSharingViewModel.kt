package com.vlog.my.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.SubScriptMessage
import com.vlog.my.data.model.SubScriptPackage
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.repository.MessageRepository
import com.vlog.my.data.repository.SubScriptPackageRepository
import com.vlog.my.data.repository.UserDataRepository
import com.vlog.my.utils.SubScriptPackager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 小程序分享ViewModel
 * 管理小程序分享相关的UI状态
 */
@HiltViewModel
class SubScriptSharingViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val subScriptPackageRepository: SubScriptPackageRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // 接收到的小程序包
    private val _receivedPackages = MutableStateFlow<List<SubScriptPackage>>(emptyList())
    val receivedPackages: StateFlow<List<SubScriptPackage>> = _receivedPackages.asStateFlow()

    // 接收到的消息
    private val _receivedMessages = MutableStateFlow<List<SubScriptMessage>>(emptyList())
    val receivedMessages: StateFlow<List<SubScriptMessage>> = _receivedMessages.asStateFlow()

    // 当前选中的消息
    private val _selectedMessage = MutableStateFlow<SubScriptMessage?>(null)
    val selectedMessage: StateFlow<SubScriptMessage?> = _selectedMessage.asStateFlow()

    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReceivedPackages()
        loadReceivedMessages()
    }

    /**
     * 加载接收到的小程序包
     */
    private fun loadReceivedPackages() {
        viewModelScope.launch {
            subScriptPackageRepository.getAllReceivedSubScriptPackages()
                .collectLatest { packages ->
                    _receivedPackages.value = packages
                }
        }
    }

    /**
     * 加载接收到的消息
     */
    private fun loadReceivedMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val messages = messageRepository.getReceivedSubScriptMessages()
                _receivedMessages.value = messages
            } catch (e: Exception) {
                _error.value = "加载消息失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 选择消息
     * @param messageId 消息ID
     */
    fun selectMessage(messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 先从缓存中查找
                val cachedMessage = messageRepository.getMessageById(messageId)
                if (cachedMessage != null) {
                    _selectedMessage.value = cachedMessage
                } else {
                    // 如果缓存中没有，则从API获取
                    val message = messageRepository.getSubScriptMessageDetail(messageId)
                    _selectedMessage.value = message
                }
            } catch (e: Exception) {
                _error.value = "加载消息详情失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 接受消息
     * @param messageId 消息ID
     */
    fun acceptMessage(messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // 获取消息详情
            val message = _selectedMessage.value ?: messageRepository.getSubScriptMessageDetail(messageId)

            if (message == null) {
                _error.value = "消息不存在"
                _isLoading.value = false
                return@launch
            }

            val currentUser = userDataRepository.getCurrentUser() ?: run {
                _error.value = "用户未登录"
                _isLoading.value = false
                return@launch
            }

            // 检查积分
            if (message.pointsCost > 0) {
                val userPoints = currentUser.points ?: 0
                if (userPoints < message.pointsCost) {
                    _error.value = "积分不足，需要${message.pointsCost}积分"
                    _isLoading.value = false
                    return@launch
                }
            }

            try {
                // 解密小程序包
                val result = SubScriptPackager.decryptAndUnpack(
                    message.encryptedPackage,
                    currentUser.id ?: ""
                )

                if (result.isSuccess) {
                    // 保存小程序包
                    val subScriptPackage = result.getOrThrow()
                    subScriptPackageRepository.saveSubScriptPackage(subScriptPackage)

                    // 扣除积分
                    if (message.pointsCost > 0) {
                        userDataRepository.deductPoints(message.pointsCost)
                    }

                    // 标记消息为已读
                    messageRepository.markMessageAsRead(messageId)

                    // 删除消息
                    messageRepository.deleteMessage(messageId)

                    // 刷新消息列表
                    loadReceivedMessages()
                } else {
                    _error.value = "解密失败: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "处理失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 拒绝消息
     * @param messageId 消息ID
     */
    fun declineMessage(messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                messageRepository.deleteMessage(messageId)
                _selectedMessage.value = null
                loadReceivedMessages()
            } catch (e: Exception) {
                _error.value = "删除消息失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 创建并发送小程序消息
     * @param subScript 小程序
     * @param recipientId 接收者ID
     * @param title 标题
     * @param description 描述
     * @param pointsCost 积分成本
     * @param isEditable 是否可编辑
     * @param isShareable 是否可分享
     */
    fun createAndSendMessage(
        subScript: SubScripts,
        recipientId: String,
        title: String,
        description: String,
        pointsCost: Int = 0,
        isEditable: Boolean = false,
        isShareable: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val currentUser = userDataRepository.getCurrentUser() ?: run {
                _error.value = "用户未登录"
                _isLoading.value = false
                return@launch
            }

            // 创建小程序包
            val subScriptPackage = SubScriptPackage(
                title = title,
                description = description,
                creatorId = currentUser.id ?: "",
                creatorName = currentUser.nickName ?: "",
                isEditable = isEditable,
                isShareable = isShareable,
                subScript = subScript
            )

            try {
                // 发送消息
                val success = messageRepository.createAndSendMessage(
                    subScriptPackage,
                    recipientId,
                    pointsCost
                )

                if (success) {
                    // 发送成功
                } else {
                    _error.value = "发送失败"
                }
            } catch (e: Exception) {
                _error.value = "发送失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 删除小程序包
     * @param packageId 小程序包ID
     */
    fun deletePackage(packageId: String) {
        viewModelScope.launch {
            subScriptPackageRepository.deleteSubScriptPackage(packageId)
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }
}
