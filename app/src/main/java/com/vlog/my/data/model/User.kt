package com.vlog.my.data.model

/**
 * 用户模型
 */
data class User(
    val id: String,
    val username: String,
    val email: String? = null,
    val avatar: String? = null
)
