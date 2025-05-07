package com.vlog.my.ui.screens.subscripts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubScriptScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubScriptsViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var subUrl by remember { mutableStateOf("") }
    var subKey by remember { mutableStateOf("") }
    var isTyped by remember { mutableStateOf(0) } // 默认为浏览器模式

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加小程序") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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

            Button(
                onClick = {
                    if (title.isNotBlank() && subUrl.isNotBlank() && subKey.isNotBlank()) {
                        viewModel.addSubScript(
                            title = title,
                            isTyped = isTyped,
                            subUrl = subUrl,
                            subKey = subKey
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}
