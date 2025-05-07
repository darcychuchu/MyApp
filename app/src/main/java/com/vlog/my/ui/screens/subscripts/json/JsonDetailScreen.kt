package com.vlog.my.ui.screens.subscripts.json

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.VideoShareContent
import com.vlog.my.data.model.json.PlayItem
import com.vlog.my.data.model.json.VideoItem
import com.vlog.my.data.model.json.parsePlayUrl
import com.vlog.my.viewmodel.UserViewModel
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.EntryPointAccessors
import com.vlog.my.di.StoriesRepositoryEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import android.widget.Toast

/**
 * JSON模式详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonDetailScreen(
    subScript: SubScripts,
    videoId: Long,
    onNavigateBack: () -> Unit,
    viewModel: JsonApiViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedVideo by viewModel.selectedVideo.collectAsState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var isFavorite by remember { mutableStateOf(false) }
    var isFullScreen by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 解析播放URL
    val playItems = remember(selectedVideo) {
        selectedVideo?.vod_play_url.parsePlayUrl()
    }

    // 当前选中的播放项
    var selectedPlayItem by remember { mutableStateOf<PlayItem?>(null) }

    // 是否显示剧集选择对话框
    var showEpisodeDialog by remember { mutableStateOf(false) }

    // 如果没有选中的播放项，但有播放列表，默认选择第一个
    LaunchedEffect(playItems) {
        if (selectedPlayItem == null && playItems.isNotEmpty()) {
            selectedPlayItem = playItems.first()
        }
    }

    // 创建 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // 加载视频详情
    LaunchedEffect(videoId, subScript) {
        if (selectedVideo == null) {
            viewModel.loadVideoById(videoId, subScript.subUrl)
        }
    }

    // 设置媒体项
    LaunchedEffect(selectedPlayItem) {
        selectedPlayItem?.let { playItem ->
            val mediaItem = MediaItem.Builder()
                .setUri(playItem.playUrl.toUri())
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    // 剧集选择对话框
    if (showEpisodeDialog) {
        EpisodeSelectionDialog(
            playItems = playItems,
            selectedPlayItem = selectedPlayItem,
            onEpisodeSelected = {
                selectedPlayItem = it
                showEpisodeDialog = false
            },
            onDismiss = { showEpisodeDialog = false }
        )
    }

    // 分享对话框
    if (showShareDialog && selectedVideo != null && selectedPlayItem != null) {
        val userViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.vlog.my.viewmodel.UserViewModel>()

        // 获取 StoriesRepository 实例
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            StoriesRepositoryEntryPoint::class.java
        )
        val storiesRepository = entryPoint.storiesRepository()

        ShareToStoriesDialog(
            video = selectedVideo!!,
            currentPlayItem = selectedPlayItem,
            subScriptId = subScript.id,
            onDismiss = { showShareDialog = false },
            onShare = { description, shareContent ->
                coroutineScope.launch {
                    try {
                        // 获取用户信息
                        if (!userViewModel.isLoggedIn()) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val userName = userViewModel.currentUser.value?.name
                        val token = userViewModel.currentUser.value?.accessToken

                        if (userName == null || token == null) {
                            Toast.makeText(context, "用户信息不完整", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // 序列化分享内容
                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val jsonAdapter = moshi.adapter(VideoShareContent::class.java)
                        val shareContentJson = jsonAdapter.toJson(shareContent)

                        // 调用专门的分享 API
                        val response = storiesRepository.shareStories(
                            name = userName,
                            token = token,
                            title = selectedVideo?.vod_name,
                            description = description,
                            tags = selectedVideo?.vod_tag,
                            shareContent = shareContentJson,
                            shareTyped = 1  // 1表示视频类型
                        )

                        // 显示结果
                        Toast.makeText(
                            context,
                            "分享成功",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 关闭对话框
                        showShareDialog = false
                    } catch (e: Exception) {
                        // 显示错误信息
                        Toast.makeText(
                            context,
                            "分享失败: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    // 处理屏幕方向切换
    LaunchedEffect(isFullScreen) {
        val activity = context as Activity
        activity.requestedOrientation = if (isFullScreen) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // 释放ExoPlayer资源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }


    if (isFullScreen) {
        // 全屏模式下只显示播放器
        VideoPlayer(
            exoPlayer = exoPlayer,
            isFullScreen = isFullScreen,
            onFullScreenChange = { newValue -> isFullScreen = newValue }
        )
    } else {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedVideo?.vod_name ?: "视频详情") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { paddingValues ->

            if (selectedVideo == null) {
                // 错误信息
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error ?: "加载失败",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                ) {
                    // 视频播放器
                    VideoPlayer(
                        exoPlayer = exoPlayer,
                        isFullScreen = isFullScreen,
                        onFullScreenChange = { newValue -> isFullScreen = newValue }
                    )
                    // 剧集选择按钮
                    if (playItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "当前播放: ${selectedPlayItem?.title ?: "未选择"}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { showEpisodeDialog = true }
                            ) {
                                Text("选集")
                            }
                        }
                    }

                    // 视频详情
                    selectedVideo?.let { video ->
                        VideoDetailContent(
                            video = video,
                            isFavorite = isFavorite,
                            onFavoriteClick = { isFavorite = !isFavorite },
                            onShareClick = {
                                if (selectedPlayItem != null) {
                                    showShareDialog = true
                                } else {
                                    Toast.makeText(context, "请先选择一集播放", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                }
            }
        }

    }

}

/**
 * 视频详情内容
 */
@Composable
fun VideoDetailContent(
    video: VideoItem,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    // 标题和操作区域
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 标题
        Text(
            text = video.vod_name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // 收藏按钮
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }

        // 分享按钮
        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享"
            )
        }
    }

    // 副标题
    video.vod_sub?.let {
        if (it.isNotEmpty()) {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // 状态信息
    video.vod_remarks?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
    }

    Divider(modifier = Modifier.padding(vertical = 8.dp))

    // 详细信息
    InfoRow("类型", video.type_name ?: "未知")
    InfoRow("年份", video.vod_year?.toString() ?: "未知")
    InfoRow("地区", video.vod_area ?: "未知")
    InfoRow("语言", video.vod_lang ?: "未知")
    InfoRow("导演", video.vod_director ?: "未知")
    InfoRow("演员", video.vod_actor ?: "未知")

    Divider(modifier = Modifier.padding(vertical = 8.dp))

    // 简介
    Text(
        text = "简介",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = video.vod_content ?: "暂无简介",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

}

/**
 * 信息行
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 剧集选择对话框
 */
@Composable
fun EpisodeSelectionDialog(
    playItems: List<PlayItem>,
    selectedPlayItem: PlayItem?,
    onEpisodeSelected: (PlayItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择剧集") },
        text = {
            if (playItems.size > 20) {
                // 如果剧集数量大于20，使用网格布局
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playItems) { item ->
                        val isSelected = item == selectedPlayItem
                        EpisodeButton(
                            title = item.title,
                            isSelected = isSelected,
                            onClick = { onEpisodeSelected(item) }
                        )
                    }
                }
            } else {
                // 如果剧集数量较少，使用列表布局
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playItems) { item ->
                        val isSelected = item == selectedPlayItem
                        EpisodeItem(
                            title = item.title,
                            isSelected = isSelected,
                            onClick = { onEpisodeSelected(item) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 剧集按钮（用于网格布局）
 */
@Composable
fun EpisodeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val buttonColors = if (isSelected) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        onClick = onClick,
        colors = buttonColors,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp)
        )
    }
}

/**
 * 剧集项（用于列表布局）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "正在播放",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 视频播放器组件
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(exoPlayer: ExoPlayer, isFullScreen: Boolean, onFullScreenChange: (Boolean) -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .then(if (isFullScreen) Modifier.fillMaxSize() else Modifier.aspectRatio(16f/9f))) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setFullscreenButtonClickListener { onFullScreenChange(!isFullScreen) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 添加全屏返回按钮
        if (isFullScreen) {
            IconButton(
                onClick = { onFullScreenChange(false) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "退出全屏",
                    tint = Color.White // 设置图标颜色为白色，以便在视频上可见
                )
            }
        }
    }
}
