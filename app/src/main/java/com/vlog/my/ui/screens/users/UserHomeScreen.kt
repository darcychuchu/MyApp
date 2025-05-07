package com.vlog.my.ui.screens.users

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.model.Stories
import com.vlog.my.ui.components.StoryItem
import com.vlog.my.ui.navigation.Screen
import com.vlog.my.ui.screens.users.UserStoryDetailScreen
import kotlinx.coroutines.launch

/**
 * 用户主页屏幕
 * 显示指定用户的动态和作品
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    onNavigateBack: () -> Unit = {},
    navController: NavController? = null,
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("动态", "作品")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 获取动态和作品列表
    val storiesList by viewModel.storiesList.collectAsState()
    val artworksList by viewModel.artworksList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val followActionState by viewModel.followActionState.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    // 处理错误信息
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 处理关注操作状态
    LaunchedEffect(followActionState) {
        when (val currentState = followActionState) {
            is UserHomeViewModel.FollowActionState.Success -> {
                snackbarHostState.showSnackbar(currentState.message)
                viewModel.resetFollowActionState()
            }
            is UserHomeViewModel.FollowActionState.Error -> {
                snackbarHostState.showSnackbar(currentState.message)
                viewModel.resetFollowActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val username = viewModel.username
                    Text("$username 的主页")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 只有当查看的不是自己的主页时才显示发送消息按钮
                    if (!viewModel.isCurrentUser()) {
                        IconButton(
                            onClick = {
                                // 导航到发送消息页面
                                navController?.navigate(
                                    Screen.ComposeMessage.createRoute(
                                        recipientId = viewModel.username,
                                        recipientName = viewModel.username
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "发送消息"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refresh() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 用户信息区域
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // 使用默认图标
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "用户头像",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 用户名
                Text(
                    text = viewModel.username,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 关注信息和按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 粉丝数
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // 导航到粉丝列表
                            navController?.navigate("followers/${viewModel.username}")
                        }
                    ) {
                        Text(
                            text = followersCount.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "粉丝",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // 关注数
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            // 导航到关注列表
                            navController?.navigate("following/${viewModel.username}")
                        }
                    ) {
                        Text(
                            text = followingCount.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "关注",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 关注/取消关注按钮 - 只在查看其他用户主页时显示
                if (!viewModel.isCurrentUser()) {
                    Button(
                        onClick = {
                            if (isFollowing) {
                                viewModel.unfollowUser()
                            } else {
                                viewModel.followUser()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 16.dp),
                        colors = if (isFollowing) {
                            ButtonDefaults.outlinedButtonColors()
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(if (isFollowing) "取消关注" else "关注")
                    }
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

            // 标签页
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            // 切换 Tab 时加载对应的数据
                            viewModel.loadDataByTab(index)
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 当 Tab 切换时加载对应的数据
            LaunchedEffect(selectedTabIndex) {
                viewModel.loadDataByTab(selectedTabIndex)
            }

            // 内容区域
            when (selectedTabIndex) {
                0 -> StoriesContent(
                    viewModel = viewModel,
                    onNavigateToStoryDetail = { userName, storyId ->
                        navController?.navigate(Screen.UserStoryDetail.createRoute(userName, storyId))
                    }
                )
                1 -> ArtworksContent(
                    viewModel = viewModel,
                    onNavigateToArtworkDetail = { userName, artworkId, isPlayerMode ->
                        // 添加日志
                        Log.d("UserHomeScreen", "导航: userName=$userName, artworkId=$artworkId, isPlayerMode=$isPlayerMode")

                        if (isPlayerMode) {
                            val route = Screen.UserArtworkPlayer.createRoute(userName, artworkId)
                            Log.d("UserHomeScreen", "导航到播放页面: $route")
                            navController?.navigate(route)
                        } else {
                            val route = Screen.UserArtworkDetail.createRoute(userName, artworkId)
                            Log.d("UserHomeScreen", "导航到详情页面: $route")
                            navController?.navigate(route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StoriesContent(
    viewModel: UserHomeViewModel,
    onNavigateToStoryDetail: (userName: String, storyId: String) -> Unit = { _, _ -> }
) {
    val storiesList by viewModel.storiesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (storiesList.isEmpty() && !isLoading) {
            // 空列表状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无动态")
            }
        } else {
            // 列表内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(storiesList) { story ->
                    StoryItem(
                        story = story,
                        onClick = {
                            // 点击动态项，导航到动态详情页面
                            story.id?.let { storyId ->
                                story.createdBy?.let { userName ->
                                    onNavigateToStoryDetail(userName, storyId)
                                }
                            }
                        },
                        onUserClick = { userName ->
                            // 点击用户头像或用户名，导航到用户主页
                            // 暂时注释掉导航逻辑
                            // navController?.let { nav ->
                            //     nav.navigate(Screen.UserHome.createRoute(userName))
                            // }
                        }
                    )
                }
            }
        }

        // 加载指示器
        if (isLoading && storiesList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ArtworksContent(
    viewModel: UserHomeViewModel,
    onNavigateToArtworkDetail: (userName: String, artworkId: String, isPlayerMode: Boolean) -> Unit = { _, _, _ -> }
) {
    val artworksList by viewModel.artworksList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (artworksList.isEmpty() && !isLoading) {
            // 空列表状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无作品")
            }
        } else {
            // 列表内容
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(artworksList) { artwork ->
                    StoryItem(
                        story = artwork,
                        onClick = {
                            // 点击作品项，根据 isLocked 值决定导航到哪个页面
                            artwork.id?.let { artworkId ->
                                artwork.createdBy?.let { userName ->
                                    // 如果 isLocked <= 2，导航到播放页面，否则导航到详情页面
                                    val isLocked = artwork.isLocked ?: 0
                                    val isPlayerMode = isLocked <= 2

                                    // 添加日志
                                    Log.d("UserHomeScreen", "作品点击: id=$artworkId, isLocked=$isLocked, isPlayerMode=$isPlayerMode")

                                    onNavigateToArtworkDetail(userName, artworkId, isPlayerMode)
                                }
                            }
                        },
                        onUserClick = { userName ->
                            // 点击用户头像或用户名，导航到用户主页
                            // 暂时注释掉导航逻辑
                            // navController?.let { nav ->
                            //     nav.navigate(Screen.UserHome.createRoute(userName))
                            // }
                        }
                    )
                }
            }
        }

        // 加载指示器
        if (isLoading && artworksList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
