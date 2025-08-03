#!/usr/bin/env python3
"""
Run script for LibraRecipes Mobile App
Handles both desktop development and Android deployment
"""

import sys
import os
from pathlib import Path

# Add mobile app to Python path
mobile_app_dir = Path(__file__).parent / "mobile_app"
sys.path.insert(0, str(mobile_app_dir))

# Set environment variables
os.environ.setdefault('KIVY_METRICS_DENSITY', '1')
os.environ.setdefault('KIVY_METRICS_FONTSCALE', '1')

# Run the app
if __name__ == '__main__':
    try:
        from main import LibraRecipesApp
        LibraRecipesApp().run()
    except KeyboardInterrupt:
        print("\nApp stopped by user")
    except Exception as e:
        print(f"Error running app: {e}")
        import traceback
        traceback.print_exc()