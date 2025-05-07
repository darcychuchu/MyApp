package com.vlog.my.ui.screens.detail

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.ui.components.FavoriteButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.vlog.my.ui.screens.subscripts.json.VideoPlayer

/**
 * 作品详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ArtworkDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ArtworkDetailViewModel = hiltViewModel()
) {
    val artwork by viewModel.artwork.collectAsState()
    val videoUrl by viewModel.videoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val favoriteActionState by viewModel.favoriteActionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // 创建 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    // 设置媒体项
    LaunchedEffect(videoUrl) {
        videoUrl?.let { it ->
            val mediaItem = MediaItem.Builder()
                .setUri(it.toUri())
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    // 处理错误信息
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 处理喜爱操作状态
    LaunchedEffect(favoriteActionState) {
        when (favoriteActionState) {
            is ArtworkDetailViewModel.FavoriteActionState.Error -> {
                val errorMessage = (favoriteActionState as ArtworkDetailViewModel.FavoriteActionState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.resetFavoriteActionState()
            }
            is ArtworkDetailViewModel.FavoriteActionState.Success -> {
                // 操作成功，可以显示一个短暂的提示
                val message = if (isFavorite) "已添加到喜爱" else "已从喜爱中移除"
                snackbarHostState.showSnackbar(message)
                viewModel.resetFavoriteActionState()
            }
            else -> {}
        }
    }


    // 释放ExoPlayer资源
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作品详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 喜爱按钮
                    Box(
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onClick = { viewModel.toggleFavorite() },
                            enabled = favoriteActionState !is ArtworkDetailViewModel.FavoriteActionState.Loading
                        )
                    }

                    // 可以添加更多操作按钮，如分享等
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && artwork == null) {
                // 加载中
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (artwork == null) {
                // 加载失败
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "加载失败，请重试",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 显示作品详情
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    VideoPlayer(
                        exoPlayer = exoPlayer,
                        isFullScreen = false,
                        onFullScreenChange = {  }
                    )


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 标题（如果有）
                        artwork?.title?.let {
                            if (it.isNotBlank()) {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        // 用户信息
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // 用户头像
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                artwork?.createdByItem?.avatar?.let { avatarId ->
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = "${ApiConstants.IMAGE_BASE_URL}$avatarId"
                                        ),
                                        contentDescription = "用户头像",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } ?: run {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "用户头像",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 用户名和时间
                            Column {
                                Text(
                                    text = artwork?.createdByItem?.nickName ?: artwork?.createdBy ?: "未知用户",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "发布时间",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatDate(artwork?.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 描述
                        Text(
                            text = artwork?.description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // 标签（如果有）
                        artwork?.tags?.let {
                            if (it.isNotBlank()) {
                                Text(
                                    text = "标签: $it",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp))

                        // 评论区（暂时留空）
                        Text(
                            text = "评论区",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "暂无评论",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "未知时间"

    return try {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.format(date)
    } catch (e: Exception) {
        "未知时间"
    }
}
