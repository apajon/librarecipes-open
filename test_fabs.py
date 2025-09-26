#!/usr/bin/env python3
"""
Test script for FAB Manager validation
"""

import sys
from pathlib import Path

# Add mobile app to Python path
mobile_app_dir = Path(__file__).parent / "mobile_app"
sys.path.insert(0, str(mobile_app_dir))
sys.path.insert(0, str(Path(__file__).parent))

try:
    print("Testing imports...")
    
    # Test components import
    from components.fab_manager import FABManager, TooltipIconButton
    print("✅ FABManager imported successfully")
    
    # Test utils import
    from utils.photo_manager import PhotoManager
    print("✅ PhotoManager imported successfully")
    
    # Test screens import
    from screens.home_screen import HomeScreen
    from screens.add_recipe_screen import AddRecipeScreen
    from screens.recipe_list_screen import RecipeListScreen  
    from screens.search_screen import SearchScreen
    print("✅ All screens imported successfully")
    
    print("\n🎉 All imports successful! FAB implementation ready for testing.")
    
except ImportError as e:
    print(f"❌ Import error: {e}")
    import traceback
    traceback.print_exc()
except Exception as e:
    print(f"❌ General error: {e}")
    import traceback
    traceback.print_exc()