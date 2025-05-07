package com.vlog.my.ui.screens.subscripts

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.vlog.my.data.model.SubScripts

/**
 * 小程序模块入口页面
 * 作为小程序模块的主入口，包含独立的导航
 * @param onNavigateBack 返回按钮点击回调
 * @param parentNavController 父导航控制器
 * @param initialRoute 初始路由，如果提供，将在导航创建后立即导航到该路由
 */
@Composable
fun SubScriptsEntryScreen(
    onNavigateBack: () -> Unit,
    parentNavController: NavController,
    initialRoute: String? = null
) {
    // 创建小程序模块内部的导航控制器
    val navController = rememberNavController()

    // 使用NavHost创建小程序模块的导航
    NavHost(
        navController = navController,
        startDestination = SubScriptsRoute.SubScripts.route
    ) {
        // 添加小程序导航
        subScriptsNavigation(
            navController = navController,
            onNavigateToVideoPlayer = { subScript, videoId ->
                // 处理视频播放导航
                // 这里可以根据需要导航到其他页面
            },
            parentNavController = parentNavController
        )
    }

    // 如果提供了初始路由，则导航到该路由
    if (initialRoute != null) {
        androidx.compose.runtime.LaunchedEffect(initialRoute) {
            // 使用延迟导航，确保NavHost已经完全初始化
            kotlinx.coroutines.delay(100)
            navController.navigate(initialRoute) {
                // 避免创建多个相同目的地的实例
                launchSingleTop = true
            }
        }
    }
}
