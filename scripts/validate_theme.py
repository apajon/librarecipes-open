"""
Simple validation script for theme configuration module.

This script demonstrates:
1. Loading a valid theme.json file
2. Parsing and validating the configuration
3. Handling invalid JSON with clear error messages
4. Saving a theme back to disk

Run with: PYTHONPATH=. python scripts/validate_theme.py
"""

import sys
from pathlib import Path

# Add project root to path
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from src.theme_config import load_theme, save_theme, ThemeConfig
from pydantic import ValidationError
import json


def test_load_valid_theme():
    """Test loading a valid theme configuration."""
    print("=" * 60)
    print("Test 1: Load valid theme.json")
    print("=" * 60)

    theme_path = project_root / "config" / "theme.android.json"

    try:
        theme = load_theme(theme_path)
        print(f"✅ Successfully loaded theme: {theme.name} v{theme.version}")
        print(f"\nTheme Details:")
        print(f"  - Primary color: {theme.colors.primary}")
        print(f"  - Secondary color: {theme.colors.secondary}")
        print(f"  - Background color: {theme.colors.background}")
        print(f"  - Headline size: {theme.typography.headlineLargeSp}sp")
        print(f"  - Body size: {theme.typography.bodyLargeSp}sp")
        print(f"  - Medium spacing: {theme.spacing.mediumDp}dp")
        print(f"  - Card elevation: {theme.spacing.cardElevationDp}dp")
        return True
    except Exception as e:
        print(f"❌ Failed to load theme: {e}")
        return False


def test_invalid_color_format():
    """Test validation of invalid color formats."""
    print("\n" + "=" * 60)
    print("Test 2: Validate color format checking")
    print("=" * 60)

    invalid_data = {
        "name": "Invalid Theme",
        "version": "1.0",
        "colors": {"primary": "not-a-color", "onPrimary": "#FFFFFF"},  # Invalid format
        "typography": {"fontFamily": "sans-serif", "bodyLargeSp": 16},
        "spacing": {"mediumDp": 16},
    }

    try:
        # This should raise a ValidationError
        theme = ThemeConfig.model_validate(invalid_data)
        print(f"❌ Expected validation error but got theme: {theme.name}")
        return False
    except ValidationError as e:
        print(f"✅ Correctly caught validation error:")
        print(f"   {e.error_count()} error(s) found:")
        for error in e.errors():
            field = " -> ".join(str(loc) for loc in error["loc"])
            print(f"   - {field}: {error['msg']}")
        return True
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False


def test_missing_required_fields():
    """Test validation of missing required fields."""
    print("\n" + "=" * 60)
    print("Test 3: Validate missing required fields")
    print("=" * 60)

    invalid_data = {
        "name": "Incomplete Theme",
        "version": "1.0",
        # Missing colors, typography, and spacing
    }

    try:
        theme = ThemeConfig.model_validate(invalid_data)
        print(f"❌ Expected validation error but got theme: {theme.name}")
        return False
    except ValidationError as e:
        print(f"✅ Correctly caught validation error:")
        print(f"   {e.error_count()} error(s) found:")
        for error in e.errors():
            field = " -> ".join(str(loc) for loc in error["loc"])
            print(f"   - {field}: {error['msg']}")
        return True
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False


def test_invalid_hex_length():
    """Test validation of hex color with wrong length."""
    print("\n" + "=" * 60)
    print("Test 4: Validate hex color length")
    print("=" * 60)

    invalid_data = {
        "name": "Bad Color Length",
        "version": "1.0",
        "colors": {
            "primary": "#12",  # Wrong length (not 4 or 7)
            "onPrimary": "#FFFFFF",
        },
        "typography": {"fontFamily": "sans-serif", "bodyLargeSp": 16},
        "spacing": {"mediumDp": 16},
    }

    try:
        theme = ThemeConfig.model_validate(invalid_data)
        print(f"❌ Expected validation error but got theme: {theme.name}")
        return False
    except ValidationError as e:
        print(f"✅ Correctly caught validation error:")
        for error in e.errors():
            field = " -> ".join(str(loc) for loc in error["loc"])
            print(f"   - {field}: {error['msg']}")
        return True
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False


def test_save_and_reload():
    """Test saving a theme and reloading it."""
    print("\n" + "=" * 60)
    print("Test 5: Save and reload theme")
    print("=" * 60)

    temp_path = project_root / "config" / "theme.test.json"

    try:
        # Load original theme
        original_theme = load_theme(project_root / "config" / "theme.android.json")

        # Modify slightly
        original_theme.name = "Test Theme"
        original_theme.colors.primary = "#2196F3"  # Change to blue

        # Save to temp file
        save_theme(original_theme, temp_path)
        print(f"✅ Saved theme to {temp_path}")

        # Reload
        reloaded_theme = load_theme(temp_path)
        print(f"✅ Reloaded theme: {reloaded_theme.name}")

        # Verify changes were preserved
        if reloaded_theme.colors.primary == "#2196F3":
            print(
                f"✅ Primary color correctly preserved: {reloaded_theme.colors.primary}"
            )
        else:
            print(f"❌ Primary color not preserved: {reloaded_theme.colors.primary}")
            return False

        # Clean up
        temp_path.unlink()
        print(f"✅ Cleaned up test file")

        return True
    except Exception as e:
        print(f"❌ Error during save/reload test: {e}")
        if temp_path.exists():
            temp_path.unlink()
        return False


def main():
    """Run all validation tests."""
    print("\n🧪 LibraRecipes Theme Configuration Validation")
    print("=" * 60)

    results = []

    # Run all tests
    results.append(("Load valid theme", test_load_valid_theme()))
    results.append(("Invalid color format", test_invalid_color_format()))
    results.append(("Missing required fields", test_missing_required_fields()))
    results.append(("Invalid hex length", test_invalid_hex_length()))
    results.append(("Save and reload", test_save_and_reload()))

    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)

    passed = sum(1 for _, result in results if result)
    total = len(results)

    for name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status}: {name}")

    print(f"\nTotal: {passed}/{total} tests passed")

    if passed == total:
        print("🎉 All tests passed!")
        return 0
    else:
        print("❌ Some tests failed")
        return 1


if __name__ == "__main__":
    sys.exit(main())
