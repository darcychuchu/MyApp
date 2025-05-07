package com.vlog.my.ui.screens.subscripts.ebook

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import com.vlog.my.data.model.ebook.EbookChapterEntity
import com.vlog.my.data.model.ebook.EbookEntity

/**
 * 电子书阅读器界面
 */
@Composable
fun EbookReaderScreen(
    ebookId: String,
    onNavigateBack: () -> Unit,
    viewModel: EbookReaderViewModel = hiltViewModel()
) {
    // 加载电子书
    LaunchedEffect(ebookId) {
        viewModel.loadEbook(ebookId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val shareState by viewModel.shareState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 显示分享结果
    LaunchedEffect(shareState) {
        when (shareState) {
            is EbookReaderViewModel.ShareState.Success -> {
                // 显示成功消息
                snackbarHostState.showSnackbar((shareState as EbookReaderViewModel.ShareState.Success).message)
            }
            is EbookReaderViewModel.ShareState.Error -> {
                // 显示错误消息
                snackbarHostState.showSnackbar((shareState as EbookReaderViewModel.ShareState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is EbookReaderUiState.Loading -> {
                    LoadingScreen()
                }
                is EbookReaderUiState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.loadEbook(ebookId) },
                        onBack = onNavigateBack
                    )
                }
                is EbookReaderUiState.Success -> {
                    var showShareDialog by remember { mutableStateOf(false) }

                    ReaderContent(
                        state = state,
                        onNavigateBack = onNavigateBack,
                        onChapterChange = { chapter -> viewModel.loadChapter(chapter) },
                        onPositionChange = { position -> viewModel.updatePosition(position) },
                        onToggleBookmark = { viewModel.toggleBookmark() },
                        onShareEbook = { showShareDialog = true },
                        onDeleteBookmark = { bookmark -> viewModel.deleteBookmark(bookmark) }
                    )

                    // 分享对话框
                    if (showShareDialog) {
                        ShareEbookDialog(
                            ebook = state.ebook,
                            isLoading = shareState is EbookReaderViewModel.ShareState.Loading,
                            onDismiss = { showShareDialog = false },
                            onShare = { comment ->
                                viewModel.shareEbookToFeed(comment)
                                showShareDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 加载中界面
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 错误界面
 */
@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onBack) {
                Text("返回")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 阅读器内容
 */
@Composable
fun ReaderContent(
    state: EbookReaderUiState.Success,
    onNavigateBack: () -> Unit,
    onChapterChange: (Int) -> Unit,
    onPositionChange: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onShareEbook: () -> Unit,
    onDeleteBookmark: (EbookBookmarkEntity) -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var showChapterList by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }

    BackHandler {
        if (showChapterList || showBookmarks) {
            showChapterList = false
            showBookmarks = false
        } else {
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 阅读内容
        ReaderView(
            content = state.currentChapter.content,
            initialPosition = state.currentPosition,
            onPositionChange = onPositionChange,
            onTap = { showControls = !showControls },
            modifier = Modifier.fillMaxSize()
        )

        // 控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ReaderControls(
                title = state.ebook.title,
                chapterTitle = state.currentChapter.title ?: "第${state.currentChapter.chapterIndex + 1}章",
                currentChapter = state.currentChapter.chapterIndex,
                totalChapters = state.ebook.totalChapters,
                onNavigateBack = onNavigateBack,
                onPreviousChapter = {
                    if (state.currentChapter.chapterIndex > 0) {
                        onChapterChange(state.currentChapter.chapterIndex - 1)
                    }
                },
                onNextChapter = {
                    if (state.currentChapter.chapterIndex < state.ebook.totalChapters - 1) {
                        onChapterChange(state.currentChapter.chapterIndex + 1)
                    }
                },
                onShowChapterList = { showChapterList = true },
                onShowBookmarks = { showBookmarks = true },
                onToggleBookmark = onToggleBookmark,
                onShareEbook = onShareEbook,
                isBookmarked = state.isCurrentPositionBookmarked
            )
        }

        // 章节列表
        if (showChapterList) {
            ChapterListDialog(
                chapters = state.chapters,
                currentChapter = state.currentChapter.chapterIndex,
                onChapterSelected = { chapter ->
                    onChapterChange(chapter)
                    showChapterList = false
                },
                onDismiss = { showChapterList = false }
            )
        }

        // 书签列表
        if (showBookmarks) {
            BookmarkListDialog(
                bookmarks = state.bookmarks,
                onBookmarkSelected = { bookmark ->
                    onChapterChange(bookmark.chapterIndex)
                    onPositionChange(bookmark.position)
                    showBookmarks = false
                },
                onDeleteBookmark = onDeleteBookmark,
                onDismiss = { showBookmarks = false }
            )
        }
    }
}

/**
 * 阅读视图
 */
@Composable
fun ReaderView(
    content: String,
    initialPosition: Int,
    onPositionChange: (Int) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState(initialPosition)

    LaunchedEffect(scrollState.value) {
        onPositionChange(scrollState.value)
    }

    Box(
        modifier = modifier
            .clickable(onClick = onTap)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        )
    }
}

/**
 * 阅读器控制栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderControls(
    title: String,
    chapterTitle: String,
    currentChapter: Int,
    totalChapters: Int,
    onNavigateBack: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onShowChapterList: () -> Unit,
    onShowBookmarks: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShareEbook: () -> Unit,
    isBookmarked: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 顶部控制栏
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onShowChapterList) {
                        Icon(Icons.Default.List, contentDescription = "章节列表")
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "书签"
                        )
                    }
                    IconButton(onClick = onShareEbook) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 底部控制栏
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousChapter,
                        enabled = currentChapter > 0
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一章")
                    }

                    Text(
                        text = "${currentChapter + 1}/$totalChapters",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = onNextChapter,
                        enabled = currentChapter < totalChapters - 1
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下一章")
                    }
                }
            }
        }
    }
}

/**
 * 章节列表对话框
 */
@Composable
fun ChapterListDialog(
    chapters: List<EbookChapterEntity>,
    currentChapter: Int,
    onChapterSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "章节列表",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(chapters) { chapter ->
                        ChapterItem(
                            chapter = chapter,
                            isSelected = chapter.chapterIndex == currentChapter,
                            onClick = { onChapterSelected(chapter.chapterIndex) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

/**
 * 章节项
 */
@Composable
fun ChapterItem(
    chapter: EbookChapterEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = chapter.title ?: "第${chapter.chapterIndex + 1}章",
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }

    Divider()
}

/**
 * 书签列表对话框
 */
@Composable
fun BookmarkListDialog(
    bookmarks: List<EbookBookmarkEntity>,
    onBookmarkSelected: (EbookBookmarkEntity) -> Unit,
    onDeleteBookmark: (EbookBookmarkEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var bookmarkToDelete by remember { mutableStateOf<EbookBookmarkEntity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "书签列表",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (bookmarks.isEmpty()) {
                    Text(
                        text = "没有书签",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    ) {
                        items(bookmarks) { bookmark ->
                            BookmarkItem(
                                bookmark = bookmark,
                                onClick = { onBookmarkSelected(bookmark) },
                                onDelete = { bookmarkToDelete = bookmark }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }

    // 删除确认对话框
    bookmarkToDelete?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { bookmarkToDelete = null },
            title = { Text("删除书签") },
            text = { Text("确定要删除这个书签吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBookmark(bookmark)
                        bookmarkToDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 书签项
 */
@Composable
fun BookmarkItem(
    bookmark: EbookBookmarkEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "第${bookmark.chapterIndex + 1}章",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bookmark.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    Divider()
}

/**
 * 分享电子书对话框
 */
@Composable
fun ShareEbookDialog(
    ebook: EbookEntity,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享到动态") },
        text = {
            Column {
                Text("分享《${ebook.title}》到您的动态")
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("添加评论（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onShare(comment) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("分享")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("取消")
            }
        }
    )
}