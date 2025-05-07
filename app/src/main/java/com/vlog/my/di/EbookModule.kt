package com.vlog.my.di

import android.content.Context
import com.vlog.my.data.db.dao.EbookBookmarkDao
import com.vlog.my.data.db.dao.EbookChapterDao
import com.vlog.my.data.db.dao.EbookDao
import com.vlog.my.data.local.subscript.SubScriptDatabase
import com.vlog.my.data.parser.EbookParser
import com.vlog.my.data.repository.EbookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EbookModule {
    
    @Provides
    @Singleton
    fun provideEbookDao(database: SubScriptDatabase): EbookDao {
        return database.ebookDao()
    }
    
    @Provides
    @Singleton
    fun provideEbookChapterDao(database: SubScriptDatabase): EbookChapterDao {
        return database.ebookChapterDao()
    }
    
    @Provides
    @Singleton
    fun provideEbookBookmarkDao(database: SubScriptDatabase): EbookBookmarkDao {
        return database.ebookBookmarkDao()
    }
    
    @Provides
    @Singleton
    fun provideEbookParser(): EbookParser {
        return EbookParser()
    }
    
    @Provides
    @Singleton
    fun provideEbookRepository(
        ebookDao: EbookDao,
        chapterDao: EbookChapterDao,
        bookmarkDao: EbookBookmarkDao,
        parser: EbookParser,
        @ApplicationContext context: Context
    ): EbookRepository {
        return EbookRepository(
            ebookDao = ebookDao,
            chapterDao = chapterDao,
            bookmarkDao = bookmarkDao,
            parser = parser,
            context = context
        )
    }
}
