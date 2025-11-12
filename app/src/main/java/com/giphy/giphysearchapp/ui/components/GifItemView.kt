package com.giphy.giphysearchapp.ui.components

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.giphy.giphysearchapp.data.model.GifItem

@Composable
fun GifItemView(
    gifItem: GifItem,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    // ✅ Use a Coil ImageLoader that supports animated GIFs
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory()) // modern GIF decoder
            } else {
                add(GifDecoder.Factory()) // fallback for older devices
            }
        }
        .crossfade(true)
        .build()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        // ✅ Use AsyncImage for a simpler and more efficient implementation.
        AsyncImage(
            model = gifItem.images?.original?.url,
            contentDescription = gifItem.title,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Enforce a 1:1 aspect ratio for a uniform grid.
                .clip(RoundedCornerShape(8.dp))
        )

        // 🏷️ Display the GIF title, ensuring it's not blank.
        gifItem.title?.takeIf { it.isNotBlank() }?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
