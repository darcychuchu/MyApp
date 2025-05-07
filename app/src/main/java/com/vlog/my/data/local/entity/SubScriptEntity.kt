package com.vlog.my.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.db.converter.ContentTypeConfigConverter
import com.vlog.my.data.model.SubScripts
import com.vlog.my.data.model.json.ContentTypeConfig
import java.util.UUID

/**
 * 小程序数据库实体
 * 包含小程序的基本信息和内容类型配置
 */
@Entity(tableName = "subscripts")
@TypeConverters(ContentTypeConfigConverter::class)
data class SubScriptEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isTyped: Int?, // 0 = edge模式，1= json模式
    val createdBy: String?,
    val subUrl: String,
    val subKey: String,
    val contentTypeConfigJson: String? = null // 内容类型配置的JSON字符串
) {
    /**
     * 转换为SubScripts模型
     */
    fun toSubScripts(): SubScripts {
        val contentTypeConfig = if (!contentTypeConfigJson.isNullOrEmpty()) {
            try {
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(ContentTypeConfig::class.java)
                adapter.fromJson(contentTypeConfigJson)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        return SubScripts(
            id = id,
            title = title,
            isTyped = isTyped,
            createdBy = createdBy,
            subUrl = subUrl,
            subKey = subKey,
            contentTypeConfig = contentTypeConfig
        )
    }

    companion object {
        /**
         * 从SubScripts模型创建实体
         */
        fun fromSubScripts(subScripts: SubScripts): SubScriptEntity {
            val contentTypeConfigJson = if (subScripts.contentTypeConfig != null) {
                try {
                    val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                    val adapter = moshi.adapter(ContentTypeConfig::class.java)
                    adapter.toJson(subScripts.contentTypeConfig)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }

            return SubScriptEntity(
                id = subScripts.id,
                title = subScripts.title,
                isTyped = subScripts.isTyped,
                createdBy = subScripts.createdBy,
                subUrl = subScripts.subUrl,
                subKey = subScripts.subKey,
                contentTypeConfigJson = contentTypeConfigJson
            )
        }
    }
}
