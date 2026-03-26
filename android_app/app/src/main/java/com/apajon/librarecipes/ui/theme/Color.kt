package com.apajon.librarecipes.ui.theme

import androidx.compose.ui.graphics.Color

// =======================
// LIGHT THEME – carnet clair, végétal, doux
// =======================

val LightPrimary = Color(0xFF6E8F6C)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD8E7D4)
val LightOnPrimaryContainer = Color(0xFF20361F)

val LightSecondary = Color(0xFFC48A52)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFF1D8BF)
val LightOnSecondaryContainer = Color(0xFF4A2A0F)

// Saumon doux, humain, cuisine maison
val LightTertiary = Color(0xFFD68C7A)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFF7D3CA)
val LightOnTertiaryContainer = Color(0xFF5A2C23)

val LightError = Color(0xFFBA1A1A)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnError = Color(0xFFFFFFFF)
val LightOnErrorContainer = Color(0xFF410002)

val LightBackground = Color(0xFFF6F0E6)
val LightOnBackground = Color(0xFF2C241E)

val LightSurface = Color(0xFFECE2D4)
val LightOnSurface = Color(0xFF2C241E)

val LightSurfaceVariant = Color(0xFFD8CCBC)
val LightOnSurfaceVariant = Color(0xFF5F564C)

val LightOutline = Color(0xFF8D8379)

val LightInverseOnSurface = Color(0xFFF6F0E6)
val LightInverseSurface = Color(0xFF2C241E)
val LightInversePrimary = Color(0xFF7FA27A)


// =======================
// DARK THEME – bibliothèque chaude + pointes de bleu nuit
// =======================

val DarkPrimary = Color(0xFF7A5A3E)
val DarkOnPrimary = Color(0xFF1F140A)
val DarkPrimaryContainer = Color(0xFF5B3F28)
val DarkOnPrimaryContainer = Color(0xFFF1DDC8)

val DarkSecondary = Color(0xFFE0B27A)
val DarkOnSecondary = Color(0xFF332006)
val DarkSecondaryContainer = Color(0xFF6B4B24)
val DarkOnSecondaryContainer = Color(0xFFF8E4C8)

val DarkTertiary = Color(0xFFB88A72)
val DarkOnTertiary = Color(0xFF2E170F)
val DarkTertiaryContainer = Color(0xFF5B3B31)
val DarkOnTertiaryContainer = Color(0xFFF0D8CF)

// Fond bibliothèque
val DarkBackground = Color(0xFF181511)
val DarkOnBackground = Color(0xFFE8E1D8)

// Pointes de bleu nuit sur les surfaces
val DarkSurface = Color(0xFF1E2329)
val DarkOnSurface = Color(0xFFE8E1D8)

val DarkSurfaceVariant = Color(0xFF37414A)
val DarkOnSurfaceVariant = Color(0xFFD0C7BC)

val DarkOutline = Color(0xFF8E8A84)

val DarkInverseOnSurface = Color(0xFF181511)
val DarkInverseSurface = Color(0xFFE8E1D8)
val DarkInversePrimary = Color(0xFFC48A52)

val DarkError = Color(0xFFFFB4AB)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnError = Color(0xFF690005)
val DarkOnErrorContainer = Color(0xFFFFDAD6)


// =======================
// CATEGORY COLORS – cohérents avec le branding
// =======================

val CategoryPreparation = Color(0xFFD9A15E)
val CategoryIngredient = Color(0xFF7FA27A)
val CategoryCuisson = Color(0xFFC48A52)
val CategoryFinal = Color(0xFFD68C7A)
val CategoryDefault = Color(0xFF8D8379)


// =======================
// Legacy colors
// =======================

@Deprecated("Use MaterialTheme.colorScheme.primary instead")
val PrimaryGreen = LightPrimary

@Deprecated("Use MaterialTheme.colorScheme.secondary instead")
val SecondaryOrange = LightSecondary

@Deprecated("Use MaterialTheme.colorScheme.tertiary instead")
val AccentRed = LightTertiary


// =======================
// Legacy Material2 colors
// =======================

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
