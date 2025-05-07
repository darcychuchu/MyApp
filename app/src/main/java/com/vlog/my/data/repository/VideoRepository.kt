package com.vlog.my.data.repository

import com.vlog.my.data.db.dao.VideoDao
import com.vlog.my.data.db.entity.VideoEntity
import com.vlog.my.data.model.json.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 视频数据仓库
 */
@Singleton
class VideoRepository @Inject constructor(
    private val videoDao: VideoDao
) {
    /**
     * 保存视频列表到数据库
     * 使用OnConflictStrategy.REPLACE策略，确保相同vod_id的记录会被新数据覆盖
     * @param videos 视频列表
     * @param subUrl 小程序URL
     */
    suspend fun saveVideos(videos: List<VideoItem>, subUrl: String) = withContext(Dispatchers.IO) {
        // 转换为数据库实体并保存
        val entities = videos.map { VideoEntity.fromVideoItem(it, subUrl) }
        videoDao.insertVideos(entities)
    }

    /**
     * 获取指定小程序的所有视频
     * @param subUrl 小程序URL
     * @return 视频列表Flow
     */
    fun getAllVideos(subUrl: String): Flow<List<VideoItem>> {
        return videoDao.getVideosBySubUrl(subUrl)
            .map { entities -> entities.map { it.toVideoItem() } }
    }

    /**
     * 获取指定小程序和分类的视频
     * @param subUrl 小程序URL
     * @param typeId 分类ID
     * @return 视频列表Flow
     */
    fun getVideosByTypeId(subUrl: String, typeId: Long): Flow<List<VideoItem>> {
        return videoDao.getVideosByTypeId(subUrl, typeId)
            .map { entities -> entities.map { it.toVideoItem() } }
    }

    /**
     * 获取指定ID的视频
     * @param vodId 视频ID
     * @param subUrl 小程序URL
     * @return 视频对象，如果不存在则返回null
     */
    suspend fun getVideoById(vodId: Long, subUrl: String): VideoItem? = withContext(Dispatchers.IO) {
        videoDao.getVideoById(vodId, subUrl)?.toVideoItem()
    }

    /**
     * 删除指定小程序的所有视频
     * @param subUrl 小程序URL
     */
    suspend fun deleteAllVideos(subUrl: String) = withContext(Dispatchers.IO) {
        videoDao.deleteVideosBySubUrl(subUrl)
    }

    /**
     * 获取指定小程序各分类的视频数量
     * @param subUrl 小程序URL
     * @return 分类视频数量统计Flow
     */
    fun getCategoryVideoCount(subUrl: String): Flow<Map<Long, Int>> {
        return videoDao.getCategoryVideoCount(subUrl)
    }
}
