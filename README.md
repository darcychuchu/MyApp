# MyApp - 个性分享应用

这是一个类似小红书的个性分享应用，使用Jetpack Compose和MVVM架构开发。

## 项目特点
- 使用Jetpack Compose构建现代化UI
- 采用MVVM架构模式，实现清晰的代码分层
- 全屏垂直滑动视频播放，类似抖音的用户体验
- 使用本地资源图标，避免因依赖包更新导致图标缺失

## 技术栈
- **网络请求**: Retrofit + OkHttp + Moshi
- **导航**: Jetpack Navigation Compose
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **图片加载**: Coil
- **视频播放**: ExoPlayer
- **语言**: Kotlin

## 项目结构
```
com.vlog.my/
├── data/
│   ├── api/           # API服务接口和实现
│   ├── model/         # 数据模型类
│   │   └── json/      # JSON API数据模型
│   ├── repository/    # 数据仓库
│   └── local/         # 本地数据存储
│       └── subscript/ # 小程序专用数据库
├── di/                # 依赖注入模块
├── ui/
│   ├── theme/         # 主题相关
│   ├── components/    # 可复用UI组件
│   └── screens/       # 应用各个屏幕
│       ├── home/      # 首页(图文列表)
│       ├── trending/  # 热门(短视频)
│       ├── publish/   # 发布
│       ├── messages/  # 信息
│       ├── profile/   # 我的(个人主页)
│       │   └── 独立导航系统 # 设置、粉丝、关注、消息、评论等
│       ├── users/     # 用户主页(查看其他用户)
│       │   └── 详情页面 # 动态详情、作品详情、视频播放等
│       ├── detail/    # 详情页面(自己的内容)
│       └── subscripts/ # 小程序功能
│           └── json/   # JSON模式小程序
├── utils/             # 工具类
└── viewmodel/         # 各屏幕的ViewModel
```

## 应用功能
1. **底部导航栏**：首页、热门、发布、信息、我
2. **顶部导航**：发现和关注（位于顶部中间）
3. **侧边栏菜单**：通过左上角汉堡图标打开，包含用户信息、小程序和设置等选项
4. **首页**：图文列表模式，展示用户分享的图文内容和视频作品
   - 支持混合内容展示（图文动态和视频作品）
   - 下拉刷新和滚动加载更多功能
   - 点击内容可查看详情，点击用户可查看用户主页
5. **热门**：短视频模式，支持全屏垂直滑动播放
6. **发布**：支持发布图文动态和视频作品
   - **图文发布**：支持拍照并上传多张图片，添加标题、描述和标签
   - **视频发布**：支持录制或选择视频，添加标题、描述和标签
7. **信息**：消息通知和互动信息
8. **我的**：用户个人资料和作品集
   - 显示用户基本信息
   - 展示用户发布的动态和作品
   - 支持刷新获取最新内容
   - 独立导航系统，管理设置、粉丝、关注、消息、评论等功能
   - 支持查看其他用户的主页和内容详情
9. **小程序**：
   - 支持浏览器模式和JSON模式的小程序
   - 可以添加、编辑和删除小程序
   - 浏览器模式可以在内置WebView中打开网页，支持JavaScript和各种Web功能
   - 支持网页内导航和链接跳转
   - JSON模式支持视频列表和详情页面，可浏览分类和内容
10. **用户认证**：
    - 支持用户注册和登录
    - 用户会话管理
    - 退出登录功能

## 开发进度
- [x] 项目初始化
- [x] 项目结构规划
- [x] 基础UI组件开发
  - [x] 底部导航栏
  - [x] 顶部导航栏（中间位置）
  - [x] 侧边栏菜单（左侧汉堡菜单）
  - [x] 基本页面结构
- [x] 网络层实现
  - [x] API接口定义
  - [x] 数据模型定义
  - [x] Retrofit配置
  - [x] 依赖注入设置
- [x] 本地存储实现
  - [x] Room数据库配置
  - [x] 数据访问对象（DAO）
  - [x] 本地数据仓库
- [x] 小程序功能
  - [x] 小程序列表页面
  - [x] 小程序添加页面
  - [x] 小程序编辑和删除功能
  - [x] 浏览器模式实现（内置WebView）
  - [x] JSON模式实现
    - [x] 视频列表页面（支持分页和分类）
    - [x] 视频详情页面
  - [x] 本地存储与远程同步
  - [x] 独立模块化设计
    - [x] 独立的NetworkModule
    - [x] 独立的Room数据库
    - [x] 与主应用完全隔离
- [x] 首页开发
  - [x] 图文列表UI实现
  - [x] 数据加载与展示
  - [x] 下拉刷新和加载更多功能
- [ ] 热门页面开发
  - [ ] 短视频播放器实现
  - [ ] 视频列表与滑动
- [x] 发布功能实现
  - [x] 图文发布
    - [x] 拍照功能
    - [x] 多图上传
    - [x] 表单验证
  - [x] 视频发布
    - [x] 视频拍摄
    - [x] 视频选择
    - [x] 视频上传
- [ ] 信息页面开发
- [x] 个人页面开发
  - [x] 用户信息展示
  - [x] 动态列表展示
  - [x] 作品列表展示
  - [x] 刷新功能
  - [x] 独立导航系统
  - [x] 用户主页功能（查看其他用户）
  - [x] 详情页面（动态和作品）
- [x] 用户认证实现
  - [x] 登录功能
  - [x] 注册功能
  - [x] 用户会话管理
  - [x] 退出登录
- [ ] 应用测试与优化


## API 接口

### API 响应处理标准

#### 主体功能模块响应模型
```kotlin
// 主体功能模块使用的响应模型
data class ApiResponse<T>(
    val code: Int = 200,  // 默认值设为200
    val message: String? = null,
    val data: T? = null
)

// 分页数据模型
data class PaginatedResponse<T>(
    val items: List<T>?,
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 10
)
```

#### SubScript 模块响应模型
```kotlin
// SubScript 模块使用的响应模型，独立于主体功能模块
data class SubScriptResponse<T>(
    val code: Int = 200,
    val message: String? = null,
    val data: T? = null
)

data class SubScriptStatusResponse(
    val code: Int = 200,
    val message: String? = null
)

data class SubScriptPage<T>(
    val data: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

data class SubScriptPaginatedResponse<T>(
    val items: List<T>?,
    val total: Int,
    val page: Int,
    val pageSize: Int
)
```

服务器内部使用的响应格式（仅供参考）：
```kotlin
// 服务器内部使用的密封类
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String, val errors: List<ApiError>? = null) : ApiResponse<Nothing>()
}

data class ApiError(
    val field: String?,
    val message: String
)
```

#### 响应码定义
```kotlin
object ApiResponseCode {
    // 成功响应码
    const val SUCCESS = 200

    // 错误响应码基线
    const val ERROR_BASELINE = 400

    // 客户端错误码 (4xx)
    const val BAD_REQUEST = 400
    const val UNAUTHORIZED = 401
    const val FORBIDDEN = 403
    const val NOT_FOUND = 404

    // 服务器错误码 (5xx)
    const val SERVER_ERROR = 500
    const val SERVICE_UNAVAILABLE = 503

    // 自定义业务错误码
    const val VALIDATION_ERROR = 422
    const val BUSINESS_ERROR = 460
}
```

#### 响应处理规范

1. **判断成功响应**：
   - 使用 `response.code == ApiResponseCode.SUCCESS` (即 `response.code == 200`) 判断请求是否成功
   - 成功时，处理 `response.data` 数据

2. **判断错误响应**：
   - 使用 `response.code >= ApiResponseCode.ERROR_BASELINE` (即 `response.code >= 400`) 判断是否为错误响应
   - 错误时，使用 `response.message` 获取错误信息

3. **Repository 层**：
   - 直接返回 API 响应，不处理业务逻辑
   - 对于网络异常，可以包装为 Result 类型
   - 示例：
     ```kotlin
     suspend fun getVideoList(url: String): Result<JsonApiResponse> = withContext(Dispatchers.IO) {
         try {
             val response = jsonApiService.getVideoList(url)
             Result.success(response)
         } catch (e: Exception) {
             Result.failure(e)
         }
     }
     ```

4. **ViewModel 层**：
   - 检查 `response.code == ApiResponseCode.SUCCESS` 判断请求是否成功
   - 成功时，检查 `response.data != null` 确保数据有效
   - 失败时，使用 `response.message` 或根据 code 提供特定错误信息
   - 示例：
     ```kotlin
     viewModelScope.launch {
         try {
             val response = repository.getUserInfo(name, token)
             if (response.code == ApiResponseCode.SUCCESS && response.data != null) {
                 // 处理成功响应
                 _userData.value = response.data
             } else if (response.code >= ApiResponseCode.ERROR_BASELINE) {
                 // 处理错误响应
                 _error.value = response.message ?: "未知错误"
             }
         } catch (e: Exception) {
             // 处理异常
             _error.value = "网络错误: ${e.message}"
         }
     }
     ```

5. **错误处理**：
   - 网络错误：捕获异常并提供友好的错误信息
   - 业务错误：根据 response.code 区分不同类型的业务错误
   - 数据验证：对于表单验证等场景，使用专门的验证状态

6. **特殊情况**：
   - 用户名/昵称验证：API 返回 `true` 表示已存在（不可用），`false` 表示可用
   - 对于 JsonApiResponse 等非标准响应，使用 Result 类型包装处理成功和失败情况

#### 响应处理规范

1. **主体功能模块**：
   - 使用 `ApiResponse<T>` 作为统一响应模型
   - 对于列表数据，使用 `ApiResponse<List<T>>`
   - 对于分页数据，使用 `ApiResponse<PaginatedResponse<T>>`
   - 对于不需要返回数据的 API，使用 `ApiResponse<Unit>`
   - 对于需要返回简单成功/失败状态的 API，使用 `ApiResponse<Boolean>`
   - 在处理响应时，统一使用 `response.code == 200` 判断成功，`response.code >= 400` 判断错误

2. **SubScript 模块**：
   - 使用独立的 `SubScriptResponse<T>` 和 `SubScriptStatusResponse` 作为响应模型
   - 这些响应模型与主体功能模块的响应模型分开，避免相互影响
   - SubScript 模块的响应处理逻辑与主体功能模块保持一致，但使用独立的响应类

### 基础 API URL
```
BASE_URL = "https://myapp.com/api/json/v1/"
IMAGE_BASE_URL = "https://myapp.com/file/attachments/image/s/{attachmentId}"
VIDEO_HLS_BASE_URL = "https://myapp.com/file/attachments/video/hls/{attachmentId}/index.m3u8"
```

### 用户相关 API
- 检查用户名是否存在: `GET users/stated-name?username=`
- 检查昵称是否存在: `GET users/stated-nickname?nickname=`
- 用户登录: `POST users/login?username=&password=`
- 用户注册: `POST users/register?username=&password=&nickname=`
- 获取用户信息: `GET users/stated-me/{name}/{token}`
- 更新用户信息: `POST users/updated/{name}/{token}?nickname=&avatar_file=`


### 数据模型

#### Userss模型
data class Users(
    var id: String? = null,
    var isLocked: Int? = null,
    var createdAt: Long? = null,
    var name: String? = null, //主要字段，跟全局API请求，唯一，不能重复
    var nickName: String? = null, //主要字段，唯一，不能重复
    var description: String? = null,
    var avatar: String? = null,  /// 存储attachmentId 通过 IMAGE_BASE_URL 展示头像
    var accessToken: String? = null
)

#### Stories模型
data class Stories(
    var id: String? = null,
    var createdAt: Long? = null,
    var isLocked: Int? = null,      // 锁定状态，影响视频播放权限
    var isValued: Int? = null,      // 是否有价值，影响推荐
    var isEnabled: Int? = null,     // 是否启用
    var isTyped: Int? = null,       // 主要字段 0=动态（图文 / stories），1=作品（视频 / artworks）
    var orderSort: Int? = null,
    var version: Int? = null,
    var createdBy: String? = null,  // 主要字段 存储Users 的name字段
    var attachmentId: String? = null, // 存储attachmentId 通过 IMAGE_BASE_URL 展示作品artworks封面，或者图文stories的列表图
    var title: String? = null,      // 可为空
    var description: String? = null, // 不能为空
    var tags: String? = null,       // 可为空
    var shareContent: String? = null, // 分享内容JSON，用于分享小程序视频等

    var createdByItem: Users? = null,
    var attachmentItem: Attachments? = null,
    var attachmentList: List<Attachments>? = null
)

data class Attachments(
    var id: String? = null,
    var createdAt: Long? = null,
    var isTyped: Int? = null,  // 9 = avatar，8= artworks cover ， 7 = Stories Images ，0 = file 下载文件
    var version: Int? = null,
    var createdBy: String? = null, ///主要字段 存储Users 的name字段
    var quoteId: String? = null, ///主要字段 存储Stories 的 id 字段
    var size: Long? = null
)




```
#### 发布规则
- 动态（图文）：`isTyped=0`
  - 可以是一句话
  - 可以是一张照片+一句话
  - 可以是多照片+一句话
  - description不能为空

- 作品（视频）：`isTyped=1`
  - 必须包含视频文件
  - description不能为空


#### 作品模块（主要为视频）
- 获取用户作品API:
  GET https://myapp.com/api/json/v1/{name}/artworks/list
  参数:
  - `token`: 用户认证令牌
  返回: `ApiResponse<List<Stories>>`


- 获取用户作品详情:
  GET https://myapp.com/api/json/v1/{name}/artworks/detail/{id}

  返回: `ApiResponse<StoriesDto>`

- 发布用户作品:
  POST https://myapp.com/api/json/v1/{name}/artworks-created?token=TOKEN
  参数:
  - `token`: 用户认证令牌
  - `videoFile`: 视频文件（一次只能上传一个视频）
  - `title`: 标题（可选）
  - `description`: 描述（必填）
  - `tags`: 标签（可选）
  返回: `ApiResponse<Unit>`

  - 获取视频文件: `GET https://myapp.com/file/attachments/video/{attachmentId}`
  说明: 通过附件ID获取视频文件流


#### 动态模块（主要为图文）

- 获取全局动态和作品列表API:
  GET https://myapp.com/api/json/v1/stories/list
  参数:
  - `typed`: 内容类型（0=图文，1=视频，-1=混合）
  - `page`: 页码
  - `token`: 用户认证令牌（可选）
  返回: `ApiResponse<PaginatedResponse<Stories>>`

- 获取用户动态API:
  GET https://myapp.com/api/json/v1/{name}/stories/list
  参数:
  - `token`: 用户认证令牌
  返回: `ApiResponse<List<Stories>>`


- 获取用户动态详情:
  GET https://myapp.com/api/json/v1/{name}/stories/detail/{id}

  返回: `ApiResponse<StoriesDto>`

- 发布用户动态:
  POST https://myapp.com/api/json/v1/{name}/stories-created?token=TOKEN
  参数:
  - `token`: 用户认证令牌
  - `photoFiles`: 图片文件（支持多张图片上传）
  - `title`: 标题（可选）
  - `description`: 描述（必填）
  - `tags`: 标签（可选）
  - `shareContent`: 分享内容JSON（可选，用于分享小程序视频等内容）
  返回: `ApiResponse<Unit>`


  - 获取图片文件: `GET https://myapp.com/file/attachments/video/{attachmentId}`
  说明: 通过附件ID获取图片流


#### 视频分享到动态功能

Stories模型中新增字段：
```kotlin
data class Stories(
    // 现有字段...

    // 新增字段，用于存储分享内容的JSON
    var shareContent: String? = null
)
```

分享内容JSON格式：
```json
{
  "subScriptId": "小程序ID",
  "videoId": "视频ID",
  "position": 12345,  // 播放位置（毫秒）
  "episodeIndex": 3,  // 集数（如果适用）
  "title": "视频标题",
  "coverUrl": "https://example.com/cover.jpg",  // 视频封面
  "description": "视频描述",
  "playerApiUrl": "https://api.example.com/player/xxx"  // 播放器API
}
```

实现流程：
1. 在小程序视频播放页面添加"分享到动态"按钮
2. 点击后，收集当前视频信息（标题、封面、播放位置等）
3. 跳转到动态发布页面，可以：
   - 自动生成描述文字（例如："我正在观看《视频标题》"）
   - 允许用户修改、追加或删除描述
4. 将视频信息序列化为JSON，存储在`shareContent`字段
5. 调用现有的`createStories` API发布动态

显示逻辑：
1. 在动态列表和详情页中，检测是否包含`shareContent`字段
2. 如果包含，解析JSON并显示视频相关信息（封面、标题等）
3. 提供"播放"按钮，点击后跳转到相应的视频播放页面

这种设计允许用户分享他们正在观看的小程序视频内容到动态中，增强社交互动和内容传播。


