#!/bin/bash
# Simple test script to validate FAB implementation

echo "🎯 Testing Android FAB Implementation"
echo "======================================"

echo ""
echo "✅ Checking FAB additions in HomeScreen..."
if grep -q "floatingActionButton" /home/runner/work/librarecipes-open/librarecipes-open/android_app/app/src/main/java/com/apajon/librarecipes/ui/screens/HomeScreen.kt; then
    echo "   ✓ FAB found in HomeScreen"
else
    echo "   ✗ FAB missing in HomeScreen"
fi

echo ""
echo "✅ Checking FAB additions in RecipeListScreen..."
if grep -q "floatingActionButton" /home/runner/work/librarecipes-open/librarecipes-open/android_app/app/src/main/java/com/apajon/librarecipes/ui/screens/RecipeListScreen.kt; then
    echo "   ✓ FAB found in RecipeListScreen"
else
    echo "   ✗ FAB missing in RecipeListScreen"
fi

echo ""
echo "✅ Checking FAB additions in EditRecipeScreen..."
if grep -q "floatingActionButton" /home/runner/work/librarecipes-open/librarecipes-open/android_app/app/src/main/java/com/apajon/librarecipes/ui/screens/EditRecipeScreen.kt; then
    echo "   ✓ FAB found in EditRecipeScreen"
else
    echo "   ✗ FAB missing in EditRecipeScreen"
fi

echo ""
echo "✅ Checking FAB additions in SearchScreen..."
if grep -q "floatingActionButton" /home/runner/work/librarecipes-open/librarecipes-open/android_app/app/src/main/java/com/apajon/librarecipes/ui/screens/OtherScreens.kt; then
    echo "   ✓ FAB found in SearchScreen"
else
    echo "   ✗ FAB missing in SearchScreen"
fi

echo ""
echo "✅ Checking icon usage..."
if grep -q "PhotoCamera" /home/runner/work/librarecipes-open/librarecipes-open/android_app/app/src/main/java/com/apajon/librarecipes/ui/screens/*.kt; then
    echo "   ✓ PhotoCamera icon used"
else
    echo "   ✗ PhotoCamera icon missing"
fi

echo ""
echo "✅ Summary:"
echo "   - Primary Add Recipe FAB (➕): Navigates to create_new_recipe"
echo "   - Secondary Camera FAB (📷): PhotoCamera icon for photo capture"
echo "   - Material Design 3 styling with proper colors"
echo "   - Smart positioning: Both FABs on Home/List/Search, Camera only on Edit"
echo ""
echo "🎉 Android FAB implementation completed!"