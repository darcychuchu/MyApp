package com.vlog.my.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "首页",
        icon = Icons.Default.Home
    )

    object Trending : BottomNavItem(
        route = "trending",
        title = "热门",
        icon = Icons.Default.Star
    )

    object Publish : BottomNavItem(
        route = "publish",
        title = "发布",
        icon = Icons.Default.Add
    )

    object Messages : BottomNavItem(
        route = "messages",
        title = "信息",
        icon = Icons.Default.Email
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "我",
        icon = Icons.Default.AccountCircle
    )
}
