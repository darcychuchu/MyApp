package com.vlog.my.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.ui.theme.MyAppTheme
import com.vlog.my.viewmodel.UserViewModel
import androidx.compose.foundation.Image
import android.util.Log
import androidx.navigation.NavController
import com.vlog.my.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFollowers: () -> Unit = {},
    onNavigateToFollowing: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToComments: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToStoryDetail: (String, String) -> Unit = { _, _ -> },
    onNavigateToArtworkDetail: (String, String, Boolean) -> Unit = { _, _, _ -> },
    navController: NavController? = null,
    userViewModel: UserViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("动态", "作品")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 获取当前用户信息
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoggedIn = userViewModel.isLoggedIn()

    // 获取动态和作品列表
    val storiesList by profileViewModel.storiesList.collectAsState()
    val artworksList by profileViewModel.artworksList.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    // 处理错误信息
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isLoggedIn) {
                FloatingActionButton(
                    onClick = { profileViewModel.refresh() }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoggedIn && currentUser != null) {
                // 已登录状态 - 显示用户信息
                // 封面背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    // 封面背景颜色
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )

                    // 用户头像 - 放在封面底部，部分重叠
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        currentUser?.avatar?.let { avatarId ->
                            // 使用Coil加载头像
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = "${ApiConstants.IMAGE_BASE_URL}$avatarId"
                                ),
                                contentDescription = "用户头像",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            // 没有头像时显示默认图标
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "用户头像",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // 用户信息区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 48.dp), // 为头像留出空间
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 用户昵称
                    Text(
                        text = currentUser?.nickName ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // 用户简介
                    if (!currentUser?.description.isNullOrBlank()) {
                        Text(
                            text = currentUser?.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                    }

                    // 用户统计信息
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 粉丝
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onNavigateToFollowers)
                        ) {
                            Text(
                                text = "0", // 这里应该从API获取实际粉丝数
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "粉丝",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // 关注
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onNavigateToFollowing)
                        ) {
                            Text(
                                text = "0", // 这里应该从API获取实际关注数
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "关注",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // 积分
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = (currentUser?.points ?: 0).toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "积分",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // 编辑资料按钮
                    OutlinedButton(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("编辑资料")
                    }

                    // 功能按钮区域
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 消息按钮
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onNavigateToMessages)
                        ) {
                            Card(
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "消息",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "消息",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // 评论按钮
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onNavigateToComments)
                        ) {
                            Card(
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Comment,
                                        contentDescription = "评论",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "评论",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                // 未登录状态 - 显示登录提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "用户头像",
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "未登录",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNavigateToLogin,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("去登录")
                        }
                    }
                }

                // 未登录时不显示标签页和内容
                return@Column
            }

            // 标签页
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            // 切换 Tab 时加载对应的数据
                            profileViewModel.loadDataByTab(index)
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 当 Tab 切换时加载对应的数据
            LaunchedEffect(selectedTabIndex) {
                profileViewModel.loadDataByTab(selectedTabIndex)
            }

            // 内容区域
            when (selectedTabIndex) {
                0 -> StoriesContent(
                    profileViewModel = profileViewModel,
                    onNavigateToStoryDetail = onNavigateToStoryDetail
                )
                1 -> ArtworksContent(
                    profileViewModel = profileViewModel,
                    onNavigateToArtworkDetail = onNavigateToArtworkDetail
                )
            }
        }
    }
}

@Composable
fun StoriesContent(
    profileViewModel: ProfileViewModel,
    onNavigateToStoryDetail: (userName: String, storyId: String) -> Unit = { _, _ -> }
) {
    val storiesList by profileViewModel.storiesList.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

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
                Text("暂无动态，快去发布吧！")
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
    profileViewModel: ProfileViewModel,
    onNavigateToArtworkDetail: (userName: String, artworkId: String, isPlayerMode: Boolean) -> Unit = { _, _, _ -> }
) {
    val artworksList by profileViewModel.artworksList.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

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
                Text("暂无作品，快去发布吧！")
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
                                    Log.d("ProfileScreen", "作品点击: id=$artworkId, isLocked=$isLocked, isPlayerMode=$isPlayerMode")

                                    onNavigateToArtworkDetail(userName, artworkId, isPlayerMode)
                                }
                            }
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

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MyAppTheme {
        ProfileScreen()
    }
}
