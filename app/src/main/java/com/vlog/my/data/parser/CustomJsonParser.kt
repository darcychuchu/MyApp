package com.vlog.my.data.parser

import com.vlog.my.data.model.json.Category
import com.vlog.my.data.model.json.ContentTypeConfig
import com.vlog.my.data.model.json.JsonApiResponse
import com.vlog.my.data.model.json.VideoItem
import org.json.JSONArray
import org.json.JSONObject

/**
 * 自定义JSON解析器
 * 根据字段映射配置解析JSON数据
 */
class CustomJsonParser(
    private val contentTypeConfig: ContentTypeConfig
) {
    /**
     * 解析JSON响应
     * @param jsonString JSON字符串
     * @return 解析后的JsonApiResponse
     */
    fun parseJsonResponse(jsonString: String): JsonApiResponse {
        val jsonObject = JSONObject(jsonString)
        
        // 解析分页信息
        val pageindex = jsonObject.optInt("pageindex", 1)
        val pagecount = jsonObject.optInt("pagecount", 1)
        val pagesize = jsonObject.optInt("pagesize", 20)
        val recordcount = jsonObject.optInt("recordcount", 0)
        
        // 解析分类列表
        val categoriesJson = jsonObject.optJSONArray(contentTypeConfig.categoryResponsePath) ?: JSONArray()
        val categories = parseCategories(categoriesJson)
        
        // 解析内容列表
        val itemsJson = jsonObject.optJSONArray(contentTypeConfig.listResponsePath) ?: JSONArray()
        val items = parseItems(itemsJson)
        
        return JsonApiResponse(
            pageindex = pageindex,
            pagecount = pagecount,
            pagesize = pagesize,
            recordcount = recordcount,
            categories = categories,
            videos = items
        )
    }
    
    /**
     * 解析分类列表
     * @param jsonArray 分类JSON数组
     * @return 分类列表
     */
    private fun parseCategories(jsonArray: JSONArray): List<Category> {
        val categories = mutableListOf<Category>()
        
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            
            // 查找type_id和type_name的映射
            val typeIdMapping = contentTypeConfig.fieldMappings.find { it.targetField == "type_id" }
            val typeNameMapping = contentTypeConfig.fieldMappings.find { it.targetField == "type_name" }
            
            if (typeIdMapping != null && typeNameMapping != null) {
                val typeId = jsonObject.optLong(typeIdMapping.sourceField, 0)
                val typeName = jsonObject.optString(typeNameMapping.sourceField, "")
                
                if (typeId > 0 && typeName.isNotEmpty()) {
                    categories.add(Category(
                        type_id = typeId,
                        type_name = typeName,
                        parent_type_id = 0 // 默认为一级分类
                    ))
                }
            }
        }
        
        return categories
    }
    
    /**
     * 解析内容列表
     * @param jsonArray 内容JSON数组
     * @return 视频项列表
     */
    private fun parseItems(jsonArray: JSONArray): List<VideoItem> {
        val items = mutableListOf<VideoItem>()
        
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val item = parseItem(jsonObject)
            items.add(item)
        }
        
        return items
    }
    
    /**
     * 解析单个内容项
     * @param jsonObject 内容JSON对象
     * @return 视频项
     */
    private fun parseItem(jsonObject: JSONObject): VideoItem {
        // 创建一个映射，用于存储解析后的字段值
        val fieldValues = mutableMapOf<String, Any?>()
        
        // 遍历字段映射配置，解析每个字段
        for (mapping in contentTypeConfig.fieldMappings) {
            val value = when {
                jsonObject.has(mapping.sourceField) -> {
                    when (mapping.targetField) {
                        "vod_id", "type_id" -> jsonObject.optLong(mapping.sourceField, 0)
                        "vod_serial", "vod_year", "vod_duration" -> jsonObject.optInt(mapping.sourceField, 0)
                        else -> jsonObject.optString(mapping.sourceField, mapping.defaultValue)
                    }
                }
                mapping.defaultValue != null -> mapping.defaultValue
                mapping.isRequired -> throw IllegalArgumentException("Required field ${mapping.sourceField} not found")
                else -> null
            }
            
            fieldValues[mapping.targetField] = value
        }
        
        // 创建VideoItem对象
        return VideoItem(
            vod_id = fieldValues["vod_id"] as? Long ?: 0,
            vod_name = fieldValues["vod_name"] as? String ?: "",
            vod_sub = fieldValues["vod_sub"] as? String,
            vod_remarks = fieldValues["vod_remarks"] as? String,
            vod_serial = fieldValues["vod_serial"] as? Int,
            type_id = fieldValues["type_id"] as? Long ?: 0,
            vod_actor = fieldValues["vod_actor"] as? String,
            vod_director = fieldValues["vod_director"] as? String,
            vod_pic = fieldValues["vod_pic"] as? String,
            vod_content = fieldValues["vod_content"] as? String,
            vod_time = fieldValues["vod_time"] as? String,
            vod_area = fieldValues["vod_area"] as? String,
            vod_lang = fieldValues["vod_lang"] as? String,
            vod_year = fieldValues["vod_year"] as? Int,
            type_name = fieldValues["type_name"] as? String,
            vod_tag = fieldValues["vod_tag"] as? String,
            vod_play_url = fieldValues["vod_play_url"] as? String,
            vod_duration = fieldValues["vod_duration"] as? Int
        )
    }
}
