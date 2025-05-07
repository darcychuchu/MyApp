package com.vlog.my.ui.screens.subscripts.enhanced

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.EnhancedJsonConfig
import java.util.UUID

/**
 * 增强型JSON配置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedJsonConfigScreen(
    configId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: EnhancedJsonViewModel = hiltViewModel()
) {
    val selectedConfig by viewModel.selectedConfig.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 加载配置
    LaunchedEffect(configId) {
        if (configId != null) {
            viewModel.loadConfigById(configId)
        }
    }

    // 处理操作状态
    LaunchedEffect(operationState) {
        when (operationState) {
            is EnhancedJsonViewModel.OperationState.Success -> {
                snackbarHostState.showSnackbar((operationState as EnhancedJsonViewModel.OperationState.Success).message)
                viewModel.resetOperationState()
                if (configId == null) {
                    // 如果是新建配置，保存成功后返回
                    onNavigateBack()
                }
            }
            is EnhancedJsonViewModel.OperationState.Error -> {
                snackbarHostState.showSnackbar((operationState as EnhancedJsonViewModel.OperationState.Error).message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    // 表单状态
    var name by remember { mutableStateOf(selectedConfig?.name ?: "") }
    var apiKey by remember { mutableStateOf(selectedConfig?.apiKey ?: "") }
    var listUrl by remember { mutableStateOf(selectedConfig?.listUrl ?: "") }
    var detailUrl by remember { mutableStateOf(selectedConfig?.detailUrl ?: "") }
    var searchUrl by remember { mutableStateOf(selectedConfig?.searchUrl ?: "") }
    var categoryUrl by remember { mutableStateOf(selectedConfig?.categoryUrl ?: "") }
    var pageParamName by remember { mutableStateOf(selectedConfig?.pageParamName ?: "page") }
    var sizeParamName by remember { mutableStateOf(selectedConfig?.sizeParamName ?: "size") }
    var classParamName by remember { mutableStateOf(selectedConfig?.classParamName ?: "class") }
    var keyParamName by remember { mutableStateOf(selectedConfig?.keyParamName ?: "key") }
    var idParamName by remember { mutableStateOf(selectedConfig?.idParamName ?: "id") }
    var keywordParamName by remember { mutableStateOf(selectedConfig?.keywordParamName ?: "keyword") }
    var defaultPageSize by remember { mutableStateOf((selectedConfig?.defaultPageSize ?: 20).toString()) }
    var maxPageSize by remember { mutableStateOf((selectedConfig?.maxPageSize ?: 30).toString()) }
    var listDataPath by remember { mutableStateOf(selectedConfig?.listDataPath ?: "list") }
    var detailDataPath by remember { mutableStateOf(selectedConfig?.detailDataPath ?: "data") }
    var categoryDataPath by remember { mutableStateOf(selectedConfig?.categoryDataPath ?: "class") }
    var searchDataPath by remember { mutableStateOf(selectedConfig?.searchDataPath ?: "list") }

    // 更新表单状态
    LaunchedEffect(selectedConfig) {
        selectedConfig?.let {
            name = it.name
            apiKey = it.apiKey
            listUrl = it.listUrl
            detailUrl = it.detailUrl
            searchUrl = it.searchUrl
            categoryUrl = it.categoryUrl
            pageParamName = it.pageParamName
            sizeParamName = it.sizeParamName
            classParamName = it.classParamName
            keyParamName = it.keyParamName
            idParamName = it.idParamName
            keywordParamName = it.keywordParamName
            defaultPageSize = it.defaultPageSize.toString()
            maxPageSize = it.maxPageSize.toString()
            listDataPath = it.listDataPath
            detailDataPath = it.detailDataPath
            categoryDataPath = it.categoryDataPath
            searchDataPath = it.searchDataPath
        }
    }

    // 验证表单
    val isFormValid = name.isNotBlank() &&
            apiKey.isNotBlank() &&
            listUrl.isNotBlank() &&
            detailUrl.isNotBlank() &&
            searchUrl.isNotBlank() &&
            categoryUrl.isNotBlank() &&
            pageParamName.isNotBlank() &&
            sizeParamName.isNotBlank() &&
            classParamName.isNotBlank() &&
            keyParamName.isNotBlank() &&
            idParamName.isNotBlank() &&
            keywordParamName.isNotBlank() &&
            listDataPath.isNotBlank() &&
            detailDataPath.isNotBlank() &&
            categoryDataPath.isNotBlank() &&
            searchDataPath.isNotBlank() &&
            defaultPageSize.toIntOrNull() != null &&
            maxPageSize.toIntOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (configId == null) "新建增强型JSON配置" else "编辑增强型JSON配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isFormValid) {
                                val config = EnhancedJsonConfig(
                                    id = selectedConfig?.id ?: UUID.randomUUID().toString(),
                                    name = name,
                                    apiKey = apiKey,
                                    listUrl = listUrl,
                                    detailUrl = detailUrl,
                                    searchUrl = searchUrl,
                                    categoryUrl = categoryUrl,
                                    pageParamName = pageParamName,
                                    sizeParamName = sizeParamName,
                                    classParamName = classParamName,
                                    keyParamName = keyParamName,
                                    idParamName = idParamName,
                                    keywordParamName = keywordParamName,
                                    defaultPageSize = defaultPageSize.toIntOrNull() ?: 20,
                                    maxPageSize = maxPageSize.toIntOrNull() ?: 30,
                                    listDataPath = listDataPath,
                                    detailDataPath = detailDataPath,
                                    categoryDataPath = categoryDataPath,
                                    searchDataPath = searchDataPath,
                                    createdAt = selectedConfig?.createdAt ?: System.currentTimeMillis()
                                )

                                if (configId == null) {
                                    viewModel.saveConfig(config)
                                } else {
                                    viewModel.updateConfig(config)
                                }
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
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
            // 显示加载中状态
            if (false) { // 暂时禁用加载状态，因为没有定义Loading状态
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 基本信息
                    Text(
                        text = "基本信息",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("配置名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API密钥") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 接口URL
                    Text(
                        text = "接口URL",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = listUrl,
                        onValueChange = { listUrl = it },
                        label = { Text("列表接口URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = detailUrl,
                        onValueChange = { detailUrl = it },
                        label = { Text("详情接口URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchUrl,
                        onValueChange = { searchUrl = it },
                        label = { Text("搜索接口URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = categoryUrl,
                        onValueChange = { categoryUrl = it },
                        label = { Text("分类接口URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 参数名称
                    Text(
                        text = "参数名称",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = pageParamName,
                            onValueChange = { pageParamName = it },
                            label = { Text("分页参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = sizeParamName,
                            onValueChange = { sizeParamName = it },
                            label = { Text("大小参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = classParamName,
                            onValueChange = { classParamName = it },
                            label = { Text("分类参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = keyParamName,
                            onValueChange = { keyParamName = it },
                            label = { Text("密钥参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = idParamName,
                            onValueChange = { idParamName = it },
                            label = { Text("ID参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = keywordParamName,
                            onValueChange = { keywordParamName = it },
                            label = { Text("关键词参数名") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 分页设置
                    Text(
                        text = "分页设置",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = defaultPageSize,
                            onValueChange = { defaultPageSize = it },
                            label = { Text("默认页大小") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = maxPageSize,
                            onValueChange = { maxPageSize = it },
                            label = { Text("最大页大小") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 数据路径
                    Text(
                        text = "数据路径",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = listDataPath,
                            onValueChange = { listDataPath = it },
                            label = { Text("列表数据路径") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = detailDataPath,
                            onValueChange = { detailDataPath = it },
                            label = { Text("详情数据路径") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = categoryDataPath,
                            onValueChange = { categoryDataPath = it },
                            label = { Text("分类数据路径") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = searchDataPath,
                            onValueChange = { searchDataPath = it },
                            label = { Text("搜索数据路径") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 保存按钮
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val config = EnhancedJsonConfig(
                                    id = selectedConfig?.id ?: UUID.randomUUID().toString(),
                                    name = name,
                                    apiKey = apiKey,
                                    listUrl = listUrl,
                                    detailUrl = detailUrl,
                                    searchUrl = searchUrl,
                                    categoryUrl = categoryUrl,
                                    pageParamName = pageParamName,
                                    sizeParamName = sizeParamName,
                                    classParamName = classParamName,
                                    keyParamName = keyParamName,
                                    idParamName = idParamName,
                                    keywordParamName = keywordParamName,
                                    defaultPageSize = defaultPageSize.toIntOrNull() ?: 20,
                                    maxPageSize = maxPageSize.toIntOrNull() ?: 30,
                                    listDataPath = listDataPath,
                                    detailDataPath = detailDataPath,
                                    categoryDataPath = categoryDataPath,
                                    searchDataPath = searchDataPath,
                                    createdAt = selectedConfig?.createdAt ?: System.currentTimeMillis()
                                )

                                if (configId == null) {
                                    viewModel.saveConfig(config)
                                } else {
                                    viewModel.updateConfig(config)
                                }
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (configId == null) "保存配置" else "更新配置")
                    }
                }
            }
        }
    }
}
