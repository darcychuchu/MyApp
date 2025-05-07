package com.vlog.my.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Image
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.model.EbookShareContent
import com.vlog.my.data.model.Stories
import com.vlog.my.data.model.VideoShareContent
import com.vlog.my.ui.components.FavoriteButton
import com.vlog.my.ui.components.SharedEbookContent
import com.vlog.my.ui.components.SharedVideoContent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 动态详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVideo: (VideoShareContent) -> Unit = {},
    onNavigateToEbook: (EbookShareContent) -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    viewModel: StoryDetailViewModel = hiltViewModel()
) {
    val story by viewModel.story.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val favoriteActionState by viewModel.favoriteActionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

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
            is StoryDetailViewModel.FavoriteActionState.Error -> {
                val errorMessage = (favoriteActionState as StoryDetailViewModel.FavoriteActionState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.resetFavoriteActionState()
            }
            is StoryDetailViewModel.FavoriteActionState.Success -> {
                // 操作成功，可以显示一个短暂的提示
                val message = if (isFavorite) "已添加到喜爱" else "已从喜爱中移除"
                snackbarHostState.showSnackbar(message)
                viewModel.resetFavoriteActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("动态详情") },
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
                            enabled = favoriteActionState !is StoryDetailViewModel.FavoriteActionState.Loading
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
            if (isLoading && story == null) {
                // 加载中
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (story == null) {
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
                // 显示动态详情
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // 用户信息
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 用户头像 - 可点击导航到用户主页
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    // 点击用户头像，导航到用户主页
                                    story?.createdBy?.let { userName ->
                                        onNavigateToProfile(userName)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            story?.createdByItem?.avatar?.let { avatarId ->
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
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // 用户名和时间
                        Column {
                            Text(
                                text = story?.createdByItem?.nickName ?: story?.createdBy ?: "未知用户",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    // 点击用户名，导航到用户主页
                                    story?.createdBy?.let { userName ->
                                        onNavigateToProfile(userName)
                                    }
                                }
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
                                    text = formatDate(story?.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 标题（如果有）
                    story?.title?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    // 描述
                    Text(
                        text = story?.description ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // 分享的内容（如果有）
                    story?.shareContent?.let { shareContentJson ->
                        if (shareContentJson.isNotBlank()) {
                            // 记录分享内容和类型
                            android.util.Log.d("StoryDetailScreen", "分享内容类型: ${story?.shareTyped}")
                            android.util.Log.d("StoryDetailScreen", "分享内容JSON: $shareContentJson")

                            // 获取创建者信息
                            val creatorName = story?.createdBy ?: ""
                            val createdByNickName = story?.createdByItem?.nickName ?: creatorName

                            // 检测电子书JSON格式
                            if (shareContentJson.contains("ebook_id") && story?.shareTyped != 2) {
                                android.util.Log.w("StoryDetailScreen", "检测到电子书JSON但shareTyped不是2，强制使用电子书显示")
                                // 强制使用电子书显示
                                SharedEbookContent(
                                    shareContentJson = shareContentJson,
                                    creatorName = createdByNickName,
                                    onEbookClick = onNavigateToEbook
                                )
                            } else if (shareContentJson.contains("video_id") && story?.shareTyped != 1) {
                                android.util.Log.w("StoryDetailScreen", "检测到视频JSON但shareTyped不是1，强制使用视频显示")
                                // 强制使用视频显示
                                SharedVideoContent(
                                    shareContentJson = shareContentJson,
                                    creatorName = createdByNickName,
                                    onVideoClick = onNavigateToVideo,
                                    onCreatorClick = onNavigateToProfile,
                                    onPlayClick = onNavigateToVideo,
                                    onMessageClick = onNavigateToProfile
                                )
                            } else {
                                // 根据分享类型显示不同的内容
                                when (story?.shareTyped) {
                                    1 -> {
                                        // 视频内容
                                        android.util.Log.d("StoryDetailScreen", "显示视频分享内容")
                                        SharedVideoContent(
                                            shareContentJson = shareContentJson,
                                            creatorName = createdByNickName,
                                            onVideoClick = onNavigateToVideo,
                                            onCreatorClick = onNavigateToProfile,
                                            onPlayClick = onNavigateToVideo,
                                            onMessageClick = onNavigateToProfile
                                        )
                                    }
                                    2 -> {
                                        // 电子书内容
                                        android.util.Log.d("StoryDetailScreen", "显示电子书分享内容")
                                        SharedEbookContent(
                                            shareContentJson = shareContentJson,
                                            creatorName = createdByNickName,
                                            onEbookClick = onNavigateToEbook
                                        )
                                    }
                                    else -> {
                                        // 尝试检测JSON内容类型
                                        if (shareContentJson.contains("ebook_id")) {
                                            android.util.Log.d("StoryDetailScreen", "检测到电子书JSON，显示电子书内容")
                                            SharedEbookContent(
                                                shareContentJson = shareContentJson,
                                                creatorName = createdByNickName,
                                                onEbookClick = onNavigateToEbook
                                            )
                                        } else {
                                            // 默认显示视频内容（向后兼容）
                                            android.util.Log.d("StoryDetailScreen", "显示默认分享内容(视频)")
                                            SharedVideoContent(
                                                shareContentJson = shareContentJson,
                                                creatorName = createdByNickName,
                                                onVideoClick = onNavigateToVideo,
                                                onCreatorClick = onNavigateToProfile,
                                                onPlayClick = onNavigateToVideo,
                                                onMessageClick = onNavigateToProfile
                                            )
                                        }
                                    }
                                }
                            }

                            // 添加JSON内容显示（仅在调试模式下显示）
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                val isDebuggable = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
                                if (isDebuggable) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "调试信息",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "分享类型: ${story?.shareTyped}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "JSON内容: $shareContentJson",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // 标签（如果有）
                    story?.tags?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = "标签: $it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    // 图片（如果有）
                    story?.attachmentId?.let { attachmentId ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (attachmentId.isNotBlank()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            model = "${ApiConstants.IMAGE_BASE_URL}$attachmentId"
                                        ),
                                        contentDescription = "图片",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "无图片",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 多图展示（如果有）
                    story?.attachmentList?.let { attachments ->
                        if (attachments.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                attachments.forEach { attachment ->
                                    attachment.id?.let { attachmentId ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(16f / 9f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    painter = rememberAsyncImagePainter(
                                                        model = "${ApiConstants.IMAGE_BASE_URL}$attachmentId"
                                                    ),
                                                    contentDescription = "图片",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
