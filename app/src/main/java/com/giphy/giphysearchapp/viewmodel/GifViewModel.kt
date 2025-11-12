package com.giphy.giphysearchapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.giphy.giphysearchapp.data.local.FavoritesManager
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.data.repository.GifRepository
import com.giphy.giphysearchapp.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GifViewModel @Inject constructor(
    private val repository: GifRepository,
    private val favoritesManager: FavoritesManager // Injected from DataModule
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiStates = mutableMapOf<String, MutableStateFlow<UiState<Flow<PagingData<GifItem>>>>>()
    private val _relatedUiStates = mutableMapOf<String, MutableStateFlow<UiState<Flow<PagingData<GifItem>>>>>()

    private val _favorites = MutableStateFlow<List<GifItem>>(emptyList())
    val favorites: StateFlow<List<GifItem>> = _favorites.asStateFlow()

    private val _uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvents: Flow<UiEvent> = _uiEvents.receiveAsFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    init {
        // Load persisted favorites from DataStore
        viewModelScope.launch {
            favoritesManager.favoritesFlow.collect { list ->
                _favorites.value = list
            }
        }

        loadGifs("trending")
        observeSearchQuery()
    }

    // --- Search ---
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(600)
                .distinctUntilChanged()
                .filter { it.length >= 2 || it.isBlank() }
                .collectLatest { query ->
                    loadGifs(query.ifBlank { "trending" })
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadGifs(query: String, forceRefresh: Boolean = false) {
        val key = query.ifBlank { "trending" }.lowercase()
        val state = _uiStates.getOrPut(key) { MutableStateFlow(UiState(isLoading = true)) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (forceRefresh || state.value.data == null) {
                fetchGifs(key, state, refresh = false)
            }
        }
    }

    fun refreshGifs(query: String) {
        val key = query.ifBlank { "trending" }.lowercase()
        val state = _uiStates.getOrPut(key) { MutableStateFlow(UiState(isRefreshing = true)) }

        viewModelScope.launch {
            fetchGifs(key, state, refresh = true)
        }
    }

    private fun fetchGifs(
        key: String,
        state: MutableStateFlow<UiState<Flow<PagingData<GifItem>>>>,
        refresh: Boolean
    ) {
        viewModelScope.launch {
            state.update { it.copy(isLoading = !refresh, isRefreshing = refresh, error = null) }

            try {
                val flow = when (key) {
                    "trending" -> repository.trendingGifs()
                    "stickers" -> repository.stickers()
                    "text" -> repository.textGifs()
                    "memes" -> repository.memes()
                    "artists" -> repository.artists()
                    "reactions" -> repository.reactions()
                    "emojis" -> repository.emojis()
                    "animals" -> repository.animals()
                    else -> repository.searchGifs(key)
                }.cachedIn(viewModelScope)

                state.value = UiState(
                    data = flow,
                    isLoading = false,
                    isRefreshing = false
                )
            } catch (e: IOException) {
                handleError(state, "Network error. Check your internet connection.")
            } catch (e: Exception) {
                handleError(state, e.localizedMessage ?: "Something went wrong.")
            }
        }
    }

    // --- Favorites ---
    fun toggleFavorite(gif: GifItem) {
        viewModelScope.launch {
            if (_favorites.value.any { it.id == gif.id }) {
                favoritesManager.removeFavorite(gif)
                _uiEvents.send(UiEvent.ShowSnackbar("Removed from favorites ❌"))
            } else {
                favoritesManager.addFavorite(gif)
                _uiEvents.send(UiEvent.ShowSnackbar("Added to favorites ❤️"))
            }
        }
    }

    fun isFavorite(gif: GifItem): Boolean =
        _favorites.value.any { it.id == gif.id }

    // --- Related GIFs ---
    fun getRelatedGifsUiState(query: String): StateFlow<UiState<Flow<PagingData<GifItem>>>> {
        val key = query.ifBlank { "funny" }.lowercase()
        val state = _relatedUiStates.getOrPut(key) { MutableStateFlow(UiState(isLoading = true)) }

        if (state.value.data == null && state.value.error == null) {
            viewModelScope.launch { fetchRelatedGifs(key, state) }
        }

        return state
    }

    private suspend fun fetchRelatedGifs(
        key: String,
        state: MutableStateFlow<UiState<Flow<PagingData<GifItem>>>>
    ) {
        state.update { it.copy(isLoading = true, error = null) }

        try {
            val flow = repository.searchGifs(key).cachedIn(viewModelScope)
            state.value = UiState(data = flow, isLoading = false, isRefreshing = false)
        } catch (e: IOException) {
            state.value = UiState(isLoading = false, error = "Network error")
        } catch (e: Exception) {
            state.value = UiState(isLoading = false, error = e.localizedMessage ?: "Something went wrong")
        }
    }

    // --- Error handling ---
    private suspend fun handleError(
        state: MutableStateFlow<UiState<Flow<PagingData<GifItem>>>>,
        message: String
    ) {
        state.value = UiState(isLoading = false, isRefreshing = false, error = message)
        _uiEvents.send(UiEvent.ShowSnackbar(message))
    }

    // --- UI Events ---
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    fun getUiState(query: String): StateFlow<UiState<Flow<PagingData<GifItem>>>> {
        val key = query.ifBlank { "trending" }.lowercase()
        return _uiStates.getOrPut(key) { MutableStateFlow(UiState()) }
    }

    fun refreshRelatedGifs(query: String) {
        val key = query.ifBlank { "funny" }.lowercase()
        val state = _relatedUiStates.getOrPut(key) { MutableStateFlow(UiState(isRefreshing = true)) }

        viewModelScope.launch { fetchRelatedGifs(key, state) }
    }
}
