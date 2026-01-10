"""
Android Theme Editor for LibraRecipes
Streamlit page for editing Android theme configuration with live preview
"""

import streamlit as st
from pathlib import Path
import json
from typing import Optional
from pydantic import ValidationError

# Import theme configuration modules
from src.theme_config import load_theme, save_theme, ThemeConfig
from src.theme_config.models import ColorPalette, Typography, Spacing

# Try to import streamlit-extras for toasts
try:
    from streamlit_extras.stylable_container import stylable_container

    HAS_EXTRAS = True
except ImportError:
    HAS_EXTRAS = False

# Configuration
CONFIG_DIR = Path("config")


def show_toast(message: str, icon: str = "✅"):
    """Show a toast-like message using streamlit"""
    st.toast(f"{icon} {message}")


def load_theme_files():
    """Get list of available theme JSON files"""
    if not CONFIG_DIR.exists():
        return []
    return sorted(CONFIG_DIR.glob("*.json"))


def initialize_session_state():
    """Initialize session state variables"""
    if "theme_cfg" not in st.session_state:
        st.session_state.theme_cfg = None
    if "current_file_path" not in st.session_state:
        st.session_state.current_file_path = None
    if "current_file_label" not in st.session_state:
        st.session_state.current_file_label = None
    if "uploaded_file_data" not in st.session_state:
        st.session_state.uploaded_file_data = None


def load_theme_from_source(
    source_type: str,
    file_path: Optional[Path] = None,
    uploaded_bytes: Optional[bytes] = None,
):
    """Load theme from file or uploaded data"""
    try:
        if source_type == "local" and file_path:
            theme = load_theme(file_path)
            st.session_state.theme_cfg = theme
            st.session_state.current_file_path = file_path
            st.session_state.current_file_label = file_path.name
            show_toast(f"Theme loaded: {file_path.name}")
            return True
        elif source_type == "upload" and uploaded_bytes:
            data = json.loads(uploaded_bytes.decode("utf-8"))
            theme = ThemeConfig.model_validate(data)
            st.session_state.theme_cfg = theme
            st.session_state.current_file_path = None
            st.session_state.current_file_label = "Uploaded file"
            st.session_state.uploaded_file_data = uploaded_bytes
            show_toast("Theme loaded from upload")
            return True
    except ValidationError as e:
        st.error(f"❌ Validation Error: {e}")
        show_toast("Failed to load theme", "❌")
        return False
    except json.JSONDecodeError as e:
        st.error(f"❌ Invalid JSON: {e}")
        show_toast("Invalid JSON file", "❌")
        return False
    except Exception as e:
        st.error(f"❌ Error loading theme: {e}")
        show_toast("Error loading theme", "❌")
        return False
    return False


def render_color_editor(theme_cfg: ThemeConfig):
    """Render color editing controls"""
    st.subheader("🎨 Color Palette")

    col1, col2 = st.columns(2)

    with col1:
        st.markdown("**Primary Colors**")
        primary = st.color_picker(
            "Primary",
            value=theme_cfg.colors.primary,
            help="Main brand color",
            key="color_primary",
        )
        theme_cfg.colors.primary = primary.upper()

        on_primary = st.color_picker(
            "On Primary",
            value=theme_cfg.colors.onPrimary,
            help="Text/icons on primary",
            key="color_on_primary",
        )
        theme_cfg.colors.onPrimary = on_primary.upper()

        primary_container = st.color_picker(
            "Primary Container",
            value=theme_cfg.colors.primaryContainer,
            key="color_primary_container",
        )
        theme_cfg.colors.primaryContainer = primary_container.upper()

        st.markdown("**Secondary Colors**")
        secondary = st.color_picker(
            "Secondary",
            value=theme_cfg.colors.secondary,
            help="Accent color",
            key="color_secondary",
        )
        theme_cfg.colors.secondary = secondary.upper()

        on_secondary = st.color_picker(
            "On Secondary", value=theme_cfg.colors.onSecondary, key="color_on_secondary"
        )
        theme_cfg.colors.onSecondary = on_secondary.upper()

    with col2:
        st.markdown("**Background & Surface**")
        background = st.color_picker(
            "Background",
            value=theme_cfg.colors.background,
            help="Screen background",
            key="color_background",
        )
        theme_cfg.colors.background = background.upper()

        on_background = st.color_picker(
            "On Background",
            value=theme_cfg.colors.onBackground,
            key="color_on_background",
        )
        theme_cfg.colors.onBackground = on_background.upper()

        surface = st.color_picker(
            "Surface",
            value=theme_cfg.colors.surface,
            help="Component surface",
            key="color_surface",
        )
        theme_cfg.colors.surface = surface.upper()

        on_surface = st.color_picker(
            "On Surface", value=theme_cfg.colors.onSurface, key="color_on_surface"
        )
        theme_cfg.colors.onSurface = on_surface.upper()

        st.markdown("**Error Colors**")
        error = st.color_picker(
            "Error", value=theme_cfg.colors.error, key="color_error"
        )
        theme_cfg.colors.error = error.upper()

        on_error = st.color_picker(
            "On Error", value=theme_cfg.colors.onError, key="color_on_error"
        )
        theme_cfg.colors.onError = on_error.upper()


def render_typography_editor(theme_cfg: ThemeConfig):
    """Render typography editing controls"""
    st.subheader("✍️ Typography")

    col1, col2, col3 = st.columns(3)

    with col1:
        st.markdown("**Display**")
        display_large = st.number_input(
            "Display Large (sp)",
            min_value=1,
            value=theme_cfg.typography.displayLargeSp,
            step=1,
            key="typo_display_large",
        )
        theme_cfg.typography.displayLargeSp = int(display_large)

        display_medium = st.number_input(
            "Display Medium (sp)",
            min_value=1,
            value=theme_cfg.typography.displayMediumSp,
            step=1,
            key="typo_display_medium",
        )
        theme_cfg.typography.displayMediumSp = int(display_medium)

    with col2:
        st.markdown("**Headline**")
        headline_large = st.number_input(
            "Headline Large (sp)",
            min_value=1,
            value=theme_cfg.typography.headlineLargeSp,
            step=1,
            key="typo_headline_large",
        )
        theme_cfg.typography.headlineLargeSp = int(headline_large)

        headline_medium = st.number_input(
            "Headline Medium (sp)",
            min_value=1,
            value=theme_cfg.typography.headlineMediumSp,
            step=1,
            key="typo_headline_medium",
        )
        theme_cfg.typography.headlineMediumSp = int(headline_medium)

    with col3:
        st.markdown("**Body**")
        body_large = st.number_input(
            "Body Large (sp)",
            min_value=1,
            value=theme_cfg.typography.bodyLargeSp,
            step=1,
            key="typo_body_large",
        )
        theme_cfg.typography.bodyLargeSp = int(body_large)

        body_medium = st.number_input(
            "Body Medium (sp)",
            min_value=1,
            value=theme_cfg.typography.bodyMediumSp,
            step=1,
            key="typo_body_medium",
        )
        theme_cfg.typography.bodyMediumSp = int(body_medium)


def render_spacing_editor(theme_cfg: ThemeConfig):
    """Render spacing editing controls"""
    st.subheader("📏 Spacing & Dimensions")

    col1, col2, col3 = st.columns(3)

    with col1:
        st.markdown("**Spacing**")
        small = st.number_input(
            "Small (dp)",
            min_value=0,
            value=theme_cfg.spacing.smallDp,
            step=1,
            key="spacing_small",
        )
        theme_cfg.spacing.smallDp = int(small)

        medium = st.number_input(
            "Medium (dp)",
            min_value=0,
            value=theme_cfg.spacing.mediumDp,
            step=1,
            key="spacing_medium",
        )
        theme_cfg.spacing.mediumDp = int(medium)

        large = st.number_input(
            "Large (dp)",
            min_value=0,
            value=theme_cfg.spacing.largeDp,
            step=1,
            key="spacing_large",
        )
        theme_cfg.spacing.largeDp = int(large)

    with col2:
        st.markdown("**Corner Radius**")
        corner_small = st.number_input(
            "Small Radius (dp)",
            min_value=0,
            value=theme_cfg.spacing.cornerRadiusSmallDp,
            step=1,
            key="corner_small",
        )
        theme_cfg.spacing.cornerRadiusSmallDp = int(corner_small)

        corner_medium = st.number_input(
            "Medium Radius (dp)",
            min_value=0,
            value=theme_cfg.spacing.cornerRadiusMediumDp,
            step=1,
            key="corner_medium",
        )
        theme_cfg.spacing.cornerRadiusMediumDp = int(corner_medium)

    with col3:
        st.markdown("**Icons**")
        icon_small = st.number_input(
            "Small Icon (dp)",
            min_value=0,
            value=theme_cfg.spacing.iconSmallDp,
            step=1,
            key="icon_small",
        )
        theme_cfg.spacing.iconSmallDp = int(icon_small)

        icon_medium = st.number_input(
            "Medium Icon (dp)",
            min_value=0,
            value=theme_cfg.spacing.iconMediumDp,
            step=1,
            key="icon_medium",
        )
        theme_cfg.spacing.iconMediumDp = int(icon_medium)


def render_preview(theme_cfg: ThemeConfig):
    """Render live theme preview"""
    st.subheader("👁️ Live Preview")

    # Create preview using HTML/CSS
    preview_html = f"""
    <style>
        .preview-container {{
            font-family: sans-serif;
            max-width: 100%;
            margin: 0 auto;
        }}
        .preview-appbar {{
            background-color: {theme_cfg.colors.primary};
            color: {theme_cfg.colors.onPrimary};
            padding: {theme_cfg.spacing.mediumDp}px;
            border-radius: {theme_cfg.spacing.cornerRadiusMediumDp}px {theme_cfg.spacing.cornerRadiusMediumDp}px 0 0;
            font-size: {theme_cfg.typography.headlineMediumSp}px;
            font-weight: bold;
        }}
        .preview-content {{
            background-color: {theme_cfg.colors.surface};
            color: {theme_cfg.colors.onSurface};
            padding: {theme_cfg.spacing.mediumDp}px;
        }}
        .preview-headline {{
            font-size: {theme_cfg.typography.headlineLargeSp}px;
            font-weight: bold;
            margin-bottom: {theme_cfg.spacing.smallDp}px;
        }}
        .preview-body {{
            font-size: {theme_cfg.typography.bodyLargeSp}px;
            margin-bottom: {theme_cfg.spacing.mediumDp}px;
        }}
        .preview-button {{
            background-color: {theme_cfg.colors.primary};
            color: {theme_cfg.colors.onPrimary};
            padding: {theme_cfg.spacing.smallDp}px {theme_cfg.spacing.mediumDp}px;
            border-radius: {theme_cfg.spacing.cornerRadiusLargeDp}px;
            border: none;
            display: inline-block;
            margin-right: {theme_cfg.spacing.smallDp}px;
            font-size: {theme_cfg.typography.bodyMediumSp}px;
            cursor: pointer;
        }}
        .preview-button-secondary {{
            background-color: {theme_cfg.colors.secondary};
            color: {theme_cfg.colors.onSecondary};
            padding: {theme_cfg.spacing.smallDp}px {theme_cfg.spacing.mediumDp}px;
            border-radius: {theme_cfg.spacing.cornerRadiusLargeDp}px;
            border: none;
            display: inline-block;
            font-size: {theme_cfg.typography.bodyMediumSp}px;
            cursor: pointer;
        }}
        .preview-card {{
            background-color: {theme_cfg.colors.background};
            padding: {theme_cfg.spacing.largeDp}px;
            border-radius: {theme_cfg.spacing.cornerRadiusMediumDp}px;
            margin-top: {theme_cfg.spacing.mediumDp}px;
        }}
    </style>

    <div class="preview-container">
        <div class="preview-appbar">
            LibraRecipes
        </div>
        <div class="preview-content">
            <div class="preview-headline">Welcome to Your App</div>
            <div class="preview-body">
                This is a preview of your theme. Text appears in the body style using the surface and onSurface colors.
            </div>
            <button class="preview-button">Primary Button</button>
            <button class="preview-button-secondary">Secondary Button</button>

            <div class="preview-card">
                <strong>Card Component</strong><br>
                Background color with rounded corners
            </div>
        </div>
    </div>
    """

    st.markdown(preview_html, unsafe_allow_html=True)


def save_current_file(theme_cfg: ThemeConfig, file_path: Path):
    """Save theme to current file"""
    try:
        save_theme(theme_cfg, file_path)
        show_toast(f"Saved to {file_path.name}")
        return True
    except Exception as e:
        st.error(f"❌ Error saving: {e}")
        show_toast("Save failed", "❌")
        return False


def save_as_new_file(theme_cfg: ThemeConfig, filename: str):
    """Save theme as a new file"""
    try:
        if not filename.endswith(".json"):
            filename += ".json"

        new_path = CONFIG_DIR / filename

        if new_path.exists():
            st.warning(f"⚠️ File {filename} already exists. It will be overwritten.")

        save_theme(theme_cfg, new_path)
        st.session_state.current_file_path = new_path
        st.session_state.current_file_label = filename
        show_toast(f"Saved as {filename}")
        return True
    except Exception as e:
        st.error(f"❌ Error saving: {e}")
        show_toast("Save failed", "❌")
        return False


def android_theme_editor_page():
    """Main Android Theme Editor page"""

    st.title("🎨 Android Theme Editor")
    st.markdown("Edit your Android app theme configuration with live preview")

    # Initialize session state
    initialize_session_state()

    # Sidebar for file selection
    with st.sidebar:
        st.header("📁 Theme Source")

        # Get available theme files
        theme_files = load_theme_files()

        # File selection mode
        source_mode = st.radio(
            "Select source:", ["Local File", "Upload File"], key="source_mode"
        )

        if source_mode == "Local File":
            if theme_files:
                file_options = {str(f.name): f for f in theme_files}
                selected_file_name = st.selectbox(
                    "Choose a theme file:",
                    options=list(file_options.keys()),
                    key="file_selector",
                )

                if st.button("Load Selected File", use_container_width=True):
                    selected_path = file_options[selected_file_name]
                    load_theme_from_source("local", file_path=selected_path)
            else:
                st.info("No theme files found in config directory")

        else:  # Upload mode
            uploaded_file = st.file_uploader(
                "Upload theme JSON:", type=["json"], key="file_uploader"
            )

            if uploaded_file is not None:
                if st.button("Load Uploaded File", use_container_width=True):
                    load_theme_from_source(
                        "upload", uploaded_bytes=uploaded_file.read()
                    )

        # Display current file info
        if st.session_state.current_file_label:
            st.success(f"📄 Current: {st.session_state.current_file_label}")

    # Main content
    if st.session_state.theme_cfg is None:
        st.info("👈 Please select or upload a theme file to begin editing")
        st.markdown(
            """
        ### Getting Started

        1. Select a theme file from the sidebar or upload your own
        2. Click "Load" to load the theme into the editor
        3. Edit colors, typography, and spacing
        4. See live preview of your changes
        5. Save your changes
        """
        )
        return

    theme_cfg = st.session_state.theme_cfg

    # Create tabs for different sections
    tab1, tab2, tab3, tab4 = st.tabs(
        ["🎨 Colors", "✍️ Typography", "📏 Spacing", "👁️ Preview"]
    )

    with tab1:
        render_color_editor(theme_cfg)

    with tab2:
        render_typography_editor(theme_cfg)

    with tab3:
        render_spacing_editor(theme_cfg)

    with tab4:
        render_preview(theme_cfg)

    # Action buttons
    st.markdown("---")
    st.subheader("💾 Save Changes")

    col1, col2, col3 = st.columns(3)

    with col1:
        if st.button("💾 Save Current File", use_container_width=True, type="primary"):
            if st.session_state.current_file_path:
                save_current_file(theme_cfg, st.session_state.current_file_path)
            else:
                st.warning("⚠️ No local file loaded. Use 'Save as New File' instead.")

    with col2:
        with st.expander("💾 Save as New File"):
            new_filename = st.text_input(
                "New filename:", value="theme.custom.json", key="new_filename"
            )
            if st.button("Save As", use_container_width=True):
                if new_filename:
                    save_as_new_file(theme_cfg, new_filename)
                else:
                    st.error("Please enter a filename")

    with col3:
        if st.button("🔄 Reload from File", use_container_width=True):
            if st.session_state.current_file_path:
                load_theme_from_source(
                    "local", file_path=st.session_state.current_file_path
                )
            elif st.session_state.uploaded_file_data:
                load_theme_from_source(
                    "upload", uploaded_bytes=st.session_state.uploaded_file_data
                )
            else:
                st.warning("No file to reload from")

    # Theme info
    with st.expander("ℹ️ Theme Information"):
        st.write(f"**Name:** {theme_cfg.name}")
        st.write(f"**Version:** {theme_cfg.version}")
        if st.session_state.current_file_path:
            st.write(f"**File:** {st.session_state.current_file_path}")
