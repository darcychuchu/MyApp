package com.vlog.my.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vlog.my.data.db.dao.ReceivedSubScriptDao
import com.vlog.my.data.db.entity.ReceivedSubScriptEntity
import com.vlog.my.data.db.converter.Converters

/**
 * 应用主数据库
 */
@Database(
    entities = [
        ReceivedSubScriptEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun receivedSubScriptDao(): ReceivedSubScriptDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                //.fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
