package com.giphy.giphysearchapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.ui.screens.FavoritesScreen
import com.giphy.giphysearchapp.ui.screens.GifDetailsScreen
import com.giphy.giphysearchapp.ui.screens.GifSearchScreen
import com.giphy.giphysearchapp.viewmodel.GifViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GifViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search.route,
        modifier = modifier
    ) {
        // 🔍 Search Screen
        composable(Screen.Search.route) {
            GifSearchScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // 🖼️ Details Screen
        composable(Screen.Details.route) {
            // ✅ Retrieve GifItem from previous back stack entry
            val selectedGif: GifItem? =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<GifItem>("selected_gif")

            if (selectedGif != null) {
                GifDetailsScreen(
                    initialGif = selectedGif,
                    onBack = { navController.popBackStack() },
                    navController = navController,
                    viewModel = viewModel
                )
            } else {
                // fallback if null
                navController.popBackStack()
            }
        }

        // ❤️ Favorites Screen
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onGifClick = { gif ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_gif", gif)
                    navController.navigate(Screen.Details.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
