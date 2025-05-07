package com.vlog.my.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.Resource
import com.vlog.my.data.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 喜爱/收藏相关的ViewModel
 */
@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    
    // 喜爱状态
    private val _favoriteState = MutableStateFlow<FavoriteState>(FavoriteState.Initial)
    val favoriteState: StateFlow<FavoriteState> = _favoriteState.asStateFlow()
    
    // 当前内容是否已喜爱
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    /**
     * 检查是否已喜爱
     * @param contentId 内容ID
     */
    fun checkFavorite(contentId: String) {
        viewModelScope.launch {
            favoritesRepository.checkFavorite(contentId)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _favoriteState.value = FavoriteState.Loading
                        }
                        is Resource.Success -> {
                            _isFavorite.value = result.data ?: false
                            _favoriteState.value = FavoriteState.Success
                        }
                        is Resource.Error -> {
                            _favoriteState.value = FavoriteState.Error(result.message ?: "检查喜爱状态失败")
                        }
                    }
                }
        }
    }
    
    /**
     * 切换喜爱状态
     * @param contentId 内容ID
     */
    fun toggleFavorite(contentId: String) {
        viewModelScope.launch {
            _favoriteState.value = FavoriteState.Loading
            
            favoritesRepository.toggleFavorite(contentId, _isFavorite.value)
                .collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            // 已经在上面设置了Loading状态
                        }
                        is Resource.Success -> {
                            // 切换状态
                            _isFavorite.value = !_isFavorite.value
                            _favoriteState.value = FavoriteState.Success
                        }
                        is Resource.Error -> {
                            _favoriteState.value = FavoriteState.Error(result.message ?: "操作失败")
                        }
                    }
                }
        }
    }
    
    /**
     * 重置状态
     */
    fun resetState() {
        _favoriteState.value = FavoriteState.Initial
    }
    
    /**
     * 喜爱状态
     */
    sealed class FavoriteState {
        object Initial : FavoriteState()
        object Loading : FavoriteState()
        object Success : FavoriteState()
        data class Error(val message: String) : FavoriteState()
    }
}
