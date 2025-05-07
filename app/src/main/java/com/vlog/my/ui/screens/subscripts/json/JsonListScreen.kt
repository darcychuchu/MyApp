package com.vlog.my.ui.screens.subscripts.json

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.Category
import com.vlog.my.data.model.json.VideoItem

/**
 * JSON模式列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JsonListScreen(
    subScript: SubScripts,
    onNavigateBack: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onNavigateToContentTypeConfig: ((String) -> Unit)? = null,
    viewModel: JsonApiViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val videoList by viewModel.videoList.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val parentCategories by viewModel.parentCategories.collectAsState()
    val childCategories by viewModel.childCategories.collectAsState()
    val pagination by viewModel.pagination.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedParentCategory by viewModel.selectedParentCategory.collectAsState()
    val categoryVideoCount by viewModel.categoryVideoCount.collectAsState()

    var currentPg by remember { mutableStateOf(1) }

    // 是否已初始加载数据
    val isInitialLoad = remember { mutableStateOf(true) }

    // 只在初次加载或页码变化时加载数据
    LaunchedEffect(subScript, currentPg, isInitialLoad.value) {
        if (isInitialLoad.value || currentPg > 1) {
            // 初始加载时，默认选择"全部"分类
            if (isInitialLoad.value) {
                // 查找"全部"分类
                val parentCats = viewModel.parentCategories.value
                if (parentCats.isNotEmpty()) {
                    val allCategory = parentCats.find { it.type_id == 0L }
                    if (allCategory != null) {
                        viewModel.selectParentCategory(allCategory)
                    }
                }
            }

            // 注意：初始加载数据的逻辑已移至JsonModeScreen中
            // 这里不再需要调用loadVideoList，避免重复加载

            isInitialLoad.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subScript.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 获取当前选中的分类和视图类型
                    val selectedCategory by viewModel.selectedCategory.collectAsState()
                    val viewType by viewModel.viewType.collectAsState()

                    // 内容类型配置按钮
                    if (onNavigateToContentTypeConfig != null) {
                        IconButton(
                            onClick = { onNavigateToContentTypeConfig(subScript.id) }
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                contentDescription = "配置内容类型"
                            )
                        }
                    }

                    // 视图切换按钮
                    IconButton(
                        onClick = { viewModel.toggleViewType() }
                    ) {
                        Icon(
                            imageVector = if (viewType == ViewType.LIST)
                                Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "切换视图"
                        )
                    }

                    // 只在"全部"分类下显示刷新按钮
                    if (selectedCategory?.type_id == 0L) {
                        IconButton(
                            onClick = {
                                currentPg = 1
                                viewModel.loadVideoList(subScript, currentPg)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 内容区域
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 分类标签页 - 始终显示，不受加载状态或错误影响
                if (parentCategories.isNotEmpty()) {
                    CategoryTabs(
                        parentCategories = parentCategories,
                        childCategories = childCategories,
                        selectedParentCategory = selectedParentCategory,
                        selectedChildCategory = selectedCategory,
                        categoryVideoCount = categoryVideoCount,
                        onParentCategorySelected = { category ->
                            viewModel.selectParentCategory(category)
                        },
                        onChildCategorySelected = { category ->
                            viewModel.selectCategory(category)
                        }
                    )
                }

                // 加载状态或错误信息
                if (isLoading && videoList.isEmpty()) {
                    // 加载中
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null && videoList.isEmpty()) {
                    // 错误信息
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = error ?: "加载失败",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 只在"全部"分类下显示刷新按钮
                            if (selectedCategory?.type_id == 0L) {
                                androidx.compose.material3.Button(
                                    onClick = {
                                        viewModel.clearError()
                                        currentPg = 1
                                        viewModel.loadVideoList(subScript, currentPg)
                                    }
                                ) {
                                    Text("刷新")
                                }
                            } else {
                                // 在其他分类下显示切换到"全部"分类的按钮
                                androidx.compose.material3.Button(
                                    onClick = {
                                        // 查找"全部"分类
                                        val allCategory = parentCategories.find { it.type_id == 0L }
                                        if (allCategory != null) {
                                            viewModel.selectParentCategory(allCategory)
                                        }
                                    }
                                ) {
                                    Text("切换到全部分类")
                                }
                            }
                        }
                    }
                } else {

                    // 数据统计信息
                    if (videoList.isNotEmpty()) {
                        Text(
                            text = "当前显示 ${videoList.size} 条视频",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // 错误提示（如果有）
                    if (error != null && videoList.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = error ?: "加载失败",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 只在"全部"分类下显示刷新按钮
                                if (selectedCategory?.type_id == 0L) {
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            viewModel.clearError()
                                            currentPg = 1
                                            viewModel.loadVideoList(subScript, currentPg)
                                        }
                                    ) {
                                        Text("刷新")
                                    }
                                }
                            }
                        }
                    }

                    // 获取当前视图类型
                    val viewType by viewModel.viewType.collectAsState()

                    // 根据视图类型显示不同的列表
                    when (viewType) {
                        ViewType.LIST -> {
                            // 列表视图
                            VideoList(
                                videos = videoList,
                                onVideoClick = onVideoClick,
                                // 只在"全部"分类下启用加载更多功能
                                onLoadMore = if (selectedCategory?.type_id == 0L) {
                                    {
                                        // 加载更多数据
                                        viewModel.loadMoreVideos(subScript)
                                    }
                                } else null
                            )
                        }
                        ViewType.GRID -> {
                            // 网格视图
                            VideoGridView(
                                videos = videoList,
                                onVideoClick = onVideoClick,
                                // 只在"全部"分类下启用加载更多功能
                                onLoadMore = if (selectedCategory?.type_id == 0L) {
                                    {
                                        // 加载更多数据
                                        viewModel.loadMoreVideos(subScript)
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 分类标签页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTabs(
    parentCategories: List<Category>,
    childCategories: List<Category>,
    selectedParentCategory: Category?,
    selectedChildCategory: Category?,
    categoryVideoCount: Map<Long, Int>,
    onParentCategorySelected: (Category) -> Unit,
    onChildCategorySelected: (Category) -> Unit
) {
    Column {
        // 一级分类标签页
        ScrollableTabRow(
            selectedTabIndex = parentCategories.indexOfFirst { it.type_id == selectedParentCategory?.type_id }
                .takeIf { it >= 0 } ?: 0,
            edgePadding = 16.dp
        ) {
            parentCategories.forEach { category ->
                Tab(
                    selected = category.type_id == selectedParentCategory?.type_id,
                    onClick = { onParentCategorySelected(category) },
                    text = { Text(category.type_name) }
                )
            }
        }

        // 二级分类（如果有）
        if (childCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(childCategories) { category ->
                    // 获取该分类的视频数量
                    val videoCount = categoryVideoCount[category.type_id] ?: 0

                    FilterChip(
                        selected = category.type_id == selectedChildCategory?.type_id,
                        onClick = { onChildCategorySelected(category) },
                        label = {
                            Text(
                                text = if (videoCount > 0) "${category.type_name} ($videoCount)" else category.type_name,
                                color = if (videoCount > 0)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 分类列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryList(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategoryClick: (Category) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category.type_id == selectedCategory?.type_id,
                onClick = { onCategoryClick(category) },
                label = { Text(category.type_name) }
            )
        }
    }
}

/**
 * 视频列表
 */
@Composable
fun VideoList(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onLoadMore: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()

    // 只有当启用了加载更多功能时，才检测滚动到底部
    if (onLoadMore != null) {
        LaunchedEffect(listState) {
            snapshotFlow {
                // 获取当前可见项和总项数
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = listState.layoutInfo.totalItemsCount

                // 只有当列表项数量大于5，并且用户明确滚动到接近底部时才触发加载更多
                totalItemsCount > 5 && lastVisibleItemIndex >= totalItemsCount - 2
            }
            .collect { isNearBottom ->
                if (isNearBottom && videos.isNotEmpty()) {
                    onLoadMore.invoke()
                }
            }
        }
    }

    // 添加一个手动加载更多的按钮，当列表项少于5个时使用
    val showLoadMoreButton = videos.size in 1..5

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(videos) { video ->
            VideoListItem(
                video = video,
                onClick = { onVideoClick(video) }
            )
        }

        // 如果有数据且启用了加载更多功能，显示加载状态或加载更多按钮
        if (videos.isNotEmpty() && onLoadMore != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLoadMoreButton) {
                        // 对于少量数据，显示加载更多按钮
                        androidx.compose.material3.Button(
                            onClick = { onLoadMore.invoke() }
                        ) {
                            Text("加载更多")
                        }
                    } else {
                        // 对于大量数据，显示上拉加载更多提示
                        Text(
                            text = "上拉加载更多",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 视频列表项
 */
@Composable
fun VideoListItem(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // 封面图片
            AsyncImage(
                model = video.vod_pic,
                contentDescription = video.vod_name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 视频信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.vod_name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                video.vod_remarks?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                video.type_name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                video.vod_actor?.let {
                    Text(
                        text = "演员: $it",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                video.vod_director?.let {
                    Text(
                        text = "导演: $it",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                video.vod_content?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 视频网格视图
 */
@Composable
fun VideoGridView(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onLoadMore: (() -> Unit)? = null
) {
    val gridState = rememberLazyGridState()

    // 只有当启用了加载更多功能时，才检测滚动到底部
    if (onLoadMore != null) {
        LaunchedEffect(gridState) {
            snapshotFlow {
                // 获取当前可见项和总项数
                val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItemsCount = gridState.layoutInfo.totalItemsCount

                // 只有当列表项数量大于10，并且用户明确滚动到接近底部时才触发加载更多
                totalItemsCount > 10 && lastVisibleItemIndex >= totalItemsCount - 4
            }
            .collect { isNearBottom ->
                if (isNearBottom && videos.isNotEmpty()) {
                    onLoadMore.invoke()
                }
            }
        }
    }

    // 添加一个手动加载更多的按钮，当列表项少于10个时使用
    val showLoadMoreButton = videos.size in 1..10

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos.size) { index ->
            val video = videos[index]
            VideoGridItem(
                video = video,
                onClick = { onVideoClick(video) }
            )
        }

        // 如果有数据且启用了加载更多功能，显示加载状态或加载更多按钮
        if (videos.isNotEmpty() && onLoadMore != null) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLoadMoreButton) {
                        // 对于少量数据，显示加载更多按钮
                        androidx.compose.material3.Button(
                            onClick = { onLoadMore.invoke() }
                        ) {
                            Text("加载更多")
                        }
                    } else {
                        // 对于大量数据，显示上拉加载更多提示
                        Text(
                            text = "上拉加载更多",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 视频网格项
 */
@Composable
fun VideoGridItem(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // 封面图片
            AsyncImage(
                model = video.vod_pic,
                contentDescription = video.vod_name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            )

            // 标题
            Text(
                text = video.vod_name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            // 备注信息
            video.vod_remarks?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * 分页控制
 */
@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一页按钮
        IconButton(
            onClick = {
                if (currentPage > 1) {
                    onPageChange(currentPage - 1)
                }
            },
            enabled = currentPage > 1
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上一页"
            )
        }

        // 页码信息
        Text(
            text = "$currentPage / $totalPages",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 下一页按钮
        IconButton(
            onClick = {
                if (currentPage < totalPages) {
                    onPageChange(currentPage + 1)
                }
            },
            enabled = currentPage < totalPages
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下一页"
            )
        }
    }
}
