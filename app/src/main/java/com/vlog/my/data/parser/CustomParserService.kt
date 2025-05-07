package com.vlog.my.data.parser

import com.vlog.my.data.local.subscript.SubScriptDao
import com.vlog.my.data.model.json.ContentType
import com.vlog.my.data.model.json.ContentTypeConfig
import com.vlog.my.data.model.json.DetailScreenType
import com.vlog.my.data.model.json.FieldMapping
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自定义解析服务
 * 管理不同内容类型的解析器
 */
@Singleton
class CustomParserService @Inject constructor(
    private val subScriptDao: SubScriptDao
) {
    // 缓存解析器，避免重复创建
    private val parserCache = mutableMapOf<String, CustomJsonParser>()
    
    /**
     * 获取指定小程序的解析器
     * @param subScriptId 小程序ID
     * @return 自定义解析器，如果没有配置则返回null
     */
    suspend fun getParser(subScriptId: String): CustomJsonParser? {
        // 检查缓存
        if (parserCache.containsKey(subScriptId)) {
            return parserCache[subScriptId]
        }
        
        // 从数据库加载配置
        val subScript = subScriptDao.getSubScriptById(subScriptId)?.toSubScripts() ?: return null
        val contentTypeConfig = subScript.contentTypeConfig ?: return null
        
        // 创建解析器并缓存
        val parser = CustomJsonParser(contentTypeConfig)
        parserCache[subScriptId] = parser
        
        return parser
    }
    
    /**
     * 清除解析器缓存
     * @param subScriptId 小程序ID，如果为null则清除所有缓存
     */
    fun clearParserCache(subScriptId: String? = null) {
        if (subScriptId != null) {
            parserCache.remove(subScriptId)
        } else {
            parserCache.clear()
        }
    }
    
    /**
     * 获取电影模板
     * @return 电影内容类型配置
     */
    fun getMovieTemplate(): ContentTypeConfig {
        return ContentTypeConfig(
            type = ContentType.MOVIE,
            fieldMappings = listOf(
                FieldMapping("id", "vod_id", true),
                FieldMapping("title", "vod_name", true),
                FieldMapping("cover", "vod_pic"),
                FieldMapping("description", "vod_content"),
                FieldMapping("director", "vod_director"),
                FieldMapping("actor", "vod_actor"),
                FieldMapping("category", "type_name"),
                FieldMapping("year", "vod_year"),
                FieldMapping("area", "vod_area"),
                FieldMapping("language", "vod_lang"),
                FieldMapping("update", "vod_remarks"),
                FieldMapping("play_url", "vod_play_url")
            ),
            detailScreenType = DetailScreenType.VIDEO_PLAYER,
            listResponsePath = "data",
            categoryResponsePath = "list"
        )
    }
    
    /**
     * 获取图书模板
     * @return 图书内容类型配置
     */
    fun getBookTemplate(): ContentTypeConfig {
        return ContentTypeConfig(
            type = ContentType.BOOK,
            fieldMappings = listOf(
                FieldMapping("id", "vod_id", true),
                FieldMapping("title", "vod_name", true),
                FieldMapping("cover", "vod_pic"),
                FieldMapping("description", "vod_content"),
                FieldMapping("author", "vod_director"),
                FieldMapping("category", "type_name"),
                FieldMapping("year", "vod_year"),
                FieldMapping("publisher", "vod_area"),
                FieldMapping("update", "vod_remarks"),
                FieldMapping("read_url", "vod_play_url")
            ),
            detailScreenType = DetailScreenType.BOOK_READER,
            listResponsePath = "books",
            categoryResponsePath = "categories"
        )
    }
    
    /**
     * 获取音乐模板
     * @return 音乐内容类型配置
     */
    fun getMusicTemplate(): ContentTypeConfig {
        return ContentTypeConfig(
            type = ContentType.MUSIC,
            fieldMappings = listOf(
                FieldMapping("id", "vod_id", true),
                FieldMapping("title", "vod_name", true),
                FieldMapping("cover", "vod_pic"),
                FieldMapping("description", "vod_content"),
                FieldMapping("artist", "vod_director"),
                FieldMapping("album", "vod_actor"),
                FieldMapping("category", "type_name"),
                FieldMapping("year", "vod_year"),
                FieldMapping("publisher", "vod_area"),
                FieldMapping("update", "vod_remarks"),
                FieldMapping("play_url", "vod_play_url"),
                FieldMapping("duration", "vod_duration")
            ),
            detailScreenType = DetailScreenType.AUDIO_PLAYER,
            listResponsePath = "songs",
            categoryResponsePath = "genres"
        )
    }
}
