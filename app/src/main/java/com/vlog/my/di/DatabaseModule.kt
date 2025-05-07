package com.vlog.my.di

import android.content.Context
import com.vlog.my.data.db.dao.CategoryDao
import com.vlog.my.data.db.dao.VideoDao
import com.vlog.my.data.db.dao.ReceivedSubScriptDao
import com.vlog.my.data.db.AppDatabase
import com.vlog.my.data.local.AppDatabase as LocalAppDatabase
import com.vlog.my.data.local.dao.AppSettingsDao
import com.vlog.my.data.local.subscript.SubScriptDatabase
import com.vlog.my.data.local.subscript.SubScriptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 主应用数据库模块
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLocalAppDatabase(@ApplicationContext context: Context): LocalAppDatabase {
        return LocalAppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAppSettingsDao(appDatabase: LocalAppDatabase): AppSettingsDao {
        return appDatabase.appSettingsDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideReceivedSubScriptDao(appDatabase: AppDatabase): ReceivedSubScriptDao {
        return appDatabase.receivedSubScriptDao()
    }
}

/**
 * 小程序专用数据库模块
 * 与主应用的数据库分开，避免相互影响
 */
@Module
@InstallIn(SingletonComponent::class)
object SubScriptDatabaseModule {

    @Provides
    @Singleton
    fun provideSubScriptDatabase(@ApplicationContext context: Context): SubScriptDatabase {
        return SubScriptDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideSubScriptDao(subScriptDatabase: SubScriptDatabase): SubScriptDao {
        return subScriptDatabase.subScriptDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(subScriptDatabase: SubScriptDatabase): CategoryDao {
        return subScriptDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideVideoDao(subScriptDatabase: SubScriptDatabase): VideoDao {
        return subScriptDatabase.videoDao()
    }
}
