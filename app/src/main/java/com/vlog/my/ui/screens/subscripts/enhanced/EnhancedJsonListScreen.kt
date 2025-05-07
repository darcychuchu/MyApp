package com.vlog.my.ui.screens.subscripts.enhanced

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.EnhancedJsonConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 增强型JSON配置列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJsonListScreen(
    onNavigateBack: () -> Unit,
    onAddConfig: () -> Unit,
    onEditConfig: (String) -> Unit,
    onOpenConfig: (String) -> Unit,
    viewModel: EnhancedJsonViewModel = hiltViewModel()
) {
    val configsState by viewModel.configsState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var configToDelete by remember { mutableStateOf<EnhancedJsonConfig?>(null) }

    // 加载配置
    LaunchedEffect(Unit) {
        viewModel.loadAllConfigs()
    }

    // 处理操作状态
    LaunchedEffect(operationState) {
        when (operationState) {
            is EnhancedJsonViewModel.OperationState.Success -> {
                snackbarHostState.showSnackbar((operationState as EnhancedJsonViewModel.OperationState.Success).message)
                viewModel.resetOperationState()
            }
            is EnhancedJsonViewModel.OperationState.Error -> {
                snackbarHostState.showSnackbar((operationState as EnhancedJsonViewModel.OperationState.Error).message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("增强型JSON配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddConfig() }) {
                Icon(Icons.Default.Add, contentDescription = "添加配置")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (configsState) {
                is EnhancedJsonViewModel.ConfigsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EnhancedJsonViewModel.ConfigsState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "暂无配置",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角按钮添加配置",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is EnhancedJsonViewModel.ConfigsState.Success -> {
                    val configs = (configsState as EnhancedJsonViewModel.ConfigsState.Success).configs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(configs) { config ->
                            ConfigItem(
                                config = config,
                                onEdit = { onEditConfig(config.id) },
                                onDelete = { configToDelete = config },
                                onOpen = { onOpenConfig(config.id) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                is EnhancedJsonViewModel.ConfigsState.Error -> {
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
                            text = (configsState as EnhancedJsonViewModel.ConfigsState.Error).message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // 删除确认对话框
    configToDelete?.let { config ->
        AlertDialog(
            onDismissRequest = { configToDelete = null },
            title = { Text("删除配置") },
            text = { Text("确定要删除配置\"${config.name}\"吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConfig(config)
                        configToDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { configToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 配置项
 */
@Composable
fun ConfigItem(
    config: EnhancedJsonConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = config.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "API密钥: ${config.apiKey}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "创建时间: ${formatDate(config.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onOpen) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "打开",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}
