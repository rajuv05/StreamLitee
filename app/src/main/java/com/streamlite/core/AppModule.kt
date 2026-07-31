package com.streamlite.core

import com.streamlite.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
  @Provides @Singleton fun provideSettingsRepository(repository: SettingsRepository): SettingsRepository = repository
}
