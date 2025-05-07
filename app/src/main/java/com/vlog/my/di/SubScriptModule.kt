package com.vlog.my.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.api.JsonApiService
import com.vlog.my.data.api.UnitJsonAdapter
import com.vlog.my.data.local.subscript.SubScriptDao
import com.vlog.my.data.parser.CustomParserService
import com.vlog.my.data.repository.JsonApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 小程序专用限定符
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SubScriptOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SubScriptMoshi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SubScriptRetrofit

/**
 * 小程序功能的依赖注入模块
 * 与主应用的网络模块分开，避免相互影响
 */
@Module
@InstallIn(SingletonComponent::class)
object SubScriptModule {

    @Provides
    @Singleton
    @SubScriptOkHttpClient
    fun provideSubScriptOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // 增加读取超时时间
            .writeTimeout(120, TimeUnit.SECONDS) // 增加写入超时时间
            .build()
    }

    @Provides
    @Singleton
    @SubScriptMoshi
    fun provideSubScriptMoshi(): Moshi {
        return Moshi.Builder()
            .add(Unit::class.java, UnitJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideJsonApiService(
        @SubScriptOkHttpClient okHttpClient: OkHttpClient,
        @SubScriptMoshi moshi: Moshi
    ): JsonApiService {
        // 创建一个不依赖于基础URL的Retrofit实例，因为JSON API的URL是动态的
        return Retrofit.Builder()
            .baseUrl("https://placeholder.com/") // 这个URL不会被使用，因为我们使用@Url注解
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JsonApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideCustomParserService(subScriptDao: SubScriptDao): CustomParserService {
        return CustomParserService(subScriptDao)
    }

    @Provides
    @Singleton
    fun provideJsonApiRepository(
        jsonApiService: JsonApiService,
        customParserService: CustomParserService
    ): JsonApiRepository {
        return JsonApiRepository(jsonApiService, customParserService)
    }
}
