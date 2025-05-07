package com.vlog.my.data.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 电子书解析器
 * 负责解析TXT文件，提取章节信息
 */
@Singleton
class EbookParser @Inject constructor() {
    /**
     * 解析TXT文件
     * @param filePath 文件路径
     * @return 解析结果
     */
    suspend fun parseTxtFile(filePath: String): EbookParseResult = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            return@withContext EbookParseResult.Error("文件不存在")
        }

        try {
            // 读取文件内容
            val content = file.readText(Charset.forName("UTF-8"))
            
            // 提取书名（使用文件名作为默认书名）
            val title = file.nameWithoutExtension
            
            // 解析章节
            val chapters = parseChapters(content)
            
            return@withContext EbookParseResult.Success(
                title = title,
                content = content,
                chapters = chapters,
                fileSize = file.length()
            )
        } catch (e: Exception) {
            return@withContext EbookParseResult.Error("解析失败: ${e.message}")
        }
    }
    
    /**
     * 解析章节
     * 使用正则表达式匹配常见的章节标题格式
     */
    private fun parseChapters(content: String): List<ChapterInfo> {
        val chapterPatterns = listOf(
            Regex("第[0-9一二三四五六七八九十百千]+章.*"),
            Regex("Chapter [0-9]+.*"),
            Regex("第[0-9一二三四五六七八九十百千]+节.*")
        )
        
        val chapters = mutableListOf<ChapterInfo>()
        var currentIndex = 0
        
        // 查找所有可能的章节标题
        for (pattern in chapterPatterns) {
            val matches = pattern.findAll(content)
            for (match in matches) {
                val startPosition = match.range.first
                val title = match.value.trim()
                chapters.add(
                    ChapterInfo(
                        index = currentIndex++,
                        title = title,
                        startPosition = startPosition,
                        endPosition = content.length // 暂时设置为文件末尾，后面会更新
                    )
                )
            }
        }
        
        // 按照位置排序章节
        chapters.sortBy { it.startPosition }
        
        // 更新章节结束位置
        for (i in 0 until chapters.size - 1) {
            chapters[i] = chapters[i].copy(endPosition = chapters[i + 1].startPosition)
        }
        
        // 如果没有找到章节，则将整个内容作为一个章节
        if (chapters.isEmpty()) {
            chapters.add(ChapterInfo(
                index = 0,
                title = "全文",
                startPosition = 0,
                endPosition = content.length
            ))
        }
        
        return chapters
    }
}

/**
 * 电子书解析结果
 */
sealed class EbookParseResult {
    data class Success(
        val title: String,
        val content: String,
        val chapters: List<ChapterInfo>,
        val fileSize: Long
    ) : EbookParseResult()
    
    data class Error(val message: String) : EbookParseResult()
}

/**
 * 章节信息
 */
data class ChapterInfo(
    val index: Int,
    val title: String?,
    val startPosition: Int,
    val endPosition: Int
)
