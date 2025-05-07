# 小程序模块开发文档

## 1. 模块概述

小程序模块（SubScripts）是应用的辅助模块，允许用户添加和管理外部内容源。该模块支持两种模式：
- Web模式：通过WebView加载网页内容
- JSON模式：通过API获取结构化数据并展示

## 2. 数据结构

### 2.1 SubScripts 模型

```kotlin
data class SubScripts(
    var id: String,           // 小程序唯一ID
    var title: String,        // 小程序标题
    var isTyped: Int? = null, // 0 = edge模式，1 = json模式 
    var createdBy: String? = null, // 创建者用户名
    var subUrl: String,       // 小程序URL
    var subKey: String        // 小程序密钥
)
```

### 2.2 VideoShareContent 模型

```kotlin
data class VideoShareContent(
    val subScriptId: String,           // 小程序ID
    val videoId: String,               // 视频ID
    val position: Long = 0,            // 播放位置（毫秒）
    val episodeIndex: Int? = null,     // 剧集索引，如"第1集"中的1
    val title: String,                 // 视频标题
    val coverUrl: String,              // 封面图片URL
    val description: String,           // 视频描述
    val playerApiUrl: String           // 播放API URL
)
```

## 3. 功能模块

### 3.1 小程序管理

- 添加小程序
- 编辑小程序
- 删除小程序
- 列表展示

### 3.2 内容展示

- Web模式：WebView加载
- JSON模式：列表展示和详情页

### 3.3 视频播放

- 支持HLS视频流播放
- 支持全屏模式
- 支持剧集选择

### 3.4 内容分享

- 分享视频到动态
- 在动态中展示分享内容
- 从动态跳转到视频播放

## 4. 跨用户分享问题解决方案

在多用户环境下，小程序内容的跨用户分享面临一些挑战，包括小程序唯一性、新用户体验等问题。以下是适合个人开发者的实用解决方案：

### 4.1 简化的小程序注册中心

**实现方式**：
- 在现有数据库中添加一个简单的表，记录小程序URL和唯一ID的映射
- 当用户添加新小程序时，检查URL是否已存在，复用已有ID或生成新ID
- 不需要复杂的认证流程，只需确保ID的唯一性

**成本估计**：
- 开发时间：1-2天
- 服务器负担：几乎可以忽略不计
- 维护成本：极低

### 4.2 基础错误处理和用户提示

**实现方式**：
- 在尝试加载分享内容失败时，显示清晰的错误信息
- 提供简单的指导，如"该内容可能需要安装特定小程序"
- 添加一个"添加小程序"的快捷入口

**成本估计**：
- 开发时间：半天到1天
- 服务器负担：无
- 维护成本：无

### 4.3 分享内容元数据优化

**实现方式**：
- 确保分享内容包含视频标题、描述、封面图等基本信息
- 存储直接的视频URL而不仅是引用
- 添加小程序名称和简短描述，帮助用户理解内容来源

**成本估计**：
- 开发时间：1天
- 服务器负担：略有增加（存储更多元数据）
- 维护成本：低

### 4.4 用户友好的分享提示

**实现方式**：
- 在分享对话框中添加简单提示："此内容最佳体验需要安装相应小程序"
- 提供选项让用户决定是否包含小程序信息在分享内容中

**成本估计**：
- 开发时间：几小时
- 服务器负担：无
- 维护成本：无

### 4.5 简单的内容可用性检查

**实现方式**：
- 尝试验证视频URL是否可访问
- 如果不可访问，显示替代内容（如封面图和描述）
- 提供手动刷新选项

**成本估计**：
- 开发时间：1天
- 服务器负担：略有增加（需要检查URL）
- 维护成本：低

## 5. 实施路线图

### 5.1 第一阶段（1-2周）
1. 实现简化的小程序注册中心
2. 优化分享内容元数据
3. 添加用户友好的分享提示

### 5.2 第二阶段（按需实施）
1. 实现基础错误处理和用户提示
2. 添加简单的内容可用性检查

### 5.3 长期考虑
随着用户增长和资源增加，可以逐步考虑：
1. 添加简单的地域检测，提供基本的服务器选择建议
2. 实现轻量级的内容缓存机制，只缓存最热门内容
3. 建立简单的小程序评分系统，帮助用户识别高质量内容

## 6. 技术实现细节

### 6.1 数据库设计

小程序表（SubScripts）：
- id: TEXT PRIMARY KEY
- title: TEXT NOT NULL
- isTyped: INTEGER
- createdBy: TEXT
- subUrl: TEXT NOT NULL
- subKey: TEXT NOT NULL

### 6.2 API接口

JSON模式API标准：
- 基本URL格式：`{baseUrl}/api/json/v1/{name}/{endpoint}`
- 分类列表：`/api/json/v1/{name}/categories`
- 视频列表：`/api/json/v1/{name}/videos`
- 视频详情：`/api/json/v1/{name}/detail`

### 6.3 UI组件

- SubScriptsScreen：小程序列表页面
- WebViewScreen：Web模式页面
- JsonListScreen：JSON模式列表页面
- JsonDetailScreen：JSON模式详情页面
- ShareToStoriesDialog：分享对话框
- SharedVideoContent：分享内容展示组件

## 7. 注意事项

- 小程序URL必须是有效的HTTP/HTTPS URL
- JSON模式API必须符合标准格式
- 视频播放需要支持HLS格式
- 分享内容应包含足够的元数据，减少对原始小程序的依赖
