package com.giphy.giphysearchapp.ui

data class UiState<T>(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val data: T? = null,
    val error: String? = null
)
