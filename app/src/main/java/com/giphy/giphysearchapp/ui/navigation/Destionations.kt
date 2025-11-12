package com.giphy.giphysearchapp.ui.navigation

/**
 * App navigation routes used across NavGraph.
 * Centralized here to avoid typos and improve scalability.
 */
sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Details : Screen("details")
    data object Favorites : Screen("favorites")
}
