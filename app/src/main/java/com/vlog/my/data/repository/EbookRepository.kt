package com.vlog.my.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.vlog.my.data.db.dao.EbookBookmarkDao
import com.vlog.my.data.db.dao.EbookChapterDao
import com.vlog.my.data.db.dao.EbookDao
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import com.vlog.my.data.model.ebook.EbookChapterEntity
import com.vlog.my.data.model.ebook.EbookEntity
import com.vlog.my.data.parser.ChapterInfo
import com.vlog.my.data.parser.EbookParseResult
import com.vlog.my.data.parser.EbookParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 电子书仓库
 * 负责电子书的导入、存储和检索
 */
@Singleton
class EbookRepository @Inject constructor(
    private val ebookDao: EbookDao,
    private val chapterDao: EbookChapterDao,
    private val bookmarkDao: EbookBookmarkDao,
    private val parser: EbookParser,
    private val context: Context
) {
    /**
     * 导入电子书
     * @param uri 文件URI
     * @return 导入结果
     */
    suspend fun importEbook(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 复制文件到应用私有目录
            val fileName = getFileName(context, uri) ?: "ebook_${System.currentTimeMillis()}.txt"
            val file = File(context.filesDir, "ebooks/$fileName")
            file.parentFile?.mkdirs()
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // 解析电子书
            val parseResult = parser.parseTxtFile(file.absolutePath)
            
            if (parseResult is EbookParseResult.Success) {
                // 创建电子书实体
                val ebook = EbookEntity(
                    title = parseResult.title,
                    filePath = file.absolutePath,
                    fileSize = parseResult.fileSize,
                    totalChapters = parseResult.chapters.size
                )
                
                // 保存电子书
                ebookDao.insertEbook(ebook)
                
                // 保存章节
                val chapters = parseResult.chapters.map { chapterInfo ->
                    EbookChapterEntity(
                        ebookId = ebook.id,
                        chapterIndex = chapterInfo.index,
                        title = chapterInfo.title,
                        startPosition = chapterInfo.startPosition,
                        endPosition = chapterInfo.endPosition,
                        content = parseResult.content.substring(
                            chapterInfo.startPosition,
                            chapterInfo.endPosition
                        )
                    )
                }
                chapterDao.insertChapters(chapters)
                
                return@withContext Result.success(ebook.id)
            } else {
                return@withContext Result.failure(
                    Exception((parseResult as EbookParseResult.Error).message)
                )
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
    
    /**
     * 获取所有电子书
     */
    fun getAllEbooks(): Flow<List<EbookEntity>> {
        return ebookDao.getAllEbooks()
    }
    
    /**
     * 获取电子书详情
     */
    suspend fun getEbookById(ebookId: String): EbookEntity? {
        return ebookDao.getEbookById(ebookId)
    }
    
    /**
     * 获取电子书章节
     */
    fun getChaptersByEbookId(ebookId: String): Flow<List<EbookChapterEntity>> {
        return chapterDao.getChaptersByEbookId(ebookId)
    }
    
    /**
     * 获取特定章节
     */
    suspend fun getChapter(ebookId: String, chapterIndex: Int): EbookChapterEntity? {
        return chapterDao.getChapter(ebookId, chapterIndex)
    }
    
    /**
     * 更新阅读进度
     */
    suspend fun updateReadProgress(ebookId: String, chapter: Int, position: Int) {
        ebookDao.updateReadProgress(ebookId, chapter, position)
    }
    
    /**
     * 添加书签
     */
    suspend fun addBookmark(bookmark: EbookBookmarkEntity): Long {
        return bookmarkDao.insertBookmark(bookmark)
    }
    
    /**
     * 获取书签
     */
    fun getBookmarksByEbookId(ebookId: String): Flow<List<EbookBookmarkEntity>> {
        return bookmarkDao.getBookmarksByEbookId(ebookId)
    }
    
    /**
     * 删除书签
     */
    suspend fun deleteBookmark(bookmark: EbookBookmarkEntity) {
        bookmarkDao.deleteBookmark(bookmark)
    }
    
    /**
     * 删除电子书
     */
    suspend fun deleteEbook(ebook: EbookEntity) {
        // 删除文件
        val file = File(ebook.filePath)
        if (file.exists()) {
            file.delete()
        }
        
        // 删除数据库记录
        ebookDao.deleteEbook(ebook)
    }
    
    /**
     * 分享电子书
     * 创建一个新的电子书记录，关联到原始电子书的文件
     */
    suspend fun shareEbook(ebookId: String, recipientId: String): Result<String> {
        val ebook = ebookDao.getEbookById(ebookId) ?: return Result.failure(Exception("电子书不存在"))
        
        // 创建分享的电子书记录
        val sharedEbook = ebook.copy(
            id = UUID.randomUUID().toString(),
            createdBy = ebook.createdBy,
            isShared = true,
            lastReadChapter = 0,
            lastReadPosition = 0,
            lastReadDate = null
        )
        
        ebookDao.insertEbook(sharedEbook)
        
        // 复制章节
        val chapters = chapterDao.getChaptersByEbookId(ebookId).first()
        val sharedChapters = chapters.map { chapter ->
            chapter.copy(
                id = 0,
                ebookId = sharedEbook.id
            )
        }
        chapterDao.insertChapters(sharedChapters)
        
        return Result.success(sharedEbook.id)
    }
    
    /**
     * 获取文件名
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        result = it.getString(nameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }
}
