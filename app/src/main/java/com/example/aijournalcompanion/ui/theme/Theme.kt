package com.example.aijournalcompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Light green colour scheme (default). */
private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Green700,
    onSecondary = Color.White,
    secondaryContainer = Lime100,
    onSecondaryContainer = Green900,
    tertiary = Lime500,
    onTertiary = Green900,
    background = Mint50,
    onBackground = InkDark,
    surface = SurfaceWhite,
    onSurface = InkDark,
    surfaceVariant = Green100,
    onSurfaceVariant = InkMuted,
    outline = OutlineGreen,
    outlineVariant = OutlineGreen,
    error = ErrorRed,
    onError = Color.White
)

/** Dark green colour scheme (used when the phone is in dark mode). */
private val DarkColorScheme = darkColorScheme(
    primary = Green400,
    onPrimary = Green900,
    primaryContainer = Green800Dark,
    onPrimaryContainer = NightText,
    secondary = Green400,
    onSecondary = Green900,
    secondaryContainer = Green800Dark,
    onSecondaryContainer = NightText,
    tertiary = Lime400,
    onTertiary = Green900,
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurface,
    onSurfaceVariant = NightMuted,
    outline = NightOutline,
    outlineVariant = NightOutline,
    error = ErrorRedLight,
    onError = Green900
)

/** Rounded, soft shapes for a minimal look. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * App theme. Dynamic (wallpaper-based) colours are turned off so the
 * green brand colours are always used.
 */
@Composable
fun AIJournalCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
