package com.giphy.giphysearchapp.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.data.network.GiphyService
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GifPagingSource(
    private val apiService: GiphyService,
    private val query: String
) : PagingSource<Int, GifItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GifItem> {
        val page = params.key ?: 0
        val limit = params.loadSize

        return try {
            val response = apiService.searchGifs(
                apiKey = "6BDaH92Eou98bHFdbMHTtFjLKegS9EEY",
                query = query,
                limit = limit,
                offset = page
            )

            val gifs = response.data

            LoadResult.Page(
                data = gifs,
                prevKey = if (page == 0) null else page - limit,
                nextKey = if (gifs.isEmpty()) null else page + limit
            )

        } catch (e: UnknownHostException) {
            // No network / DNS issue
            LoadResult.Error(NetworkUnavailableException("No internet connection. Please check your network."))
        } catch (e: SocketTimeoutException) {
            // Slow network timeout
            LoadResult.Error(NetworkUnavailableException("Network timeout. Please try again."))
        } catch (e: IOException) {
            // Generic I/O errors
            LoadResult.Error(NetworkUnavailableException("Network error. Please check your connection."))
        } catch (e: HttpException) {
            // Server/API error codes
            val message = when (e.code()) {
                400 -> "Invalid request."
                401 -> "Unauthorized access. Please check your API key."
                404 -> "GIFs not found."
                500 -> "Server error. Please try again later."
                else -> "Unexpected server error."
            }
            LoadResult.Error(ApiException(message))
        } catch (e: Exception) {
            // Fallback for unexpected issues
            LoadResult.Error(UnknownException("Something went wrong. Please try again."))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GifItem>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}

/** Custom Exceptions for cleaner user messages */
class NetworkUnavailableException(message: String) : IOException(message)
class ApiException(message: String) : Exception(message)
class UnknownException(message: String) : Exception(message)
