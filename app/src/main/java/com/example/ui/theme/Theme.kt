package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PixelDarkColorScheme = darkColorScheme(
    primary = PixelGold,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF4A370A),
    onPrimaryContainer = PixelGoldGlow,
    secondary = PixelMana,
    onSecondary = Color(0xFF002233),
    secondaryContainer = Color(0xFF10344A),
    onSecondaryContainer = Color(0xFFC7EFFF),
    tertiary = PixelEmerald,
    onTertiary = Color(0xFF032600),
    background = PixelBackground,
    onBackground = PixelTextParchment,
    surface = PixelSurface,
    onSurface = PixelTextParchment,
    surfaceVariant = PixelSurfaceElevated,
    onSurfaceVariant = PixelTextMuted,
    outline = PixelBorder,
    outlineVariant = PixelBorderHighlight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Keep consistent indie retro theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PixelDarkColorScheme,
        typography = Typography,
        content = content
    )
}
