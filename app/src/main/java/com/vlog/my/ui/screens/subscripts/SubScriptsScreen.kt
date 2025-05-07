package com.vlog.my.ui.screens.subscripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vlog.my.data.model.SubScripts
import com.vlog.my.ui.navigation.NavigationManager
import com.vlog.my.ui.screens.subscripts.SubScriptsRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScriptsScreen(
    onNavigateBack: () -> Unit,
    onAddSubScript: () -> Unit,
    onSubScriptClick: (SubScripts) -> Unit,
    onEditSubScript: (String) -> Unit = {},
    onOpenBrowserMode: (SubScripts) -> Unit = {},
    onOpenJsonMode: (SubScripts) -> Unit = {},
    onSendSubScript: (SubScripts) -> Unit = {},
    onViewReceivedMessages: () -> Unit = {},
    onViewReceivedSubScripts: () -> Unit = {},
    onOpenEbookList: () -> Unit = {},
    viewModel: SubScriptsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val subScripts by viewModel.subScripts.collectAsState(initial = emptyList())

    // 检查是否有待打开的电子书
    LaunchedEffect(Unit) {
        NavigationManager.pendingEbookId?.let { ebookId ->
            android.util.Log.d("SubScriptsScreen", "检测到待打开电子书: $ebookId")
            // 导航到电子书阅读器
            onOpenEbookList()
            // 清除待打开的电子书ID
            NavigationManager.pendingEbookId = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小程序") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 查看接收到的消息
                    IconButton(onClick = onViewReceivedMessages) {
                        Icon(Icons.Default.Email, contentDescription = "接收到的消息")
                    }

                    // 查看接收到的小程序
                    IconButton(onClick = onViewReceivedSubScripts) {
                        Icon(Icons.Default.Inbox, contentDescription = "接收到的小程序")
                    }

                    // 电子书
                    IconButton(onClick = onOpenEbookList) {
                        Icon(Icons.Default.Book, contentDescription = "电子书")
                    }

                    // 增强型JSON
                    IconButton(onClick = {
                        navController?.navigate(SubScriptsRoute.EnhancedJsonList.createRoute())
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "增强型JSON")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSubScript) {
                Icon(Icons.Default.Add, contentDescription = "添加小程序")
            }
        }
    ) { paddingValues ->
        if (subScripts.isEmpty()) {
            // 显示空状态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "暂无小程序",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击右下角按钮添加小程序",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            // 显示小程序列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(subScripts) { subScript ->
                    SubScriptItem(
                        subScript = subScript,
                        onClick = { onSubScriptClick(subScript) },
                        onEdit = { onEditSubScript(subScript.id) },
                        onOpen = {
                            if (subScript.isTyped == 0) {
                                onOpenBrowserMode(subScript)
                            } else if (subScript.isTyped == 1) {
                                onOpenJsonMode(subScript)
                            }
                        },
                        onSend = { onSendSubScript(subScript) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScriptItem(
    subScript: SubScripts,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onOpen: () -> Unit,
    onSend: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = subScript.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (subScript.isTyped == 0) "浏览器模式" else "JSON模式",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // URL显示
                Text(
                    text = subScript.subUrl,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )

                // 操作按钮
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑"
                    )
                }

                IconButton(onClick = onOpen) {
                    Icon(
                        imageVector = if (subScript.isTyped == 0)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.Build,
                        contentDescription = "打开"
                    )
                }

                IconButton(onClick = onSend) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "分享"
                    )
                }
            }
        }
    }
}
