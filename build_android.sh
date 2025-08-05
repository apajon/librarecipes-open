#!/bin/bash
# LibraRecipes Android Build Script
# This script builds the Android APK for LibraRecipes mobile app

echo "🍴 LibraRecipes Android Build Script"
echo "======================================"

# Check if buildozer is installed
if ! command -v buildozer &> /dev/null; then
    echo "❌ Buildozer not found. Installing..."
    pip install buildozer
fi

# Navigate to app directory
cd /app

echo "📱 Building LibraRecipes Android APK..."
echo "This process may take 10-30 minutes on first build."
echo ""

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf .buildozer/
rm -rf bin/

# Initialize buildozer (creates .buildozer directory)
echo "⚙️ Initializing build environment..."
buildozer init

# Build the APK in debug mode
echo "🔨 Building debug APK..."
buildozer android debug

# Check if build was successful
if [ -f "bin/librarecipes-*-debug.apk" ]; then
    echo "✅ BUILD SUCCESSFUL!"
    echo ""
    echo "📁 APK Location: $(ls bin/librarecipes-*-debug.apk)"
    echo "📱 File size: $(du -h bin/librarecipes-*-debug.apk | cut -f1)"
    echo ""
    echo "🚀 To install on device:"
    echo "   adb install bin/librarecipes-*-debug.apk"
    echo ""
    echo "📤 Or use:"
    echo "   buildozer android deploy"
    echo ""
else
    echo "❌ Build failed. Check logs above for errors."
    echo "💡 Common issues:"
    echo "   - Missing Android SDK/NDK"
    echo "   - Insufficient disk space"
    echo "   - Missing dependencies"
fi

echo "🏁 Build process complete!"