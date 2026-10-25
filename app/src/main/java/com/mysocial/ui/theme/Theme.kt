package com.mysocial.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// App é dark-only (estilo "Lights out" do X), por isso só existe um ColorScheme,
// sem variação clara/escura e sem dynamicColor (que substituiria a paleta fixa
// pelas cores do papel de parede do usuário no Android 12+).
private val MySocialColorScheme = darkColorScheme(
    primary = PrimaryColor,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnBackground,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder,
    error = ErrorColor
)

@Composable
fun MySocialTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MySocialColorScheme,
        typography = Typography,
        content = content
    )
}
