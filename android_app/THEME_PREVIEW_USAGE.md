# Theme Preview Usage Guide

## Overview

The `ThemePreview.kt` file provides a comprehensive visual testing system for the LibraRecipes Android app's theming without needing to run the full application. This allows for fast iteration on colors and typography using Android Studio's built-in preview capabilities.

Previews are split into three sections (Colors, Typography, Components) so that each one fits inside the Android Studio preview panel without needing to scroll.

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
3. **Interactive previews**: Each section has its own preview configurations

### Runtime Theme Settings

In the app itself, tap the ⚙️ gear icon in the HomeScreen top bar to switch themes:
- **Follow system** / **Force dark mode**
- **Dynamic colors** (Material You on Android 12+) / **Static colors** (fixed palette)

### Making Theme Changes (development)

1. **Edit colors**: Modify values in `Color.kt`
2. **Edit typography**: Modify values in `Type.kt`
3. **See changes instantly**: The preview will update automatically without recompiling the app

## Preview Sections

### Colors (4 previews)
- Light Static, Dark Static, Light Dynamic, Dark Dynamic
- Shows all color tokens: background, surface, primary, secondary, tertiary, error

### Typography (2 previews)
- Light, Dark
- Shows all type styles: display, headline, title, body, label

### Components (4 previews)
- Light Static, Dark Static, Light Dynamic, Dark Dynamic
- Shows buttons, cards, and surfaces

## Static Colors vs Dynamic Colors

**Static colors** (`dynamicColor = false`): the palette is fixed and defined entirely in `Color.kt`. LibraRecipes uses a green/orange/red scheme regardless of what the user's wallpaper looks like.

**Dynamic colors** (`dynamicColor = true`): available on Android 12+ (Material You). The system extracts a tonal palette directly from the user's wallpaper and overrides the static palette at runtime. On devices below Android 12 the app falls back to the static palette. In Android Studio previews there is no wallpaper, so both modes render identically; the difference only manifests on a real Android 12+ device.

## Troubleshooting

### Previews not showing
- Ensure Android Studio is up to date
- Try "Build > Refresh Preview"
- Check that Compose preview support is enabled in Android Studio settings

### Preview shows errors
- Make sure the project builds successfully
- Run "Build > Clean Project" and "Build > Rebuild Project"
- Check that all dependencies in `build.gradle` are properly resolved
