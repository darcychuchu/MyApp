package com.vlog.my.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vlog.my.data.model.json.Category

/**
 * 分类数据库实体
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val type_id: Long,        // 分类ID
    val type_name: String,    // 分类名称
    val parent_type_id: Long, // 父分类ID，0表示一级分类
    val sub_url: String       // 所属小程序URL，用于区分不同小程序的分类
) {
    /**
     * 转换为Category模型
     */
    fun toCategory(): Category {
        return Category(
            type_id = type_id,
            type_name = type_name,
            parent_type_id = parent_type_id
        )
    }

    companion object {
        /**
         * 从Category模型创建实体
         */
        fun fromCategory(category: Category, subUrl: String): CategoryEntity {
            return CategoryEntity(
                type_id = category.type_id,
                type_name = category.type_name,
                parent_type_id = category.parent_type_id,
                sub_url = subUrl
            )
        }
    }
}
