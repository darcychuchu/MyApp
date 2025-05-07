package com.vlog.my.ui.screens.subscripts.enhanced

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.EnhancedJsonConfig

/**
 * 增强型JSON主界面
 * 包含列表、分类、搜索三个标签页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJsonMainScreen(
    configId: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String, String) -> Unit,
    onNavigateToSearch: (String) -> Unit,
    viewModel: EnhancedJsonViewModel = hiltViewModel()
) {
    val selectedConfig by viewModel.selectedConfig.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // 加载配置
    LaunchedEffect(configId) {
        viewModel.loadConfigById(configId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(selectedConfig?.name ?: "增强型JSON") 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToSearch(configId) }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
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
            if (selectedConfig == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column {
                    // 标签页
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("列表") }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("分类") }
                        )
                    }
                    
                    // 标签页内容
                    when (selectedTabIndex) {
                        0 -> EnhancedJsonContentList(
                            config = selectedConfig!!,
                            onItemClick = { itemId -> 
                                onNavigateToDetail(configId, itemId) 
                            }
                        )
                        1 -> EnhancedJsonCategoryList(
                            config = selectedConfig!!,
                            onCategoryClick = { categoryId ->
                                // 切换到列表标签页，并设置当前分类
                                selectedTabIndex = 0
                                // TODO: 设置当前分类
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 增强型JSON内容列表
 */
@Composable
fun EnhancedJsonContentList(
    config: EnhancedJsonConfig,
    onItemClick: (String) -> Unit
) {
    // TODO: 实现内容列表
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "内容列表（待实现）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * 增强型JSON分类列表
 */
@Composable
fun EnhancedJsonCategoryList(
    config: EnhancedJsonConfig,
    onCategoryClick: (String) -> Unit
) {
    // TODO: 实现分类列表
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "分类列表（待实现）",
            style = MaterialTheme.typography.titleMedium
        )
    }
}
