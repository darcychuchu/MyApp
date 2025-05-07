package com.vlog.my.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 喜爱按钮组件
 * @param isFavorite 是否已喜爱
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 记录上一次的喜爱状态，用于动画
    var prevFavorite by remember { mutableStateOf(isFavorite) }
    
    // 如果状态发生变化，触发动画
    val triggerAnimation = isFavorite != prevFavorite
    prevFavorite = isFavorite
    
    // 动画状态
    val scale by animateFloatAsState(
        targetValue = if (triggerAnimation) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )
    
    // 颜色动画
    val color by animateColorAsState(
        targetValue = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "color"
    )
    
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavorite) "取消喜爱" else "喜爱",
            tint = color,
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
        )
    }
}
