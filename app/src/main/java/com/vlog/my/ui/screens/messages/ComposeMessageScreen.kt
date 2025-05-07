package com.vlog.my.ui.screens.messages

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * 发送消息屏幕
 * 允许用户向指定用户发送消息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeMessageScreen(
    recipientId: String,
    recipientName: String,
    onNavigateBack: () -> Unit,
    viewModel: ComposeMessageViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val messageSent by viewModel.messageSent.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    // 设置接收者ID
    LaunchedEffect(recipientId) {
        viewModel.setRecipientId(recipientId)
    }

    // 处理错误消息
    LaunchedEffect(error) {
        error?.let {
            Log.d("ComposeMessageScreen", "显示错误消息: $it")
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    // 处理发送成功
    LaunchedEffect(messageSent) {
        if (messageSent) {
            Log.d("ComposeMessageScreen", "消息发送成功，显示提示并返回")
            scope.launch {
                snackbarHostState.showSnackbar("消息已发送")
                // 重置状态
                viewModel.resetMessageSent()
                // 返回上一页
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发送消息给 $recipientName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // 主题输入
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("主题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 内容输入
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 发送按钮
            Button(
                onClick = {
                    Log.d("ComposeMessageScreen", "点击发送按钮")

                    if (subject.isBlank()) {
                        Log.d("ComposeMessageScreen", "主题为空")
                        scope.launch {
                            snackbarHostState.showSnackbar("请输入主题")
                        }
                        return@Button
                    }

                    if (content.isBlank()) {
                        Log.d("ComposeMessageScreen", "内容为空")
                        scope.launch {
                            snackbarHostState.showSnackbar("请输入内容")
                        }
                        return@Button
                    }

                    Log.d("ComposeMessageScreen", "调用 viewModel.sendMessage: subject=$subject, content=$content")
                    viewModel.sendMessage(subject, content)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送中...")
                } else {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送消息")
                }
            }
        }
    }
}
