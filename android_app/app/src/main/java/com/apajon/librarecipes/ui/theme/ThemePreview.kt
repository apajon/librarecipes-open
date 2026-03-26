package com.apajon.librarecipes.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * ThemePreview - Comprehensive theme showcase for LibraRecipes
 *
 * This file provides visual testing for the app's theming system without running the app.
 * It showcases all Material 3 theme tokens including colors, typography, and components.
 *
 * ## Static colors vs Dynamic colors
 *
 * **Static colors** (`dynamicColor = false`): the palette is fixed and defined entirely in
 * [Color.kt]. LibraRecipes uses a green / orange / red scheme regardless of what the user's
 * wallpaper looks like.
 *
 * **Dynamic colors** (`dynamicColor = true`): available on Android 12+ (Material You). The
 * system extracts a tonal palette directly from the user's wallpaper and overrides the static
 * palette at runtime. The app then automatically adapts to the user's personalization choices.
 * On devices below Android 12 the app falls back to the static palette. In Android Studio
 * previews there is no wallpaper, so both modes render identically; the difference only
 * manifests on a real Android 12+ device.
 *
 * Previews are split into smaller sections (Colors, Typography, Components) so that each one
 * fits in the Android Studio preview panel without needing to scroll.
 *
 * Usage:
 * - Open this file in Android Studio
 * - Use the "Split" or "Design" view to see live previews
 * - Modify colors in Color.kt or typography in Type.kt
 * - Previews update instantly without recompiling the app
 */

// ===================================================================
// Section composables – reusable building blocks
// ===================================================================

/**
 * Informational banner that explains which color mode is active in this preview.
 */
@Composable
private fun ColorModeBanner(dynamicColor: Boolean) {
    val containerColor = if (dynamicColor)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (dynamicColor)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer
    val title = if (dynamicColor)
        "Dynamic Colors (Material You) — Android 12+"
    else
        "Static Colors — Fixed Palette"
    val description = if (dynamicColor)
        "Colors are generated from the user's wallpaper at runtime. " +
            "This preview falls back to static colors because no wallpaper context is available."
    else
        "Colors are defined in Color.kt (green / orange / red theme). " +
            "They are the same for every user on every device."

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

/**
 * Displays a color swatch with name and sample text.
 */
@Composable
private fun ColorSwatch(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    onColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = onColor,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/**
 * Color scheme showcase – background, surface, primary, secondary, tertiary, error.
 */
@Composable
private fun ColorSchemeSection(dynamicColor: Boolean = false) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LibraRecipes Theme — Colors",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            ColorModeBanner(dynamicColor = dynamicColor)

            // Background and Surface
            ColorSwatch("Background", MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onBackground)
            ColorSwatch("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
            ColorSwatch("Surface Variant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)

            // Primary
            ColorSwatch("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            ColorSwatch("Primary Container", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)

            // Secondary
            ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            ColorSwatch("Secondary Container", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)

            // Tertiary
            ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            ColorSwatch("Tertiary Container", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)

            // Error
            ColorSwatch("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
            ColorSwatch("Error Container", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

/**
 * Typography showcase – display, headline, title, body, label styles.
 */
@Composable
private fun TypographySection() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("LibraRecipes Theme — Typography", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Display Large", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Display Medium", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Display Small", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Headline Large", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Headline Medium", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Headline Small", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Title Large", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Title Medium", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Title Small", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Body Large", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Body Medium", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Body Small", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Label Large", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
            Text("Label Medium", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            Text("Label Small", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

/**
 * Components showcase – buttons, cards, and surfaces.
 */
@Composable
private fun ComponentsSection() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("LibraRecipes Theme — Components", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

            // Buttons
            Text("Buttons", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Filled") }
                FilledTonalButton(onClick = {}) { Text("Tonal") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {}) { Text("Outlined") }
                ElevatedButton(onClick = {}) { Text("Elevated") }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {}) { Text("Text Button") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cards
            Text("Cards", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Card Title", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Surface variant card with appropriate text color.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Primary Container Card", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uses primary container color scheme.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Surfaces
            Text("Surfaces", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(modifier = Modifier.weight(1f).height(80.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.Center) {
                        Text("Surface", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Surface(modifier = Modifier.weight(1f).height(80.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.Center) {
                        Text("Surface Variant", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ===================================================================
// Preview: Colors (light / dark × static / dynamic)
// ===================================================================

@Preview(name = "Colors – Light Static", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ColorsPreview_Light_Static() {
    LibraRecipesTheme(darkTheme = false, dynamicColor = false) { ColorSchemeSection(dynamicColor = false) }
}

@Preview(name = "Colors – Dark Static", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorsPreview_Dark_Static() {
    LibraRecipesTheme(darkTheme = true, dynamicColor = false) { ColorSchemeSection(dynamicColor = false) }
}

@Preview(name = "Colors – Light Dynamic", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ColorsPreview_Light_Dynamic() {
    LibraRecipesTheme(darkTheme = false, dynamicColor = true) { ColorSchemeSection(dynamicColor = true) }
}

@Preview(name = "Colors – Dark Dynamic", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorsPreview_Dark_Dynamic() {
    LibraRecipesTheme(darkTheme = true, dynamicColor = true) { ColorSchemeSection(dynamicColor = true) }
}

// ===================================================================
// Preview: Typography (light / dark)
// ===================================================================

@Preview(name = "Typography – Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun TypographyPreview_Light() {
    LibraRecipesTheme(darkTheme = false, dynamicColor = false) { TypographySection() }
}

@Preview(name = "Typography – Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TypographyPreview_Dark() {
    LibraRecipesTheme(darkTheme = true, dynamicColor = false) { TypographySection() }
}

// ===================================================================
// Preview: Components (light / dark × static / dynamic)
// ===================================================================

@Preview(name = "Components – Light Static", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ComponentsPreview_Light_Static() {
    LibraRecipesTheme(darkTheme = false, dynamicColor = false) { ComponentsSection() }
}

@Preview(name = "Components – Dark Static", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentsPreview_Dark_Static() {
    LibraRecipesTheme(darkTheme = true, dynamicColor = false) { ComponentsSection() }
}

@Preview(name = "Components – Light Dynamic", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ComponentsPreview_Light_Dynamic() {
    LibraRecipesTheme(darkTheme = false, dynamicColor = true) { ComponentsSection() }
}

@Preview(name = "Components – Dark Dynamic", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentsPreview_Dark_Dynamic() {
    LibraRecipesTheme(darkTheme = true, dynamicColor = true) { ComponentsSection() }
}
