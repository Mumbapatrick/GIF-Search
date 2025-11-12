package com.giphy.giphysearchapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.giphy.giphysearchapp.data.model.GifItem
import com.giphy.giphysearchapp.viewmodel.GifViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifDetailsScreen(
    initialGif: GifItem,
    onBack: () -> Unit,
    navController: NavController,
    viewModel: GifViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var currentGif by remember { mutableStateOf(initialGif) }

    // Related GIFs state (safe default title)
    val relatedUiState = viewModel
        .getRelatedGifsUiState(currentGif.title?.ifBlank { "funny" } ?: "funny")
        .collectAsState()

    // Favorites state
    val favorites by viewModel.favorites.collectAsState()
    val isFavorite = favorites.any { it.id == currentGif.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGif.title?.ifBlank { "GIF Details" } ?: "GIF Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareUrl = currentGif.images?.original?.url ?: return@IconButton
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareUrl)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share GIF via"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share GIF")
                    }

                    IconButton(onClick = {
                        viewModel.toggleFavorite(currentGif)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isFavorite) "Removed from favorites 💔"
                                else "Added to favorites ❤️"
                            )
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (isFavorite)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main GIF — safe nullable handling
            AnimatedContent(targetState = currentGif, label = "GifTransition") { gif ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gif.images?.original?.url ?: gif.images?.preview?.url)
                        .decoderFactory { result, options, _ -> GifDecoder(result.source, options) }
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = gif.title ?: "GIF Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f),
                    contentScale = ContentScale.Crop,
                    onError = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Failed to load main GIF.")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = currentGif.title?.ifBlank { "Untitled GIF" } ?: "Untitled GIF",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            // Username
            currentGif.username?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "By @$it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Rating stars
            currentGif.rating?.let { rating ->
                val ratingValue = when (rating.lowercase()) {
                    "g" -> 5; "pg" -> 4; "pg-13" -> 3; "r" -> 2; else -> 1
                }
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .height(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < ratingValue)
                                Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star Rating",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${rating.uppercase()})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Related GIFs section
            Text(
                text = "Related GIFs",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )

            when {
                relatedUiState.value.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }

                relatedUiState.value.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = relatedUiState.value.error ?: "Failed to load related GIFs.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.refreshRelatedGifs(currentGif.title?.ifBlank { "funny" } ?: "funny")
                        }) {
                            Text("Retry")
                        }
                    }
                }

                relatedUiState.value.data != null -> {
                    val relatedGifs = relatedUiState.value.data!!.collectAsLazyPagingItems()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                    ) {
                        items(relatedGifs.itemCount) { index ->
                            relatedGifs[index]?.let { gif ->
                                Column(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .clickable { currentGif = gif },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(gif.images?.preview?.url ?: gif.images?.original?.url)
                                            .decoderFactory { result, options, _ -> GifDecoder(result.source, options) }
                                            .size(Size.ORIGINAL)
                                            .build(),
                                        contentDescription = gif.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentScale = ContentScale.Crop,
                                        onError = {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to load related GIF.")
                                            }
                                        }
                                    )
                                    Text(
                                        text = gif.title?.ifBlank { "Untitled" } ?: "Untitled",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
