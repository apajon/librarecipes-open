# Android Theme Editor - Documentation

## Overview

The Android Theme Editor is a Streamlit-based UI for editing Android theme configuration files. It provides an intuitive interface for modifying colors, typography, and spacing with live preview.

## Location

**Page:** `streamlit_app/pages_functions/android_theme_editor.py`  
**Navigation:** Available under "⚙️ Configuration" → "Android Theme Editor"

## Features

### 1. Theme Source Selection

- **Local Files**: Select from existing JSON files in the `config/` directory
- **Upload**: Upload a custom theme JSON file from your computer

### 2. Live Editing

The editor provides controls for:

- **Colors** (Material3 color slots): Primary, Secondary, Background, Surface, Error, and all "On" colors
- **Typography** (text sizes in sp): Display, Headline, and Body sizes
- **Spacing** (dimensions in dp): Spacing values, Corner radius, Icon sizes

### 3. Live Preview

A real-time preview panel shows how your theme changes affect the UI with app bar, buttons, and card components.

### 4. Save Options

- **Save Current File**: Overwrites the currently loaded local file
- **Save as New File**: Creates a new JSON file in the config directory

## Usage

### Basic Workflow

1. Launch Streamlit and navigate to "Android Theme Editor"
2. Select an existing theme or upload a new one
3. Edit colors, typography, and spacing using the controls
4. Preview changes in real-time
5. Save using "Save Current File" or "Save as New File"

### Loading a Theme

**From Local File:**
1. Select "Local File" in the sidebar
2. Choose a theme from the dropdown
3. Click "Load Selected File"

**From Upload:**
1. Select "Upload File" in the sidebar
2. Upload your JSON file
3. Click "Load Uploaded File"

### Validation

The editor uses Pydantic models for validation:
- Invalid colors are rejected (must be #RGB or #RRGGBB)
- Invalid JSON structure shows clear error messages
- All saved files are valid `ThemeConfig` objects

## Integration with Android

After editing a theme, use an Android XML generator script to convert JSON to Android resources, then rebuild the Android app.

---

**Version:** 1.0  
**Last Updated:** November 2024
