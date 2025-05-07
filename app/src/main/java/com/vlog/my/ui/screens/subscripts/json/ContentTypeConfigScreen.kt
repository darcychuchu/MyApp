package com.vlog.my.ui.screens.subscripts.json

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.ContentType
import com.vlog.my.data.model.json.ContentTypeConfig
import com.vlog.my.data.model.json.DetailScreenType
import com.vlog.my.data.model.json.FieldMapping
import com.vlog.my.data.parser.CustomParserService

/**
 * 内容类型配置界面
 * 允许用户配置内容类型和字段映射
 */
@Composable
fun ContentTypeConfigScreen(
    subScript: SubScripts,
    customParserService: CustomParserService,
    onSave: (SubScripts) -> Unit,
    onCancel: () -> Unit
) {
    var selectedContentType by remember {
        mutableStateOf(subScript.contentTypeConfig?.type ?: ContentType.MOVIE)
    }
    var selectedDetailScreenType by remember {
        mutableStateOf(subScript.contentTypeConfig?.detailScreenType ?: DetailScreenType.VIDEO_PLAYER)
    }
    var listResponsePath by remember {
        mutableStateOf(subScript.contentTypeConfig?.listResponsePath ?: "data")
    }
    var categoryResponsePath by remember {
        mutableStateOf(subScript.contentTypeConfig?.categoryResponsePath ?: "list")
    }

    // 字段映射列表
    val fieldMappings = remember {
        mutableStateListOf<FieldMapping>().apply {
            // 添加默认映射或现有映射
            val config = subScript.contentTypeConfig
            if (config != null && config.fieldMappings.isNotEmpty()) {
                addAll(config.fieldMappings)
            } else {
                // 添加默认映射
                when (selectedContentType) {
                    ContentType.MOVIE -> addAll(customParserService.getMovieTemplate().fieldMappings)
                    ContentType.BOOK -> addAll(customParserService.getBookTemplate().fieldMappings)
                    ContentType.MUSIC -> addAll(customParserService.getMusicTemplate().fieldMappings)
                    ContentType.CUSTOM -> add(FieldMapping("", "", false, null))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "内容类型配置",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 内容类型选择
        Text(
            text = "选择内容类型",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContentType.values().forEach { contentType ->
                FilterChip(
                    selected = selectedContentType == contentType,
                    onClick = {
                        selectedContentType = contentType
                        // 更新字段映射
                        if (contentType != ContentType.CUSTOM) {
                            fieldMappings.clear()
                            when (contentType) {
                                ContentType.MOVIE -> fieldMappings.addAll(customParserService.getMovieTemplate().fieldMappings)
                                ContentType.BOOK -> fieldMappings.addAll(customParserService.getBookTemplate().fieldMappings)
                                ContentType.MUSIC -> fieldMappings.addAll(customParserService.getMusicTemplate().fieldMappings)
                                else -> {}
                            }
                        }
                    },
                    label = { Text(contentType.typeName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 详情页面类型选择
        Text(
            text = "选择详情页面类型",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailScreenType.values().forEach { screenType ->
                FilterChip(
                    selected = selectedDetailScreenType == screenType,
                    onClick = { selectedDetailScreenType = screenType },
                    label = { Text(getDetailScreenTypeName(screenType)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // JSON路径配置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = listResponsePath,
                onValueChange = { listResponsePath = it },
                label = { Text("列表数据路径") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = categoryResponsePath,
                onValueChange = { categoryResponsePath = it },
                label = { Text("分类数据路径") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 字段映射列表
        Text(
            text = "字段映射配置",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(fieldMappings.size) { index ->
                val mapping = fieldMappings[index]
                FieldMappingItem(
                    mapping = mapping,
                    onUpdate = { updatedMapping ->
                        fieldMappings[index] = updatedMapping
                    },
                    onDelete = {
                        fieldMappings.removeAt(index)
                    }
                )
            }

            item {
                Button(
                    onClick = {
                        fieldMappings.add(FieldMapping("", "", false, null))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加字段映射")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("取消")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    // 创建内容类型配置
                    val contentTypeConfig = ContentTypeConfig(
                        type = selectedContentType,
                        fieldMappings = fieldMappings.toList(),
                        detailScreenType = selectedDetailScreenType,
                        listResponsePath = listResponsePath,
                        categoryResponsePath = categoryResponsePath
                    )

                    // 更新SubScripts
                    val updatedSubScript = subScript.copy(
                        contentTypeConfig = contentTypeConfig
                    )

                    // 保存
                    onSave(updatedSubScript)
                }
            ) {
                Text("保存")
            }
        }
    }
}

/**
 * 字段映射项
 */
@Composable
fun FieldMappingItem(
    mapping: FieldMapping,
    onUpdate: (FieldMapping) -> Unit,
    onDelete: () -> Unit
) {
    var sourceField by remember { mutableStateOf(mapping.sourceField) }
    var targetField by remember { mutableStateOf(mapping.targetField) }
    var isRequired by remember { mutableStateOf(mapping.isRequired) }
    var defaultValue by remember { mutableStateOf(mapping.defaultValue ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = sourceField,
                    onValueChange = {
                        sourceField = it
                        onUpdate(mapping.copy(sourceField = it))
                    },
                    label = { Text("外部API字段名") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetField,
                    onValueChange = {
                        targetField = it
                        onUpdate(mapping.copy(targetField = it))
                    },
                    label = { Text("内部标准字段名") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = defaultValue,
                    onValueChange = {
                        defaultValue = it
                        onUpdate(mapping.copy(defaultValue = if (it.isEmpty()) null else it))
                    },
                    label = { Text("默认值（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Checkbox(
                        checked = isRequired,
                        onCheckedChange = {
                            isRequired = it
                            onUpdate(mapping.copy(isRequired = it))
                        }
                    )

                    Text("必需字段")
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

/**
 * 获取详情页面类型名称
 */
@Composable
fun getDetailScreenTypeName(detailScreenType: DetailScreenType): String {
    return when (detailScreenType) {
        DetailScreenType.VIDEO_PLAYER -> "视频播放器"
        DetailScreenType.BOOK_READER -> "图书阅读器"
        DetailScreenType.AUDIO_PLAYER -> "音频播放器"
        DetailScreenType.WEB_VIEW -> "网页视图"
    }
}
