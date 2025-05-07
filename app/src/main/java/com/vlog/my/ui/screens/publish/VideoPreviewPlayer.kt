package com.vlog.my.ui.screens.publish

import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.video.VideoFrameMetadataListener
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

/**
 * 视频预览播放器组件
 * 支持多种格式的视频预览，包括本地文件和网络视频
 *
 * @param videoUri 视频URI
 * @param modifier 修饰符
 * @param autoPlay 是否自动播放
 * @param showControls 是否显示控制器
 * @param repeatMode 是否循环播放
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPreviewPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    showControls: Boolean = true,
    repeatMode: Boolean = true
) {
    val context = LocalContext.current

    // 创建 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 根据文件扩展名判断MIME类型
            val path = videoUri.path ?: ""
            val mimeType = when {
                path.endsWith(".m3u8", ignoreCase = true) -> MimeTypes.APPLICATION_M3U8
                path.endsWith(".mp4", ignoreCase = true) -> MimeTypes.VIDEO_MP4
                path.endsWith(".mov", ignoreCase = true) -> MimeTypes.VIDEO_MP4 // MOV格式也可以用MP4处理
                else -> null // 让ExoPlayer自动检测
            }

            // 创建媒体项
            val mediaItem = if (mimeType != null) {
                MediaItem.Builder()
                    .setUri(videoUri)
                    .setMimeType(mimeType)
                    .build()
            } else {
                MediaItem.fromUri(videoUri)
            }

            // 设置媒体项
            setMediaItem(mediaItem)

            // 设置循环播放
            if (repeatMode) {
                this.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
            }

            // 添加视频帧元数据监听器，用于处理视频旋转
            setVideoFrameMetadataListener(object : VideoFrameMetadataListener {
                override fun onVideoFrameAboutToBeRendered(
                    presentationTimeUs: Long,
                    releaseTimeNs: Long,
                    format: Format,
                    mediaFormat: android.media.MediaFormat?
                ) {
                    // 检查视频是否有旋转信息
                    val rotation = format.rotationDegrees
                    if (rotation != 0) {
                        Log.d("VideoPreviewPlayer", "检测到视频旋转: $rotation 度")
                    }
                }
            })

            // 准备播放器
            prepare()

            // 自动播放
            if (autoPlay) {
                play()
            }
        }
    }

    // 在组件销毁时释放 ExoPlayer 资源
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 视频播放器UI
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 使用动态宽高比，根据视频方向自动调整
            .aspectRatio(9f / 16f) // 默认为竖屏比例，适合手机拍摄的视频
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(1.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                // 创建 PlayerView
                PlayerView(ctx).apply {
                    player = exoPlayer
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    useController = showControls

                    // 设置视频调整模式为自适应填充
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

                    // 监听视频尺寸变化，自动调整显示方向
                    exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            super.onVideoSizeChanged(videoSize)

                            // 获取视频的宽高和旋转角度
                            val width = videoSize.width
                            val height = videoSize.height
                            // Use the deprecated property for now, but we should find a better solution
                            @Suppress("DEPRECATION")
                            val rotation = videoSize.unappliedRotationDegrees

                            Log.d("VideoPreviewPlayer", "视频尺寸变化: ${width}x${height}, 旋转: $rotation 度")

                            // 根据旋转角度判断视频的实际方向
                            val isPortrait = if (rotation == 90 || rotation == 270) {
                                // 如果旋转了90或270度，则宽高需要互换来判断
                                width < height
                            } else {
                                // 没有旋转或旋转了180度，直接比较宽高
                                width < height
                            }

                            // 根据视频方向设置合适的调整模式
                            if (isPortrait) {
                                // 竖屏视频
                                Log.d("VideoPreviewPlayer", "竖屏视频，使用FIT模式")
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            } else {
                                // 横屏视频
                                Log.d("VideoPreviewPlayer", "横屏视频，使用ZOOM模式")
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
