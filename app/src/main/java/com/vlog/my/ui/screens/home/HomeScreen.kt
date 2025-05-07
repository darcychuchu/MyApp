package com.vlog.my.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vlog.my.data.model.Stories
import com.vlog.my.ui.components.AppDrawer
import com.vlog.my.ui.components.StoryItem
import com.vlog.my.ui.navigation.Screen
import com.vlog.my.ui.theme.MyAppTheme
import com.vlog.my.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSubScripts: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    navController: NavController? = null,
    userViewModel: UserViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("发现", "关注")
    val snackbarHostState = remember { SnackbarHostState() }

    // 获取当前用户信息
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoggedIn = userViewModel.isLoggedIn()
    val userName = currentUser?.nickName ?: "未登录"
    val userAvatar = currentUser?.avatar

    // 获取全局动态和作品列表
    val storiesList by homeViewModel.storiesList.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val error by homeViewModel.error.collectAsState()
    val hasMoreData by homeViewModel.hasMoreData.collectAsState()

    // 处理错误信息
    error?.let { errorMsg ->
        androidx.compose.runtime.LaunchedEffect(errorMsg) {
            scope.launch {
                snackbarHostState.showSnackbar(errorMsg)
                homeViewModel.clearError()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onProfileClick = onNavigateToProfile,
                onSubScriptsClick = onNavigateToSubScripts,
                onSettingsClick = onNavigateToSettings,
                isUserLoggedIn = isLoggedIn,
                userName = userName,
                userAvatar = userAvatar
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 左侧汉堡菜单
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "菜单"
                                )
                            }

                            // 中间的标签页
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Row {
                                    tabs.forEachIndexed { index, title ->
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .clickable { selectedTabIndex = index },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .let {
                                                        if (selectedTabIndex == index) {
                                                            it
                                                        } else {
                                                            it
                                                        }
                                                    },
                                                textAlign = TextAlign.Center,
                                                color = if (selectedTabIndex == index) {
                                                    androidx.compose.ui.graphics.Color.Black
                                                } else {
                                                    androidx.compose.ui.graphics.Color.Gray
                                                }
                                            )
                                        }

                                        if (index < tabs.size - 1) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                }
                            }

                            // 右侧占位，保持对称
                            IconButton(
                                onClick = { /* 可以添加搜索功能 */ }
                            ) {
                                // 可以放置搜索图标或其他功能图标
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 内容区域
                when (selectedTabIndex) {
                    0 -> DiscoverContent(
                        storiesList = storiesList,
                        isLoading = isLoading,
                        isRefreshing = isRefreshing,
                        hasMoreData = hasMoreData,
                        onRefresh = { homeViewModel.refresh() },
                        onLoadMore = { homeViewModel.loadMore() },
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        onNavigateToStoryDetail = { userName, storyId ->
                            navController?.navigate(Screen.UserStoryDetail.createRoute(userName, storyId))
                        },
                        onNavigateToArtworkDetail = { userName, artworkId, isPlayerMode ->
                            if (isPlayerMode) {
                                navController?.navigate(Screen.UserArtworkPlayer.createRoute(userName, artworkId))
                            } else {
                                navController?.navigate(Screen.UserArtworkDetail.createRoute(userName, artworkId))
                            }
                        }
                    )
                    1 -> FollowContent(
                        storiesList = homeViewModel.followingStoriesList.collectAsState().value,
                        isLoading = homeViewModel.isFollowingLoading.collectAsState().value,
                        isRefreshing = homeViewModel.isFollowingRefreshing.collectAsState().value,
                        hasMoreData = homeViewModel.followingHasMoreData.collectAsState().value,
                        onRefresh = { homeViewModel.refreshFollowing() },
                        onLoadMore = { homeViewModel.loadMoreFollowing() },
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        onNavigateToStoryDetail = { userName, storyId ->
                            navController?.navigate(Screen.UserStoryDetail.createRoute(userName, storyId))
                        },
                        onNavigateToArtworkDetail = { userName, artworkId, isPlayerMode ->
                            if (isPlayerMode) {
                                navController?.navigate(Screen.UserArtworkPlayer.createRoute(userName, artworkId))
                            } else {
                                navController?.navigate(Screen.UserArtworkDetail.createRoute(userName, artworkId))
                            }
                        },
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DiscoverContent(
    storiesList: List<Stories>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    hasMoreData: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToStoryDetail: (String, String) -> Unit = { _, _ -> },
    onNavigateToArtworkDetail: (String, String, Boolean) -> Unit = { _, _, _ -> }
) {
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )

    // 监听滚动到底部，加载更多
    if (storiesList.isNotEmpty()) {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastVisibleIndex != null && lastVisibleIndex >= storiesList.size - 3 && !isLoading && hasMoreData) {
            androidx.compose.runtime.LaunchedEffect(lastVisibleIndex) {
                onLoadMore()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        if (storiesList.isEmpty() && !isLoading && !isRefreshing) {
            // 空列表状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("暂无内容")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRefresh) {
                        Text("刷新")
                    }
                }
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
                            // 根据isTyped值决定导航到哪个页面
                            story.id?.let { id ->
                                story.createdBy?.let { userName ->
                                    if (story.isTyped == 0) {
                                        // 图文动态
                                        onNavigateToStoryDetail(userName, id)
                                    } else {
                                        // 视频作品
                                        val isLocked = story.isLocked ?: 0
                                        val isPlayerMode = isLocked <= 2
                                        onNavigateToArtworkDetail(userName, id, isPlayerMode)
                                    }
                                }
                            }
                        },
                        onUserClick = { userName ->
                            onNavigateToUserProfile(userName)
                        }
                    )
                }

                // 底部加载更多指示器
                if (isLoading && !isRefreshing && storiesList.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
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

        // 下拉刷新指示器
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}



@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyAppTheme {
        HomeScreen()
    }
}
