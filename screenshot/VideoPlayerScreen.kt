package com.vlog.app.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.app.data.model.VideoDetailResponse
import com.vlog.app.data.model.VideoItem
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String, 
    onBackClick: () -> Unit,
    onPlayUrlChange: (String) -> Unit = {}
) {
    var isFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 创建 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    val videoDetail = VideoItem()

    // 设置媒体项
    LaunchedEffect(videoDetail) {
        if (videoDetail.videoPlayList.isNotEmpty() && videoDetail.videoPlayList[0].playList.isNotEmpty()) {
            val playUrl = videoDetail.videoPlayList[0].playList[0].playUrl
            val mediaItem = MediaItem.Builder()
                .setUri(playUrl.toUri())
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
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
        // 非全屏模式下显示完整布局
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = videoDetail.title) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        IconButton(onClick = { isFullScreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "全屏")
                        }
                    }
                )
            }
        ) { paddingValues ->
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
                
                // 播放控制按钮
                val currentPlayIndex = remember { mutableIntStateOf(0) }
                val hasPrevious = remember { derivedStateOf { currentPlayIndex.intValue > 0 } }
                val hasNext = remember { derivedStateOf { 
                    videoDetail.videoPlayList.isNotEmpty() && 
                    currentPlayIndex.intValue < videoDetail.videoPlayList[0].playList.size - 1
                } }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { 
                            if (hasPrevious.value) {
                                currentPlayIndex.intValue--
                                val playUrl = videoDetail.videoPlayList[0].playList[currentPlayIndex.value].playUrl
                                onPlayUrlChange(playUrl)
                            }
                        },
                        enabled = hasPrevious.value
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "上一集",
                            tint = if (hasPrevious.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    
                    IconButton(
                        onClick = { exoPlayer.seekBack() },
                        enabled = exoPlayer.isPlaying
                    ) {
                        Text(text = "-5s")
                    }
                    
                    IconButton(
                        onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() }
                    ) {
                        Icon(
                            imageVector = if (exoPlayer.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "播放/暂停"
                        )
                    }
                    
                    IconButton(
                        onClick = { exoPlayer.seekForward() },
                        enabled = exoPlayer.isPlaying
                    ) {
                        Text(text = "+15s")
                    }
                    
                    IconButton(
                        onClick = { 
                            if (hasNext.value) {
                                currentPlayIndex.intValue++
                                val playUrl = videoDetail.videoPlayList[0].playList[currentPlayIndex.value].playUrl
                                onPlayUrlChange(playUrl)
                            }
                        },
                        enabled = hasNext.value
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "下一集",
                            tint = if (hasNext.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }

                // 视频信息
                VideoInfo(videoDetail) { newPlayUrl ->
                    val mediaItem = MediaItem.Builder()
                        .setUri(newPlayUrl.toUri())
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    // 切换后自动播放
                    exoPlayer.play()
                }
            }
        }
    }
}

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
                    tint = androidx.compose.ui.graphics.Color.White // 设置图标颜色为白色，以便在视频上可见
                )
            }
        }
    }
}

@Composable
fun VideoInfo(video: VideoItem, onPlayUrlChange: (String) -> Unit) {
    var currentPlayTitle by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        // 显示当前播放的标题
        if (currentPlayTitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "正在播放: $currentPlayTitle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row {
            Text(
                text = "评分: ${video.score}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 添加播放列表
        Text(
            text = "播放源:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // 二级分类列表
        video.videoPlayList.forEach { playListGroup ->
            var isExpanded by remember { mutableStateOf(false) }
            
            Column {
                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = playListGroup.gatherTitle)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
                        contentDescription = null
                    )
                }
                
                if (isExpanded) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        playListGroup.playList.forEach { playItem ->
                            Button(
                                onClick = {
                                    onPlayUrlChange(playItem.playUrl)
                                    currentPlayTitle = "${playListGroup.gatherTitle}-${playItem.title}"
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = playItem.title)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        InfoItem(title = "导演", content = video.director)
        InfoItem(title = "主演", content = video.actors)
        InfoItem(title = "地区", content = video.region)
        InfoItem(title = "语言", content = video.language)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "剧情简介",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun InfoItem(title: String, content: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}