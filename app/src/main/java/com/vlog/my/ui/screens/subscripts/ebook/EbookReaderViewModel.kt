package com.vlog.my.ui.screens.subscripts.ebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import com.vlog.my.data.model.ebook.EbookChapterEntity
import com.vlog.my.data.model.ebook.EbookEntity
import com.vlog.my.data.repository.EbookRepository
import com.vlog.my.data.repository.EbookShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 电子书阅读器界面状态
 */
sealed class EbookReaderUiState {
    object Loading : EbookReaderUiState()
    data class Success(
        val ebook: EbookEntity,
        val chapters: List<EbookChapterEntity>,
        val currentChapter: EbookChapterEntity,
        val currentPosition: Int,
        val bookmarks: List<EbookBookmarkEntity>,
        val isCurrentPositionBookmarked: Boolean
    ) : EbookReaderUiState()
    data class Error(val message: String) : EbookReaderUiState()
}

/**
 * 电子书阅读器ViewModel
 */
@HiltViewModel
class EbookReaderViewModel @Inject constructor(
    private val repository: EbookRepository,
    private val shareRepository: EbookShareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EbookReaderUiState>(EbookReaderUiState.Loading)
    val uiState: StateFlow<EbookReaderUiState> = _uiState.asStateFlow()

    private val _shareState = MutableStateFlow<ShareState>(ShareState.Idle)
    val shareState: StateFlow<ShareState> = _shareState.asStateFlow()

    private var ebookId: String? = null
    private var currentChapterIndex: Int = 0
    private var currentPosition: Int = 0

    /**
     * 分享状态
     */
    sealed class ShareState {
        object Idle : ShareState()
        object Loading : ShareState()
        data class Success(val message: String) : ShareState()
        data class Error(val message: String) : ShareState()
    }

    /**
     * 加载电子书
     */
    fun loadEbook(id: String) {
        ebookId = id
        viewModelScope.launch {
            _uiState.value = EbookReaderUiState.Loading
            try {
                // 获取电子书信息
                val ebook = repository.getEbookById(id) ?: throw Exception("电子书不存在")

                // 获取章节列表
                val chapters = repository.getChaptersByEbookId(id).first()
                if (chapters.isEmpty()) {
                    throw Exception("电子书没有章节")
                }

                // 获取当前章节
                currentChapterIndex = ebook.lastReadChapter
                currentPosition = ebook.lastReadPosition

                // 确保章节索引有效
                if (currentChapterIndex >= chapters.size) {
                    currentChapterIndex = 0
                    currentPosition = 0
                }

                // 获取当前章节内容
                val currentChapter = chapters[currentChapterIndex]

                // 获取书签
                val bookmarks = repository.getBookmarksByEbookId(id).first()

                // 检查当前位置是否有书签
                val isBookmarked = bookmarks.any {
                    it.chapterIndex == currentChapterIndex && it.position == currentPosition
                }

                _uiState.value = EbookReaderUiState.Success(
                    ebook = ebook,
                    chapters = chapters,
                    currentChapter = currentChapter,
                    currentPosition = currentPosition,
                    bookmarks = bookmarks,
                    isCurrentPositionBookmarked = isBookmarked
                )
            } catch (e: Exception) {
                _uiState.value = EbookReaderUiState.Error(e.message ?: "加载电子书失败")
            }
        }
    }

    /**
     * 加载指定章节
     */
    fun loadChapter(chapterIndex: Int) {
        val id = ebookId ?: return
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is EbookReaderUiState.Success) {
                    // 确保章节索引有效
                    if (chapterIndex < 0 || chapterIndex >= currentState.chapters.size) {
                        return@launch
                    }

                    // 更新当前章节
                    currentChapterIndex = chapterIndex
                    currentPosition = 0

                    // 获取章节内容
                    val chapter = currentState.chapters[chapterIndex]

                    // 更新阅读进度
                    repository.updateReadProgress(id, chapterIndex, 0)

                    // 检查当前位置是否有书签
                    val isBookmarked = currentState.bookmarks.any {
                        it.chapterIndex == chapterIndex && it.position == 0
                    }

                    _uiState.value = currentState.copy(
                        currentChapter = chapter,
                        currentPosition = 0,
                        isCurrentPositionBookmarked = isBookmarked
                    )
                }
            } catch (e: Exception) {
                _uiState.value = EbookReaderUiState.Error(e.message ?: "加载章节失败")
            }
        }
    }

    /**
     * 更新阅读位置
     */
    fun updatePosition(position: Int) {
        val id = ebookId ?: return
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is EbookReaderUiState.Success) {
                    // 更新当前位置
                    currentPosition = position

                    // 更新阅读进度
                    repository.updateReadProgress(id, currentChapterIndex, position)

                    // 检查当前位置是否有书签
                    val isBookmarked = currentState.bookmarks.any {
                        it.chapterIndex == currentChapterIndex && it.position == position
                    }

                    _uiState.value = currentState.copy(
                        currentPosition = position,
                        isCurrentPositionBookmarked = isBookmarked
                    )
                }
            } catch (e: Exception) {
                // 忽略更新位置的错误
            }
        }
    }

    /**
     * 切换书签
     */
    fun toggleBookmark() {
        val id = ebookId ?: return
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is EbookReaderUiState.Success) {
                    val chapter = currentState.currentChapter

                    // 检查当前位置是否有书签
                    val existingBookmark = currentState.bookmarks.find {
                        it.chapterIndex == currentChapterIndex && it.position == currentPosition
                    }

                    if (existingBookmark != null) {
                        // 删除书签
                        repository.deleteBookmark(existingBookmark)

                        // 更新状态
                        _uiState.value = currentState.copy(
                            bookmarks = currentState.bookmarks - existingBookmark,
                            isCurrentPositionBookmarked = false
                        )
                    } else {
                        // 获取当前文本
                        val text = getTextAroundPosition(chapter.content, currentPosition)

                        // 添加书签
                        val bookmark = EbookBookmarkEntity(
                            ebookId = id,
                            chapterIndex = currentChapterIndex,
                            position = currentPosition,
                            text = text
                        )
                        repository.addBookmark(bookmark)

                        // 更新状态
                        _uiState.value = currentState.copy(
                            bookmarks = currentState.bookmarks + bookmark,
                            isCurrentPositionBookmarked = true
                        )
                    }
                }
            } catch (e: Exception) {
                // 忽略切换书签的错误
            }
        }
    }

    /**
     * 删除书签
     */
    fun deleteBookmark(bookmark: EbookBookmarkEntity) {
        viewModelScope.launch {
            try {
                repository.deleteBookmark(bookmark)

                val currentState = _uiState.value
                if (currentState is EbookReaderUiState.Success) {
                    // 更新状态
                    _uiState.value = currentState.copy(
                        bookmarks = currentState.bookmarks - bookmark,
                        isCurrentPositionBookmarked = currentState.isCurrentPositionBookmarked &&
                                !(bookmark.chapterIndex == currentChapterIndex && bookmark.position == currentPosition)
                    )
                }
            } catch (e: Exception) {
                // 忽略删除书签的错误
            }
        }
    }

    /**
     * 获取位置周围的文本
     */
    private fun getTextAroundPosition(content: String, position: Int): String {
        val start = maxOf(0, position - 20)
        val end = minOf(content.length, position + 20)
        return content.substring(start, end)
    }

    /**
     * 分享电子书到动态
     */
    fun shareEbookToFeed(comment: String = "") {
        val currentState = _uiState.value
        if (currentState !is EbookReaderUiState.Success) {
            _shareState.value = ShareState.Error("电子书未加载")
            return
        }

        viewModelScope.launch {
            _shareState.value = ShareState.Loading

            try {
                val result = shareRepository.shareEbookToFeed(currentState.ebook, comment)

                if (result.isSuccess) {
                    _shareState.value = ShareState.Success(result.getOrDefault("分享成功"))
                } else {
                    _shareState.value = ShareState.Error(result.exceptionOrNull()?.message ?: "分享失败")
                }
            } catch (e: Exception) {
                _shareState.value = ShareState.Error(e.message ?: "分享失败")
            }
        }
    }
}
