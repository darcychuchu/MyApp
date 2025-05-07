package com.vlog.my.ui.screens.users

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vlog.my.data.model.Followers
import com.vlog.my.ui.viewmodels.FollowersViewModel

/**
 * 用户关注的列表界面
 * @param username 要查看的用户名，如果为null则查看当前登录用户
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen(
    username: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: FollowersViewModel = hiltViewModel()
) {
    val followingState by viewModel.followingState.collectAsState()
    val followActionState by viewModel.followActionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 加载关注列表
    LaunchedEffect(username) {
        viewModel.getFollowing(username)
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
                    val title = if (username == currentUser) "我的关注" else "$username 的关注"
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
            when (followingState) {
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
                            text = "暂无关注",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "您还没有关注任何用户",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is FollowersViewModel.FollowersState.Success -> {
                    val followers = (followingState as FollowersViewModel.FollowersState.Success).followers
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(followers) { follower ->
                            FollowerItem(
                                follower = follower,
                                isFollowing = true,
                                onUserClick = { onNavigateToUserProfile(follower.alternateId ?: follower.userId ?: "") },
                                onFollowClick = { viewModel.unfollowUser(follower.userId ?: "") }
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
                            text = (followingState as FollowersViewModel.FollowersState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.getFollowing() }
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

/**
 * 关注/粉丝项
 */
@Composable
fun FollowerItem(
    follower: Followers,
    isFollowing: Boolean,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            if (follower.userInfo?.avatar != null) {
                AsyncImage(
                    model = follower.userInfo.avatar,
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "头像",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 用户信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = follower.userInfo?.nickName ?: follower.userInfo?.name ?: "未知用户",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (follower.userInfo?.description != null) {
                    Text(
                        text = follower.userInfo.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 关注/取消关注按钮
            if (isFollowing) {
                OutlinedButton(
                    onClick = onFollowClick
                ) {
                    Text("取消关注")
                }
            } else {
                Button(
                    onClick = onFollowClick
                ) {
                    Text("关注")
                }
            }
        }
    }
}
