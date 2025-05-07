package com.vlog.my.ui.screens.publish

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vlog.my.data.api.ApiResponseCode
import com.vlog.my.data.preferences.UserSessionManager
import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

/**
 * 视频发布视图模型
 */
@HiltViewModel
class VideoPublishViewModel @Inject constructor(
    private val storiesRepository: StoriesRepository,
    private val userSessionManager: UserSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        // 缓冲区大小：8KB
        private const val BUFFER_SIZE = 8 * 1024
    }

    // 标题
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    // 描述
    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    // 标签
    private val _tags = MutableStateFlow("")
    val tags: StateFlow<String> = _tags

    // 视频文件
    private val _videoUri = MutableStateFlow<Uri?>(null)
    val videoUri: StateFlow<Uri?> = _videoUri

    // 视频缩略图
    private val _thumbnailBitmap = MutableStateFlow<Bitmap?>(null)
    val thumbnailBitmap: StateFlow<Bitmap?> = _thumbnailBitmap

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 发布成功状态
    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess

    /**
     * 更新标题
     */
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    /**
     * 更新描述
     */
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    /**
     * 更新标签
     */
    fun updateTags(newTags: String) {
        _tags.value = newTags
    }

    /**
     * 设置视频
     * @return 如果视频时长超过限制，返回false；否则返回true
     */
    fun setVideo(uri: Uri): Boolean {
        // 检查视频时长
        val duration = getVideoDuration(uri)
        val maxDuration = 30000L // 30秒 (毫秒)

        if (duration > maxDuration) {
            return false
        }

        _videoUri.value = uri
        generateThumbnail(uri)
        return true
    }

    /**
     * 获取视频时长（毫秒）
     */
    private fun getVideoDuration(uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            retriever.release()
            duration
        } catch (e: Exception) {
            Log.e("VideoPublishViewModel", "获取视频时长失败: ${e.message}")
            0
        }
    }

    /**
     * 清除视频
     */
    fun clearVideo() {
        _videoUri.value = null
        _thumbnailBitmap.value = null
    }

    /**
     * 生成视频缩略图
     */
    private fun generateThumbnail(videoUri: Uri) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, videoUri)

            // 获取视频时长
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

            // 从视频的 1/3 处获取缩略图
            val timeUs = duration * 1000 / 3
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            _thumbnailBitmap.value = bitmap
            retriever.release()
        } catch (e: Exception) {
            Log.e("VideoPublishViewModel", "生成缩略图失败: ${e.message}")
        }
    }

    /**
     * 发布视频
     */
    fun publishVideo() {
        // 验证输入
        if (_videoUri.value == null) {
            _error.value = "请选择视频"
            return
        }

        if (_description.value.isBlank()) {
            _error.value = "请输入描述"
            return
        }

        val userName = userSessionManager.getUserName()
        val token = userSessionManager.getAccessToken()

        if (userName.isNullOrBlank() || token.isNullOrBlank()) {
            _error.value = "用户未登录"
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // 获取视频文件
                val videoFile = getFileFromUri(_videoUri.value!!) ?: run {
                    _error.value = "获取视频文件失败"
                    _isLoading.value = false
                    return@launch
                }

                // 创建 MultipartBody.Part，使用缓冲流处理大文件
                val mediaType = when {
                    videoFile.name.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
                    videoFile.name.endsWith(".mov", ignoreCase = true) -> "video/quicktime"
                    videoFile.name.endsWith(".m3u8", ignoreCase = true) -> "application/x-mpegURL"
                    else -> "video/*"
                }

                val requestFile = videoFile.asRequestBody(mediaType.toMediaTypeOrNull())
                val videoPart = MultipartBody.Part.createFormData("videoFile", videoFile.name, requestFile)

                // 发布视频
                val response = storiesRepository.createArtwork(
                    name = userName,
                    token = token,
                    videoFile = videoPart,
                    title = _title.value.takeIf { it.isNotBlank() },
                    description = _description.value,
                    tags = _tags.value.takeIf { it.isNotBlank() }
                )

                if (response.code == ApiResponseCode.SUCCESS) {
                    // 发布成功
                    _publishSuccess.value = true

                    // 重置表单
                    _title.value = ""
                    _description.value = ""
                    _tags.value = ""
                    _videoUri.value = null
                    _thumbnailBitmap.value = null
                } else {
                    // 发布失败
                    _error.value = response.message ?: "发布失败"
                }
            } catch (e: Exception) {
                _error.value = "发布失败: ${e.message}"
                Log.e("VideoPublishViewModel", "发布失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 从 Uri 获取文件
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            Log.d("VideoPublishViewModel", "处理URI: $uri, scheme: ${uri.scheme}")

            when {
                uri.scheme == "file" -> {
                    val file = File(uri.path ?: return null)
                    Log.d("VideoPublishViewModel", "文件路径: ${file.absolutePath}, 存在: ${file.exists()}, 大小: ${file.length()}")
                    file
                }
                uri.scheme == "content" -> {
                    // 尝试从 content URI 获取实际文件路径
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                        if (columnIndex != -1) {
                            val filePath = cursor.getString(columnIndex)
                            cursor.close()

                            if (!filePath.isNullOrEmpty()) {
                                val file = File(filePath)
                                if (file.exists()) {
                                    Log.d("VideoPublishViewModel", "从内容URI获取到文件: ${file.absolutePath}, 大小: ${file.length()}")
                                    return file
                                }
                            }
                        } else {
                            cursor.close()
                        }
                    }

                    // 如果无法获取实际路径，则复制到临时文件
                    Log.d("VideoPublishViewModel", "无法获取实际路径，复制到临时文件")

                    // 确定文件扩展名
                    val mimeType = context.contentResolver.getType(uri)
                    val extension = when {
                        mimeType?.contains("mp4") == true -> ".mp4"
                        mimeType?.contains("quicktime") == true -> ".mov"
                        mimeType?.contains("mpegurl") == true -> ".m3u8"
                        else -> ".mp4" // 默认扩展名
                    }

                    // 创建临时文件
                    val tempFile = File.createTempFile("video_", extension, context.cacheDir)

                    // 使用缓冲流复制文件，减少内存使用
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val bufferedInput = input.buffered(BUFFER_SIZE)
                        tempFile.outputStream().buffered(BUFFER_SIZE).use { output ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var bytesRead: Int
                            var totalBytesRead = 0L

                            while (bufferedInput.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead

                                // 每复制10MB记录一次日志
                                if (totalBytesRead % (10 * 1024 * 1024) < BUFFER_SIZE) {
                                    Log.d("VideoPublishViewModel", "已复制: ${totalBytesRead / (1024 * 1024)}MB")
                                }
                            }
                        }
                    }

                    Log.d("VideoPublishViewModel", "复制到临时文件完成: ${tempFile.absolutePath}, 大小: ${tempFile.length()}")
                    tempFile
                }
                else -> {
                    Log.e("VideoPublishViewModel", "不支持的URI scheme: ${uri.scheme}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("VideoPublishViewModel", "获取文件失败", e)
            null
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 重置发布成功状态
     */
    fun resetPublishSuccess() {
        _publishSuccess.value = false
    }
}
