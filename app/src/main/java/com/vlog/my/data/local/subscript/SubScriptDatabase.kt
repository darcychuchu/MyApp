package com.vlog.my.data.local.subscript

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vlog.my.data.db.converter.ContentTypeConfigConverter
import com.vlog.my.data.db.dao.CategoryDao
import com.vlog.my.data.db.dao.EbookBookmarkDao
import com.vlog.my.data.db.dao.EbookChapterDao
import com.vlog.my.data.db.dao.EbookDao
import com.vlog.my.data.db.dao.VideoDao
import com.vlog.my.data.db.entity.CategoryEntity
import com.vlog.my.data.db.entity.VideoEntity
import com.vlog.my.data.local.entity.SubScriptEntity
import com.vlog.my.data.model.EnhancedJsonConfig
import com.vlog.my.data.model.ebook.EbookBookmarkEntity
import com.vlog.my.data.model.ebook.EbookChapterEntity
import com.vlog.my.data.model.ebook.EbookEntity

/**
 * 小程序专用数据库
 * 与主应用的数据库分开，避免相互影响
 */
@Database(
    entities = [
        SubScriptEntity::class,
        CategoryEntity::class,
        VideoEntity::class,
        EbookEntity::class,
        EbookChapterEntity::class,
        EbookBookmarkEntity::class,
        EnhancedJsonConfig::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(ContentTypeConfigConverter::class)
abstract class SubScriptDatabase : RoomDatabase() {
    abstract fun subScriptDao(): SubScriptDao
    abstract fun categoryDao(): CategoryDao
    abstract fun videoDao(): VideoDao
    abstract fun ebookDao(): EbookDao
    abstract fun ebookChapterDao(): EbookChapterDao
    abstract fun ebookBookmarkDao(): EbookBookmarkDao
    abstract fun enhancedJsonConfigDao(): EnhancedJsonConfigDao

    companion object {
        @Volatile
        private var INSTANCE: SubScriptDatabase? = null

        fun getDatabase(context: Context): SubScriptDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubScriptDatabase::class.java,
                    "subscript_database"
                )
                //.fallbackToDestructiveMigration() // 数据库版本升级时，如果没有提供迁移策略，则重新创建数据库
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
