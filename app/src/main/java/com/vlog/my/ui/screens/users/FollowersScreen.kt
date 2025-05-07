package com.vlog.my.ui.screens.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.ui.viewmodels.FollowersViewModel

/**
 * 用户的粉丝列表界面
 * @param username 要查看的用户名，如果为null则查看当前登录用户
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowersScreen(
    username: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: FollowersViewModel = hiltViewModel()
) {
    val followersState by viewModel.followersState.collectAsState()
    val followActionState by viewModel.followActionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 加载粉丝列表
    LaunchedEffect(username) {
        viewModel.getFollowers(username)
    }

    // 处理关注/取消关注操作状态
    LaunchedEffect(followActionState) {
        when (followActionState) {
            is FollowersViewModel.FollowActionState.Success -> {
                snackbarHostState.showSnackbar(
                    (followActionState as FollowersViewModel.FollowActionState.Success).message
                )
                viewModel.resetFollowActionState()
            }
            is FollowersViewModel.FollowActionState.Error -> {
                snackbarHostState.showSnackbar(
                    (followActionState as FollowersViewModel.FollowActionState.Error).message
                )
                viewModel.resetFollowActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 根据是否是当前用户显示不同的标题
                    val currentUser = viewModel.getCurrentUserName()
                    val title = if (username == currentUser) "我的粉丝" else "$username 的粉丝"
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (followersState) {
                is FollowersViewModel.FollowersState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FollowersViewModel.FollowersState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "暂无粉丝",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "还没有用户关注您",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is FollowersViewModel.FollowersState.Success -> {
                    val followers = (followersState as FollowersViewModel.FollowersState.Success).followers
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(followers) { follower ->
                            // 这里需要判断当前用户是否已经关注了这个粉丝
                            // 由于API可能没有提供这个信息，这里简单处理为未关注
                            // 实际应用中可能需要从关注列表中查找
                            val isFollowing = false

                            FollowerItem(
                                follower = follower,
                                isFollowing = isFollowing,
                                onUserClick = { onNavigateToUserProfile(follower.alternateId ?: follower.userId ?: "") },
                                onFollowClick = {
                                    if (isFollowing) {
                                        viewModel.unfollowUser(follower.userId ?: "")
                                    } else {
                                        viewModel.followUser(follower.userId ?: "")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is FollowersViewModel.FollowersState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "加载失败",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (followersState as FollowersViewModel.FollowersState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getFollowers() }
                        ) {
                            Text("重试")
                        }
                    }
                }
                else -> {}
            }

            // 显示加载中状态
            if (followActionState is FollowersViewModel.FollowActionState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
