package com.giphy.giphysearchapp.di

import android.content.Context
import com.giphy.giphysearchapp.data.local.FavoritesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFavoritesManager(@ApplicationContext context: Context): FavoritesManager {
        return FavoritesManager(context)
    }
}
