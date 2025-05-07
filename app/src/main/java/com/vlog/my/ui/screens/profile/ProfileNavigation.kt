package com.vlog.my.ui.screens.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vlog.my.data.repository.UserDataRepository
import com.vlog.my.ui.navigation.BottomNavItem
import com.vlog.my.ui.navigation.Screen
import com.vlog.my.viewmodel.UserViewModel

/**
 * Profile模块路由
 */
sealed class ProfileScreen(val route: String) {
    // 个人主页
    object Main : ProfileScreen("profile_main")

    // 设置页面
    object Settings : ProfileScreen("profile_settings")

    // 粉丝列表
    object Followers : ProfileScreen("profile_followers")

    // 关注列表
    object Following : ProfileScreen("profile_following")

    // 消息列表
    object Messages : ProfileScreen("profile_messages")

    // 消息详情
    object MessageDetail : ProfileScreen("profile_message_detail/{messageId}") {
        fun createRoute(messageId: String): String = "profile_message_detail/$messageId"
    }

    // 评论列表
    object Comments : ProfileScreen("profile_comments")

    // 编辑个人资料
    object EditProfile : ProfileScreen("profile_edit")
}

/**
 * Profile导航宿主
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileNavHost(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToStoryDetail: (String, String) -> Unit = { _, _ -> },
    onNavigateToArtworkDetail: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToFollowers: () -> Unit = {},
    onNavigateToFollowing: () -> Unit = {},
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ProfileScreen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 个人主页
        composable(ProfileScreen.Main.route) {
            ProfileScreen(
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToSettings = {
                    navController.navigate(ProfileScreen.Settings.route)
                },
                onNavigateToFollowers = {
                    navController.navigate(ProfileScreen.Followers.route)
                },
                onNavigateToFollowing = {
                    navController.navigate(ProfileScreen.Following.route)
                },
                onNavigateToMessages = {
                    navController.navigate(ProfileScreen.Messages.route)
                },
                onNavigateToComments = {
                    navController.navigate(ProfileScreen.Comments.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(ProfileScreen.EditProfile.route)
                },
                onNavigateToStoryDetail = onNavigateToStoryDetail,
                onNavigateToArtworkDetail = onNavigateToArtworkDetail,
                navController = navController
            )
        }

        // 设置页面
        composable(ProfileScreen.Settings.route) {
            ProfileSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 粉丝列表
        composable(ProfileScreen.Followers.route) {
            ProfileFollowersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToUserProfile = onNavigateToUserProfile
            )
        }

        // 关注列表
        composable(ProfileScreen.Following.route) {
            ProfileFollowingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToUserProfile = onNavigateToUserProfile
            )
        }

        // 消息列表
        composable(ProfileScreen.Messages.route) {
            ProfileMessagesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMessageDetail = { messageId ->
                    navController.navigate(ProfileScreen.MessageDetail.createRoute(messageId))
                }
            )
        }

        // 消息详情
        composable(
            route = ProfileScreen.MessageDetail.route,
            arguments = listOf(
                navArgument("messageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val messageId = backStackEntry.arguments?.getString("messageId") ?: ""

            ProfileMessageDetailScreen(
                messageId = messageId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 评论列表
        composable(ProfileScreen.Comments.route) {
            ProfileCommentsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // 编辑个人资料
        composable(ProfileScreen.EditProfile.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 将Profile导航添加到主导航图
 */
fun NavGraphBuilder.profileNavigation(
    onNavigateToLogin: () -> Unit,
    onNavigateToStoryDetail: (String, String) -> Unit,
    onNavigateToArtworkDetail: (String, String, Boolean) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToFollowers: () -> Unit = {},
    onNavigateToFollowing: () -> Unit = {}
) {
    composable(BottomNavItem.Profile.route) {
        ProfileNavHost(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToStoryDetail = onNavigateToStoryDetail,
            onNavigateToArtworkDetail = onNavigateToArtworkDetail,
            onNavigateToUserProfile = onNavigateToUserProfile,
            onNavigateToFollowers = onNavigateToFollowers,
            onNavigateToFollowing = onNavigateToFollowing
        )
    }
}

/**
 * 设置页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit
) {
    // 设置页面的实现将在后续添加
    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "设置",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // 设置页面内容
    }
}

/**
 * 粉丝列表页面
 */
@Composable
fun ProfileFollowersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    userViewModel: com.vlog.my.viewmodel.UserViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // 使用我们实现的 FollowersScreen
    com.vlog.my.ui.screens.users.FollowersScreen(
        username = userViewModel.getCurrentUser()?.name ?: "",
        onNavigateBack = onNavigateBack,
        onNavigateToUserProfile = onNavigateToUserProfile
    )
}

/**
 * 关注列表页面
 */
@Composable
fun ProfileFollowingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    userViewModel: com.vlog.my.viewmodel.UserViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    // 使用我们实现的 FollowingScreen
    com.vlog.my.ui.screens.users.FollowingScreen(
        username = userViewModel.getCurrentUser()?.name ?: "",
        onNavigateBack = onNavigateBack,
        onNavigateToUserProfile = onNavigateToUserProfile
    )
}

/**
 * 消息列表页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMessagesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMessageDetail: (String) -> Unit
) {
    // 消息列表页面的实现将在后续添加
    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "消息",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // 消息列表内容
    }
}

/**
 * 消息详情页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMessageDetailScreen(
    messageId: String,
    onNavigateBack: () -> Unit
) {
    // 消息详情页面的实现将在后续添加
    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "消息详情",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // 消息详情内容
    }
}

/**
 * 评论列表页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCommentsScreen(
    onNavigateBack: () -> Unit
) {
    // 评论列表页面的实现将在后续添加
    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "评论",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // 评论列表内容
    }
}

/**
 * 编辑个人资料页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit
) {
    // 编辑个人资料页面的实现将在后续添加
    Scaffold(
        topBar = {
            ProfileTopBar(
                title = "编辑个人资料",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        // 编辑个人资料内容
    }
}
