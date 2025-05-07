package com.vlog.my.data.repository

import com.vlog.my.data.db.dao.CategoryDao
import com.vlog.my.data.db.entity.CategoryEntity
import com.vlog.my.data.model.json.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分类数据仓库
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    /**
     * 保存分类列表到数据库
     * @param categories 分类列表
     * @param subUrl 小程序URL
     */
    suspend fun saveCategories(categories: List<Category>, subUrl: String) = withContext(Dispatchers.IO) {
        // 如果分类列表为空，则使用预定义的分类数据
        if (categories.isEmpty()) {
            initializeDefaultCategories(subUrl)
            return@withContext
        }

        // 先删除旧的分类数据
        categoryDao.deleteCategoriesBySubUrl(subUrl)

        // 处理分类的父子关系
        val processedCategories = processCategories(categories)

        // 转换为数据库实体并保存
        val entities = processedCategories.map { CategoryEntity.fromCategory(it, subUrl) }
        categoryDao.insertCategories(entities)
    }

    /**
     * 初始化预定义的分类数据
     * @param subUrl 小程序URL
     */
    suspend fun initializeDefaultCategories(subUrl: String) = withContext(Dispatchers.IO) {
        // 先删除旧的分类数据
        categoryDao.deleteCategoriesBySubUrl(subUrl)

        // 预定义的一级分类，添加"全部"分类
        val parentCategories = listOf(
            Category(type_id = 0, type_name = "全部", parent_type_id = 0),
            Category(type_id = 1, type_name = "电影", parent_type_id = 0),
            Category(type_id = 2, type_name = "连续剧", parent_type_id = 0),
            Category(type_id = 3, type_name = "动漫", parent_type_id = 0),
            Category(type_id = 4, type_name = "综艺", parent_type_id = 0)
        )

        // 预定义的二级分类
        val childCategories = listOf(
            // 电影的子分类
            Category(type_id = 101, type_name = "动作片", parent_type_id = 1),
            Category(type_id = 102, type_name = "喜剧片", parent_type_id = 1),
            Category(type_id = 103, type_name = "爱情片", parent_type_id = 1),
            Category(type_id = 104, type_name = "科幻片", parent_type_id = 1),
            Category(type_id = 105, type_name = "恐怖片", parent_type_id = 1),
            Category(type_id = 106, type_name = "剧情片", parent_type_id = 1),

            // 连续剧的子分类
            Category(type_id = 201, type_name = "国产剧", parent_type_id = 2),
            Category(type_id = 202, type_name = "港台剧", parent_type_id = 2),
            Category(type_id = 203, type_name = "日韩剧", parent_type_id = 2),
            Category(type_id = 204, type_name = "欧美剧", parent_type_id = 2),

            // 动漫的子分类
            Category(type_id = 301, type_name = "国产动漫", parent_type_id = 3),
            Category(type_id = 302, type_name = "日本动漫", parent_type_id = 3),
            Category(type_id = 303, type_name = "欧美动漫", parent_type_id = 3),

            // 综艺的子分类
            Category(type_id = 401, type_name = "大陆综艺", parent_type_id = 4),
            Category(type_id = 402, type_name = "港台综艺", parent_type_id = 4),
            Category(type_id = 403, type_name = "日韩综艺", parent_type_id = 4),
            Category(type_id = 404, type_name = "欧美综艺", parent_type_id = 4)
        )

        // 合并所有分类
        val allCategories = parentCategories + childCategories

        // 转换为数据库实体并保存
        val entities = allCategories.map { CategoryEntity.fromCategory(it, subUrl) }
        categoryDao.insertCategories(entities)
    }

    /**
     * 获取指定小程序的所有分类
     * @param subUrl 小程序URL
     * @return 分类列表Flow
     */
    fun getAllCategories(subUrl: String): Flow<List<Category>> {
        return categoryDao.getCategoriesBySubUrl(subUrl)
            .map { entities -> entities.map { it.toCategory() } }
    }

    /**
     * 获取指定小程序的一级分类
     * @param subUrl 小程序URL
     * @return 一级分类列表Flow
     */
    fun getParentCategories(subUrl: String): Flow<List<Category>> {
        return categoryDao.getParentCategoriesBySubUrl(subUrl)
            .map { entities -> entities.map { it.toCategory() } }
    }

    /**
     * 获取指定父分类的子分类
     * @param subUrl 小程序URL
     * @param parentTypeId 父分类ID
     * @return 子分类列表Flow
     */
    fun getChildCategories(subUrl: String, parentTypeId: Long): Flow<List<Category>> {
        return categoryDao.getChildCategoriesByParent(subUrl, parentTypeId)
            .map { entities -> entities.map { it.toCategory() } }
    }

    /**
     * 删除指定小程序的所有分类
     * @param subUrl 小程序URL
     */
    suspend fun deleteAllCategories(subUrl: String) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategoriesBySubUrl(subUrl)
    }

    /**
     * 处理分类数据
     * 解析分类的父子关系，构建层级结构
     * @param categories 原始分类列表
     * @return 处理后的分类列表
     */
    private fun processCategories(categories: List<Category>): List<Category> {
        val result = mutableListOf<Category>()

        // 预定义的一级分类映射（名称 -> ID）
        val predefinedParentCategories = mapOf(
            "电影" to 1L,
            "电视剧" to 2L, // 注意：API中可能返回"电视剧"，而我们预定义的是"连续剧"
            "连续剧" to 2L,
            "动漫" to 3L,
            "综艺" to 4L
        )

        // 添加"全部"分类
        result.add(Category(type_id = 0, type_name = "全部", parent_type_id = 0))

        // 添加预定义的一级分类
        result.add(Category(type_id = 1, type_name = "电影", parent_type_id = 0))
        result.add(Category(type_id = 2, type_name = "连续剧", parent_type_id = 0))
        result.add(Category(type_id = 3, type_name = "动漫", parent_type_id = 0))
        result.add(Category(type_id = 4, type_name = "综艺", parent_type_id = 0))

        // 处理API返回的分类数据
        categories.forEach { category ->
            val nameParts = category.type_name.split("/")
            if (nameParts.size > 1) {
                // 这是带有父子关系的分类
                val parentName = nameParts[0]
                val childName = nameParts[1]

                // 查找对应的预定义一级分类ID
                val parentId = predefinedParentCategories[parentName]

                if (parentId != null) {
                    // 找到了对应的预定义一级分类
                    // 创建子分类，使用原始type_id，但修改type_name和parent_type_id
                    val childCategory = category.copy(
                        type_name = childName,
                        parent_type_id = parentId
                    )
                    result.add(childCategory)
                }
            } else {
                // 没有父子关系的分类，直接添加
                // 但要确保不重复添加已经预定义的一级分类
                val existingCategory = result.find { it.type_name == category.type_name }
                if (existingCategory == null) {
                    result.add(category)
                }
            }
        }

        return result
    }
}
