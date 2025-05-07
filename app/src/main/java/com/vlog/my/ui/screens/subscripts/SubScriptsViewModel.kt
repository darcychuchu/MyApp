package com.vlog.my.ui.screens.subscripts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.repository.SubScriptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SubScriptsViewModel @Inject constructor(
    private val repository: SubScriptRepository
) : ViewModel() {

    val subScripts: Flow<List<SubScripts>> = repository.getAllSubScripts()

    // 缓存所有小程序，用于快速查找
    private val _cachedSubScripts = MutableStateFlow<List<SubScripts>>(emptyList())
    val cachedSubScripts: StateFlow<List<SubScripts>> = _cachedSubScripts

    init {
        // 初始化时加载所有小程序到缓存
        viewModelScope.launch {
            repository.getAllSubScripts().collect { scripts ->
                _cachedSubScripts.value = scripts
            }
        }
    }

    // 根据ID获取小程序
    fun getSubScriptById(id: String): SubScripts? {
        return _cachedSubScripts.value.find { it.id == id }
    }

    // 添加新小程序
    fun addSubScript(title: String, isTyped: Int, subUrl: String, subKey: String, createdBy: String? = null) {
        val newSubScript = SubScripts(
            id = UUID.randomUUID().toString(),
            title = title,
            isTyped = isTyped,
            createdBy = createdBy,
            subUrl = subUrl,
            subKey = subKey
        )

        viewModelScope.launch {
            repository.insertSubScript(newSubScript)
        }
    }

    // 更新小程序
    fun updateSubScript(subScript: SubScripts) {
        viewModelScope.launch {
            repository.updateSubScript(subScript)
        }
    }

    // 同步小程序到服务器
    fun syncSubScript(token: String, subScript: SubScripts) {
        viewModelScope.launch {
            repository.syncSubScript(token, subScript)
        }
    }

    // 删除小程序
    fun deleteSubScript(subScript: SubScripts) {
        viewModelScope.launch {
            repository.deleteSubScript(subScript)
        }
    }

    // 加载所有小程序（用于强制刷新）
    fun loadAllSubScripts() {
        viewModelScope.launch {
            val scripts = repository.getAllSubScripts().first()
            _cachedSubScripts.value = scripts
        }
    }
}
