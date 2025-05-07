package com.vlog.my.ui.screens.subscripts.sharing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.SubScripts
import com.vlog.my.viewmodel.SubScriptSharingViewModel
import kotlinx.coroutines.launch

/**
 * 发送小程序屏幕
 * 允许用户配置和发送小程序
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSubScriptScreen(
    subScript: SubScripts,
    onNavigateBack: () -> Unit,
    viewModel: SubScriptSharingViewModel = hiltViewModel()
) {
    var recipientId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(subScript.title) }
    var description by remember { mutableStateOf("") }
    var pointsCost by remember { mutableStateOf("0") }
    var isEditable by remember { mutableStateOf(false) }
    var isShareable by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 显示错误消息
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发送小程序") },
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
        ) {
            OutlinedTextField(
                value = recipientId,
                onValueChange = { recipientId = it },
                label = { Text("接收者ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pointsCost,
                onValueChange = { 
                    // 只允许输入数字
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        pointsCost = it
                    }
                },
                label = { Text("积分成本") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isEditable,
                    onCheckedChange = { isEditable = it }
                )
                Text("允许编辑")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isShareable,
                    onCheckedChange = { isShareable = it }
                )
                Text("允许分享")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = {
                        if (recipientId.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请输入接收者ID")
                            }
                            return@Button
                        }
                        
                        if (title.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("请输入标题")
                            }
                            return@Button
                        }
                        
                        viewModel.createAndSendMessage(
                            subScript = subScript,
                            recipientId = recipientId,
                            title = title,
                            description = description,
                            pointsCost = pointsCost.toIntOrNull() ?: 0,
                            isEditable = isEditable,
                            isShareable = isShareable
                        )
                        
                        scope.launch {
                            snackbarHostState.showSnackbar("小程序已发送")
                        }
                        
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.width(24.dp).height(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("发送")
                    }
                }
            }
        }
    }
}
