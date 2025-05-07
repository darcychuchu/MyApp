package com.vlog.my.ui.screens.subscripts.ebook

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.vlog.my.data.model.ebook.EbookEntity
import com.vlog.my.ui.navigation.NavigationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 电子书列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EbookListScreen(
    onNavigateBack: () -> Unit,
    onEbookClick: (String) -> Unit,
    onShareEbook: (String) -> Unit,
    viewModel: EbookViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val importState by viewModel.importState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 检查是否有待打开的电子书
    LaunchedEffect(Unit) {
        NavigationManager.pendingEbookId?.let { ebookId ->
            android.util.Log.d("EbookListScreen", "检测到待打开电子书: $ebookId")
            // 导航到电子书阅读器
            onEbookClick(ebookId)
            // 清除待打开的电子书ID
            NavigationManager.pendingEbookId = null
        }
    }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importEbook(it) }
    }

    // 处理导入状态
    LaunchedEffect(importState) {
        when (importState) {
            is EbookImportState.Success -> {
                snackbarHostState.showSnackbar("电子书导入成功")
                viewModel.resetImportState()

                // 导航到阅读器界面
                val ebookId = (importState as EbookImportState.Success).ebookId
                onEbookClick(ebookId)
            }
            is EbookImportState.Error -> {
                snackbarHostState.showSnackbar(
                    (importState as EbookImportState.Error).message
                )
                viewModel.resetImportState()
            }
            else -> {}
        }
    }

    var ebookToDelete by remember { mutableStateOf<EbookEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("电子书") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("text/plain") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加电子书")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState) {
                is EbookListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is EbookListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as EbookListUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadEbooks() }) {
                            Text("重试")
                        }
                    }
                }
                is EbookListUiState.Success -> {
                    val ebooks = (uiState as EbookListUiState.Success).ebooks
                    if (ebooks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "没有电子书",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击右下角的按钮添加电子书",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            items(ebooks) { ebook ->
                                EbookItem(
                                    ebook = ebook,
                                    onClick = { onEbookClick(ebook.id) },
                                    onDelete = { ebookToDelete = ebook },
                                    onShare = { onShareEbook(ebook.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 导入中
            if (importState is EbookImportState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在导入电子书...")
                    }
                }
            }
        }
    }

    // 删除确认对话框
    ebookToDelete?.let { ebook ->
        AlertDialog(
            onDismissRequest = { ebookToDelete = null },
            title = { Text("删除电子书") },
            text = { Text("确定要删除《${ebook.title}》吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEbook(ebook)
                        ebookToDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { ebookToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 电子书项
 */
@Composable
fun EbookItem(
    ebook: EbookEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = ebook.title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ebook.author ?: "未知作者",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = formatFileSize(ebook.fileSize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "上次阅读: ${formatDate(ebook.lastReadDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0

    return when {
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$size 字节"
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "未读"

    val date = Date(timestamp)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(date)
}
