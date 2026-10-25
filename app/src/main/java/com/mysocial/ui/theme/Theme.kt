package com.mysocial.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// App é dark-only
private val MySocialColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    error = ErrorColor
)

// Formato padrão dos componentes
private val MySocialShapes = Shapes(
    // Deixa os cantos arendodandos em 50%
    large = RoundedCornerShape(50)
)

@Composable
fun MySocialTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MySocialColorScheme,
        typography = Typography,
        shapes = MySocialShapes,
        content = content
    )
}
