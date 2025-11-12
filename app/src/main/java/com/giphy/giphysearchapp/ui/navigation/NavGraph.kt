package com.giphy.giphysearchapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.giphy.giphysearchapp.ui.screens.FavoritesScreen
import com.giphy.giphysearchapp.ui.screens.GifDetailsScreen
import com.giphy.giphysearchapp.ui.screens.GifSearchScreen
import com.giphy.giphysearchapp.viewmodel.GifViewModel

@Composable
fun AppNavGraph(
    navController: androidx.navigation.NavHostController,
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

        // 🖼️ Details Screen with GIF ID argument
        composable(
            route = Screen.Details.route + "/{gifId}",
            arguments = listOf(navArgument("gifId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gifId = backStackEntry.arguments?.getString("gifId")
            val selectedGif = viewModel.getGifById(gifId)

            if (selectedGif != null) {
                GifDetailsScreen(
                    initialGif = selectedGif,
                    navController = navController,
                    viewModel = viewModel
                )
            } else {
                navController.popBackStack() // safe fallback
            }
        }

        // ❤️ Favorites Screen
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel = viewModel,
                onGifClick = { gif ->
                    navController.navigate(Screen.Details.route + "/${gif.id}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
