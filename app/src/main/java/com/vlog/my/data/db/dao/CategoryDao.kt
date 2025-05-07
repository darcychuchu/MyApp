package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 */
@Dao
interface CategoryDao {
    /**
     * 插入分类列表，如果已存在则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    /**
     * 获取指定小程序的所有分类
     */
    @Query("SELECT * FROM categories WHERE sub_url = :subUrl ORDER BY parent_type_id, type_id")
    fun getCategoriesBySubUrl(subUrl: String): Flow<List<CategoryEntity>>

    /**
     * 获取指定小程序的一级分类（parent_type_id = 0）
     */
    @Query("SELECT * FROM categories WHERE sub_url = :subUrl AND parent_type_id = 0 ORDER BY type_id")
    fun getParentCategoriesBySubUrl(subUrl: String): Flow<List<CategoryEntity>>

    /**
     * 获取指定父分类的子分类
     */
    @Query("SELECT * FROM categories WHERE sub_url = :subUrl AND parent_type_id = :parentTypeId ORDER BY type_id")
    fun getChildCategoriesByParent(subUrl: String, parentTypeId: Long): Flow<List<CategoryEntity>>

    /**
     * 删除指定小程序的所有分类
     */
    @Query("DELETE FROM categories WHERE sub_url = :subUrl")
    suspend fun deleteCategoriesBySubUrl(subUrl: String)
}
