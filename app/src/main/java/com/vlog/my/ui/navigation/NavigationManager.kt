package com.vlog.my.ui.navigation

/**
 * 导航管理器
 * 用于在不同导航图之间传递数据
 */
object NavigationManager {
    /**
     * 待打开的电子书ID
     * 用于从主导航图跳转到小程序导航图时传递电子书ID
     */
    var pendingEbookId: String? = null
}
