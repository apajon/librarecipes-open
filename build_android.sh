#!/bin/bash
# LibraRecipes Android Build Script
# This script builds the Android APK for LibraRecipes mobile app

set -euo pipefail

echo "🍴 LibraRecipes Android Build Script"
echo "======================================"

# Check if buildozer is installed
if ! command -v buildozer &> /dev/null; then
    echo "❌ Buildozer not found. Installing..."
    pip install --user buildozer
    export PATH="$HOME/.local/bin:$PATH"
fi

# Move to repo root (this script sits at project root)
cd "$(dirname "$0")"

# Clean previous builds
echo "🧹 Cleaning previous builds..."
rm -rf .buildozer/ bin/

# Build the APK in debug mode
echo "🔨 Building debug APK..."
buildozer android debug

# Report
APK_PATH=$(ls bin/*-debug.apk 2>/dev/null || true)
if [[ -n "${APK_PATH}" ]]; then
    echo "✅ BUILD SUCCESSFUL!"
    echo "📁 APK: ${APK_PATH}"
    du -h ${APK_PATH} | awk '{print "📱 Size:", $1}'
else
    echo "❌ BUILD FAILED"
    exit 1
fi
