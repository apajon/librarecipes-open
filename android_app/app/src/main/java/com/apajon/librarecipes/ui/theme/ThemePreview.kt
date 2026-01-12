package com.apajon.librarecipes.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * Usage:
 * - Open this file in Android Studio
 * - Use the "Split" or "Design" view to see live previews
 * - Modify colors in Color.kt or typography in Type.kt
 * - Previews update instantly without recompiling the app
 */

/**
 * Main reusable composable that showcases all theme tokens
 */
@Composable
fun ThemeShowcase(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "LibraRecipes Theme",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Color Scheme Section
            ColorSchemeSection()
            
            // Typography Section
            TypographySection()
            
            // Components Section
            ComponentsSection()
        }
    }
}

/**
 * Displays all color tokens from the theme
 */
@Composable
private fun ColorSchemeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Colors",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Background and Surface
        ColorSwatch(
            name = "Background",
            color = MaterialTheme.colorScheme.background,
            onColor = MaterialTheme.colorScheme.onBackground
        )
        ColorSwatch(
            name = "Surface",
            color = MaterialTheme.colorScheme.surface,
            onColor = MaterialTheme.colorScheme.onSurface
        )
        ColorSwatch(
            name = "Surface Variant",
            color = MaterialTheme.colorScheme.surfaceVariant,
            onColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Primary colors
        ColorSwatch(
            name = "Primary",
            color = MaterialTheme.colorScheme.primary,
            onColor = MaterialTheme.colorScheme.onPrimary
        )
        ColorSwatch(
            name = "Primary Container",
            color = MaterialTheme.colorScheme.primaryContainer,
            onColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        // Secondary colors
        ColorSwatch(
            name = "Secondary",
            color = MaterialTheme.colorScheme.secondary,
            onColor = MaterialTheme.colorScheme.onSecondary
        )
        ColorSwatch(
            name = "Secondary Container",
            color = MaterialTheme.colorScheme.secondaryContainer,
            onColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        
        // Tertiary colors
        ColorSwatch(
            name = "Tertiary",
            color = MaterialTheme.colorScheme.tertiary,
            onColor = MaterialTheme.colorScheme.onTertiary
        )
        ColorSwatch(
            name = "Tertiary Container",
            color = MaterialTheme.colorScheme.tertiaryContainer,
            onColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        
        // Error colors
        ColorSwatch(
            name = "Error",
            color = MaterialTheme.colorScheme.error,
            onColor = MaterialTheme.colorScheme.onError
        )
        ColorSwatch(
            name = "Error Container",
            color = MaterialTheme.colorScheme.errorContainer,
            onColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Displays a color swatch with name and sample text
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
 * Displays all typography styles from the theme
 */
@Composable
private fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Typography",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Display styles
        Text(
            text = "Display Large",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Display Medium",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Display Small",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Headline styles
        Text(
            text = "Headline Large",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Headline Medium",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Headline Small",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Title styles
        Text(
            text = "Title Large",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Title Medium",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Title Small",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Body styles
        Text(
            text = "Body Large - This is the main body text style used throughout the app",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Body Medium - This is a medium body text style",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Body Small - This is a small body text style",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label styles
        Text(
            text = "Label Large",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Label Medium",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Label Small",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Displays common Material 3 components with theme styling
 */
@Composable
private fun ComponentsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Components",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Buttons
        Text(
            text = "Buttons",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = {}) {
                Text("Filled")
            }
            FilledTonalButton(onClick = {}) {
                Text("Tonal")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = {}) {
                Text("Outlined")
            }
            ElevatedButton(onClick = {}) {
                Text("Elevated")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(onClick = {}) {
                Text("Text Button")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Cards
        Text(
            text = "Cards",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Card Title",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This is a card component showcasing the surface variant color with appropriate text color.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Primary Container Card",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This card uses the primary container color scheme.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Surfaces
        Text(
            text = "Surfaces",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Surface",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Surface Variant",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ===== Preview Configurations =====

/**
 * Preview: Light theme with dynamic color disabled
 * This shows the custom static color scheme in light mode
 */
@Preview(
    name = "Light Theme (Static Colors)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun ThemePreview_Light_Static() {
    LibraRecipesTheme(
        darkTheme = false,
        dynamicColor = false
    ) {
        ThemeShowcase()
    }
}

/**
 * Preview: Dark theme with dynamic color disabled
 * This shows the custom static color scheme in dark mode
 */
@Preview(
    name = "Dark Theme (Static Colors)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ThemePreview_Dark_Static() {
    LibraRecipesTheme(
        darkTheme = true,
        dynamicColor = false
    ) {
        ThemeShowcase()
    }
}

/**
 * Preview: Light theme with dynamic color enabled
 * This shows how the theme adapts to Android 12+ dynamic colors in light mode
 * Note: Dynamic colors won't actually appear in preview, but this tests the theme setup
 */
@Preview(
    name = "Light Theme (Dynamic Colors)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
private fun ThemePreview_Light_Dynamic() {
    LibraRecipesTheme(
        darkTheme = false,
        dynamicColor = true
    ) {
        ThemeShowcase()
    }
}

/**
 * Preview: Dark theme with dynamic color enabled
 * This shows how the theme adapts to Android 12+ dynamic colors in dark mode
 * Note: Dynamic colors won't actually appear in preview, but this tests the theme setup
 */
@Preview(
    name = "Dark Theme (Dynamic Colors)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ThemePreview_Dark_Dynamic() {
    LibraRecipesTheme(
        darkTheme = true,
        dynamicColor = true
    ) {
        ThemeShowcase()
    }
}
