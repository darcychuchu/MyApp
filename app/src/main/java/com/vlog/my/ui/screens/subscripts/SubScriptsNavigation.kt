package com.vlog.my.ui.screens.subscripts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.EbookShareContent
import com.vlog.my.ui.screens.subscripts.ebook.EbookListScreen
import com.vlog.my.ui.screens.subscripts.ebook.EbookReaderScreen
import com.vlog.my.ui.screens.subscripts.ebook.EbookShareScreen
import com.vlog.my.ui.screens.subscripts.json.ContentTypeConfigRoute
import com.vlog.my.ui.screens.subscripts.json.JsonDetailScreen
import com.vlog.my.ui.screens.subscripts.enhanced.EnhancedJsonConfigScreen
import com.vlog.my.ui.screens.subscripts.enhanced.EnhancedJsonListScreen
import com.vlog.my.ui.screens.subscripts.enhanced.EnhancedJsonMainScreen
import com.vlog.my.ui.screens.subscripts.sharing.MessageDetailScreen
import com.vlog.my.ui.screens.subscripts.sharing.ReceivedMessagesScreen
import com.vlog.my.ui.screens.subscripts.sharing.ReceivedSubScriptsScreen
import com.vlog.my.ui.screens.subscripts.sharing.SendSubScriptScreen
import kotlinx.coroutines.delay

/**
 * 小程序相关的路由定义
 */
sealed class SubScriptsRoute(val route: String) {
    object SubScripts : SubScriptsRoute("subscripts")
    object AddSubScript : SubScriptsRoute("add_subscript")
    object EditSubScript : SubScriptsRoute("edit_subscript/{subScriptId}") {
        fun createRoute(subScriptId: String): String = "edit_subscript/$subScriptId"
    }
    object WebViewScreen : SubScriptsRoute("webview/{subScriptId}") {
        fun createRoute(subScriptId: String): String = "webview/$subScriptId"
    }
    object JsonModeScreen : SubScriptsRoute("jsonmode/{subScriptId}") {
        fun createRoute(subScriptId: String): String = "jsonmode/$subScriptId"
    }
    object JsonVideoPlayer : SubScriptsRoute("json_video_player/{subScriptId}/{videoId}") {
        fun createRoute(subScriptId: String, videoId: String): String = "json_video_player/$subScriptId/$videoId"
    }
    object SendSubScript : SubScriptsRoute("send_subscript/{subScriptId}") {
        fun createRoute(subScriptId: String): String = "send_subscript/$subScriptId"
    }
    object ReceivedMessages : SubScriptsRoute("received_messages")
    object MessageDetail : SubScriptsRoute("message_detail/{messageId}") {
        fun createRoute(messageId: String): String = "message_detail/$messageId"
    }
    object ReceivedSubScripts : SubScriptsRoute("received_subscripts")
    object ContentTypeConfig : SubScriptsRoute("content_type_config/{subScriptId}") {
        fun createRoute(subScriptId: String): String = "content_type_config/$subScriptId"
    }

    // 电子书相关路由
    object EbookList : SubScriptsRoute("ebook_list")
    object EbookReader : SubScriptsRoute("ebook_reader/{ebookId}") {
        fun createRoute(ebookId: String): String = "ebook_reader/$ebookId"
    }
    object EbookShare : SubScriptsRoute("ebook_share/{ebookId}") {
        fun createRoute(ebookId: String): String = "ebook_share/$ebookId"
    }

    // 增强型JSON相关路由
    object EnhancedJsonList : SubScriptsRoute("enhanced_json_list") {
        fun createRoute(): String = "enhanced_json_list"
    }
    object EnhancedJsonConfig : SubScriptsRoute("enhanced_json_config") {
        fun createRoute(): String = "enhanced_json_config"
    }
    object EnhancedJsonConfigEdit : SubScriptsRoute("enhanced_json_config_edit/{configId}") {
        fun createRoute(configId: String): String = "enhanced_json_config_edit/$configId"
    }
    object EnhancedJsonMain : SubScriptsRoute("enhanced_json_main/{configId}") {
        fun createRoute(configId: String): String = "enhanced_json_main/$configId"
    }
    object EnhancedJsonDetail : SubScriptsRoute("enhanced_json_detail/{configId}/{itemId}") {
        fun createRoute(configId: String, itemId: String): String = "enhanced_json_detail/$configId/$itemId"
    }
    object EnhancedJsonSearch : SubScriptsRoute("enhanced_json_search/{configId}") {
        fun createRoute(configId: String): String = "enhanced_json_search/$configId"
    }
}

/**
 * 小程序导航图构建器
 * 将所有小程序相关的导航集中在一起
 */
fun NavGraphBuilder.subScriptsNavigation(
    navController: NavController,
    onNavigateToVideoPlayer: (SubScripts, Long) -> Unit,
    parentNavController: NavController? = null
) {
    // 小程序列表页面
    composable(SubScriptsRoute.SubScripts.route) {
        SubScriptsScreen(
            onNavigateBack = {
                // 如果有父导航控制器，则返回到父导航
                if (parentNavController != null) {
                    parentNavController.popBackStack()
                } else {
                    navController.popBackStack()
                }
            },
            onAddSubScript = {
                navController.navigate(SubScriptsRoute.AddSubScript.route)
            },
            onSubScriptClick = { subScript ->
                // 点击小程序项时的默认行为，可以根据类型导航到不同页面
                if (subScript.isTyped == 0) {
                    navController.navigate(SubScriptsRoute.WebViewScreen.createRoute(subScript.id))
                } else if (subScript.isTyped == 1) {
                    navController.navigate(SubScriptsRoute.JsonModeScreen.createRoute(subScript.id))
                }
            },
            onEditSubScript = { subScriptId ->
                navController.navigate(SubScriptsRoute.EditSubScript.createRoute(subScriptId))
            },
            onOpenBrowserMode = { subScript ->
                navController.navigate(SubScriptsRoute.WebViewScreen.createRoute(subScript.id))
            },
            onOpenJsonMode = { subScript ->
                navController.navigate(SubScriptsRoute.JsonModeScreen.createRoute(subScript.id))
            },
            onSendSubScript = { subScript ->
                navController.navigate(SubScriptsRoute.SendSubScript.createRoute(subScript.id))
            },
            onViewReceivedMessages = {
                navController.navigate(SubScriptsRoute.ReceivedMessages.route)
            },
            onViewReceivedSubScripts = {
                navController.navigate(SubScriptsRoute.ReceivedSubScripts.route)
            },
            onOpenEbookList = {
                navController.navigate(SubScriptsRoute.EbookList.route)
            },
            navController = navController
        )
    }

    // 添加小程序页面
    composable(SubScriptsRoute.AddSubScript.route) {
        AddSubScriptScreen(
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    // 编辑小程序页面
    composable(
        route = SubScriptsRoute.EditSubScript.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
        val viewModel = hiltViewModel<SubScriptsViewModel>()
        val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
        val subScript = cachedSubScripts.find { it.id == subScriptId }

        if (subScript != null) {
            EditSubScriptScreen(
                subScriptId = subScriptId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToContentTypeConfig = { id ->
                    navController.navigate(SubScriptsRoute.ContentTypeConfig.createRoute(id))
                }
            )
        } else {
            // 尝试重新加载
            LaunchedEffect(Unit) {
                viewModel.loadAllSubScripts()
                // 如果仍然找不到，返回上一页
                delay(500) // 给加载一些时间
                if (viewModel.cachedSubScripts.value.none { it.id == subScriptId }) {
                    navController.popBackStack()
                }
            }
            // 显示加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // WebView模式页面
    composable(
        route = SubScriptsRoute.WebViewScreen.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
        val viewModel = hiltViewModel<SubScriptsViewModel>()
        val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
        val subScript = cachedSubScripts.find { it.id == subScriptId }

        if (subScript != null) {
            WebViewScreen(
                url = subScript.subUrl,
                title = subScript.title,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        } else {
            // 尝试重新加载
            LaunchedEffect(Unit) {
                viewModel.loadAllSubScripts()
                // 如果仍然找不到，返回上一页
                delay(500) // 给加载一些时间
                if (viewModel.cachedSubScripts.value.none { it.id == subScriptId }) {
                    navController.popBackStack()
                }
            }
            // 显示加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // JSON模式页面
    composable(
        route = SubScriptsRoute.JsonModeScreen.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
        val viewModel = hiltViewModel<SubScriptsViewModel>()
        val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
        val subScript = cachedSubScripts.find { it.id == subScriptId }

        if (subScript != null) {
            JsonModeScreen(
                subScript = subScript,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToContentTypeConfig = { id ->
                    navController.navigate(SubScriptsRoute.ContentTypeConfig.createRoute(id))
                }
            )
        } else {
            // 尝试重新加载
            LaunchedEffect(Unit) {
                viewModel.loadAllSubScripts()
                // 如果仍然找不到，返回上一页
                delay(500) // 给加载一些时间
                if (viewModel.cachedSubScripts.value.none { it.id == subScriptId }) {
                    navController.popBackStack()
                }
            }
            // 显示加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // JSON视频播放页面
    composable(
        route = SubScriptsRoute.JsonVideoPlayer.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType },
            navArgument("videoId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
        val videoId = backStackEntry.arguments?.getString("videoId") ?: "0"
        val viewModel = hiltViewModel<SubScriptsViewModel>()
        val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
        val subScript = cachedSubScripts.find { it.id == subScriptId }

        if (subScript != null) {
            // 使用原始的 SubScripts 对象
            val tempSubScript = subScript.copy()

            // 导航到 JsonDetailScreen
            JsonDetailScreen(
                subScript = tempSubScript,
                videoId = videoId.toLongOrNull() ?: 0L,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        } else {
            // 尝试重新加载
            LaunchedEffect(Unit) {
                viewModel.loadAllSubScripts()
                // 如果仍然找不到，返回上一页
                delay(500) // 给加载一些时间
                if (viewModel.cachedSubScripts.value.none { it.id == subScriptId }) {
                    navController.popBackStack()
                }
            }
            // 显示加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // 发送小程序页面
    composable(
        route = SubScriptsRoute.SendSubScript.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
        val viewModel = hiltViewModel<SubScriptsViewModel>()
        val cachedSubScripts by viewModel.cachedSubScripts.collectAsState()
        val subScript = cachedSubScripts.find { it.id == subScriptId }

        if (subScript != null) {
            SendSubScriptScreen(
                subScript = subScript,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        } else {
            // 尝试重新加载
            LaunchedEffect(Unit) {
                viewModel.loadAllSubScripts()
                // 如果仍然找不到，返回上一页
                delay(500) // 给加载一些时间
                if (viewModel.cachedSubScripts.value.none { it.id == subScriptId }) {
                    navController.popBackStack()
                }
            }
            // 显示加载中
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // 接收到的消息列表页面
    composable(SubScriptsRoute.ReceivedMessages.route) {
        ReceivedMessagesScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onMessageSelected = { messageId ->
                navController.navigate(SubScriptsRoute.MessageDetail.createRoute(messageId))
            }
        )
    }

    // 消息详情页面
    composable(
        route = SubScriptsRoute.MessageDetail.route,
        arguments = listOf(
            navArgument("messageId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val messageId = backStackEntry.arguments?.getString("messageId") ?: ""

        MessageDetailScreen(
            messageId = messageId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onAccepted = {
                // 接受消息后，导航到接收到的小程序列表
                navController.navigate(SubScriptsRoute.ReceivedSubScripts.route) {
                    popUpTo(SubScriptsRoute.ReceivedMessages.route) { inclusive = true }
                }
            }
        )
    }

    // 接收到的小程序列表页面
    composable(SubScriptsRoute.ReceivedSubScripts.route) {
        ReceivedSubScriptsScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onSubScriptSelected = { subScriptId ->
                // 这里可以导航到小程序详情页面
                // 暂时简单实现，直接使用JsonModeScreen
                navController.navigate(SubScriptsRoute.JsonModeScreen.createRoute(subScriptId))
            }
        )
    }

    // 内容类型配置页面
    composable(
        route = SubScriptsRoute.ContentTypeConfig.route,
        arguments = listOf(
            navArgument("subScriptId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""

        ContentTypeConfigRoute(
            subScriptId = subScriptId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    // 电子书列表页面
    composable(SubScriptsRoute.EbookList.route) {
        EbookListScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onEbookClick = { ebookId ->
                navController.navigate(SubScriptsRoute.EbookReader.createRoute(ebookId))
            },
            onShareEbook = { ebookId ->
                navController.navigate(SubScriptsRoute.EbookShare.createRoute(ebookId))
            }
        )
    }

    // 电子书阅读器页面
    composable(
        route = SubScriptsRoute.EbookReader.route,
        arguments = listOf(
            navArgument("ebookId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val ebookId = backStackEntry.arguments?.getString("ebookId") ?: ""

        EbookReaderScreen(
            ebookId = ebookId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    // 电子书分享页面
    composable(
        route = SubScriptsRoute.EbookShare.route,
        arguments = listOf(
            navArgument("ebookId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val ebookId = backStackEntry.arguments?.getString("ebookId") ?: ""

        EbookShareScreen(
            ebookId = ebookId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onShareSuccess = {
                navController.popBackStack()
            }
        )
    }

    // 增强型JSON配置列表页面
    composable(
        route = SubScriptsRoute.EnhancedJsonList.route
    ) {
        EnhancedJsonListScreen(
            onNavigateBack = {
                navController.popBackStack()
            },
            onAddConfig = {
                navController.navigate(SubScriptsRoute.EnhancedJsonConfig.createRoute())
            },
            onEditConfig = { configId ->
                navController.navigate(SubScriptsRoute.EnhancedJsonConfigEdit.createRoute(configId))
            },
            onOpenConfig = { configId ->
                navController.navigate(SubScriptsRoute.EnhancedJsonMain.createRoute(configId))
            }
        )
    }

    // 增强型JSON配置添加页面
    composable(
        route = SubScriptsRoute.EnhancedJsonConfig.route
    ) {
        EnhancedJsonConfigScreen(
            configId = null,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    // 增强型JSON配置编辑页面
    composable(
        route = SubScriptsRoute.EnhancedJsonConfigEdit.route,
        arguments = listOf(
            navArgument("configId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val configId = backStackEntry.arguments?.getString("configId") ?: ""

        EnhancedJsonConfigScreen(
            configId = configId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    // 增强型JSON主页面
    composable(
        route = SubScriptsRoute.EnhancedJsonMain.route,
        arguments = listOf(
            navArgument("configId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val configId = backStackEntry.arguments?.getString("configId") ?: ""

        EnhancedJsonMainScreen(
            configId = configId,
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToDetail = { id, itemId ->
                navController.navigate(SubScriptsRoute.EnhancedJsonDetail.createRoute(id, itemId))
            },
            onNavigateToSearch = { id ->
                navController.navigate(SubScriptsRoute.EnhancedJsonSearch.createRoute(id))
            }
        )
    }
}
