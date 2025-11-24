# Theme Configuration Module

This module provides Pydantic models and utilities for managing LibraRecipes Android theme configuration.

## Overview

The theme configuration system allows:
- **Streamlit UI editing**: Edit theme values through a web interface
- **JSON-based storage**: Human-readable configuration files
- **Strong validation**: Pydantic models ensure data integrity
- **Android XML generation**: Easily convert to Android resources (future)

## Structure

```
src/theme_config/
├── __init__.py        # Package exports
├── models.py          # Pydantic models (ThemeConfig, ColorPalette, etc.)
└── io.py              # Load/save utilities

config/
└── theme.android.json # Default theme configuration

scripts/
└── validate_theme.py  # Validation and testing script

streamlit_app/pages_functions/
└── theme_editor_example.py  # Example Streamlit UI
```

## Models

### `ThemeConfig`
Root configuration model containing:
- `name`: Theme name (string)
- `version`: Theme version (string)
- `colors`: ColorPalette instance
- `typography`: Typography instance
- `spacing`: Spacing instance

### `ColorPalette`
Material Design 3 color system with 23 color slots:
- Primary colors: `primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`
- Secondary colors: `secondary`, `onSecondary`, `secondaryContainer`, `onSecondaryContainer`
- Tertiary colors: `tertiary`, `onTertiary`, `tertiaryContainer`, `onTertiaryContainer`
- Error colors: `error`, `onError`, `errorContainer`, `onErrorContainer`
- Surface colors: `background`, `onBackground`, `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`
- Outline: `outline`

All colors must be valid hex strings (`#RGB` or `#RRGGBB`).

### `Typography`
Text size configuration in sp (scale-independent pixels):
- Display sizes: `displayLargeSp`, `displayMediumSp`, `displaySmallSp`
- Headline sizes: `headlineLargeSp`, `headlineMediumSp`, `headlineSmallSp`
- Title sizes: `titleLargeSp`, `titleMediumSp`, `titleSmallSp`
- Body sizes: `bodyLargeSp`, `bodyMediumSp`, `bodySmallSp`
- Label sizes: `labelLargeSp`, `labelMediumSp`, `labelSmallSp`
- Font family: `fontFamily`

### `Spacing`
Dimension configuration in dp (density-independent pixels):
- Spacing: `tinyDp`, `smallDp`, `mediumDp`, `largeDp`, `xlargeDp`, `xxlargeDp`
- Elevation: `cardElevationDp`, `cardElevationRaisedDp`
- Corner radius: `cornerRadiusSmallDp`, `cornerRadiusMediumDp`, `cornerRadiusLargeDp`
- Icon sizes: `iconSmallDp`, `iconMediumDp`, `iconLargeDp`

## Usage

### Load a theme

```python
from src.theme_config import load_theme

theme = load_theme("config/theme.android.json")
print(f"Primary color: {theme.colors.primary}")
print(f"Headline size: {theme.typography.headlineLargeSp}sp")
```

### Modify and save a theme

```python
from src.theme_config import load_theme, save_theme

# Load current theme
theme = load_theme("config/theme.android.json")

# Modify colors
theme.colors.primary = "#2196F3"  # Blue instead of green

# Save changes
save_theme(theme, "config/theme.android.json")
```

### Create a new theme programmatically

```python
from src.theme_config import ThemeConfig, ColorPalette, Typography, Spacing, save_theme

theme = ThemeConfig(
    name="Custom Theme",
    version="1.0",
    colors=ColorPalette(
        primary="#E91E63",  # Pink
        secondary="#9C27B0"  # Purple
    ),
    typography=Typography(
        headlineLargeSp=36,  # Larger headlines
        bodyLargeSp=18       # Larger body text
    ),
    spacing=Spacing(
        mediumDp=20  # More spacing
    )
)

save_theme(theme, "config/theme.custom.json")
```

### Validation

The models automatically validate:

```python
from src.theme_config.models import ColorPalette
from pydantic import ValidationError

try:
    # This will raise ValidationError
    palette = ColorPalette(
        primary="not-a-hex-color"  # Invalid!
    )
except ValidationError as e:
    print(f"Validation errors: {e}")
```

### Streamlit Integration

See `streamlit_app/pages_functions/theme_editor_example.py` for a complete example of building a theme editor UI.

```python
from src.theme_config import load_theme, save_theme
import streamlit as st

# Load theme
theme = load_theme("config/theme.android.json")

# Edit with Streamlit widgets
primary = st.color_picker("Primary Color", value=theme.colors.primary)
theme.colors.primary = primary

# Save changes
if st.button("Save"):
    save_theme(theme, "config/theme.android.json")
```

## Validation Script

Test the module with:

```bash
PYTHONPATH=. python scripts/validate_theme.py
```

This runs 5 tests:
1. Load valid theme.json
2. Validate invalid color format rejection
3. Validate missing field rejection
4. Validate invalid hex length rejection
5. Test save and reload cycle

## Default Theme

The default theme (`config/theme.android.json`) matches the current Android app colors:
- **Primary**: Green (#4CAF50)
- **Secondary**: Orange (#FF9800)
- **Tertiary**: Red (#F44336)
- **Background**: Off-white (#FDFCF9)

All Material Design 3 color slots are defined with proper contrast ratios.

## Future: Android XML Generation

The models are designed to support automatic generation of Android XML resources:

```python
from src.theme_config import load_theme

theme = load_theme("config/theme.android.json")

# Generate colors.xml
colors_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="md_theme_light_primary">{theme.colors.primary}</color>
    <color name="md_theme_light_onPrimary">{theme.colors.onPrimary}</color>
    <!-- ... more colors ... -->
</resources>
"""

# Generate dimens.xml
dimens_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="spacing_medium">{theme.spacing.mediumDp}dp</dimen>
    <!-- ... more dimensions ... -->
</resources>
"""
```

A full generator script can be implemented to automate this process.

## Benefits

1. **Type Safety**: Pydantic validates all data at runtime
2. **Single Source**: One JSON file for entire theme
3. **Human Friendly**: JSON is easy to read and edit
4. **UI Editable**: Streamlit provides visual editing
5. **Version Control**: JSON files work well with git
6. **Extensible**: Easy to add new theme properties

## Python Version

Requires Python 3.12+ and Pydantic v2 (>=2.5.0).
