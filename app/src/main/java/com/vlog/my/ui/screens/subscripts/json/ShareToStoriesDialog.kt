package com.vlog.my.ui.screens.subscripts.json

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.vlog.my.data.model.VideoShareContent
import com.vlog.my.data.model.json.PlayItem
import com.vlog.my.data.model.json.VideoItem

/**
 * 分享到动态对话框
 *
 * @param video 要分享的视频
 * @param currentPlayItem 当前播放的剧集
 * @param onDismiss 取消回调
 * @param onShare 分享回调，参数为用户输入的描述文本和构建的VideoShareContent对象
 */
@Composable
fun ShareToStoriesDialog(
    video: VideoItem,
    currentPlayItem: PlayItem?,
    subScriptId: String,
    onDismiss: () -> Unit,
    onShare: (String, VideoShareContent) -> Unit
) {
    // 默认描述文本
    val defaultDescription = buildString {
        append("我正在观看《${video.vod_name}》")
        currentPlayItem?.let {
            append("，当前播放：${it.title}")
        }
        append("，推荐推荐，非常好看！！！")
    }

    // 用户可编辑的描述文本
    var description by remember { mutableStateOf(defaultDescription) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // 标题
                Text(
                    text = "分享到动态",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 视频信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 封面图片
                        AsyncImage(
                            model = video.vod_pic,
                            contentDescription = video.vod_name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // 视频信息
                        Column(modifier = Modifier.weight(1f)) {
                            // 标题
                            Text(
                                text = video.vod_name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 当前播放
                            currentPlayItem?.let {
                                Text(
                                    text = "当前播放：${it.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 演员
                            video.vod_actor?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text = "演员：$it",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 导演
                            video.vod_director?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        text = "导演：$it",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 描述文本输入框
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("说点什么...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            // 构建分享内容
                            val shareContent = VideoShareContent(
                                subScriptId = subScriptId,
                                videoId = video.vod_id.toString(),
                                position = 0, // 当前不跟踪播放位置
                                episodeIndex = currentPlayItem?.title?.let {
                                    // 尝试从标题中提取集数，如"第1集"提取为1
                                    val regex = Regex("第(\\d+)集")
                                    val matchResult = regex.find(it)
                                    matchResult?.groupValues?.get(1)?.toIntOrNull()
                                },
                                title = video.vod_name,
                                coverUrl = video.vod_pic ?: "",
                                description = video.vod_content ?: "",
                                playerApiUrl = currentPlayItem?.playUrl ?: "",
                                sharedBy = null, // 由服务器设置
                                sharedAt = System.currentTimeMillis() // 当前时间
                            )

                            // 调用分享回调
                            onShare(description, shareContent)
                        }
                    ) {
                        Text("分享")
                    }
                }
            }
        }
    }
}
