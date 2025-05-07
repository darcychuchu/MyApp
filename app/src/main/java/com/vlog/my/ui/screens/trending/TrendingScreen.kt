package com.vlog.my.ui.screens.trending

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vlog.my.ui.components.AppDrawer
import com.vlog.my.ui.theme.MyAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    onNavigateToSubScripts: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("发现", "关注")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                onProfileClick = onNavigateToProfile,
                onSubScriptsClick = onNavigateToSubScripts,
                onSettingsClick = onNavigateToSettings
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
                                            modifier = Modifier.padding(horizontal = 8.dp),
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
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                // 短视频内容区域
                when (selectedTabIndex) {
                    0 -> TrendingDiscoverContent(onNavigateToUserProfile = onNavigateToUserProfile)
                    1 -> TrendingFollowContent(onNavigateToUserProfile = onNavigateToUserProfile)
                }
            }
        }
    }
}

@Composable
fun TrendingDiscoverContent(onNavigateToUserProfile: (String) -> Unit = {}) {
    // 热门短视频 - 发现内容
    Text("热门短视频 - 发现")
    // 这里将来会添加实际的内容列表，并支持点击用户头像导航到用户主页
}

@Composable
fun TrendingFollowContent(onNavigateToUserProfile: (String) -> Unit = {}) {
    // 热门短视频 - 关注内容
    Text("热门短视频 - 关注")
    // 这里将来会添加实际的内容列表，并支持点击用户头像导航到用户主页
}

@Preview(showBackground = true)
@Composable
fun TrendingScreenPreview() {
    MyAppTheme {
        TrendingScreen()
    }
}
