# Android Theming Guide for LibraRecipes

This document explains how to customize the visual appearance of the LibraRecipes Android app through the centralized theming system.

## Overview

LibraRecipes uses a modern Android theming system built on:
- **Material Design 3** (Material Components for Compose)
- **Jetpack Compose** for UI rendering
- **XML resources** for traditional Android views and theme definitions

All visual styling is centralized in theme resource files, making it easy to change colors, typography, spacing, and other design elements without modifying business logic or screen layouts.

## Theme File Locations

### XML Resources (for traditional Android views)
Located in `app/src/main/res/`:

- **`values/colors.xml`** - Complete color palette for light and dark themes
- **`values/themes.xml`** - Light theme definitions and typography styles
- **`values-night/themes.xml`** - Dark theme definitions
- **`values/dimens.xml`** - Spacing, padding, sizes, and dimension constants
- **`values/strings.xml`** - Text strings

### Compose Theme Files (Kotlin)
Located in `app/src/main/java/com/apajon/librarecipes/ui/theme/`:

- **`Color.kt`** - Compose color definitions for light and dark themes
- **`Theme.kt`** - Main theme composable and color scheme setup
- **`Type.kt`** - Typography system (text styles)

### Drawable Resources
Located in `app/src/main/res/drawable/`:

- **`bg_card.xml`** - Card background shape
- **`bg_button_primary.xml`** - Primary button background
- **`bg_button_outlined.xml`** - Outlined button background
- **`bg_badge.xml`** - Badge background shape

## How to Change Colors

### Compose Colors (Most Common)

Edit `app/src/main/java/com/apajon/librarecipes/ui/theme/Color.kt`:

```kotlin
// Change the primary color for light theme
val LightPrimary = Color(0xFF4CAF50)  // Green

// Change the primary color for dark theme
val DarkPrimary = Color(0xFF81C784)   // Light green
```

After changing colors in `Color.kt`, they automatically apply across all screens that use `MaterialTheme.colorScheme`.

### XML Colors (for XML-based views)

Edit `app/src/main/res/values/colors.xml`:

```xml
<color name="md_theme_light_primary">#4CAF50</color>
<color name="md_theme_dark_primary">#81C784</color>
```

### Available Color Slots

The Material Design 3 color system includes:
- **Primary** - Main brand color (buttons, headers, FABs)
- **Secondary** - Accent color for secondary actions
- **Tertiary** - Additional accent color
- **Error** - Error states and messages
- **Background** - Screen background
- **Surface** - Card and component surfaces
- **Outline** - Borders and dividers

Each color slot has corresponding "on" colors for text/icons that appear on those backgrounds.

## How to Change Typography

Edit `app/src/main/java/com/apajon/librarecipes/ui/theme/Type.kt`:

```kotlin
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,  // Change size here
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // ... other text styles
)
```

### Available Text Styles

Material Design 3 provides these text style categories:
- **Display** (Large/Medium/Small) - For hero text
- **Headline** (Large/Medium/Small) - For section headers
- **Title** (Large/Medium/Small) - For card titles
- **Body** (Large/Medium/Small) - For body text
- **Label** (Large/Medium/Small) - For buttons and small text

## How to Change Spacing and Dimensions

Edit `app/src/main/res/values/dimens.xml`:

```xml
<dimen name="spacing_medium">16dp</dimen>
<dimen name="padding_large">24dp</dimen>
<dimen name="card_elevation_default">4dp</dimen>
```

Then reference these in your layouts or Compose code:
```kotlin
Modifier.padding(dimensionResource(R.dimen.spacing_medium))
```

## How to Add a New Theme Variant

### Option 1: Create a Seasonal Theme

1. Create new color definitions in `Color.kt`:
```kotlin
// Christmas theme colors
val ChristmasRed = Color(0xFFDC143C)
val ChristmasGreen = Color(0xFF228B22)
```

2. Create a new color scheme in `Theme.kt`:
```kotlin
private val ChristmasColorScheme = lightColorScheme(
    primary = ChristmasRed,
    secondary = ChristmasGreen,
    // ... other colors
)
```

3. Add a parameter to toggle the theme:
```kotlin
@Composable
fun LibraRecipesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useChristmasTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useChristmasTheme -> ChristmasColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    // ... rest of theme
}
```

### Option 2: Create Configuration-Based Themes

Store theme preferences in SharedPreferences or DataStore and load them at app startup.

## How Dark Mode Works

LibraRecipes automatically switches between light and dark themes based on system settings:

- Light theme resources: `values/themes.xml`
- Dark theme resources: `values-night/themes.xml`

To force dark or light mode:
```kotlin
LibraRecipesTheme(
    darkTheme = true,  // Force dark mode
    dynamicColor = false  // Disable Android 12+ dynamic colors
) {
    // Your content
}
```

## Testing Theme Changes

### Quick Visual Test

1. Change a color value in `Color.kt` or `colors.xml`
2. Rebuild the app: `./gradlew assembleDebug`
3. Run on device/emulator
4. The new color should appear immediately across all screens

### Test Dark Mode

On your device:
1. Go to Settings → Display → Dark theme
2. Toggle dark mode on/off
3. Reopen the app to see theme changes

### Verify Consistency

After theme changes, check these screens:
- Home screen (cards, FAB)
- Recipe list (list items, search)
- Recipe detail (photos, badges, buttons)
- Recipe form (input fields, dropdowns)

## Common Theming Tasks

### Change App Primary Color

1. Edit `Color.kt`: Change `LightPrimary` and `DarkPrimary`
2. Edit `colors.xml`: Change `md_theme_light_primary` and `md_theme_dark_primary`
3. Build and run

### Change Photo Category Colors

Edit the category color constants in `Color.kt`:
```kotlin
val CategoryPreparation = Color(0xFFFFEB3B)  // Yellow
val CategoryIngredient = Color(0xFF4CAF50)   // Green
val CategoryCuisson = Color(0xFFFF9800)      // Orange
val CategoryFinal = Color(0xFF2196F3)        // Blue
```

### Adjust Card Elevation

Edit `dimens.xml`:
```xml
<dimen name="card_elevation_default">8dp</dimen>  <!-- Increase from 4dp -->
```

### Change Button Corner Radius

Edit drawable resources like `drawable/bg_button_primary.xml`:
```xml
<corners android:radius="16dp" />  <!-- Change from 24dp for less rounded -->
```

## Best Practices

1. **Always use theme colors** - Reference `MaterialTheme.colorScheme.primary` instead of hardcoding `Color(0xFF...)`
2. **Use semantic color names** - Use "primary" instead of "green" so colors can change
3. **Test both themes** - Always check light and dark mode
4. **Keep contrast accessible** - Ensure text is readable on backgrounds (use "on" colors)
5. **Use dimension resources** - Define spacing in `dimens.xml` for consistency

## Troubleshooting

### Colors don't change after editing
- Clean and rebuild: `./gradlew clean assembleDebug`
- Make sure you edited both Compose (`.kt`) and XML (`.xml`) files if needed

### Dark mode not working
- Check that `values-night/themes.xml` exists
- Verify system dark mode is enabled on device

### Text is hard to read
- Use "on" colors: `colorScheme.onPrimary` for text on primary backgrounds
- Check contrast ratios meet accessibility guidelines

## Additional Resources

- [Material Design 3 Color System](https://m3.material.io/styles/color/the-color-system/overview)
- [Material Design 3 Typography](https://m3.material.io/styles/typography/overview)
- [Jetpack Compose Theming Guide](https://developer.android.com/jetpack/compose/themes)

---

**Version:** 1.0  
**Last Updated:** November 2024
