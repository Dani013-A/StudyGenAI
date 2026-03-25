package com.studygenai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary       = RoyalBlue,
    secondary     = LavenderPurple,
    tertiary      = BlushPink,
    background    = NeutralGray,
    surface       = SurfaceWhite,
    onPrimary     = SurfaceWhite,
    onBackground  = DarkNavy,
    onSurface     = DarkNavy,
    surfaceVariant = NeutralGray,
    onSurfaceVariant = DarkNavy
)

private val DarkColorScheme = darkColorScheme(
    primary       = RoyalBlue,
    secondary     = LavenderPurple,
    tertiary      = BlushPink,
    background    = AppBackgroundDark,
    surface       = DarkSurface,
    onPrimary     = SurfaceWhite,
    onBackground  = TextPrimaryDark,
    onSurface     = TextPrimaryDark,
    surfaceVariant = AppBackgroundDark,
    onSurfaceVariant = TextPrimaryDark
)

@Composable
fun StudyGenAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
