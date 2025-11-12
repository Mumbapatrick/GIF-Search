package com.giphy.giphysearchapp.ui.screens

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.giphy.giphysearchapp.ui.components.GifItemView
import com.giphy.giphysearchapp.viewmodel.GifViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GifSearchScreen(
    navController: NavController,
    viewModel: GifViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("search") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Internet connection monitoring
    val isOnline = remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) { isOnline.value = false }
            override fun onAvailable(network: Network) { isOnline.value = true }
        })
    }

    // Track app background to refresh after 30s
    var lastPausedTime by remember { mutableStateOf<Long?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> lastPausedTime = System.currentTimeMillis()
                Lifecycle.Event.ON_RESUME -> {
                    lastPausedTime?.let {
                        if (System.currentTimeMillis() - it > 30_000) {
                            val currentQuery = query.ifBlank { "trending" }
                            viewModel.refreshGifs(currentQuery)
                        }
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Initial load
    LaunchedEffect(Unit) { viewModel.loadGifs("trending") }

    // Debounced search
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(700)
            viewModel.loadGifs(query)
        }
    }

    val uiState = viewModel.getUiState(query).collectAsState().value
    val gifs = uiState.data?.collectAsLazyPagingItems()
    val gridCells = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE)
        GridCells.Fixed(4) else GridCells.Fixed(2)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Giphy Search") }) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onHomeClick = {
                    selectedTab = "home"
                    query = ""
                    focusManager.clearFocus()
                    viewModel.loadGifs("trending")
                },
                onSearchClick = {
                    selectedTab = "search"
                    scope.launch { delay(100); focusRequester.requestFocus() }
                },
                onFavoritesClick = { navController.navigate("favorites") }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshGifs(query.ifBlank { "trending" }) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isOnline.value) {
                OfflineState { viewModel.loadGifs(query.ifBlank { "trending" }) }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search GIFs") },
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.clickable {
                                    if (query.isNotBlank()) {
                                        focusManager.clearFocus()
                                        viewModel.loadGifs(query)
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .focusRequester(focusRequester)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Categories
                    val categories = listOf(
                        "Trending", "Stickers", "Text", "Memes", "Artists",
                        "Reactions", "Emojis", "Animals", "Sports", "Movies"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { category ->
                            AssistChip(
                                onClick = {
                                    query = category
                                    focusManager.clearFocus()
                                    viewModel.loadGifs(category)
                                },
                                label = { Text(category) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content Grid
                    gifs?.let { pagingItems ->
                        when (val loadState = pagingItems.loadState.refresh) {
                            is LoadState.Loading -> AnimatedLoading()
                            is LoadState.Error -> ErrorState(loadState.error) { pagingItems.retry() }
                            is LoadState.NotLoading -> {
                                if (pagingItems.itemCount == 0) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No GIFs found",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = gridCells,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(pagingItems.itemCount) { index ->
                                            pagingItems[index]?.let { gif ->
                                                GifItemView(
                                                    gifItem = gif,
                                                    onClick = {
                                                        navController.currentBackStackEntry
                                                            ?.savedStateHandle
                                                            ?.set("selected_gif", gif)
                                                        navController.navigate("details")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } ?: AnimatedLoading()
                }
            }
        }
    }
}

@Composable
fun OfflineState(onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No internet connection",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun AnimatedLoading() {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp).scale(scale))
    }
}

@Composable
fun ErrorState(error: Throwable, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error.localizedMessage ?: "Something went wrong.",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedItem: String,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        val items = listOf(
            Triple("home", Icons.Default.Home, onHomeClick),
            Triple("search", Icons.Default.Search, onSearchClick)
        )
        items.forEach { (name, icon, onClick) ->
            val isSelected = name == selectedItem
            val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1f)
            NavigationBarItem(
                selected = isSelected,
                onClick = onClick,
                icon = { Icon(icon, contentDescription = name, modifier = Modifier.scale(scale)) },
                label = {
                    Text(
                        text = name.replaceFirstChar { it.uppercase() },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
        NavigationBarItem(
            selected = false,
            onClick = onFavoritesClick,
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") }
        )
    }
}
