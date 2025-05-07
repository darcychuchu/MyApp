package com.vlog.my.ui.screens.subscripts.enhanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.EnhancedJsonConfig
import com.vlog.my.data.repository.EnhancedJsonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 增强型JSON配置ViewModel
 */
@HiltViewModel
class EnhancedJsonViewModel @Inject constructor(
    private val enhancedJsonRepository: EnhancedJsonRepository
) : ViewModel() {

    // 配置列表状态
    private val _configsState = MutableStateFlow<ConfigsState>(ConfigsState.Loading)
    val configsState: StateFlow<ConfigsState> = _configsState.asStateFlow()

    // 当前选中的配置
    private val _selectedConfig = MutableStateFlow<EnhancedJsonConfig?>(null)
    val selectedConfig: StateFlow<EnhancedJsonConfig?> = _selectedConfig.asStateFlow()

    // 操作状态
    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()



    /**
     * 加载所有配置
     */
    fun loadAllConfigs() {
        viewModelScope.launch {
            enhancedJsonRepository.getAllConfigs()
                .catch { e ->
                    _configsState.value = ConfigsState.Error(e.message ?: "加载配置失败")
                }
                .collect { configs ->
                    _configsState.value = if (configs.isEmpty()) {
                        ConfigsState.Empty
                    } else {
                        ConfigsState.Success(configs)
                    }
                }
        }
    }

    /**
     * 加载指定ID的配置
     */
    fun loadConfigById(id: String) {
        viewModelScope.launch {
            try {
                val config = enhancedJsonRepository.getConfigById(id)
                if (config != null) {
                    _selectedConfig.value = config
                } else {
                    _operationState.value = OperationState.Error("找不到指定的配置")
                }
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "加载配置失败")
            }
        }
    }

    /**
     * 保存配置
     */
    fun saveConfig(config: EnhancedJsonConfig) {
        viewModelScope.launch {
            try {
                enhancedJsonRepository.saveConfig(config)
                _operationState.value = OperationState.Success("配置保存成功")
                loadAllConfigs()
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "保存配置失败")
            }
        }
    }

    /**
     * 更新配置
     */
    fun updateConfig(config: EnhancedJsonConfig) {
        viewModelScope.launch {
            try {
                enhancedJsonRepository.updateConfig(config)
                _operationState.value = OperationState.Success("配置更新成功")
                _selectedConfig.value = config
                loadAllConfigs()
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "更新配置失败")
            }
        }
    }

    /**
     * 删除配置
     */
    fun deleteConfig(config: EnhancedJsonConfig) {
        viewModelScope.launch {
            try {
                enhancedJsonRepository.deleteConfig(config)
                _operationState.value = OperationState.Success("配置删除成功")
                if (_selectedConfig.value?.id == config.id) {
                    _selectedConfig.value = null
                }
                loadAllConfigs()
            } catch (e: Exception) {
                _operationState.value = OperationState.Error(e.message ?: "删除配置失败")
            }
        }
    }

    /**
     * 选择配置
     */
    fun selectConfig(config: EnhancedJsonConfig) {
        _selectedConfig.value = config
    }

    /**
     * 清除选择
     */
    fun clearSelection() {
        _selectedConfig.value = null
    }

    /**
     * 重置操作状态
     */
    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    /**
     * 配置列表状态
     */
    sealed class ConfigsState {
        object Loading : ConfigsState()
        object Empty : ConfigsState()
        data class Success(val configs: List<EnhancedJsonConfig>) : ConfigsState()
        data class Error(val message: String) : ConfigsState()
    }

    /**
     * 操作状态
     */
    sealed class OperationState {
        object Idle : OperationState()
        data class Success(val message: String) : OperationState()
        data class Error(val message: String) : OperationState()
    }
}
