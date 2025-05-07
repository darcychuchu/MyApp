package com.vlog.my.di

import com.vlog.my.data.repository.StoriesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StoriesRepositoryEntryPoint {
    fun storiesRepository(): StoriesRepository
}
