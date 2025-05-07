package com.vlog.my.di

import com.vlog.my.data.local.subscript.EnhancedJsonConfigDao
import com.vlog.my.data.local.subscript.SubScriptDatabase
import com.vlog.my.data.repository.EnhancedJsonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 增强型JSON模块的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object EnhancedJsonModule {
    
    @Provides
    @Singleton
    fun provideEnhancedJsonConfigDao(database: SubScriptDatabase): EnhancedJsonConfigDao {
        return database.enhancedJsonConfigDao()
    }
    
    @Provides
    @Singleton
    fun provideEnhancedJsonRepository(dao: EnhancedJsonConfigDao): EnhancedJsonRepository {
        return EnhancedJsonRepository(dao)
    }
}
