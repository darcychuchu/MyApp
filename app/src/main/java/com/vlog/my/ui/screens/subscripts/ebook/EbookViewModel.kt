package com.vlog.my.ui.screens.subscripts.ebook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import com.vlog.my.data.model.ebook.EbookChapterEntity
import com.vlog.my.data.model.ebook.EbookEntity
import com.vlog.my.data.repository.EbookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 电子书列表界面状态
 */
sealed class EbookListUiState {
    object Loading : EbookListUiState()
    data class Success(val ebooks: List<EbookEntity>) : EbookListUiState()
    data class Error(val message: String) : EbookListUiState()
}

/**
 * 电子书导入状态
 */
sealed class EbookImportState {
    object Idle : EbookImportState()
    object Loading : EbookImportState()
    data class Success(val ebookId: String) : EbookImportState()
    data class Error(val message: String) : EbookImportState()
}

/**
 * 电子书ViewModel
 */
@HiltViewModel
class EbookViewModel @Inject constructor(
    private val repository: EbookRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<EbookListUiState>(EbookListUiState.Loading)
    val uiState: StateFlow<EbookListUiState> = _uiState.asStateFlow()
    
    private val _importState = MutableStateFlow<EbookImportState>(EbookImportState.Idle)
    val importState: StateFlow<EbookImportState> = _importState.asStateFlow()
    
    init {
        loadEbooks()
    }
    
    /**
     * 加载所有电子书
     */
    fun loadEbooks() {
        viewModelScope.launch {
            _uiState.value = EbookListUiState.Loading
            try {
                repository.getAllEbooks().collectLatest { ebooks ->
                    _uiState.value = EbookListUiState.Success(ebooks)
                }
            } catch (e: Exception) {
                _uiState.value = EbookListUiState.Error(e.message ?: "加载电子书失败")
            }
        }
    }
    
    /**
     * 导入电子书
     */
    fun importEbook(uri: Uri) {
        viewModelScope.launch {
            _importState.value = EbookImportState.Loading
            repository.importEbook(uri)
                .onSuccess { ebookId ->
                    _importState.value = EbookImportState.Success(ebookId)
                }
                .onFailure { e ->
                    _importState.value = EbookImportState.Error(e.message ?: "导入电子书失败")
                }
        }
    }
    
    /**
     * 删除电子书
     */
    fun deleteEbook(ebook: EbookEntity) {
        viewModelScope.launch {
            repository.deleteEbook(ebook)
        }
    }
    
    /**
     * 重置导入状态
     */
    fun resetImportState() {
        _importState.value = EbookImportState.Idle
    }
}
