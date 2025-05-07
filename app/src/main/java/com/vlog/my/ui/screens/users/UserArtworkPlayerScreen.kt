package com.vlog.my.ui.screens.users

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.model.Stories

/**
 * 用户作品播放页面
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun UserArtworkPlayerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Stories) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: UserArtworkPlayerViewModel = hiltViewModel()
) {
    val artwork by viewModel.artwork.collectAsState()
    val videoUrl by viewModel.videoUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val username = viewModel.username

    // 处理错误
    LaunchedEffect(error) {
        error?.let {
            // 显示错误信息
            viewModel.clearError()
        }
    }

    // 检查 isLocked 值，如果 > 2，则导航到详情页面
    LaunchedEffect(artwork) {
        artwork?.let {
            if ((it.isLocked ?: 0) > 2) {
                onNavigateToDetail(it)
            }
        }
    }

    // 播放器状态
    var isLiked by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 视频播放器（全屏）
            videoUrl?.let { url ->
                val context = LocalContext.current
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        // 创建 HLS 媒体源
                        val mediaItem = MediaItem.Builder()
                            .setUri(android.net.Uri.parse(url))
                            .setMimeType(MimeTypes.APPLICATION_M3U8)
                            .build()

                        setMediaItem(mediaItem)
                        prepare()
                    }
                }

                // 在组件销毁时释放 ExoPlayer 资源
                DisposableEffect(exoPlayer) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                AndroidView(
                    factory = {
                        // 创建 PlayerView
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            useController = true // 显示控制器

                            // 设置视频调整模式为自适应填充
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT

                            // 监听视频尺寸变化，自动调整显示方向
                            exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                    super.onVideoSizeChanged(videoSize)

                                    // 获取视频的宽高和旋转角度
                                    val width = videoSize.width
                                    val height = videoSize.height
                                    // Use the deprecated property for now, but we should find a better solution
                                    @Suppress("DEPRECATION")
                                    val rotation = videoSize.unappliedRotationDegrees

                                    Log.d("UserArtworkPlayerScreen", "视频尺寸变化: ${width}x${height}, 旋转: $rotation 度")

                                    // 根据旋转角度判断视频的实际方向
                                    val isPortrait = if (rotation == 90 || rotation == 270) {
                                        // 如果旋转了90或270度，则宽高需要互换来判断
                                        width < height
                                    } else {
                                        // 没有旋转或旋转了180度，直接比较宽高
                                        width < height
                                    }

                                    // 根据视频方向设置合适的调整模式
                                    // 无论视频方向如何，都使用FIT模式确保显示完整视频
                                    Log.d("UserArtworkPlayerScreen", "使用FIT模式显示完整视频")
                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 顶部导航栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
            }

            // 右侧操作栏
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 用户头像
                artwork?.createdByItem?.avatar?.let { avatarId ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                // 点击用户头像，导航到用户主页
                                artwork?.createdBy?.let { userName ->
                                    onNavigateToProfile(userName)
                                }
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = "${ApiConstants.IMAGE_BASE_URL}$avatarId"
                            ),
                            contentDescription = "用户头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable {
                                // 点击用户头像，导航到用户主页
                                artwork?.createdBy?.let { userName ->
                                    onNavigateToProfile(userName)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "用户头像",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(vertical = 16.dp))

                // 点赞按钮
                IconButton(
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = if (isLiked) "1" else "0",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.padding(vertical = 16.dp))

                // 收藏按钮
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "分享",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 底部信息栏
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column {
                    // 用户信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 用户名
                        Text(
                            text = artwork?.createdByItem?.nickName ?: artwork?.createdBy ?: "未知用户",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                // 点击用户名，导航到用户主页
                                artwork?.createdBy?.let { userName ->
                                    onNavigateToProfile(userName)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 作品标题和描述
                    artwork?.title?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    artwork?.description?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
