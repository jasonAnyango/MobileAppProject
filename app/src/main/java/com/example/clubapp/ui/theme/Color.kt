package com.example.clubapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// School Brand Colors
val SchoolBlue = Color(0xFF1E73BE)
val SchoolGold = Color(0xFFF56E28)
val SchoolWhite = Color(0xFFFFFFFF)

// Light Theme Colors
val LightPrimary = SchoolBlue
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFD3E4F8)
val LightOnPrimaryContainer = Color(0xFF001D35)

val LightSecondary = SchoolGold
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFFFDCC5)
val LightOnSecondaryContainer = Color(0xFF2B1700)

val LightTertiary = Color(0xFF6750A4)
val LightOnTertiary = Color.White
val LightTertiaryContainer = Color(0xFFE9DDFF)
val LightOnTertiaryContainer = Color(0xFF22005D)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color.White
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightBackground = Color(0xFFFCFCFF)
val LightOnBackground = Color(0xFF1A1C1E)
val LightSurface = Color(0xFFFCFCFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightSurfaceVariant = Color(0xFFE1E2EC)
val LightOnSurfaceVariant = Color(0xFF44474E)

val LightOutline = Color(0xFF75777F)
val LightOutlineVariant = Color(0xFFC5C6D0)

// Dark Theme Colors
val DarkPrimary = Color(0xFFA3CAED)
val DarkOnPrimary = Color(0xFF003354)
val DarkPrimaryContainer = Color(0xFF004A77)
val DarkOnPrimaryContainer = Color(0xFFD3E4F8)

val DarkSecondary = Color(0xFFFFB68C)
val DarkOnSecondary = Color(0xFF4F2500)
val DarkSecondaryContainer = Color(0xFF703800)
val DarkOnSecondaryContainer = Color(0xFFFFDCC5)

val DarkTertiary = Color(0xFFCFBCFF)
val DarkOnTertiary = Color(0xFF381E72)
val DarkTertiaryContainer = Color(0xFF4F378A)
val DarkOnTertiaryContainer = Color(0xFFE9DDFF)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF1A1C1E)
val DarkOnBackground = Color(0xFFE2E2E5)
val DarkSurface = Color(0xFF1A1C1E)
val DarkOnSurface = Color(0xFFE2E2E5)
val DarkSurfaceVariant = Color(0xFF44474E)
val DarkOnSurfaceVariant = Color(0xFFC5C6D0)

val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF44474E)

// Material 3 Color Schemes
val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant
)

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)