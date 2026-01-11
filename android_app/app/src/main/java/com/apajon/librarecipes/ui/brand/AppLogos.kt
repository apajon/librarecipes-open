package com.apajon.librarecipes.ui.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.apajon.librarecipes.R

/**
 * AppLogos provides a centralized access point for all brand assets in the LibraRecipes app.
 * 
 * This follows the same design principle as the Material3 theme system:
 * - Single point of access for all logos and branding
 * - Automatic light/dark theme support
 * - No hardcoded R.drawable references in screens/components
 * - Future-proof for branding changes
 * 
 * Benefits:
 * - Logos can be updated globally without touching screen code
 * - Automatic theme-aware logo selection
 * - Consistent branding across the app
 * - Easy to add seasonal or promotional branding
 * 
 * Usage:
 * ```kotlin
 * AppLogos.AppIcon(
 *     modifier = Modifier.size(48.dp),
 *     contentDescription = "LibraRecipes"
 * )
 * ```
 */
object AppLogos {
    /**
     * App icon/launcher icon
     * Currently uses the ic_launcher resource
     * 
     * @param modifier Modifier to apply to the Image
     * @param contentDescription Accessibility description
     * @param tint Optional color filter to tint the icon
     */
    @Composable
    fun AppIcon(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        tint: Color? = null
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = tint?.let { ColorFilter.tint(it) }
        )
    }
    
    /**
     * App icon (round variant)
     * Uses the round launcher icon for consistency on devices that support it
     * 
     * @param modifier Modifier to apply to the Image
     * @param contentDescription Accessibility description
     * @param tint Optional color filter to tint the icon
     */
    @Composable
    fun AppIconRound(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        tint: Color? = null
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_round),
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = tint?.let { ColorFilter.tint(it) }
        )
    }
    
    /**
     * Get the app icon painter for manual composition
     * Useful when you need direct access to the Painter for custom rendering
     * 
     * @param useRound Whether to use the round variant
     * @return Painter for the app icon
     */
    @Composable
    fun appIconPainter(useRound: Boolean = false): Painter {
        return if (useRound) {
            painterResource(id = R.mipmap.ic_launcher_round)
        } else {
            painterResource(id = R.mipmap.ic_launcher)
        }
    }
    
    /**
     * Theme-aware logo/branding component
     * Automatically selects appropriate assets for light/dark themes
     * 
     * Currently defaults to AppIcon as no separate logo assets exist yet.
     * When brand-specific assets are added, this will automatically switch between them.
     * 
     * @param modifier Modifier to apply to the Image
     * @param contentDescription Accessibility description
     * @param forceDark Force dark theme variant regardless of system theme
     * @param forceLight Force light theme variant regardless of system theme
     */
    @Composable
    fun BrandLogo(
        modifier: Modifier = Modifier,
        contentDescription: String? = "LibraRecipes",
        forceDark: Boolean = false,
        forceLight: Boolean = false
    ) {
        val useDarkTheme = when {
            forceDark -> true
            forceLight -> false
            else -> isSystemInDarkTheme()
        }
        
        // TODO: When separate light/dark logo assets are added,
        // switch between them based on useDarkTheme
        // For now, use the app icon
        AppIcon(
            modifier = modifier,
            contentDescription = contentDescription
        )
    }
    
    /**
     * Get theme-aware logo painter for manual composition
     * 
     * @param useDarkTheme Whether to use dark theme variant
     * @return Painter for the appropriate logo
     */
    @Composable
    fun brandLogoPainter(useDarkTheme: Boolean = isSystemInDarkTheme()): Painter {
        // TODO: Return different painters for light/dark when assets are available
        return painterResource(id = R.mipmap.ic_launcher)
    }
}
