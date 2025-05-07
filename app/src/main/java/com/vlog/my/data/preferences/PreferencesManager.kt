package com.vlog.my.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.vlog.my.ui.screens.subscripts.json.ViewType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * 用户偏好设置管理器
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    
    /**
     * 保存小程序视图类型偏好
     * @param subScriptId 小程序ID
     * @param viewType 视图类型
     */
    fun saveSubScriptViewType(subScriptId: String, viewType: ViewType) {
        sharedPreferences.edit {
            putString(getSubScriptViewTypeKey(subScriptId), viewType.name)
        }
    }
    
    /**
     * 获取小程序视图类型偏好
     * @param subScriptId 小程序ID
     * @return 视图类型，默认为列表视图
     */
    fun getSubScriptViewType(subScriptId: String): ViewType {
        val viewTypeName = sharedPreferences.getString(getSubScriptViewTypeKey(subScriptId), ViewType.LIST.name)
        return try {
            ViewType.valueOf(viewTypeName ?: ViewType.LIST.name)
        } catch (e: IllegalArgumentException) {
            ViewType.LIST
        }
    }
    
    /**
     * 生成小程序视图类型偏好的键
     * @param subScriptId 小程序ID
     * @return 偏好键
     */
    private fun getSubScriptViewTypeKey(subScriptId: String): String {
        return "$KEY_SUBSCRIPT_VIEW_TYPE_PREFIX$subScriptId"
    }
    
    companion object {
        private const val PREFERENCES_NAME = "subscripts_preferences"
        private const val KEY_SUBSCRIPT_VIEW_TYPE_PREFIX = "subscript_view_type_"
    }
}
