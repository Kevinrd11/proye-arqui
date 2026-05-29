package com.example.iotambientaltec.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(primary = Color(0xFF0B6E4F), secondary = Color(0xFF1976D2), tertiary = Color(0xFFFFA000), background = Color(0xFFF6FBF8))
private val DarkColors = darkColorScheme(primary = Color(0xFF7BD389), secondary = Color(0xFF90CAF9), tertiary = Color(0xFFFFD54F))

@Composable
fun IoTAmbientalTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = androidx.compose.material3.Typography(), content = content)
}
