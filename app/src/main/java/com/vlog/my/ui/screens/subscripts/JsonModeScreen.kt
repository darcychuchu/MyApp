package com.vlog.my.ui.screens.subscripts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.VideoItem
import com.vlog.my.ui.screens.subscripts.json.JsonApiViewModel
import com.vlog.my.ui.screens.subscripts.json.JsonDetailScreen
import com.vlog.my.ui.screens.subscripts.json.JsonListScreen

/**
 * JSON模式小程序主屏幕
 * 根据状态显示列表页面或详情页面
 */
@Composable
fun JsonModeScreen(
    subScript: SubScripts,
    onNavigateBack: () -> Unit,
    onNavigateToContentTypeConfig: ((String) -> Unit)? = null
) {
    // 共享ViewModel以便在列表和详情页面之间传递数据
    val viewModel: JsonApiViewModel = hiltViewModel()

    // 确保在初始加载时请求API数据并加载保存的视图偏好
    androidx.compose.runtime.LaunchedEffect(subScript) {
        // 直接加载第一页数据，不考虑分类
        // loadVideoList方法内部会自动加载保存的视图偏好
        viewModel.loadVideoList(subScript, 1)
    }

    // 当前选中的视频
    var selectedVideo by remember { mutableStateOf<VideoItem?>(null) }

    if (selectedVideo == null) {
        // 显示列表页面
        JsonListScreen(
            subScript = subScript,
            onNavigateBack = onNavigateBack,
            onVideoClick = { video ->
                // 设置选中的视频到ViewModel
                viewModel.selectVideo(video)
                selectedVideo = video
            },
            onNavigateToContentTypeConfig = onNavigateToContentTypeConfig,
            viewModel = viewModel
        )
    } else {
        // 显示详情页面，传递选中的视频ID
        JsonDetailScreen(
            subScript = subScript,
            videoId = selectedVideo!!.vod_id,
            onNavigateBack = { selectedVideo = null },
            viewModel = viewModel
        )
    }
}
