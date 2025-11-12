package com.giphy.giphysearchapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.data.network.GiphyService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GifRepository @Inject constructor(
    private val apiService: GiphyService
) {

    fun searchGifs(query: String): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, query) }
        ).flow
    }

    fun trendingGifs(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "trending") }
        ).flow
    }

    fun stickers(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "stickers") }
        ).flow
    }

    fun textGifs(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "text") }
        ).flow
    }

    fun memes(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "memes") }
        ).flow
    }

    fun artists(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "artists") }
        ).flow
    }

    fun reactions(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "reactions") }
        ).flow
    }

    fun emojis(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "emojis") }
        ).flow
    }

    fun animals(): Flow<PagingData<GifItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { GifPagingSource(apiService, "animals") }
        ).flow
    }
}
