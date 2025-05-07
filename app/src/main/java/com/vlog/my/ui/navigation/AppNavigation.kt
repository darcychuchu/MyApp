package com.vlog.my.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vlog.my.ui.screens.subscripts.SubScriptsViewModel
import kotlinx.coroutines.delay
import com.vlog.my.ui.screens.auth.LoginScreen
import com.vlog.my.ui.screens.auth.RegisterScreen
import com.vlog.my.ui.screens.home.HomeScreen
import com.vlog.my.ui.screens.messages.MessageDetailScreen
import com.vlog.my.ui.screens.messages.MessagesScreen
import com.vlog.my.ui.screens.profile.profileNavigation
import com.vlog.my.ui.screens.publish.PhotoPublishScreen
import com.vlog.my.ui.screens.publish.PublishScreen
import com.vlog.my.ui.screens.publish.VideoPublishScreen
import com.vlog.my.ui.screens.subscripts.SubScriptsEntryScreen
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.VideoShareContent
import com.vlog.my.ui.screens.trending.TrendingScreen
import com.vlog.my.ui.screens.users.UserHomeScreen
import com.vlog.my.ui.screens.users.UserStoryDetailScreen
import com.vlog.my.ui.screens.users.UserArtworkDetailScreen
import com.vlog.my.ui.screens.users.UserArtworkPlayerScreen
import com.vlog.my.ui.screens.users.FollowingScreen
import com.vlog.my.ui.screens.users.FollowersScreen
import com.vlog.my.viewmodel.SubScriptSharingViewModel
import com.vlog.my.viewmodel.UserViewModel

sealed class Screen(val route: String) {
    // 认证相关页面
    object Login : Screen("login")
    object Register : Screen("register")

    // 小程序相关页面
    object SubScripts : Screen("subscripts")

    // 发布相关页面
    object PhotoPublish : Screen("photo_publish")
    object VideoPublish : Screen("video_publish")

    // 详情页面
    object StoryDetail : Screen("story_detail/{userName}/{storyId}") {
        fun createRoute(userName: String, storyId: String): String = "story_detail/$userName/$storyId"
    }
    object ArtworkDetail : Screen("artwork_detail/{userName}/{artworkId}") {
        fun createRoute(userName: String, artworkId: String): String = "artwork_detail/$userName/$artworkId"
    }
    object ArtworkPlayer : Screen("artwork_player/{userName}/{artworkId}") {
        fun createRoute(userName: String, artworkId: String): String = "artwork_player/$userName/$artworkId"
    }



    object Settings : Screen("settings")

    // 用户主页相关页面
    object UserHome : Screen("user_home/{username}") {
        fun createRoute(username: String): String = "user_home/$username"
    }

    object UserStoryDetail : Screen("user_story_detail/{username}/{storyId}") {
        fun createRoute(username: String, storyId: String): String = "user_story_detail/$username/$storyId"
    }

    object UserArtworkDetail : Screen("user_artwork_detail/{username}/{artworkId}") {
        fun createRoute(username: String, artworkId: String): String = "user_artwork_detail/$username/$artworkId"
    }

    object UserArtworkPlayer : Screen("user_artwork_player/{username}/{artworkId}") {
        fun createRoute(username: String, artworkId: String): String = "user_artwork_player/$username/$artworkId"
    }

    // 消息相关页面
    object ComposeMessage : Screen("compose_message/{recipientId}/{recipientName}") {
        fun createRoute(recipientId: String, recipientName: String): String =
            "compose_message/$recipientId/$recipientName"
    }

    object Messages : Screen("messages")

    object UserMessageDetail : Screen("messages/{messageId}") {
        fun createRoute(messageId: String): String = "messages/$messageId"
    }

    // 关注/粉丝相关页面
    object Following : Screen("following/{username}") {
        fun createRoute(username: String): String = "following/$username"
    }

    object Followers : Screen("followers/{username}") {
        fun createRoute(username: String): String = "followers/$username"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()
    val isLoggedIn = userViewModel.isLoggedIn()

    // 确定起始目的地
    val startDestination = if (isLoggedIn) {
        BottomNavItem.Home.route
    } else {
        Screen.Login.route
    }

    Scaffold(
        bottomBar = {
            // 只在主要页面显示底部导航栏
            val currentRoute = currentRoute(navController)
            if (currentRoute in listOf(
                    BottomNavItem.Home.route,
                    BottomNavItem.Trending.route,
                    BottomNavItem.Publish.route,
                    BottomNavItem.Messages.route,
                    BottomNavItem.Profile.route
                )
            ) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 认证相关页面
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToSubScripts = {
                        navController.navigate(Screen.SubScripts.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(BottomNavItem.Profile.route)
                    },
                    onNavigateToUserProfile = { username ->
                        navController.navigate(Screen.UserHome.createRoute(username))
                    },
                    onNavigateToSettings = {
                        // 导航到设置页面
                    },
                    navController = navController
                )
            }
            composable(BottomNavItem.Trending.route) {
                TrendingScreen(
                    onNavigateToSubScripts = {
                        navController.navigate(Screen.SubScripts.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(BottomNavItem.Profile.route)
                    },
                    onNavigateToUserProfile = { username ->
                        navController.navigate(Screen.UserHome.createRoute(username))
                    },
                    onNavigateToSettings = {
                        // 导航到设置页面
                    }
                )
            }
            composable(BottomNavItem.Publish.route) {
                PublishScreen(
                    onNavigateToPhotoPublish = {
                        navController.navigate(Screen.PhotoPublish.route)
                    },
                    onNavigateToVideoPublish = {
                        navController.navigate(Screen.VideoPublish.route)
                    }
                )
            }

            // 图文发布页面
            composable(Screen.PhotoPublish.route) {
                PhotoPublishScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 视频发布页面
            composable(Screen.VideoPublish.route) {
                VideoPublishScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(BottomNavItem.Messages.route) {
                MessagesScreen(
                    onNavigateBack = {
                        // 底部导航栏页面不需要返回按钮，但为了接口一致性，提供一个空实现
                    },
                    onMessageSelected = { messageId ->
                        navController.navigate(Screen.UserMessageDetail.createRoute(messageId))
                    }
                )
            }

            // 消息详情页面
            composable(
                route = Screen.UserMessageDetail.route,
                arguments = listOf(
                    navArgument("messageId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getString("messageId") ?: ""

                MessageDetailScreen(
                    messageId = messageId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            // 个人主页导航
            profileNavigation(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToStoryDetail = { userName, storyId ->
                    navController.navigate(Screen.StoryDetail.createRoute(userName, storyId))
                },
                onNavigateToArtworkDetail = { userName, artworkId, isPlayerMode ->
                    if (isPlayerMode) {
                        navController.navigate(Screen.ArtworkPlayer.createRoute(userName, artworkId))
                    } else {
                        navController.navigate(Screen.ArtworkDetail.createRoute(userName, artworkId))
                    }
                },
                onNavigateToUserProfile = { username ->
                    navController.navigate(Screen.UserHome.createRoute(username))
                },
                onNavigateToFollowers = {
                    // 导航到当前用户的粉丝列表
                    val currentUser = userViewModel.getCurrentUser()?.name ?: ""
                    navController.navigate(Screen.Followers.createRoute(currentUser))
                },
                onNavigateToFollowing = {
                    // 导航到当前用户的关注列表
                    val currentUser = userViewModel.getCurrentUser()?.name ?: ""
                    navController.navigate(Screen.Following.createRoute(currentUser))
                }
            )

            // 小程序相关页面
            composable(Screen.SubScripts.route) {
                // 使用小程序入口页面
                SubScriptsEntryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    parentNavController = navController
                )
            }

            // 小程序视频播放页面
            composable(
                route = "subscripts/json_video_player/{subScriptId}/{videoId}",
                arguments = listOf(
                    navArgument("subScriptId") { type = NavType.StringType },
                    navArgument("videoId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val subScriptId = backStackEntry.arguments?.getString("subScriptId") ?: ""
                val videoId = backStackEntry.arguments?.getString("videoId") ?: "0"

                // 使用小程序入口页面，但传递特定参数
                SubScriptsEntryScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    parentNavController = navController,
                    initialRoute = "json_video_player/$subScriptId/$videoId"
                )
            }

            // 动态详情页面
            composable(
                route = Screen.StoryDetail.route,
                arguments = listOf(
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                val storyId = backStackEntry.arguments?.getString("storyId") ?: ""

                com.vlog.my.ui.screens.detail.StoryDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToVideo = { videoShareContent ->
                        // 导航到小程序视频播放页面
                        navController.navigate("subscripts/json_video_player/${videoShareContent.subScriptId}/${videoShareContent.videoId}")
                    },
                    onNavigateToEbook = { ebookShareContent ->
                        // 导航到电子书阅读器页面
                        navController.navigate("subscripts/ebook_reader/${ebookShareContent.ebookId}")
                    },
                    onNavigateToProfile = { userName ->
                        // 导航到用户个人资料页面
                        navController.navigate(Screen.UserHome.createRoute(userName)) {
                            // 保存当前状态，以便返回
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 作品详情页面
            composable(
                route = Screen.ArtworkDetail.route,
                arguments = listOf(
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("artworkId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""

                com.vlog.my.ui.screens.detail.ArtworkDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 作品播放页面
            composable(
                route = Screen.ArtworkPlayer.route,
                arguments = listOf(
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("artworkId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""

                com.vlog.my.ui.screens.detail.ArtworkPlayerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDetail = { artwork ->
                        // 导航到详情页面
                        artwork.createdBy?.let { userName ->
                            artwork.id?.let { artworkId ->
                                navController.navigate(Screen.ArtworkDetail.createRoute(userName, artworkId)) {
                                    popUpTo(Screen.ArtworkPlayer.route) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }



            // 用户主页
            composable(
                route = Screen.UserHome.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""

                UserHomeScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    navController = navController
                )
            }

            // 用户动态详情页面
            composable(
                route = Screen.UserStoryDetail.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val storyId = backStackEntry.arguments?.getString("storyId") ?: ""

                UserStoryDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToVideo = { videoShareContent: VideoShareContent ->
                        // 导航到小程序视频播放页面
                        navController.navigate("subscripts/json_video_player/${videoShareContent.subScriptId}/${videoShareContent.videoId}")
                    },
                    onNavigateToEbook = { ebookShareContent ->
                        // 导航到电子书阅读器页面
                        // 首先导航到小程序模块，然后再导航到电子书阅读器
                        navController.navigate("subscripts") {
                            // 保存状态，以便返回时恢复
                            launchSingleTop = true
                        }
                        // 使用延迟导航，确保先导航到小程序模块
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            // 获取当前导航控制器
                            val currentNavController = navController.currentBackStackEntry?.destination?.route
                            android.util.Log.d("AppNavigation", "当前导航路径: $currentNavController")

                            // 记录电子书ID，以便在小程序模块中使用
                            com.vlog.my.ui.navigation.NavigationManager.pendingEbookId = ebookShareContent.ebookId
                            android.util.Log.d("AppNavigation", "设置待打开电子书ID: ${ebookShareContent.ebookId}")
                        }, 100)
                    },
                    onNavigateToProfile = { userName: String ->
                        // 导航到用户主页
                        navController.navigate(Screen.UserHome.createRoute(userName))
                    }
                )
            }

            // 用户作品详情页面
            composable(
                route = Screen.UserArtworkDetail.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("artworkId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""

                UserArtworkDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProfile = { userName: String ->
                        // 导航到用户主页
                        navController.navigate(Screen.UserHome.createRoute(userName))
                    }
                )
            }

            // 用户作品播放页面
            composable(
                route = Screen.UserArtworkPlayer.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("artworkId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val artworkId = backStackEntry.arguments?.getString("artworkId") ?: ""

                UserArtworkPlayerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToDetail = { artwork: com.vlog.my.data.model.Stories ->
                        // 导航到详情页面
                        artwork.createdBy?.let { userName ->
                            artwork.id?.let { artworkId ->
                                navController.navigate(Screen.UserArtworkDetail.createRoute(userName, artworkId)) {
                                    popUpTo(Screen.UserArtworkPlayer.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onNavigateToProfile = { userName: String ->
                        // 导航到用户主页
                        navController.navigate(Screen.UserHome.createRoute(userName))
                    }
                )
            }



            // 关注列表页面
            composable(
                route = Screen.Following.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""

                FollowingScreen(
                    username = username,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigate(Screen.UserHome.createRoute(userId))
                    }
                )
            }

            // 粉丝列表页面
            composable(
                route = Screen.Followers.route,
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""

                FollowersScreen(
                    username = username,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToUserProfile = { userId ->
                        navController.navigate(Screen.UserHome.createRoute(userId))
                    }
                )
            }

            // 发送消息页面
            composable(
                route = Screen.ComposeMessage.route,
                arguments = listOf(
                    navArgument("recipientId") { type = NavType.StringType },
                    navArgument("recipientName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""
                val recipientName = backStackEntry.arguments?.getString("recipientName") ?: ""

                com.vlog.my.ui.screens.messages.ComposeMessageScreen(
                    recipientId = recipientId,
                    recipientName = recipientName,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }


        }
    }
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Trending,
        BottomNavItem.Publish,
        BottomNavItem.Messages,
        BottomNavItem.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
