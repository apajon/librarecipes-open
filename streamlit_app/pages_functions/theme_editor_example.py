"""
Example Streamlit page for editing Android theme configuration.

This demonstrates how to use the theme_config module to load, edit, and save
theme configurations through a Streamlit UI.

To use this page:
1. Copy this file to streamlit_app/pages_functions/
2. Add it to the navigation in config/pages.py
3. Run the Streamlit app and navigate to this page
"""

import streamlit as st
from pathlib import Path
from src.theme_config import load_theme, save_theme, ThemeConfig

# Path to the theme configuration file
THEME_PATH = Path("config/theme.android.json")


def theme_editor_page():
    """
    Streamlit page function for editing theme configuration.

    This can be integrated into the main app by:
    - Adding to config/pages.py as a new page
    - Calling this function from a page file in streamlit_app/pages/
    """
    st.title("🎨 Android Theme Editor")
    st.markdown(
        "Edit the Android app theme configuration. "
        "Changes will be saved to `config/theme.android.json`."
    )

    # Load current theme
    try:
        theme = load_theme(THEME_PATH)
    except Exception as e:
        st.error(f"Error loading theme: {e}")
        st.info("Creating a new theme with default values...")
        from src.theme_config.models import ColorPalette, Typography, Spacing

        theme = ThemeConfig(
            name="LibraRecipes Default",
            version="1.0",
            colors=ColorPalette(),
            typography=Typography(),
            spacing=Spacing(),
        )

    # Store theme in session state if not already there
    if "theme_config" not in st.session_state:
        st.session_state.theme_config = theme

    # Theme metadata
    st.header("📋 Theme Information")
    col1, col2 = st.columns(2)
    with col1:
        theme_name = st.text_input(
            "Theme Name", value=st.session_state.theme_config.name
        )
    with col2:
        theme_version = st.text_input(
            "Version", value=st.session_state.theme_config.version
        )

    # Color palette editor
    st.header("🎨 Color Palette")
    st.markdown("Colors must be in hex format (#RGB or #RRGGBB)")

    # Primary colors
    with st.expander("Primary Colors", expanded=True):
        col1, col2 = st.columns(2)
        with col1:
            primary = st.color_picker(
                "Primary",
                value=st.session_state.theme_config.colors.primary,
                help="Main brand color for buttons, headers, etc.",
            )
            primary_container = st.color_picker(
                "Primary Container",
                value=st.session_state.theme_config.colors.primaryContainer,
            )
        with col2:
            on_primary = st.color_picker(
                "On Primary",
                value=st.session_state.theme_config.colors.onPrimary,
                help="Text/icon color on primary background",
            )
            on_primary_container = st.color_picker(
                "On Primary Container",
                value=st.session_state.theme_config.colors.onPrimaryContainer,
            )

    # Secondary colors
    with st.expander("Secondary Colors"):
        col1, col2 = st.columns(2)
        with col1:
            secondary = st.color_picker(
                "Secondary", value=st.session_state.theme_config.colors.secondary
            )
            secondary_container = st.color_picker(
                "Secondary Container",
                value=st.session_state.theme_config.colors.secondaryContainer,
            )
        with col2:
            on_secondary = st.color_picker(
                "On Secondary", value=st.session_state.theme_config.colors.onSecondary
            )
            on_secondary_container = st.color_picker(
                "On Secondary Container",
                value=st.session_state.theme_config.colors.onSecondaryContainer,
            )

    # Background and surface colors
    with st.expander("Background & Surface Colors"):
        col1, col2 = st.columns(2)
        with col1:
            background = st.color_picker(
                "Background", value=st.session_state.theme_config.colors.background
            )
            surface = st.color_picker(
                "Surface", value=st.session_state.theme_config.colors.surface
            )
        with col2:
            on_background = st.color_picker(
                "On Background", value=st.session_state.theme_config.colors.onBackground
            )
            on_surface = st.color_picker(
                "On Surface", value=st.session_state.theme_config.colors.onSurface
            )

    # Typography editor
    st.header("✍️ Typography")
    st.markdown("Text sizes in sp (scale-independent pixels)")

    col1, col2, col3 = st.columns(3)
    with col1:
        st.subheader("Display")
        display_large = st.number_input(
            "Display Large",
            min_value=1,
            value=st.session_state.theme_config.typography.displayLargeSp,
        )
        display_medium = st.number_input(
            "Display Medium",
            min_value=1,
            value=st.session_state.theme_config.typography.displayMediumSp,
        )
        display_small = st.number_input(
            "Display Small",
            min_value=1,
            value=st.session_state.theme_config.typography.displaySmallSp,
        )

    with col2:
        st.subheader("Headline")
        headline_large = st.number_input(
            "Headline Large",
            min_value=1,
            value=st.session_state.theme_config.typography.headlineLargeSp,
        )
        headline_medium = st.number_input(
            "Headline Medium",
            min_value=1,
            value=st.session_state.theme_config.typography.headlineMediumSp,
        )
        headline_small = st.number_input(
            "Headline Small",
            min_value=1,
            value=st.session_state.theme_config.typography.headlineSmallSp,
        )

    with col3:
        st.subheader("Body")
        body_large = st.number_input(
            "Body Large",
            min_value=1,
            value=st.session_state.theme_config.typography.bodyLargeSp,
        )
        body_medium = st.number_input(
            "Body Medium",
            min_value=1,
            value=st.session_state.theme_config.typography.bodyMediumSp,
        )
        body_small = st.number_input(
            "Body Small",
            min_value=1,
            value=st.session_state.theme_config.typography.bodySmallSp,
        )

    # Spacing editor
    st.header("📏 Spacing & Dimensions")
    st.markdown("Dimensions in dp (density-independent pixels)")

    col1, col2 = st.columns(2)
    with col1:
        st.subheader("Spacing")
        tiny = st.number_input(
            "Tiny", min_value=0, value=st.session_state.theme_config.spacing.tinyDp
        )
        small = st.number_input(
            "Small", min_value=0, value=st.session_state.theme_config.spacing.smallDp
        )
        medium = st.number_input(
            "Medium", min_value=0, value=st.session_state.theme_config.spacing.mediumDp
        )
        large = st.number_input(
            "Large", min_value=0, value=st.session_state.theme_config.spacing.largeDp
        )

    with col2:
        st.subheader("Corner Radius")
        corner_small = st.number_input(
            "Small Radius",
            min_value=0,
            value=st.session_state.theme_config.spacing.cornerRadiusSmallDp,
        )
        corner_medium = st.number_input(
            "Medium Radius",
            min_value=0,
            value=st.session_state.theme_config.spacing.cornerRadiusMediumDp,
        )
        corner_large = st.number_input(
            "Large Radius",
            min_value=0,
            value=st.session_state.theme_config.spacing.cornerRadiusLargeDp,
        )

    # Save button
    st.markdown("---")
    col1, col2, col3 = st.columns([1, 1, 2])

    with col1:
        if st.button("💾 Save Theme", type="primary", use_container_width=True):
            try:
                # Update theme config with new values
                from src.theme_config.models import ColorPalette, Typography, Spacing

                updated_theme = ThemeConfig(
                    name=theme_name,
                    version=theme_version,
                    colors=ColorPalette(
                        primary=primary,
                        onPrimary=on_primary,
                        primaryContainer=primary_container,
                        onPrimaryContainer=on_primary_container,
                        secondary=secondary,
                        onSecondary=on_secondary,
                        secondaryContainer=secondary_container,
                        onSecondaryContainer=on_secondary_container,
                        background=background,
                        onBackground=on_background,
                        surface=surface,
                        onSurface=on_surface,
                        # Use existing values for fields not in UI
                        tertiary=st.session_state.theme_config.colors.tertiary,
                        onTertiary=st.session_state.theme_config.colors.onTertiary,
                        tertiaryContainer=st.session_state.theme_config.colors.tertiaryContainer,
                        onTertiaryContainer=st.session_state.theme_config.colors.onTertiaryContainer,
                        error=st.session_state.theme_config.colors.error,
                        onError=st.session_state.theme_config.colors.onError,
                        errorContainer=st.session_state.theme_config.colors.errorContainer,
                        onErrorContainer=st.session_state.theme_config.colors.onErrorContainer,
                        surfaceVariant=st.session_state.theme_config.colors.surfaceVariant,
                        onSurfaceVariant=st.session_state.theme_config.colors.onSurfaceVariant,
                        outline=st.session_state.theme_config.colors.outline,
                    ),
                    typography=Typography(
                        fontFamily=st.session_state.theme_config.typography.fontFamily,
                        displayLargeSp=display_large,
                        displayMediumSp=display_medium,
                        displaySmallSp=display_small,
                        headlineLargeSp=headline_large,
                        headlineMediumSp=headline_medium,
                        headlineSmallSp=headline_small,
                        bodyLargeSp=body_large,
                        bodyMediumSp=body_medium,
                        bodySmallSp=body_small,
                        # Use existing values for fields not in UI
                        titleLargeSp=st.session_state.theme_config.typography.titleLargeSp,
                        titleMediumSp=st.session_state.theme_config.typography.titleMediumSp,
                        titleSmallSp=st.session_state.theme_config.typography.titleSmallSp,
                        labelLargeSp=st.session_state.theme_config.typography.labelLargeSp,
                        labelMediumSp=st.session_state.theme_config.typography.labelMediumSp,
                        labelSmallSp=st.session_state.theme_config.typography.labelSmallSp,
                    ),
                    spacing=Spacing(
                        tinyDp=tiny,
                        smallDp=small,
                        mediumDp=medium,
                        largeDp=large,
                        cornerRadiusSmallDp=corner_small,
                        cornerRadiusMediumDp=corner_medium,
                        cornerRadiusLargeDp=corner_large,
                        # Use existing values for fields not in UI
                        xlargeDp=st.session_state.theme_config.spacing.xlargeDp,
                        xxlargeDp=st.session_state.theme_config.spacing.xxlargeDp,
                        cardElevationDp=st.session_state.theme_config.spacing.cardElevationDp,
                        cardElevationRaisedDp=st.session_state.theme_config.spacing.cardElevationRaisedDp,
                        iconSmallDp=st.session_state.theme_config.spacing.iconSmallDp,
                        iconMediumDp=st.session_state.theme_config.spacing.iconMediumDp,
                        iconLargeDp=st.session_state.theme_config.spacing.iconLargeDp,
                    ),
                )

                # Save to file
                save_theme(updated_theme, THEME_PATH)
                st.session_state.theme_config = updated_theme
                st.success(f"✅ Theme saved to {THEME_PATH}")

            except Exception as e:
                st.error(f"❌ Error saving theme: {e}")

    with col2:
        if st.button("🔄 Reset to Default", use_container_width=True):
            try:
                theme = load_theme(THEME_PATH)
                st.session_state.theme_config = theme
                st.rerun()
            except Exception as e:
                st.error(f"Error loading theme: {e}")

    # Info box
    st.info(
        "💡 **Next Steps:**\n\n"
        "1. Edit colors, typography, or spacing values above\n"
        "2. Click 'Save Theme' to persist changes\n"
        "3. Use a generator script to create Android XML resources from this config\n"
        "4. Rebuild the Android app to see your changes"
    )


# Example usage comment for integration
"""
To integrate this into your Streamlit app:

1. In config/pages.py, add:
   ```python
   from streamlit_app.pages_functions.theme_editor import theme_editor_page
   
   # In get_pages_config():
   st.Page(
       theme_editor_page,
       title="Theme Editor",
       icon="🎨"
   )
   ```

2. Or create a simple page file in streamlit_app/pages/:
   ```python
   # streamlit_app/pages/theme_editor.py
   from streamlit_app.pages_functions.theme_editor_example import theme_editor_page
   
   theme_editor_page()
   ```
"""
