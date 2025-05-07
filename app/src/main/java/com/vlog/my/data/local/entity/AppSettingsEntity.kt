package com.vlog.my.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 应用设置实体
 * 这是一个占位实体，确保AppDatabase有至少一个实体
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val settingKey: String,
    val settingValue: String
)
