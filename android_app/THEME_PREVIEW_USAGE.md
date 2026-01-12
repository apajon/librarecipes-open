# Theme Preview Usage Guide

## Overview

The `ThemePreview.kt` file provides a comprehensive visual testing system for the LibraRecipes Android app's theming without needing to run the full application. This allows for fast iteration on colors and typography using Android Studio's built-in preview capabilities.

## Location

```
android_app/app/src/main/java/com/apajon/librarecipes/ui/theme/ThemePreview.kt
```

## How to Use

### In Android Studio

1. **Open the file**: Navigate to `ThemePreview.kt` in Android Studio
2. **View previews**: 
   - Click the "Split" button in the top-right corner to see code and previews side-by-side
   - Or click "Design" to see only the previews
3. **Interactive previews**: All four preview configurations will be displayed:
   - Light Theme (Static Colors)
   - Dark Theme (Static Colors)
   - Light Theme (Dynamic Colors)
   - Dark Theme (Dynamic Colors)

### Making Theme Changes

1. **Edit colors**: Modify values in `Color.kt`
2. **Edit typography**: Modify values in `Type.kt`
3. **See changes instantly**: The preview will update automatically without recompiling the app

## Preview Configurations

The file includes four `@Preview` annotated functions:

### 1. Light Theme (Static Colors)
- Uses custom color scheme defined in `Color.kt`
- `uiMode = Configuration.UI_MODE_NIGHT_NO`
- `dynamicColor = false`

### 2. Dark Theme (Static Colors)
- Uses custom dark color scheme defined in `Color.kt`
- `uiMode = Configuration.UI_MODE_NIGHT_YES`
- `dynamicColor = false`

### 3. Light Theme (Dynamic Colors)
- Shows how theme adapts to Android 12+ Material You dynamic colors
- `uiMode = Configuration.UI_MODE_NIGHT_NO`
- `dynamicColor = true`
- Note: Dynamic colors won't actually render in preview but this tests the theme setup

### 4. Dark Theme (Dynamic Colors)
- Shows how theme adapts to Android 12+ Material You dynamic colors in dark mode
- `uiMode = Configuration.UI_MODE_NIGHT_YES`
- `dynamicColor = true`
- Note: Dynamic colors won't actually render in preview but this tests the theme setup

## Theme Elements Showcased

### Colors
- **Background and Surface colors**: Primary surfaces used throughout the app
- **Primary colors**: Main brand colors (green theme)
- **Secondary colors**: Accent colors (orange theme)
- **Tertiary colors**: Additional accent colors (red theme)
- **Error colors**: Error states and messaging
- All with their respective "on" colors for text/icons

### Typography
- **Display styles**: Large, Medium, Small (for prominent text)
- **Headline styles**: Large, Medium, Small (for section headers)
- **Title styles**: Large, Medium, Small (for card headers and important text)
- **Body styles**: Large, Medium, Small (for main content)
- **Label styles**: Large, Medium, Small (for buttons and small text)

### Components
- **Buttons**: Filled, Tonal, Outlined, Elevated, and Text buttons
- **Cards**: With different color schemes (surface variant, primary container)
- **Surfaces**: Basic surfaces with tonal elevation

## Best Practices

1. **Avoid hardcoded colors**: Always use `MaterialTheme.colorScheme.*` for colors
2. **Use semantic colors**: Choose colors based on their semantic meaning (e.g., `primary` for main actions, `error` for errors)
3. **Test both themes**: Always verify changes look good in both light and dark modes
4. **Typography consistency**: Use `MaterialTheme.typography.*` styles for all text
5. **Keep preview isolated**: The preview code is in a separate file and doesn't affect production UI

## Troubleshooting

### Previews not showing
- Ensure Android Studio is up to date
- Try "Build > Refresh Preview"
- Check that Compose preview support is enabled in Android Studio settings

### Preview shows errors
- Make sure the project builds successfully
- Run "Build > Clean Project" and "Build > Rebuild Project"
- Check that all dependencies in `build.gradle` are properly resolved

### Colors don't look right
- Verify that `LibraRecipesTheme` is wrapping the preview content
- Check that you're using `MaterialTheme.colorScheme.*` and not hardcoded colors
- Ensure color definitions in `Color.kt` are correct

## Integration with Production Code

The `ThemeShowcase` composable is reusable and could potentially be integrated into the app itself (e.g., in a settings or debug screen) if needed, but its primary purpose is for development-time preview.

The four preview functions (`ThemePreview_*`) are marked as `private` and are only used by Android Studio's preview system. They don't increase your app's APK size as they're not included in release builds.

## Example: Adding a New Color Token

1. Open `Color.kt` and add your new color:
   ```kotlin
   val LightNewColor = Color(0xFF123456)
   val DarkNewColor = Color(0xFF789ABC)
   ```

2. Add it to the color scheme in `Theme.kt`:
   ```kotlin
   // In LightColorScheme
   newColor = LightNewColor,
   
   // In DarkColorScheme
   newColor = DarkNewColor,
   ```

3. Open `ThemePreview.kt` in split view to see your new color in all preview configurations

4. Add a color swatch to the `ColorSchemeSection` if you want it displayed:
   ```kotlin
   ColorSwatch(
       name = "New Color",
       color = MaterialTheme.colorScheme.newColor,
       onColor = MaterialTheme.colorScheme.onNewColor
   )
   ```

## Advantages

- **Fast iteration**: No need to compile and run the app
- **Multiple configurations**: See light/dark and static/dynamic themes simultaneously
- **Comprehensive showcase**: All theme tokens and common components in one place
- **No hardcoded values**: Uses only theme tokens, ensuring consistency
- **No new dependencies**: Uses existing Compose tooling
- **Isolated from production**: Preview code doesn't affect production logic
