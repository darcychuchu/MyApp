package com.vlog.my.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vlog.my.data.db.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 视频数据访问对象
 */
@Dao
interface VideoDao {
    /**
     * 插入视频列表，如果已存在则替换
     * 使用OnConflictStrategy.REPLACE策略，确保相同vod_id的记录会被新数据覆盖
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    /**
     * 获取指定小程序的所有视频
     */
    @Query("SELECT * FROM videos WHERE sub_url = :subUrl")
    fun getVideosBySubUrl(subUrl: String): Flow<List<VideoEntity>>

    /**
     * 获取指定小程序和分类的视频
     */
    @Query("SELECT * FROM videos WHERE sub_url = :subUrl AND type_id = :typeId")
    fun getVideosByTypeId(subUrl: String, typeId: Long): Flow<List<VideoEntity>>

    /**
     * 获取指定ID的视频
     */
    @Query("SELECT * FROM videos WHERE vod_id = :vodId AND sub_url = :subUrl LIMIT 1")
    suspend fun getVideoById(vodId: Long, subUrl: String): VideoEntity?

    /**
     * 删除指定小程序的所有视频
     */
    @Query("DELETE FROM videos WHERE sub_url = :subUrl")
    suspend fun deleteVideosBySubUrl(subUrl: String)

    /**
     * 获取指定小程序各分类的视频数量
     * 返回一个包含type_id和count的Map
     */
    @MapInfo(keyColumn = "type_id", valueColumn = "count")
    @Query("SELECT type_id, COUNT(*) as count FROM videos WHERE sub_url = :subUrl GROUP BY type_id")
    fun getCategoryVideoCount(subUrl: String): Flow<Map<Long, Int>>
}
