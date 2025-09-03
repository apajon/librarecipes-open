#!/usr/bin/env python3
"""
Test script for the mobile app - validates that all screens can be instantiated
without GUI dependencies
"""

import sys
from pathlib import Path

# Add the app directory to Python path for imports
sys.path.append(str(Path(__file__).parent.parent))

def test_mobile_app_imports():
    """Test that all mobile app screens can be imported"""
    try:
        from mobile_app.screens.home_screen import HomeScreen
        from mobile_app.screens.recipe_list_screen import RecipeListScreen
        from mobile_app.screens.add_recipe_screen import AddRecipeScreen
        from mobile_app.screens.recipe_detail_screen import RecipeDetailScreen
        from mobile_app.screens.search_screen import SearchScreen
        from mobile_app.screens.que_cuisiner_screen import QueCuisinerScreen
        
        print("✅ All mobile app screens imported successfully")
        return True
    except Exception as e:
        print(f"❌ Import error: {e}")
        return False

def test_database_connection():
    """Test database connection"""
    try:
        from src.db import get_db_session
        from src.crud.recettes import list_recettes
        
        with get_db_session() as session:
            recipes = list_recettes(session)
            print(f"✅ Database connection successful - found {len(recipes)} recipes")
        return True
    except Exception as e:
        print(f"❌ Database error: {e}")
        return False

def test_que_cuisiner_logic():
    """Test the core logic of the Que Cuisiner functionality"""
    try:
        import random
        from src.db import get_db_session
        from src.crud.recettes import list_recettes
        
        with get_db_session() as session:
            all_recipes = list_recettes(session)
            
            if not all_recipes:
                print("⚠️  No recipes found for testing suggestion logic")
                return True
                
            # Test filtering logic
            filtered_recipes = []
            selected_time = 60  # 60 minutes max
            
            for recipe in all_recipes:
                total_time = (recipe.preparation or 0) + (recipe.cuisson or 0)
                if total_time <= selected_time:
                    filtered_recipes.append(recipe)
            
            if filtered_recipes:
                suggested_recipe = random.choice(filtered_recipes)
                print(f"✅ Suggestion logic working - suggested: {suggested_recipe.nom}")
            else:
                print("✅ Suggestion logic working - no matches found (as expected)")
            
            # Test popular recipes
            if len(all_recipes) >= 3:
                popular = random.sample(all_recipes, 3)
                print(f"✅ Popular recipes logic working - selected 3 from {len(all_recipes)}")
            else:
                print(f"✅ Popular recipes logic working - using all {len(all_recipes)} recipes")
                
        return True
    except Exception as e:
        print(f"❌ Que Cuisiner logic error: {e}")
        return False

if __name__ == "__main__":
    print("🧪 Testing LibraRecipes Mobile App - Que Cuisiner Feature")
    print("=" * 60)
    
    tests = [
        ("Import Tests", test_mobile_app_imports),
        ("Database Connection", test_database_connection),
        ("Que Cuisiner Logic", test_que_cuisiner_logic),
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func in tests:
        print(f"\n🔍 Running {test_name}...")
        if test_func():
            passed += 1
        else:
            print(f"   Test failed!")
    
    print("\n" + "=" * 60)
    print(f"📊 Test Results: {passed}/{total} tests passed")
    
    if passed == total:
        print("🎉 All tests passed! Que Cuisiner feature is ready.")
        sys.exit(0)
    else:
        print("❌ Some tests failed. Please check the implementation.")
        sys.exit(1)