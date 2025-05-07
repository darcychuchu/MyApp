package com.vlog.my.di

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.api.ApiConstants
import com.vlog.my.data.api.FavoritesService
import com.vlog.my.data.api.FollowersService
import com.vlog.my.data.api.LoggingInterceptor
import com.vlog.my.data.api.MessageService
import com.vlog.my.data.api.StoriesService
import com.vlog.my.data.api.SubScriptService
import com.vlog.my.data.api.UnitJsonAdapter
import com.vlog.my.data.api.UserService
import com.vlog.my.data.api.VideoUploadService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val customLoggingInterceptor = LoggingInterceptor()

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)  // 增加读取超时时间
            .writeTimeout(120, TimeUnit.SECONDS) // 增加写入超时时间
            .build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(Unit::class.java, UnitJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        val baseUrl = ApiConstants.BASE_URL
        Log.d("NetworkModule", "创建Retrofit实例，BASE_URL: $baseUrl")

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    @Provides
    @Singleton
    fun provideStoriesService(retrofit: Retrofit): StoriesService {
        return retrofit.create(StoriesService::class.java)
    }

    @Provides
    @Singleton
    fun provideSubScriptService(retrofit: Retrofit): SubScriptService {
        return retrofit.create(SubScriptService::class.java)
    }

    @Provides
    @Singleton
    fun provideMessageService(retrofit: Retrofit): MessageService {
        return retrofit.create(MessageService::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoritesService(retrofit: Retrofit): FavoritesService {
        return retrofit.create(FavoritesService::class.java)
    }

    @Provides
    @Singleton
    fun provideFollowersService(retrofit: Retrofit): FollowersService {
        return retrofit.create(FollowersService::class.java)
    }

    /**
     * 提供视频上传服务
     * 使用专用的OkHttpClient，避免OOM问题
     */
    @Provides
    @Singleton
    fun provideVideoUploadService(
        @VideoUploadClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): VideoUploadService {
        val baseUrl = ApiConstants.BASE_URL
        Log.d("NetworkModule", "创建VideoUploadService实例，BASE_URL: $baseUrl")

        val videoUploadRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return videoUploadRetrofit.create(VideoUploadService::class.java)
    }
}
