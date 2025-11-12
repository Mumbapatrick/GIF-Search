package com.giphy.giphysearchapp.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items // Use this specific import for lists
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.viewmodel.GifViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    viewModel: GifViewModel,
    onGifClick: (GifItem) -> Unit,
    onBackClick: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // Added for Snackbar notifications
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val columns = if (configuration.orientation ==
        android.content.res.Configuration.ORIENTATION_LANDSCAPE
    ) 4 else 2

    // ✅ FIX: This refreshes the data on the previous screen to prevent a crash on back navigation.
    LaunchedEffect(Unit) {
        viewModel.refreshGifs("trending")
    }

    // ✅ Optimization: Memoize the ImageLoader so it's not rebuilt on every recomposition.
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
    }

    // Handle system back button press
    BackHandler { onBackClick() }

    Scaffold(
        // ✅ ADD: A host for showing Snackbar notifications to the user.
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No favorites yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // ✅ FIX: Use items(list, key) for better performance and stability when removing items.
                items(favorites, key = { it.id!! }) { gif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { onGifClick(gif) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    // Maintained your original URL logic as requested
                                    .data(gif.images?.preview?.url ?: gif.images?.original?.url)
                                    .crossfade(true)
                                    .build(),
                                imageLoader = imageLoader,
                                contentDescription = gif.title ?: "GIF",
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite(gif)
                                    // ✅ ADD: Show a notification when an item is removed.
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Removed from favorites 💔")
                                    }
                                },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Remove from favorites",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
