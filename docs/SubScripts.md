# SubScripts模块开发文档

## 1. 模块概述

SubScripts模块是一个独立的功能模块，用于支持应用内的"小程序"功能。该模块允许用户配置和访问外部API数据源，以JSON格式展示内容，包括视频、图片和文本等多媒体内容。

### 1.1 模块目标

- 提供独立的网络请求模块，不影响应用原有功能
- 实现本地数据持久化，支持离线访问
- 提供统一的UI展示界面，支持列表和详情页面
- 支持分类数据的多级展示
- 实现分页加载和刷新功能

### 1.2 技术栈

- **UI框架**: Jetpack Compose
- **网络请求**: Retrofit + OkHttp
- **数据持久化**: Room数据库
- **依赖注入**: Dagger Hilt
- **异步处理**: Kotlin Coroutines + Flow

## 2. 数据模型

### 2.1 API数据格式

SubScripts模块使用标准的JSON API格式，基本结构如下：

```json
{
  "status": 200,
  "code": 200,
  "page": {
    "pageindex": 1,
    "pagecount": 3127,
    "pagesize": 30,
    "recordcount": 93805
  },
  "list": [
    {
      "type_id": 16515072,
      "type_name": "动漫/海外动漫"
    },
    // 更多分类...
  ],
  "data": [
    {
      "vod_id": 1533693,
      "vod_name": "霹雳邪章之道劫龙战",
      "vod_pic": "https://www.taopianimage1.com:43333/33b2352a8c74v.jpeg",
      "vod_remarks": "连载中 连载到1集",
      "vod_content": "太玄封羲挟寰界之力席卷苦境...",
      "type_name": "动漫/国产动漫",
      "vod_actor": "青阳子/剑非道/寂无初/太玄封羲/天下式",
      "vod_director": "黄强华",
      // 更多字段...
    },
    // 更多视频数据...
  ]
}
```

### 2.2 核心数据类

#### 2.2.1 SubScripts (小程序配置)

```kotlin
data class SubScripts(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isTyped: Int?, // 0 = edge模式，1= json模式
    val createdBy: String?,
    val subUrl: String,
    val subKey: String
)
```

#### 2.2.2 JsonApiResponse (API响应)

```kotlin
data class JsonApiResponse(
    val status: Int,          // 状态码
    val code: Int,            // 响应码
    val page: PageInfo,       // 分页信息
    val list: List<Category>, // 分类列表 (原 class)
    val data: List<VideoItem> // 视频列表 (原 list)
)

data class PageInfo(
    val pageindex: Int,       // 当前页面
    val pagecount: Int,       // 总页码
    val pagesize: Int,        // 每页数据量
    val recordcount: Int?     // 总数据量
)
```

#### 2.2.3 Category (分类)

```kotlin
data class Category(
    val type_id: Long,        // 分类ID
    val type_name: String,    // 分类名称
    var parent_type_id: Long = 0 // 父分类ID，默认为0表示一级分类
)
```

#### 2.2.4 VideoItem (视频项)

```kotlin
data class VideoItem(
    val vod_id: Long,         // 视频ID
    val vod_name: String,     // 视频名称
    val vod_sub: String?,     // 视频副标题
    val vod_remarks: String?, // 视频备注（如：连载中 连载到5集）
    val vod_serial: Int?,     // 连载集数
    val type_id: Long,        // 分类ID
    val vod_actor: String?,   // 演员
    val vod_director: String?, // 导演
    val vod_pic: String?,     // 封面图片URL
    val vod_content: String?, // 内容简介
    val vod_time: String?,    // 更新时间
    val vod_area: String?,    // 地区
    val vod_lang: String?,    // 语言
    val vod_year: Int?,       // 年份
    val type_name: String?,   // 分类名称
    val vod_tag: String?,     // 标签
    val vod_play_url: String?, // 播放URL
    val vod_duration: Int?    // 时长（分钟）
)
```

### 2.3 数据库实体

#### 2.3.1 SubScriptEntity

```kotlin
@Entity(tableName = "subscripts")
data class SubScriptEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isTyped: Int?, // 0 = edge模式，1= json模式
    val createdBy: String?,
    val subUrl: String,
    val subKey: String
)
```

#### 2.3.2 CategoryEntity

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val type_id: Long,        // 分类ID
    val type_name: String,    // 分类名称
    val parent_type_id: Long, // 父分类ID，0表示一级分类
    val sub_url: String       // 所属小程序URL，用于区分不同小程序的分类
)
```

## 3. 模块架构

### 3.1 数据层 (Data Layer)

#### 3.1.1 API服务

- **JsonApiService**: 定义API接口和请求方法
  - `getVideoList`: 获取视频列表
  - `getVideoListByPage`: 获取分页视频列表

#### 3.1.2 数据库

- **SubScriptDatabase**: Room数据库实例
  - `SubScriptDao`: 小程序配置数据访问对象
  - `CategoryDao`: 分类数据访问对象

#### 3.1.3 仓库 (Repository)

- **JsonApiRepository**: 处理API请求和响应
- **SubScriptRepository**: 管理小程序配置
- **CategoryRepository**: 管理分类数据，处理分类的父子关系

### 3.2 业务层 (Domain Layer)

- **JsonApiViewModel**: 管理视频列表和分类数据
  - 加载视频列表
  - 加载更多视频
  - 管理分类数据
  - 处理分类的父子关系

### 3.3 表现层 (Presentation Layer)

- **JsonModeScreen**: 小程序主界面，根据状态显示列表或详情
- **JsonListScreen**: 显示视频列表和分类
- **JsonDetailScreen**: 显示视频详情
- **VideoList**: 视频列表组件，支持下拉加载更多
- **CategoryList**: 分类列表组件，支持二级分类显示

## 4. 功能实现

### 4.1 网络请求

使用Retrofit和OkHttp实现网络请求，主要功能包括：

- 配置OkHttp客户端，设置超时时间和日志拦截器
- 配置Retrofit实例，设置基础URL和转换器
- 实现JsonApiService接口，定义API请求方法
- 在Repository中封装API请求，处理异常和转换响应

### 4.2 数据持久化

使用Room数据库实现数据持久化，主要功能包括：

- 定义数据库实体类，映射到数据表
- 实现DAO接口，定义数据访问方法
- 配置数据库实例，设置版本和迁移策略
- 在Repository中封装数据库操作，提供统一的数据访问接口

### 4.3 分类处理

实现分类数据的处理和展示，主要功能包括：

- 解析分类名称中的"/"符号，建立父子关系
- 将分类数据保存到数据库，支持离线访问
- 加载一级分类和二级分类，支持分类筛选
- 在UI中展示二级分类，提供更好的用户体验

### 4.4 分页加载

实现分页加载和下拉刷新功能，主要功能包括：

- 在API请求中添加页码参数，支持分页加载
- 在ViewModel中管理分页状态，追加新数据而不是替换
- 在UI中监听滚动事件，触发加载更多
- 提供刷新按钮，允许用户手动刷新数据

## 5. 当前进度

### 5.1 已完成功能

1. **基础架构搭建**
   - 创建SubScripts模块的基本架构
   - 实现依赖注入配置
   - 设置网络请求模块
   - 配置Room数据库

2. **API集成**
   - 实现JsonApiService接口
   - 处理API响应格式
   - 支持分页请求

3. **数据持久化**
   - 实现SubScriptEntity和CategoryEntity
   - 创建DAO接口和数据库实例
   - 实现数据库迁移策略
   - 实现视频数据的本地缓存，支持离线访问

4. **UI实现**
   - 创建列表页面和详情页面
   - 实现视频列表和分类列表组件
   - 支持列表项点击导航到详情页
   - 实现列表和网格两种视图模式，并保存用户偏好
   - 添加视图切换按钮，允许用户在列表视图和网格视图之间切换

5. **分类处理**
   - 解析分类名称中的"/"符号
   - 建立分类的父子关系
   - 在UI中展示二级分类
   - 在分类名称后显示数据统计，帮助用户了解哪些分类有数据
   - 使用不同颜色区分有数据和无数据的分类

6. **分页加载**
   - 实现下拉加载更多功能
   - 添加刷新按钮
   - 优化加载状态管理
   - 只在"全部"分类下启用刷新功能，避免用户混淆
   - 改进滚动检测逻辑，避免少量数据时自动触发加载更多

7. **用户体验优化**
   - 保存用户的视图偏好设置（列表/网格），下次进入时自动应用
   - 优化错误提示和加载状态显示
   - 添加数据统计信息，显示当前分类的视频数量
   - 改进分类选择交互，提供更直观的分类导航

### 5.2 待完成功能

1. **UI进一步优化**
   - 添加更多自定义设置选项
   - 实现更多视图模式和布局选项

2. **性能优化**
   - 进一步优化列表滚动性能
   - 优化图片加载和缓存策略
   - 优化内存使用

3. **测试**
   - 编写单元测试
   - 进行UI测试
   - 进行性能测试

4. **扩展功能**
   - 实现搜索功能
   - 添加收藏和历史记录功能
   - 支持更多的内容交互方式

## 6. 使用示例

### 6.1 配置小程序

```kotlin
// 创建小程序配置
val subScript = SubScripts(
    title = "电影资源",
    isTyped = 1, // JSON模式
    subUrl = "https://taopianapi.com/cjapi/wc/vod/json.html",
    subKey = "your_api_key"
)

// 保存小程序配置
viewModel.saveSubScript(subScript)
```

### 6.2 加载数据

```kotlin
// 在ViewModel中加载数据
fun loadData(subScript: SubScripts) {
    viewModelScope.launch {
        // 加载第一页数据
        repository.getVideoListByPage(subScript.subUrl, 1)
    }
}
```

### 6.3 显示UI

```kotlin
@Composable
fun SubScriptScreen(subScript: SubScripts) {
    // 根据小程序类型显示不同的UI
    when (subScript.isTyped) {
        1 -> JsonModeScreen(subScript)
        else -> EdgeModeScreen(subScript)
    }
}
```

## 7. 功能详解

### 7.1 视图模式切换

SubScripts模块支持两种视图模式：列表视图和网格视图。用户可以根据自己的偏好选择不同的视图模式，系统会记住用户的选择，下次进入时自动应用。

#### 7.1.1 列表视图

列表视图以垂直列表的形式展示视频内容，每个项目占据整行，显示更多的详细信息：

- 视频封面图片
- 视频标题
- 视频备注（如：更新状态、集数等）
- 分类信息
- 演员和导演信息

列表视图适合需要查看详细信息的场景，提供更丰富的内容展示。

#### 7.1.2 网格视图

网格视图以网格形式展示视频内容，每行显示两个项目，主要展示：

- 视频封面图片
- 视频标题
- 视频备注（简化版）

网格视图适合浏览大量内容的场景，一屏可以显示更多的视频，减少滚动操作，提高浏览效率。

#### 7.1.3 视图偏好保存

系统使用SharedPreferences保存用户的视图偏好设置：

- 每个小程序有独立的视图偏好设置
- 使用小程序ID作为键的一部分，确保不同小程序的设置互不影响
- 在用户切换视图时自动保存偏好
- 在打开小程序时自动加载上次的视图设置

### 7.2 分类数据统计

为了帮助用户快速了解哪些分类有数据，系统在分类名称后显示该分类的视频数量统计：

#### 7.2.1 数据统计实现

- 使用Room数据库查询各分类的视频数量
- 在分类名称后显示数量，如"电影 (25)"
- 有数据的分类使用主题色显示，更加醒目
- 无数据的分类使用较淡的颜色显示

#### 7.2.2 统计数据更新

- 每次加载数据后，自动更新分类数据统计
- 使用Flow实现数据的实时更新
- 当数据库中的视频数据变化时，统计数据自动更新

### 7.3 刷新机制优化

为了避免用户混淆和提供更清晰的使用体验，系统对刷新机制进行了优化：

#### 7.3.1 分类刷新控制

- 只在"全部"分类下启用刷新功能
- 其他分类页面隐藏刷新按钮，禁用下拉刷新
- 在非"全部"分类下，提供切换到"全部"分类的按钮

#### 7.3.2 加载更多优化

- 改进滚动检测逻辑，避免少量数据时自动触发加载更多
- 对于少量数据（10个以下），显示"加载更多"按钮，让用户手动触发
- 对于大量数据，保留滚动到底部自动加载的功能

## 8. 注意事项

1. **API限制**
   - 小程序API只有一个接口，不需要单独的详情请求
   - 所有数据都在列表中，详情页直接使用列表数据

2. **性能考虑**
   - 避免自动刷新API请求造成卡顿
   - 添加用户控制的刷新按钮
   - 实现列表页面下拉到底部后请求下一页数据

3. **分类处理**
   - 使用type_name中的"/"分隔符解析上下级关系
   - 将分类数据保存到Room数据库
   - 在UI中实现二级分类显示

## 9. 小程序分享与积分系统

### 9.1 小程序分享机制

小程序分享机制是一种以用户为中心的内容分享方式，允许用户创建和分享小程序，同时保护创作者权益和促进用户间交流。

#### 9.1.1 核心概念

1. **用户中心化分享**
   - 小程序作为用户的"个人资产"，创建者拥有完全控制权
   - 创建者可以决定分享范围和使用条件
   - 分享数据主要在用户之间直接传递，减轻服务器负担

2. **分享限制机制**
   - 接收者可以使用小程序，但不能再次分享给其他用户
   - 确保创作者与内容之间的直接联系不被稀释
   - 防止内容被错误归属，保护创作者权益

3. **创作者归属**
   - 在分享内容中显示创作者信息
   - 当用户分享小程序内容到动态时，自动包含创作者信息
   - 提供联系创作者的快捷方式，促进用户间交流

#### 9.1.2 技术实现

1. **数据存储**
   - 使用加密的本地存储，不依赖中央服务器
   - 小程序数据包含所有必要的数据和元数据
   - 使用数字签名确保小程序的完整性和来源验证

2. **消息传递**
   - 通过消息系统作为小程序传递的主要渠道
   - 对分享的小程序进行加密，确保只有指定接收者可以使用
   - 设计特殊的消息类型专门用于小程序分享

3. **权限控制**
   - 基于接收者设备ID或账户的加密机制
   - 在小程序元数据中明确记录权限信息
   - 接收者只能使用或删除小程序，不能编辑或再次分享

### 9.2 积分/贡献值系统

积分系统为小程序创建和分享提供激励机制，形成一个内部经济循环系统。

#### 9.2.1 核心概念

1. **创作者价值回报**
   - 小程序创作者通过分享自己的创作获得积分/贡献值
   - 对创作者时间和技能投入的认可和回报
   - 创作高质量内容的动力机制

2. **内容价值定价**
   - 创作者可以为自己的小程序设定"价格"（以积分计)
   - 价格反映内容的价值、独特性或实用性
   - 支持免费、固定价格或"按意愿付费"等多种模式

3. **积分流通机制**
   - 用户通过各种活动获得积分
   - 积分可用于"购买"小程序使用权
   - 形成内部经济循环系统

#### 9.2.2 积分获取途径

1. **创作内容**
   - 创建并分享小程序
   - 发布优质动态或视频作品
   - 内容被点赞/收藏/分享

2. **社区贡献**
   - 参与讨论和评论
   - 举报不良内容
   - 帮助新用户

3. **日常活动**
   - 每日登录奖励
   - 完成特定任务
   - 参与活动和挑战

#### 9.2.3 小程序交易模式

1. **固定价格模式**
   - 创作者设定固定积分价格
   - 用户支付后获得使用权
   - 简单直接，适合大多数情况

2. **分级定价模式**
   - 基础版：免费或低积分
   - 高级版：更多功能，更高积分
   - 允许用户先体验后决定是否升级

3. **公开/私有设置**
   - 公开：所有人可见，可设定积分门槛
   - 私有：仅邀请可见，可免积分或设定特殊价格
   - 半公开：在特定圈子或群组中可见

### 9.3 实施路线图

#### 9.3.1 第一阶段：基础分享系统
- 实现小程序的基本分享功能
- 添加创作者信息显示
- 实现基本的权限控制

#### 9.3.2 第二阶段：积分基础系统
- 实现简单的积分获取和消费机制
- 支持固定价格的小程序交易
- 基本的小程序市场界面

#### 9.3.3 第三阶段：增强功能
- 添加多种定价模式
- 实现评价和反馈系统
- 改进市场界面和搜索功能

### 9.4 潜在挑战

1. **价值平衡**
   - 确保积分的获取难度与小程序价值匹配
   - 定期调整积分获取机制，监控市场价格趋势

2. **质量控制**
   - 确保付费小程序的质量
   - 实现退款机制、预览功能、用户评价系统

3. **系统滥用**
   - 防止用户刷分或绕过积分系统
   - 实现行为监控、交易限制、账户验证

## 10. 自定义JSON解析接口

为了支持不同内容类型（如电影、图书等）的JSON解析，我们设计了一个灵活的自定义解析接口，允许用户将外部API的字段名映射到我们的标准字段名。

### 10.1 设计目标

1. **灵活性**：支持不同内容类型的JSON格式
2. **易用性**：提供简单直观的配置界面
3. **可扩展性**：支持未来添加更多内容类型
4. **性能优化**：高效解析大量数据

### 10.2 核心组件

#### 10.2.1 字段映射模型

```kotlin
/**
 * 字段映射模型
 * 用于定义外部API字段名与内部标准字段名的映射关系
 */
@JsonClass(generateAdapter = true)
data class FieldMapping(
    val sourceField: String,      // 外部API字段名
    val targetField: String,      // 内部标准字段名
    val isRequired: Boolean = false, // 是否必需字段
    val defaultValue: String? = null // 默认值，当字段不存在时使用
)
```

#### 10.2.2 内容类型模型

```kotlin
/**
 * 内容类型枚举
 */
enum class ContentType(val typeName: String) {
    MOVIE("电影"),
    BOOK("图书"),
    MUSIC("音乐"),
    CUSTOM("自定义")
}

/**
 * 内容类型配置
 * 定义特定内容类型的字段映射和详情页面类型
 */
@JsonClass(generateAdapter = true)
data class ContentTypeConfig(
    val type: ContentType,
    val fieldMappings: List<FieldMapping>,
    val detailScreenType: DetailScreenType,
    val listResponsePath: String = "data", // JSON响应中列表数据的路径
    val categoryResponsePath: String = "list" // JSON响应中分类数据的路径
)

/**
 * 详情页面类型
 */
enum class DetailScreenType {
    VIDEO_PLAYER,  // 视频播放器
    BOOK_READER,   // 图书阅读器
    AUDIO_PLAYER,  // 音频播放器
    WEB_VIEW       // 网页视图
}
```

#### 10.2.3 SubScripts扩展

```kotlin
/**
 * 扩展SubScripts模型，添加内容类型和字段映射配置
 */
data class SubScripts(
    var id: String,
    var title: String,
    var isTyped: Int? = null,  // 0 = edge模式，1= json模式
    var createdBy: String? = null,
    var subUrl: String,
    var subKey: String,
    var contentTypeConfig: ContentTypeConfig? = null // 内容类型配置
)
```

#### 10.2.4 自定义JSON解析器

```kotlin
/**
 * 自定义JSON解析器
 * 根据字段映射配置解析JSON数据
 */
class CustomJsonParser(
    private val contentTypeConfig: ContentTypeConfig
) {
    /**
     * 解析JSON响应
     * @param jsonString JSON字符串
     * @return 解析后的JsonApiResponse
     */
    fun parseJsonResponse(jsonString: String): JsonApiResponse {
        val jsonObject = JSONObject(jsonString)

        // 解析分页信息
        val pageindex = jsonObject.optInt("pageindex", 1)
        val pagecount = jsonObject.optInt("pagecount", 1)
        val pagesize = jsonObject.optInt("pagesize", 20)
        val recordcount = jsonObject.optInt("recordcount", 0)

        // 解析分类列表
        val categoriesJson = jsonObject.optJSONArray(contentTypeConfig.categoryResponsePath) ?: JSONArray()
        val categories = parseCategories(categoriesJson)

        // 解析内容列表
        val itemsJson = jsonObject.optJSONArray(contentTypeConfig.listResponsePath) ?: JSONArray()
        val items = parseItems(itemsJson)

        return JsonApiResponse(
            pageindex = pageindex,
            pagecount = pagecount,
            pagesize = pagesize,
            recordcount = recordcount,
            categories = categories,
            videos = items
        )
    }

    // 其他解析方法...
}
```

### 10.3 实现流程

1. **配置界面**：用户选择内容类型并配置字段映射
2. **保存配置**：将配置保存到SubScripts实体中
3. **解析数据**：使用自定义解析器解析JSON数据
4. **展示内容**：根据内容类型选择适当的详情页面

### 10.4 预定义模板

为了简化用户配置，我们提供了几种常见内容类型的预定义模板：

#### 10.4.1 电影模板

```kotlin
val movieTemplate = ContentTypeConfig(
    type = ContentType.MOVIE,
    fieldMappings = listOf(
        FieldMapping("id", "vod_id", true),
        FieldMapping("title", "vod_name", true),
        FieldMapping("cover", "vod_pic"),
        FieldMapping("description", "vod_content"),
        FieldMapping("director", "vod_director"),
        FieldMapping("actor", "vod_actor"),
        FieldMapping("category", "type_name"),
        FieldMapping("year", "vod_year"),
        FieldMapping("area", "vod_area"),
        FieldMapping("language", "vod_lang"),
        FieldMapping("update", "vod_remarks"),
        FieldMapping("play_url", "vod_play_url")
    ),
    detailScreenType = DetailScreenType.VIDEO_PLAYER,
    listResponsePath = "data",
    categoryResponsePath = "list"
)
```

#### 10.4.2 图书模板

```kotlin
val bookTemplate = ContentTypeConfig(
    type = ContentType.BOOK,
    fieldMappings = listOf(
        FieldMapping("id", "vod_id", true),
        FieldMapping("title", "vod_name", true),
        FieldMapping("cover", "vod_pic"),
        FieldMapping("description", "vod_content"),
        FieldMapping("author", "vod_director"),
        FieldMapping("category", "type_name"),
        FieldMapping("year", "vod_year"),
        FieldMapping("publisher", "vod_area"),
        FieldMapping("update", "vod_remarks"),
        FieldMapping("read_url", "vod_play_url")
    ),
    detailScreenType = DetailScreenType.BOOK_READER,
    listResponsePath = "books",
    categoryResponsePath = "categories"
)
```

### 10.5 用户界面

自定义解析配置界面允许用户：

1. 选择内容类型（电影、图书、音乐等）
2. 选择详情页面类型（视频播放器、图书阅读器等）
3. 配置JSON响应中列表和分类数据的路径
4. 添加、编辑和删除字段映射
5. 预览解析结果

### 10.6 实现计划

1. **第一阶段**：基础字段映射功能
   - 实现ContentTypeConfig和FieldMapping模型
   - 扩展SubScripts模型
   - 实现基本的自定义解析器

2. **第二阶段**：配置界面
   - 实现内容类型选择界面
   - 实现字段映射配置界面
   - 添加预定义模板

3. **第三阶段**：详情页面适配
   - 根据内容类型选择适当的详情页面
   - 实现图书阅读器和音频播放器
   - 优化用户体验

## 11. 未来计划

1. **支持更多数据源**
   - 添加更多API格式支持
   - 实现自定义数据源配置

2. **增强用户体验**
   - 添加搜索功能
   - 实现收藏和历史记录
   - 支持自定义主题和布局

3. **扩展功能**
   - 添加下载功能
   - 实现内容分享
   - 支持用户评论和评分

4. **小程序生态系统**
   - 完善小程序分享机制
   - 实现积分/贡献值系统
   - 建立小程序市场

## 11. 参考资料

1. **API文档**
   - 标准格式: taopianapi.com
   - 响应结构: 包含list(分类)、data(数据)、pageindex、pagecount、pagesize和recordcount字段

2. **UI设计**
   - 参考设计: Screenshot_home.jpg和Screenshot_artworks_3.jpg

3. **技术文档**
   - Jetpack Compose: https://developer.android.com/jetpack/compose
   - Room: https://developer.android.com/training/data-storage/room
   - Retrofit: https://square.github.io/retrofit/
