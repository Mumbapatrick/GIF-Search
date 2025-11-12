package com.giphy.giphysearchapp.ui.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
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

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(gifItem.images?.original?.url)
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build(),
        imageLoader = imageLoader
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🖼️ Animated GIF display
            Image(
                painter = painter,
                contentDescription = gifItem.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            // 🏷️ GIF Title (trimmed if too long)
            gifItem.title?.ifBlank { "Untitled GIF" }?.let {
                Text(
                    text = it  ,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }
    }
}
