package com.giphy.giphysearchapp.ui.themes

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Gradient examples
val GradientPrimary = Brush.linearGradient(
    colors = listOf(Purple40, Pink40)
)

val GradientSecondary = Brush.linearGradient(
    colors = listOf(PurpleGrey40, Purple40)
)

val GradientBackground = Brush.linearGradient(
    colors = listOf(Color(0xFF1A1A2E), Color(0xFF162447))
)
