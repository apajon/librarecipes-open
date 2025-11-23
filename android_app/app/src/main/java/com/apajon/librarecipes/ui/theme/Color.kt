package com.apajon.librarecipes.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val LightPrimary = Color(0xFF4CAF50)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFC8E6C9)
val LightOnPrimaryContainer = Color(0xFF1B5E20)

val LightSecondary = Color(0xFFFF9800)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFE0B2)
val LightOnSecondaryContainer = Color(0xFFE65100)

val LightTertiary = Color(0xFFF44336)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFCDD2)
val LightOnTertiaryContainer = Color(0xFFB71C1C)

val LightError = Color(0xFFBA1A1A)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnError = Color(0xFFFFFFFF)
val LightOnErrorContainer = Color(0xFF410002)

val LightBackground = Color(0xFFFDFCF9)
val LightOnBackground = Color(0xFF1B1C18)
val LightSurface = Color(0xFFFDFCF9)
val LightOnSurface = Color(0xFF1B1C18)
val LightSurfaceVariant = Color(0xFFDDE5DA)
val LightOnSurfaceVariant = Color(0xFF424940)
val LightOutline = Color(0xFF727970)
val LightInverseOnSurface = Color(0xFFF1F1EC)
val LightInverseSurface = Color(0xFF30312D)
val LightInversePrimary = Color(0xFFA8D3A9)

// Dark Theme Colors
val DarkPrimary = Color(0xFF81C784)
val DarkOnPrimary = Color(0xFF003A00)
val DarkPrimaryContainer = Color(0xFF2E7D32)
val DarkOnPrimaryContainer = Color(0xFFC8E6C9)

val DarkSecondary = Color(0xFFFFB74D)
val DarkOnSecondary = Color(0xFF4E2600)
val DarkSecondaryContainer = Color(0xFFF57C00)
val DarkOnSecondaryContainer = Color(0xFFFFE0B2)

val DarkTertiary = Color(0xFFEF9A9A)
val DarkOnTertiary = Color(0xFF690000)
val DarkTertiaryContainer = Color(0xFFC62828)
val DarkOnTertiaryContainer = Color(0xFFFFCDD2)

val DarkError = Color(0xFFFFB4AB)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnError = Color(0xFF690005)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF1B1C18)
val DarkOnBackground = Color(0xFFE4E3DE)
val DarkSurface = Color(0xFF1B1C18)
val DarkOnSurface = Color(0xFFE4E3DE)
val DarkSurfaceVariant = Color(0xFF424940)
val DarkOnSurfaceVariant = Color(0xFFC1C9BF)
val DarkOutline = Color(0xFF8B9389)
val DarkInverseOnSurface = Color(0xFF1B1C18)
val DarkInverseSurface = Color(0xFFE4E3DE)
val DarkInversePrimary = Color(0xFF4CAF50)

// Photo Category Colors (for consistency with XML resources)
val CategoryPreparation = Color(0xFFFFEB3B)  // Yellow
val CategoryIngredient = Color(0xFF4CAF50)   // Green
val CategoryCuisson = Color(0xFFFF9800)      // Orange
val CategoryFinal = Color(0xFF2196F3)        // Blue
val CategoryDefault = Color(0xFF9E9E9E)      // Gray

// Legacy colors (keep for compatibility, but prefer theme colors)
@Deprecated("Use MaterialTheme.colorScheme.primary instead")
val PrimaryGreen = LightPrimary
@Deprecated("Use MaterialTheme.colorScheme.secondary instead")
val SecondaryOrange = LightSecondary
@Deprecated("Use MaterialTheme.colorScheme.tertiary instead")
val AccentRed = LightTertiary

// Legacy Material2 colors (kept for backward compatibility but not used in theme)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)