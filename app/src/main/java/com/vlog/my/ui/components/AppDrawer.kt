package com.vlog.my.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onProfileClick: () -> Unit,
    onSubScriptsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isUserLoggedIn: Boolean = false,
    userName: String = "未登录",
    userAvatar: String? = null
) {
    ModalDrawerSheet {
        // 用户信息区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onProfileClick() }
        ) {
            if (userAvatar != null) {
                // 如果有头像，显示头像
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 否则显示默认图标
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "用户头像",
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = if (isUserLoggedIn) "点击查看个人资料" else "点击登录",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Divider()
        
        // 菜单项
        DrawerMenuItem(
            icon = Icons.Default.Warning,
            title = "小程序",
            onClick = onSubScriptsClick
        )
        
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            title = "设置",
            onClick = onSettingsClick
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
