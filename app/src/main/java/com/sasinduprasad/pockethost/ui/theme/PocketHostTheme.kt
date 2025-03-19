package com.sasinduprasad.pockethost.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PocketHostTheme(content: @Composable () -> Unit) {

    val isDarkMode = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = if (isDarkMode) {
            darkColorScheme(
                primary = Color(0xFF0C0D0E),
                secondary = Color(0xFF111113),
                tertiary = Color(0xFFFFFFFF),
                surface = Color(0XFF5B25FF),
                onSecondary = Color(0XFF2E3135),
                error = Color(0XFFFF3E34)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0C0D0E),
                secondary = Color(0xFF111113),
                tertiary = Color(0xFFFFFFFF),
                surface = Color(0XFF5B25FF),
                onSecondary = Color(0XFF2E3135),
                error = Color(0XFFFF3E34)
            )
        }
    ) {
        content()
    }
}