package com.vlog.my.ui.screens.subscripts.json

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * 内容类型配置导航路由
 */
const val CONTENT_TYPE_CONFIG_ROUTE = "content_type_config"
const val CONTENT_TYPE_CONFIG_ARG_SUB_SCRIPT_ID = "subScriptId"

/**
 * 获取内容类型配置路由
 * @param subScriptId 小程序ID
 * @return 路由字符串
 */
fun getContentTypeConfigRoute(subScriptId: String): String {
    return "$CONTENT_TYPE_CONFIG_ROUTE/$subScriptId"
}

/**
 * 添加内容类型配置导航
 * @param navGraphBuilder 导航图构建器
 * @param navController 导航控制器
 */
fun NavGraphBuilder.contentTypeConfigNavigation(
    navController: NavController
) {
    composable(
        route = "$CONTENT_TYPE_CONFIG_ROUTE/{$CONTENT_TYPE_CONFIG_ARG_SUB_SCRIPT_ID}",
        arguments = listOf(
            navArgument(CONTENT_TYPE_CONFIG_ARG_SUB_SCRIPT_ID) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val subScriptId = backStackEntry.arguments?.getString(CONTENT_TYPE_CONFIG_ARG_SUB_SCRIPT_ID) ?: ""
        ContentTypeConfigRoute(
            subScriptId = subScriptId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/**
 * 内容类型配置路由
 * @param subScriptId 小程序ID
 * @param onNavigateBack 返回回调
 */
@Composable
fun ContentTypeConfigRoute(
    subScriptId: String,
    onNavigateBack: () -> Unit,
    viewModel: ContentTypeConfigViewModel = hiltViewModel()
) {
    // 加载小程序
    LaunchedEffect(subScriptId) {
        viewModel.loadSubScript(subScriptId)
    }
    
    // 监听保存状态
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }
    
    // 获取小程序
    val subScript by viewModel.subScript.collectAsState()
    
    // 显示内容类型配置界面
    subScript?.let { script ->
        ContentTypeConfigScreen(
            subScript = script,
            customParserService = viewModel.customParserService,
            onSave = { updatedSubScript ->
                viewModel.saveContentTypeConfig(updatedSubScript)
            },
            onCancel = onNavigateBack
        )
    }
}
