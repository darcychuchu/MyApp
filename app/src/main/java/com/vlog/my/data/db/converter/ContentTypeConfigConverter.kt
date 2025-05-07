package com.vlog.my.data.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.model.json.ContentTypeConfig

/**
 * ContentTypeConfig类型转换器
 * 用于Room数据库存储和读取ContentTypeConfig对象
 */
class ContentTypeConfigConverter {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(ContentTypeConfig::class.java)

    /**
     * 将ContentTypeConfig对象转换为JSON字符串
     */
    @TypeConverter
    fun fromContentTypeConfig(contentTypeConfig: ContentTypeConfig?): String? {
        return contentTypeConfig?.let { adapter.toJson(it) }
    }

    /**
     * 将JSON字符串转换为ContentTypeConfig对象
     */
    @TypeConverter
    fun toContentTypeConfig(json: String?): ContentTypeConfig? {
        return json?.let { adapter.fromJson(it) }
    }
}
