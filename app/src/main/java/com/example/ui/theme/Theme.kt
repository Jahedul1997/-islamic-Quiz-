package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val IslamicDarkColorScheme = darkColorScheme(
    primary = Gold400,
    onPrimary = Emerald950,
    primaryContainer = Emerald700,
    onPrimaryContainer = Gold100,
    secondary = Emerald500,
    onSecondary = Color.White,
    secondaryContainer = MidnightForestCard,
    onSecondaryContainer = Emerald100,
    tertiary = Gold500,
    onTertiary = Emerald950,
    background = MidnightForestBg,
    onBackground = TextLightPrimary,
    surface = MidnightForestSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = MidnightForestCard,
    onSurfaceVariant = TextLightSecondary,
    outline = Emerald600,
    error = ErrorCrimson,
    onError = Color.White
)

private val IslamicLightColorScheme = lightColorScheme(
    primary = Emerald800,
    onPrimary = Color.White,
    primaryContainer = Emerald50,
    onPrimaryContainer = Emerald900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold700,
    tertiary = Gold500,
    onTertiary = Emerald950,
    background = IvoryBg,
    onBackground = TextDarkPrimary,
    surface = IvorySurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = Emerald200,
    error = ErrorCrimson,
    onError = Color.White
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IslamicQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IslamicDarkColorScheme else IslamicLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
