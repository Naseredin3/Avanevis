package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPolishBlueLight,
    onPrimary = Color(0xFF002D6C),
    primaryContainer = PrimaryPolishBlueDark,
    onPrimaryContainer = PrimaryPolishContainer,
    secondary = SecondaryPolishCyanLight,
    onSecondary = Color(0xFF003353),
    secondaryContainer = Color(0xFF004B76),
    onSecondaryContainer = SecondaryPolishContainer,
    tertiary = AccentAmberLight,
    onTertiary = Color(0xFF4A3400),
    tertiaryContainer = Color(0xFF684900),
    onTertiaryContainer = AccentAmberContainer,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPolishBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryPolishContainer,
    onPrimaryContainer = OnPrimaryPolishContainer,
    secondary = SecondaryPolishCyan,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPolishContainer,
    onSecondaryContainer = OnSecondaryPolishContainer,
    tertiary = AccentAmber,
    onTertiary = Color.White,
    tertiaryContainer = AccentAmberContainer,
    onTertiaryContainer = AccentAmberOnContainer,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve our custom branded Persian theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
