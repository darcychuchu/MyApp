# 自定义JSON解析接口集成指南

本文档提供了如何将自定义JSON解析接口集成到应用中的指南。

## 1. 添加内容类型配置导航

在应用的导航图中添加内容类型配置导航：

```kotlin
// 在应用的导航图中添加内容类型配置导航
fun NavGraphBuilder.subScriptsNavGraph(navController: NavController) {
    // 现有的小程序导航...
    
    // 添加内容类型配置导航
    contentTypeConfigNavigation(navController)
}
```

## 2. 添加内容类型配置按钮

在小程序编辑界面或详情界面添加内容类型配置按钮：

```kotlin
@Composable
fun SubScriptDetailScreen(
    subScript: SubScripts,
    onNavigateToContentTypeConfig: (String) -> Unit,
    // 其他参数...
) {
    // 现有的UI...
    
    // 添加内容类型配置按钮
    Button(
        onClick = { onNavigateToContentTypeConfig(subScript.id) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("配置内容类型")
    }
}
```

## 3. 处理导航

在导航处理代码中添加内容类型配置导航：

```kotlin
// 在导航处理代码中添加内容类型配置导航
val onNavigateToContentTypeConfig: (String) -> Unit = { subScriptId ->
    navController.navigate(getContentTypeConfigRoute(subScriptId))
}
```

## 4. 使用自定义解析器

在加载数据时使用自定义解析器：

```kotlin
// 在ViewModel中使用自定义解析器
viewModel.loadVideoList(subScript)
```

## 5. 测试

测试自定义解析功能：

1. 创建一个小程序
2. 配置内容类型和字段映射
3. 加载数据，验证解析结果是否正确

## 6. 注意事项

- 确保在SubScriptDatabase中添加了TypeConverters注解
- 确保在SubScriptModule中注册了CustomParserService
- 确保在JsonApiRepository中使用了自定义解析器
- 确保在JsonApiViewModel中传递了subScriptId参数
