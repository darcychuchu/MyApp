package com.vlog.my.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 增强型JSON小程序配置
 * 支持多接口、API密钥和自定义参数名
 */
@Entity(tableName = "enhanced_json_configs")
data class EnhancedJsonConfig(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,                     // 配置名称
    val apiKey: String,                   // API密钥
    val listUrl: String,                  // 列表接口URL
    val detailUrl: String,                // 详情接口URL
    val searchUrl: String,                // 搜索接口URL
    val categoryUrl: String,              // 分类接口URL
    val pageParamName: String = "page",   // 分页参数名
    val sizeParamName: String = "size",   // 大小参数名
    val classParamName: String = "class", // 分类参数名
    val keyParamName: String = "key",     // 密钥参数名
    val idParamName: String = "id",       // ID参数名
    val keywordParamName: String = "keyword", // 关键词参数名
    val defaultPageSize: Int = 20,        // 默认页大小
    val maxPageSize: Int = 30,            // 最大页大小
    val listDataPath: String = "list",    // 列表数据路径
    val detailDataPath: String = "data",  // 详情数据路径
    val categoryDataPath: String = "class", // 分类数据路径
    val searchDataPath: String = "list",  // 搜索数据路径
    val createdAt: Long = System.currentTimeMillis()
)
