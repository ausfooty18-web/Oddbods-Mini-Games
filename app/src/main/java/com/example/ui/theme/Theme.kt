package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OddbodsPurpleSecondary,
    onPrimary = Color.White,
    secondary = OddbodsYellowAccent,
    onSecondary = Color.Black,
    tertiary = OddbodsFuseOrange,
    background = OddbodsDarkBg,
    surface = OddbodsDarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = OddbodsPurplePrimary,
    onPrimary = Color.White,
    secondary = OddbodsFuseOrange,
    onSecondary = Color.White,
    tertiary = OddbodsPogoBlue,
    background = OddbodsLightBg,
    surface = OddbodsLightSurface,
    onBackground = Color(0xFF1D1B20),
    onSurface = Color(0xFF1D1B20),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Oddbods bold vibrant cartoon colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
