package com.vlog.my.ui.screens.subscripts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.SubScripts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubScriptScreen(
    subScriptId: String,
    onNavigateBack: () -> Unit,
    onNavigateToContentTypeConfig: ((String) -> Unit)? = null,
    viewModel: SubScriptsViewModel = hiltViewModel()
) {
    // 加载当前SubScript
    val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
    val subScript = cachedSubScripts.find { it.id == subScriptId }

    // 确保SubScript不为空
    if (subScript == null) {
        return
    }

    var title by remember { mutableStateOf(subScript.title) }
    var subUrl by remember { mutableStateOf(subScript.subUrl) }
    var subKey by remember { mutableStateOf(subScript.subKey) }
    var isTyped by remember { mutableStateOf(subScript.isTyped ?: 0) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑小程序") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 模式选择
            Text("选择模式", modifier = Modifier.align(Alignment.Start))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isTyped == 0,
                    onClick = { isTyped = 0 }
                )
                Text("浏览器模式")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isTyped == 1,
                    onClick = { isTyped = 1 }
                )
                Text("JSON模式")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = subUrl,
                onValueChange = { subUrl = it },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = subKey,
                onValueChange = { subKey = it },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 只有当选择JSON模式时才显示内容类型配置按钮
            if (isTyped == 1 && onNavigateToContentTypeConfig != null) {
                Button(
                    onClick = { onNavigateToContentTypeConfig(subScriptId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("配置内容类型")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && subUrl.isNotBlank() && subKey.isNotBlank()) {
                        val updatedSubScript = SubScripts(
                            id = subScript.id,
                            title = title,
                            isTyped = isTyped,
                            createdBy = subScript.createdBy,
                            subUrl = subUrl,
                            subKey = subKey,
                            contentTypeConfig = subScript.contentTypeConfig // 保留原有的内容类型配置
                        )
                        viewModel.updateSubScript(updatedSubScript)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("删除小程序") },
                text = { Text("确定要删除这个小程序吗？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSubScript(subScript)
                            showDeleteConfirmDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
}
