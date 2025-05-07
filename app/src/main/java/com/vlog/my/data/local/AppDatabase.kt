package com.vlog.my.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vlog.my.data.local.dao.AppSettingsDao
import com.vlog.my.data.local.entity.AppSettingsEntity

/**
 * 应用主数据库
 * 包含应用设置等基本实体
 */
@Database(entities = [AppSettingsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // 应用设置DAO
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
