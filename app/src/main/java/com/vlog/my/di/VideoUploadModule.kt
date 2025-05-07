package com.vlog.my.di

import com.vlog.my.data.api.LoggingInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 视频上传模块
 * 提供专用于视频上传的OkHttpClient，避免OOM问题
 */
@Module
@InstallIn(SingletonComponent::class)
object VideoUploadModule {

    /**
     * 提供专用于视频上传的OkHttpClient
     * 不记录请求体，避免大文件导致OOM
     */
    @Provides
    @Singleton
    @VideoUploadClient
    fun provideVideoUploadOkHttpClient(): OkHttpClient {
        // 创建一个只记录请求头的日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 只记录请求头，不记录请求体
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(120, TimeUnit.SECONDS)  // 增加连接超时时间
            .readTimeout(180, TimeUnit.SECONDS)     // 增加读取超时时间
            .writeTimeout(180, TimeUnit.SECONDS)    // 增加写入超时时间
            .build()
    }
}

/**
 * 视频上传客户端限定符
 * 用于标识专用于视频上传的OkHttpClient
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class VideoUploadClient
