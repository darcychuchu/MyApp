package com.vlog.my.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 关注关系数据模型
 */
@JsonClass(generateAdapter = true)
data class Followers(
    val id: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    
    @Json(name = "isLocked")
    val isLocked: Int? = null,
    
    @Json(name = "isEnabled")
    val isEnabled: Int? = null,
    
    @Json(name = "isTyped")
    val isTyped: Int? = null,
    
    @Json(name = "orderSort")
    val orderSort: Int? = null,
    
    val version: Int? = null,
    val createdBy: String? = null,
    val userId: String? = null,
    val alternateId: String? = null,
    
    // 用户信息（可能从关联查询中获取）
    val userInfo: UserInfo? = null
)

/**
 * 用户基本信息（用于显示关注/粉丝列表）
 */
@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: String,
    val name: String,
    val nickName: String? = null,
    val avatar: String? = null,
    val description: String? = null
)
