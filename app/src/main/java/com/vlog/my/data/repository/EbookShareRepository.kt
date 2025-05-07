package com.vlog.my.data.repository

import com.squareup.moshi.Moshi
import com.vlog.my.data.model.EbookShareContent
import com.vlog.my.data.model.Users
import com.vlog.my.data.model.ebook.EbookEntity
import com.vlog.my.data.repository.StoriesRepository
import com.vlog.my.data.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 电子书分享仓库
 * 负责将电子书分享到动态
 */
@Singleton
class EbookShareRepository @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userDataRepository: UserDataRepository,
    private val moshi: Moshi
) {
    /**
     * 分享电子书到动态
     * @param ebook 电子书
     * @param comment 评论
     * @return 分享结果
     */
    suspend fun shareEbookToFeed(ebook: EbookEntity, comment: String = ""): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 创建电子书分享内容
            val shareContent = EbookShareContent(
                ebookId = ebook.id,
                title = ebook.title,
                author = ebook.author ?: "未知作者",
                description = comment.ifEmpty { "一本精彩的电子书" },
                totalChapters = ebook.totalChapters,
                coverUrl = null, // 暂无封面
                sharedBy = null, // 由服务器设置
                sharedAt = System.currentTimeMillis() // 当前时间
            )

            // 将分享内容转换为JSON
            val adapter = moshi.adapter(EbookShareContent::class.java)
            val shareContentJson = adapter.toJson(shareContent)

            // 获取当前用户信息
            val currentUser = userDataRepository.getCurrentUser() ?: return@withContext Result.failure(Exception("用户未登录"))
            val userName = currentUser.name ?: return@withContext Result.failure(Exception("用户名为空"))
            val token = currentUser.accessToken ?: return@withContext Result.failure(Exception("访问令牌为空"))

            // 调用API发布动态
            val response = storiesRepository.shareStories(
                name = userName,
                token = token,
                title = ebook.title,
                description = comment.ifEmpty { "一本精彩的电子书" },
                tags = null,
                shareContent = shareContentJson,
                shareTyped = 2 // 电子书类型
            )

            // 处理响应
            if (response.code == 0) {
                Result.success("分享成功")
            } else {
                Result.failure(Exception(response.message ?: "分享失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
