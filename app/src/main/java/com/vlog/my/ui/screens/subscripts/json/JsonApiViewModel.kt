package com.vlog.my.ui.screens.subscripts.json

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.Category
import com.vlog.my.data.model.json.JsonApiResponse
import com.vlog.my.data.model.json.VideoItem
import com.vlog.my.data.preferences.PreferencesManager
import com.vlog.my.data.repository.CategoryRepository
import com.vlog.my.data.repository.JsonApiRepository
import com.vlog.my.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 视图类型
 */
enum class ViewType {
    LIST, // 列表视图
    GRID  // 网格视图
}

/**
 * 小程序JSON模式视图模型
 */
@HiltViewModel
class JsonApiViewModel @Inject constructor(
    private val repository: JsonApiRepository,
    private val categoryRepository: CategoryRepository,
    private val videoRepository: VideoRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // 当前小程序ID
    private var currentSubScriptId: String? = null

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 视频列表
    private val _videoList = MutableStateFlow<List<VideoItem>>(emptyList())
    val videoList: StateFlow<List<VideoItem>> = _videoList

    // 分类列表（从API获取的原始分类）
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    // 一级分类列表（从数据库获取）
    private val _parentCategories = MutableStateFlow<List<Category>>(emptyList())
    val parentCategories: StateFlow<List<Category>> = _parentCategories

    // 当前选中的一级分类的子分类列表
    private val _childCategories = MutableStateFlow<List<Category>>(emptyList())
    val childCategories: StateFlow<List<Category>> = _childCategories

    // 当前选中的一级分类
    private val _selectedParentCategory = MutableStateFlow<Category?>(null)
    val selectedParentCategory: StateFlow<Category?> = _selectedParentCategory

    // 分页信息
    private val _pagination = MutableStateFlow<PaginationInfo?>(null)
    val pagination: StateFlow<PaginationInfo?> = _pagination

    // 当前选中的视频
    private val _selectedVideo = MutableStateFlow<VideoItem?>(null)
    val selectedVideo: StateFlow<VideoItem?> = _selectedVideo

    // 当前选中的分类
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    // 当前视图类型（列表或网格）
    private val _viewType = MutableStateFlow(ViewType.LIST)
    val viewType: StateFlow<ViewType> = _viewType

    // 分类视频数量统计
    private val _categoryVideoCount = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val categoryVideoCount: StateFlow<Map<Long, Int>> = _categoryVideoCount

    /**
     * 加载视频列表
     * @param subScript 小程序配置
     * @param pg 页码
     * @param isLoadMore 是否是加载更多（如果是，则追加数据而不是替换）
     */
    fun loadVideoList(subScript: SubScripts, pg: Int = 1, isLoadMore: Boolean = false) {
        // 保存当前小程序ID
        currentSubScriptId = subScript.id

        // 加载保存的视图类型偏好
        if (!isLoadMore && pg == 1) {
            loadSavedViewType(subScript.id)
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // 使用小程序ID调用API，支持自定义解析
                val result = repository.getVideoListByPage(subScript.subUrl, pg, subScript.id)
                result.onSuccess { response ->
                    // 如果是加载更多，则追加数据，否则替换数据
                    if (isLoadMore && pg > 1) {
                        _videoList.value = _videoList.value + response.videos
                    } else {
                        _videoList.value = response.videos
                    }

                    // 保存视频到数据库
                    saveVideosToDatabase(response.videos, subScript.subUrl)

                    // 分类列表只在第一页加载
                    if (pg == 1 || _categories.value.isEmpty()) {
                        val categoriesList = response.categories
                        _categories.value = categoriesList

                        // 保存分类到数据库并处理父子关系
                        // 如果API返回的分类列表为空，将使用预定义的分类数据
                        saveCategoriesToDatabase(categoriesList, subScript.subUrl)
                    }

                    // 加载分类数据统计
                    loadCategoryVideoCount(subScript.subUrl)

                    _pagination.value = PaginationInfo(
                        currentPage = response.pageindex,
                        totalPages = response.pagecount,
                        pageSize = response.pagesize,
                        totalItems = response.recordcount ?: 0
                    )
                }.onFailure { exception ->
                    // API请求失败，显示错误信息
                    _error.value = "API请求失败: ${exception.message}"

                    // 如果是加载更多失败，不影响当前显示的数据
                    if (!isLoadMore) {
                        // 只有在初次加载时才尝试从本地数据库加载数据
                        // 根据当前选中的分类加载数据
                        val selectedCategory = _selectedCategory.value
                        if (selectedCategory != null) {
                            loadVideosByCategory(selectedCategory.type_id)
                        } else {
                            // 如果没有选中的分类，加载所有视频
                            loadVideosFromDatabase(subScript.subUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                // 发生异常，尝试从本地数据库加载数据
                _error.value = "加载失败: ${e.message}，尝试从本地加载数据..."
                loadVideosFromDatabase(subScript.subUrl)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 从本地数据库加载所有视频
     * @param subUrl 小程序URL
     */
    private fun loadVideosFromDatabase(subUrl: String) {
        viewModelScope.launch {
            try {
                videoRepository.getAllVideos(subUrl).collect { videos ->
                    if (videos.isNotEmpty()) {
                        _videoList.value = videos
                        _error.value = null
                    } else {
                        _error.value = "本地数据库中没有视频数据"
                    }
                }

                // 加载分类数据
                loadParentCategories(subUrl)
            } catch (e: Exception) {
                _error.value = "从本地数据库加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载更多视频
     * @param subScript 小程序配置
     */
    fun loadMoreVideos(subScript: SubScripts) {
        val currentPagination = _pagination.value ?: return
        val currentCategory = _selectedCategory.value

        // 如果当前页已经是最后一页，则不加载更多
        if (currentPagination.currentPage >= currentPagination.totalPages) {
            return
        }

        // 加载下一页
        val nextPage = currentPagination.currentPage + 1

        // 如果当前有选中的分类，并且不是"全部"分类，则直接从数据库加载更多该分类的视频
        if (currentCategory != null && currentCategory.type_id != 0L) {
            // 这里我们不调用API，而是直接从数据库加载更多数据
            // 因为API可能不支持按分类分页加载
            // 实际上，这里可能需要更复杂的逻辑，但为了简化问题，我们先这样处理
            _error.value = "当前分类不支持加载更多，请切换到全部分类查看更多视频"
            return
        }

        // 只有在"全部"分类下才调用API加载更多
        loadVideoList(subScript, nextPage, true)
    }



    /**
     * 保存分类到数据库并处理父子关系
     * @param categories 分类列表
     * @param subUrl 小程序URL
     */
    private fun saveCategoriesToDatabase(categories: List<Category>, subUrl: String) {
        viewModelScope.launch {
            try {
                // 保存分类到数据库
                categoryRepository.saveCategories(categories, subUrl)

                // 加载一级分类
                loadParentCategories(subUrl)
            } catch (e: Exception) {
                _error.value = "保存分类失败: ${e.message}"
            }
        }
    }

    /**
     * 加载分类
     * @param subUrl 小程序URL
     */
    private fun loadParentCategories(subUrl: String) {
        viewModelScope.launch {
            // 加载一级分类
            categoryRepository.getParentCategories(subUrl).collect { parentCats ->
                _parentCategories.value = parentCats

                // 如果有一级分类，默认选中"全部"分类
                if (parentCats.isNotEmpty() && _selectedParentCategory.value == null) {
                    // 查找"全部"分类
                    val allCategory = parentCats.find { it.type_id == 0L }
                    if (allCategory != null) {
                        selectParentCategory(allCategory)
                    } else if (parentCats.isNotEmpty()) {
                        // 如果没有"全部"分类，选择第一个分类
                        selectParentCategory(parentCats.first())
                    }
                }
            }
        }
    }

    /**
     * 选择一级分类
     * @param category 分类
     */
    fun selectParentCategory(category: Category) {
        _selectedParentCategory.value = category

        // 如果选择的是"全部"分类，直接加载所有视频
        if (category.type_id == 0L) {
            _selectedCategory.value = category
            loadVideosByCategory(category.type_id)
            // 清空二级分类
            _childCategories.value = emptyList()
            return
        }

        // 加载该一级分类下的二级分类
        loadChildCategories(category)

        // 同时也将该一级分类设为当前选中的分类，加载该分类的视频
        _selectedCategory.value = category
        loadVideosByCategory(category.type_id)
    }

    /**
     * 加载子分类
     * @param parentCategory 父分类
     */
    private fun loadChildCategories(parentCategory: Category) {
        viewModelScope.launch {
            val subUrl = repository.getLastUsedSubUrl()
            if (subUrl.isNotEmpty()) {
                categoryRepository.getChildCategories(subUrl, parentCategory.type_id).collect { childCats ->
                    _childCategories.value = childCats
                }
            }
        }
    }

    /**
     * 选择分类
     * @param category 分类
     */
    fun selectCategory(category: Category) {
        _selectedCategory.value = category

        // 从本地数据库加载该分类的视频
        loadVideosByCategory(category.type_id)
    }

    /**
     * 根据分类ID从本地数据库加载视频
     * @param typeId 分类ID
     */
    private fun loadVideosByCategory(typeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val subUrl = repository.getLastUsedSubUrl()
                if (subUrl.isNotEmpty()) {
                    // 保留分页信息，确保用户可以继续加载更多数据
                    // 如果分页信息为空，创建一个默认的分页信息
                    if (_pagination.value == null) {
                        _pagination.value = PaginationInfo(
                            currentPage = 1,
                            totalPages = 100, // 假设有100页
                            pageSize = 20,    // 假设每页20条
                            totalItems = 2000 // 假设总共2000条
                        )
                    }

                    // 根据分类ID加载视频
                    if (typeId == 0L) {
                        // 如果分类ID为0，加载所有视频
                        videoRepository.getAllVideos(subUrl).collect { videos ->
                            _videoList.value = videos
                            if (videos.isEmpty()) {
                                _error.value = "暂无视频数据，请点击刷新按钮获取最新数据"
                            } else {
                                _error.value = null
                            }
                            _isLoading.value = false
                        }
                    } else {
                        // 加载特定分类的视频
                        videoRepository.getVideosByTypeId(subUrl, typeId).collect { videos ->
                            if (videos.isEmpty()) {
                                // 如果该分类没有视频，显示明确的提示，但不自动切换到全部视频
                                _videoList.value = emptyList()
                                _error.value = "该分类暂无视频，请选择其他分类或切换到「全部」分类刷新数据"
                            } else {
                                _videoList.value = videos
                                _error.value = null
                            }
                            _isLoading.value = false
                        }
                    }
                } else {
                    _error.value = "未找到小程序URL"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "加载失败"
                _isLoading.value = false
            }
        }
    }

    /**
     * 选择视频
     * @param video 视频
     */
    fun selectVideo(video: VideoItem) {
        _selectedVideo.value = video
    }

    /**
     * 通过ID加载视频详情
     * @param videoId 视频ID
     * @param subUrl 小程序URL
     */
    fun loadVideoById(videoId: Long, subUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val video = videoRepository.getVideoById(videoId, subUrl)
                if (video != null) {
                    _selectedVideo.value = video
                    _error.value = null
                } else {
                    _error.value = "未找到视频详情"
                }
            } catch (e: Exception) {
                _error.value = "加载视频详情失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 保存视频到数据库
     * @param videos 视频列表
     * @param subUrl 小程序URL
     */
    private fun saveVideosToDatabase(videos: List<VideoItem>, subUrl: String) {
        viewModelScope.launch {
            try {
                // 保存视频到数据库，使用REPLACE策略直接覆盖相同ID的记录
                videoRepository.saveVideos(videos, subUrl)
            } catch (e: Exception) {
                // 保存失败不影响UI显示，只记录错误
                Log.e("JsonApiViewModel", "保存视频失败: ${e.message}")
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
     * 切换视图类型（列表/网格）
     */
    fun toggleViewType() {
        val newViewType = if (_viewType.value == ViewType.LIST) ViewType.GRID else ViewType.LIST
        _viewType.value = newViewType

        // 保存视图类型偏好
        currentSubScriptId?.let { subScriptId ->
            saveViewTypePreference(subScriptId, newViewType)
        }
    }

    /**
     * 设置视图类型
     * @param viewType 视图类型
     */
    fun setViewType(viewType: ViewType) {
        _viewType.value = viewType

        // 保存视图类型偏好
        currentSubScriptId?.let { subScriptId ->
            saveViewTypePreference(subScriptId, viewType)
        }
    }

    /**
     * 保存视图类型偏好
     * @param subScriptId 小程序ID
     * @param viewType 视图类型
     */
    private fun saveViewTypePreference(subScriptId: String, viewType: ViewType) {
        preferencesManager.saveSubScriptViewType(subScriptId, viewType)
    }

    /**
     * 加载保存的视图类型偏好
     * @param subScriptId 小程序ID
     */
    private fun loadSavedViewType(subScriptId: String) {
        val savedViewType = preferencesManager.getSubScriptViewType(subScriptId)
        _viewType.value = savedViewType
    }

    /**
     * 加载分类数据统计
     * @param subUrl 小程序URL
     */
    private fun loadCategoryVideoCount(subUrl: String) {
        viewModelScope.launch {
            videoRepository.getCategoryVideoCount(subUrl).collect { counts ->
                _categoryVideoCount.value = counts
            }
        }
    }
}

/**
 * 分页信息
 */
data class PaginationInfo(
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val totalItems: Int
)
