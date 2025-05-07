package com.vlog.my.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.model.VideoShareContent
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 分享的视频内容组件
 *
 * @param shareContentJson 分享内容的JSON字符串
 * @param creatorName 小程序创建者用户名
 * @param onVideoClick 点击视频时的回调
 * @param onCreatorClick 点击创建者名称时的回调
 * @param onPlayClick 点击播放图标时的回调，快速跳转到小程序
 * @param onMessageClick 点击消息图标时的回调，给站长发消息
 */
@Composable
fun SharedVideoContent(
    shareContentJson: String,
    creatorName: String? = null,
    onVideoClick: (VideoShareContent) -> Unit,
    onCreatorClick: (String) -> Unit = {},
    onPlayClick: (VideoShareContent) -> Unit = {},
    onMessageClick: (String) -> Unit = {}
) {
    // 解析分享内容
    val shareContent = parseShareContent(shareContentJson)

    if (shareContent != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onVideoClick(shareContent) },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 视频封面
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    // 封面图片
                    Image(
                        painter = rememberAsyncImagePainter(shareContent.coverUrl),
                        contentDescription = shareContent.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // 播放图标
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "播放",
                            modifier = Modifier.fillMaxSize(0.3f),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // 剧集信息
                    shareContent.episodeIndex?.let { episodeIndex ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "第${episodeIndex}集",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 视频标题
                Text(
                    text = shareContent.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 视频描述
                Text(
                    text = shareContent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // 分享时间
                if (shareContent.sharedAt > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "分享于: ${formatDate(shareContent.sharedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // 小程序创建者信息和提示
                if (creatorName != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "此内容来自小程序，由 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "@$creatorName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onCreatorClick(creatorName) }
                        )

                        Text(
                            text = " 创建",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "如无法播放，可私信站长获取接口信息",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )

                        // 播放图标 - 快速跳转到小程序
                        IconButton(
                            onClick = { onPlayClick(shareContent) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "播放",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // 消息图标 - 给站长发消息
                        IconButton(
                            onClick = {
                                if (creatorName != null) {
                                    onMessageClick(creatorName)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "发送消息",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 解析分享内容
 */
private fun parseShareContent(shareContentJson: String): VideoShareContent? {
    return try {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val jsonAdapter = moshi.adapter(VideoShareContent::class.java)
        val result = jsonAdapter.fromJson(shareContentJson)

        // 如果解析成功，记录日志
        if (result != null) {
            android.util.Log.d("SharedVideoContent", "视频分享内容解析成功: $result")
        } else {
            android.util.Log.e("SharedVideoContent", "视频分享内容解析为null")
        }

        result
    } catch (e: Exception) {
        android.util.Log.e("SharedVideoContent", "解析视频分享内容失败: ${e.message}", e)
        android.util.Log.e("SharedVideoContent", "JSON内容: $shareContentJson")
        null
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        format.format(date)
    } catch (e: Exception) {
        "未知时间"
    }
}
