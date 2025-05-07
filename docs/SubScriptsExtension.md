# SubScripts 拓展性规划文档

## 1. 概述

本文档描述了SubScripts模块的拓展性规划，旨在将当前的简单JSON实现转变为用户可主导的工具平台。这个规划考虑了个人开发者的资源限制，提供了渐进式的实现路径。

## 2. 拓展性架构

### 2.1 模板化系统

**核心思想**：提供预定义的模板，用户可以基于这些模板创建自己的小程序。

**组件**：
- 基础模板库
- 模板配置界面
- 预览功能

### 2.2 数据源灵活性

**核心思想**：支持多种数据来源，让用户可以连接自己的数据。

**组件**：
- 多种API格式支持
- 本地数据导入
- 数据转换工具

### 2.3 UI自定义

**核心思想**：让用户能够在一定程度上自定义小程序的外观。

**组件**：
- 主题选择
- 布局选项
- 自定义图标和品牌

### 2.4 功能模块化

**核心思想**：将小程序功能模块化，用户可以选择需要的功能。

**组件**：
- 核心功能模块
- 模块配置界面
- 渐进式复杂度

## 3. 技术架构

### 3.1 模板系统

```kotlin
// 模板接口
interface SubScriptTemplate {
    val id: String
    val name: String
    val type: TemplateType
    fun createScreen(navController: NavController): @Composable () -> Unit
    fun getConfigScreen(navController: NavController): @Composable () -> Unit
}

// 模板类型
enum class TemplateType {
    JSON_API,
    WEB_VIEW,
    IMAGE_GALLERY,
    SIMPLE_TOOL
}
```

### 3.2 配置系统

```kotlin
// JSON API配置
data class JsonApiConfig(
    val apiUrl: String,
    val apiKey: String?,
    val updateInterval: Long = 3600000, // 默认1小时
    val layoutType: LayoutType = LayoutType.LIST,
    val enabledModules: Set<ModuleType> = setOf(ModuleType.DISPLAY)
)

// 布局类型
enum class LayoutType {
    LIST,
    GRID,
    CARD
}

// 功能模块类型
enum class ModuleType {
    DISPLAY,  // 基础显示功能
    SEARCH,   // 搜索功能
    FAVORITE, // 收藏功能
    SHARE,    // 分享功能
    COMMENT   // 评论功能
}
```

### 3.3 数据处理

```kotlin
// 数据源接口
interface DataSource {
    suspend fun fetchData(page: Int, pageSize: Int): Result<List<Any>>
    suspend fun fetchDetail(id: String): Result<Any>
}

// API数据源
class ApiDataSource(
    private val url: String,
    private val apiKey: String?,
    private val mapper: DataMapper
) : DataSource {
    // 实现...
}

// 本地文件数据源
class LocalFileDataSource(
    private val fileUri: Uri,
    private val mapper: DataMapper
) : DataSource {
    // 实现...
}

// 数据映射器
interface DataMapper {
    fun mapToEntities(rawData: String): List<Any>
    fun mapToDetailEntity(rawData: String): Any
}
```

## 4. 实现优先级和路线图

### 4.1 第一阶段（基础拓展）

1. **完善现有JSON模板**
   - 增强稳定性和用户体验
   - 优化错误处理和加载状态
   - 改进UI响应性

2. **添加简单的UI自定义选项**
   - 主题颜色选择
   - 布局选择（列表/网格）
   - 字体大小调整

3. **实现基本的模板配置界面**
   - JSON API配置表单
   - 配置保存和加载
   - 基本预览功能

### 4.2 第二阶段（数据源扩展）

1. **添加本地数据导入功能**
   - 支持JSON文件上传
   - 基本数据验证
   - 导入向导

2. **扩展API格式支持**
   - 增加1-2种常见格式
   - 格式自动检测
   - 格式转换工具

3. **添加简单的数据过滤和排序功能**
   - 基于字段的过滤
   - 多字段排序
   - 保存过滤/排序偏好

### 4.3 第三阶段及以后

1. **功能模块实现**
   - 搜索和收藏功能
   - 分享功能
   - 评论功能

2. **高级功能**
   - 向导式创建流程
   - 模板市场
   - 高级数据转换

## 5. 当前可实现的功能点

以下是几个可以立即开始实现的功能点，考虑到个人开发者的资源限制：

### 5.1 UI主题自定义

**描述**：允许用户选择小程序的主题颜色和暗/亮模式。

**实现步骤**：
1. 创建主题配置数据类
2. 在SubScripts实体中添加主题配置字段
3. 添加主题选择UI
4. 实现主题应用逻辑

**代码示例**：
```kotlin
// 主题配置
data class ThemeConfig(
    val primaryColor: Color = Color(0xFF6200EE),
    val isDarkMode: Boolean = false
)

// 更新SubScripts实体
@Entity(tableName = "subscripts")
data class SubScriptEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isTyped: Int?,
    val createdBy: String?,
    val subUrl: String,
    val subKey: String,
    val themeConfig: String? = null // 存储JSON格式的主题配置
)

// 主题选择UI
@Composable
fun ThemeSelectionScreen(
    currentTheme: ThemeConfig,
    onThemeSelected: (ThemeConfig) -> Unit
) {
    // 实现...
}
```

### 5.2 布局切换增强

**描述**：扩展现有的列表/网格切换功能，添加更多布局选项和自定义。

**实现步骤**：
1. 定义更多布局类型（卡片、瀑布流等）
2. 在配置中添加布局相关参数（如列数、间距等）
3. 实现布局切换UI
4. 保存用户布局偏好

**代码示例**：
```kotlin
// 布局配置
data class LayoutConfig(
    val type: LayoutType = LayoutType.LIST,
    val columns: Int = 2, // 网格模式的列数
    val showDetails: Boolean = true, // 是否显示详细信息
    val cardElevation: Dp = 4.dp // 卡片模式的阴影
)

// 布局选择UI
@Composable
fun LayoutSelectionScreen(
    currentLayout: LayoutConfig,
    onLayoutSelected: (LayoutConfig) -> Unit
) {
    // 实现...
}
```

### 5.3 本地JSON文件导入

**描述**：允许用户上传JSON文件作为小程序的数据源。

**实现步骤**：
1. 创建文件选择和上传UI
2. 实现JSON文件解析
3. 将数据映射到现有模型
4. 存储到Room数据库

**代码示例**：
```kotlin
// 文件导入服务
class JsonFileImportService(private val context: Context) {
    suspend fun importFile(uri: Uri): Result<List<VideoItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { 
                    it.bufferedReader().readText() 
                } ?: return@withContext Result.failure(IOException("Cannot read file"))
                
                val jsonData = Json.decodeFromString<JsonApiResponse>(jsonString)
                Result.success(jsonData.data)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// 文件导入UI
@Composable
fun JsonFileImportScreen(
    onFileImported: (List<VideoItem>) -> Unit
) {
    // 实现...
}
```

### 5.4 基本搜索功能

**描述**：为小程序添加基本的搜索功能，允许用户搜索本地缓存的数据。

**实现步骤**：
1. 在VideoDao中添加搜索方法
2. 创建搜索UI组件
3. 实现搜索逻辑
4. 显示搜索结果

**代码示例**：
```kotlin
// 在VideoDao中添加搜索方法
@Dao
interface VideoDao {
    // 现有方法...
    
    @Query("SELECT * FROM videos WHERE sub_url = :subUrl AND (vod_name LIKE '%' || :query || '%' OR vod_content LIKE '%' || :query || '%')")
    fun searchVideos(subUrl: String, query: String): Flow<List<VideoEntity>>
}

// 搜索ViewModel扩展
class JsonApiViewModel : ViewModel() {
    // 现有代码...
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    val searchResults = searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.length >= 2) {
                repository.searchVideos(currentSubUrl, query)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

// 搜索UI
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    // 实现...
}
```

### 5.5 配置保存和加载

**描述**：实现小程序配置的保存和加载功能，允许用户保存自定义设置。

**实现步骤**：
1. 设计配置数据结构
2. 实现配置序列化和反序列化
3. 创建配置UI
4. 保存配置到SharedPreferences或数据库

**代码示例**：
```kotlin
// 小程序配置
data class SubScriptConfig(
    val themeConfig: ThemeConfig = ThemeConfig(),
    val layoutConfig: LayoutConfig = LayoutConfig(),
    val updateInterval: Long = 3600000, // 1小时
    val enabledModules: Set<String> = setOf("display")
)

// 配置管理器
class ConfigManager(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("subscript_configs", Context.MODE_PRIVATE)
    
    fun saveConfig(subScriptId: String, config: SubScriptConfig) {
        val json = Json.encodeToString(config)
        sharedPrefs.edit().putString(subScriptId, json).apply()
    }
    
    fun getConfig(subScriptId: String): SubScriptConfig {
        val json = sharedPrefs.getString(subScriptId, null)
        return if (json != null) {
            try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                SubScriptConfig()
            }
        } else {
            SubScriptConfig()
        }
    }
}
```

## 6. 实现注意事项

1. **渐进式实现**：
   - 从最简单的功能开始
   - 确保每个功能都完全可用后再添加新功能
   - 保持向后兼容性

2. **用户体验优先**：
   - 保持界面简洁直观
   - 提供清晰的错误信息和帮助提示
   - 确保性能流畅，避免卡顿

3. **代码质量**：
   - 使用接口和抽象类实现松耦合
   - 编写单元测试确保功能正确性
   - 使用依赖注入便于测试和扩展

4. **资源限制考虑**：
   - 优化内存和存储使用
   - 实现增量更新减少网络流量
   - 使用缓存提高性能

## 7. 结论

这个拓展性规划为SubScripts模块提供了一个可行的发展路径，将其从简单的JSON查看器转变为用户可主导的工具平台。通过渐进式实现，即使是个人开发者也可以逐步构建这个系统，同时为用户提供越来越强大的功能。

从当前可实现的功能点开始，可以快速提升SubScripts模块的用户体验和功能性，为后续更复杂功能的实现奠定基础。
