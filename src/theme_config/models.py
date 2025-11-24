"""
Pydantic models for LibraRecipes theme configuration.

These models define the structure and validation rules for theme.json files.
"""

from typing import Annotated
from pydantic import BaseModel, Field, field_validator
import re


class ColorPalette(BaseModel):
    """Material Design 3 color palette for light theme."""
    
    # Primary colors
    primary: Annotated[str, Field(description="Primary brand color (hex)")] = "#4CAF50"
    onPrimary: Annotated[str, Field(description="Text/icons on primary (hex)")] = "#FFFFFF"
    primaryContainer: Annotated[str, Field(description="Primary container color (hex)")] = "#C8E6C9"
    onPrimaryContainer: Annotated[str, Field(description="Text/icons on primary container (hex)")] = "#1B5E20"
    
    # Secondary colors
    secondary: Annotated[str, Field(description="Secondary accent color (hex)")] = "#FF9800"
    onSecondary: Annotated[str, Field(description="Text/icons on secondary (hex)")] = "#FFFFFF"
    secondaryContainer: Annotated[str, Field(description="Secondary container color (hex)")] = "#FFE0B2"
    onSecondaryContainer: Annotated[str, Field(description="Text/icons on secondary container (hex)")] = "#E65100"
    
    # Tertiary colors
    tertiary: Annotated[str, Field(description="Tertiary accent color (hex)")] = "#F44336"
    onTertiary: Annotated[str, Field(description="Text/icons on tertiary (hex)")] = "#FFFFFF"
    tertiaryContainer: Annotated[str, Field(description="Tertiary container color (hex)")] = "#FFCDD2"
    onTertiaryContainer: Annotated[str, Field(description="Text/icons on tertiary container (hex)")] = "#B71C1C"
    
    # Error colors
    error: Annotated[str, Field(description="Error state color (hex)")] = "#BA1A1A"
    onError: Annotated[str, Field(description="Text/icons on error (hex)")] = "#FFFFFF"
    errorContainer: Annotated[str, Field(description="Error container color (hex)")] = "#FFDAD6"
    onErrorContainer: Annotated[str, Field(description="Text/icons on error container (hex)")] = "#410002"
    
    # Background and surface colors
    background: Annotated[str, Field(description="Screen background color (hex)")] = "#FDFCF9"
    onBackground: Annotated[str, Field(description="Text/icons on background (hex)")] = "#1B1C18"
    surface: Annotated[str, Field(description="Component surface color (hex)")] = "#FDFCF9"
    onSurface: Annotated[str, Field(description="Text/icons on surface (hex)")] = "#1B1C18"
    surfaceVariant: Annotated[str, Field(description="Surface variant color (hex)")] = "#DDE5DA"
    onSurfaceVariant: Annotated[str, Field(description="Text/icons on surface variant (hex)")] = "#424940"
    
    # Outline
    outline: Annotated[str, Field(description="Border and divider color (hex)")] = "#727970"
    
    @field_validator("*")
    @classmethod
    def validate_hex_color(cls, v: str) -> str:
        """Validate that color is a valid hex string."""
        if not isinstance(v, str):
            raise ValueError(f"Color must be a string, got {type(v)}")
        
        # Check if it starts with #
        if not v.startswith("#"):
            raise ValueError(f"Color must start with #, got '{v}'")
        
        # Check length (either #RGB or #RRGGBB)
        if len(v) not in (4, 7):
            raise ValueError(f"Color must be #RGB or #RRGGBB format, got '{v}' with length {len(v)}")
        
        # Check if hex characters
        if not re.match(r"^#[0-9A-Fa-f]+$", v):
            raise ValueError(f"Color must contain only hex characters, got '{v}'")
        
        # Normalize to uppercase
        return v.upper()


class Typography(BaseModel):
    """Typography configuration for the theme."""
    
    fontFamily: Annotated[str, Field(description="Font family name")] = "sans-serif"
    
    # Display text sizes (sp - scale-independent pixels)
    displayLargeSp: Annotated[int, Field(description="Display large text size in sp", ge=1)] = 57
    displayMediumSp: Annotated[int, Field(description="Display medium text size in sp", ge=1)] = 45
    displaySmallSp: Annotated[int, Field(description="Display small text size in sp", ge=1)] = 36
    
    # Headline text sizes
    headlineLargeSp: Annotated[int, Field(description="Headline large text size in sp", ge=1)] = 32
    headlineMediumSp: Annotated[int, Field(description="Headline medium text size in sp", ge=1)] = 28
    headlineSmallSp: Annotated[int, Field(description="Headline small text size in sp", ge=1)] = 24
    
    # Title text sizes
    titleLargeSp: Annotated[int, Field(description="Title large text size in sp", ge=1)] = 22
    titleMediumSp: Annotated[int, Field(description="Title medium text size in sp", ge=1)] = 16
    titleSmallSp: Annotated[int, Field(description="Title small text size in sp", ge=1)] = 14
    
    # Body text sizes
    bodyLargeSp: Annotated[int, Field(description="Body large text size in sp", ge=1)] = 16
    bodyMediumSp: Annotated[int, Field(description="Body medium text size in sp", ge=1)] = 14
    bodySmallSp: Annotated[int, Field(description="Body small text size in sp", ge=1)] = 12
    
    # Label text sizes
    labelLargeSp: Annotated[int, Field(description="Label large text size in sp", ge=1)] = 14
    labelMediumSp: Annotated[int, Field(description="Label medium text size in sp", ge=1)] = 12
    labelSmallSp: Annotated[int, Field(description="Label small text size in sp", ge=1)] = 11


class Spacing(BaseModel):
    """Spacing and dimension configuration for the theme."""
    
    # Spacing (dp - density-independent pixels)
    tinyDp: Annotated[int, Field(description="Tiny spacing in dp", ge=0)] = 4
    smallDp: Annotated[int, Field(description="Small spacing in dp", ge=0)] = 8
    mediumDp: Annotated[int, Field(description="Medium spacing in dp", ge=0)] = 16
    largeDp: Annotated[int, Field(description="Large spacing in dp", ge=0)] = 24
    xlargeDp: Annotated[int, Field(description="Extra large spacing in dp", ge=0)] = 32
    xxlargeDp: Annotated[int, Field(description="Extra extra large spacing in dp", ge=0)] = 48
    
    # Card elevation
    cardElevationDp: Annotated[int, Field(description="Card elevation in dp", ge=0)] = 4
    cardElevationRaisedDp: Annotated[int, Field(description="Raised card elevation in dp", ge=0)] = 8
    
    # Corner radius
    cornerRadiusSmallDp: Annotated[int, Field(description="Small corner radius in dp", ge=0)] = 4
    cornerRadiusMediumDp: Annotated[int, Field(description="Medium corner radius in dp", ge=0)] = 8
    cornerRadiusLargeDp: Annotated[int, Field(description="Large corner radius in dp", ge=0)] = 12
    
    # Icon sizes
    iconSmallDp: Annotated[int, Field(description="Small icon size in dp", ge=0)] = 20
    iconMediumDp: Annotated[int, Field(description="Medium icon size in dp", ge=0)] = 24
    iconLargeDp: Annotated[int, Field(description="Large icon size in dp", ge=0)] = 32


class ThemeConfig(BaseModel):
    """Root theme configuration model."""
    
    name: Annotated[str, Field(description="Theme name")] = "LibraRecipes Default"
    version: Annotated[str, Field(description="Theme version")] = "1.0"
    colors: Annotated[ColorPalette, Field(description="Color palette configuration")]
    typography: Annotated[Typography, Field(description="Typography configuration")]
    spacing: Annotated[Spacing, Field(description="Spacing configuration")]
    
    model_config = {
        "json_schema_extra": {
            "example": {
                "name": "LibraRecipes Default",
                "version": "1.0",
                "colors": {},
                "typography": {},
                "spacing": {}
            }
        }
    }
