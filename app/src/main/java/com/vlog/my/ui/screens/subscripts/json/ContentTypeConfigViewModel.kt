package com.vlog.my.ui.screens.subscripts.json

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.ContentTypeConfig
import com.vlog.my.data.parser.CustomParserService
import com.vlog.my.data.repository.SubScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 内容类型配置ViewModel
 * 管理内容类型配置的状态和操作
 */
@HiltViewModel
class ContentTypeConfigViewModel @Inject constructor(
    private val subScriptRepository: SubScriptRepository,
    val customParserService: CustomParserService
) : ViewModel() {
    
    private val _subScript = MutableStateFlow<SubScripts?>(null)
    val subScript: StateFlow<SubScripts?> = _subScript.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    /**
     * 加载小程序
     * @param subScriptId 小程序ID
     */
    fun loadSubScript(subScriptId: String) {
        viewModelScope.launch {
            val result = subScriptRepository.getSubScriptById(subScriptId)
            _subScript.value = result
        }
    }
    
    /**
     * 保存内容类型配置
     * @param subScript 更新后的小程序配置
     */
    fun saveContentTypeConfig(subScript: SubScripts) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                subScriptRepository.updateSubScript(subScript)
                
                // 清除解析器缓存，确保下次使用时重新创建
                customParserService.clearParserCache(subScript.id)
                
                _saveSuccess.value = true
            } catch (e: Exception) {
                // 处理错误
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    /**
     * 重置保存状态
     */
    fun resetSaveState() {
        _saveSuccess.value = false
    }
}
