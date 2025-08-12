#!/bin/bash

# LibraRecipes Android Build Wrapper
# This script provides intelligent build handling for different environments

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "🏗️  LibraRecipes Android Build Wrapper"
echo "====================================="

# Function to check if we're in an offline environment
is_offline_environment() {
    # Quick check for internet connectivity
    # Return 0 if offline, 1 if online
    if ping -c 1 -W 2 google.com >/dev/null 2>&1; then
        return 1  # Online
    else
        return 0  # Offline
    fi
}

# Function to check if Gradle cache has dependencies
has_cached_dependencies() {
    local cache_dir="$HOME/.gradle/caches"
    
    # Basic cache directory check
    if [ ! -d "$cache_dir" ]; then
        return 1
    fi
    
    # Check for general cached files
    local cached_files=$(find "$cache_dir" -name "*.jar" -o -name "*.pom" | wc -l)
    if [ "$cached_files" -lt 10 ]; then
        return 1
    fi
    
    # Check for crucial buildscript dependencies
    local modules_dir="$cache_dir/modules-2/files-2.1"
    if [ -d "$modules_dir" ]; then
        # Check for Android Gradle Plugin
        if [ ! -d "$modules_dir/com.android.tools.build/gradle" ]; then
            echo "⚠️  Android Gradle Plugin not cached"
            return 1
        fi
        
        # Check for Kotlin Gradle Plugin
        if [ ! -d "$modules_dir/org.jetbrains.kotlin/kotlin-gradle-plugin" ]; then
            echo "⚠️  Kotlin Gradle Plugin not cached"
            return 1
        fi
        
        # Check for Hilt Plugin
        if [ ! -d "$modules_dir/com.google.dagger/hilt-android-gradle-plugin" ]; then
            echo "⚠️  Hilt Gradle Plugin not cached"
            return 1
        fi
    fi
    
    return 0
}

# Function to run build with appropriate flags
run_build() {
    local build_args=("$@")
    
    echo "🔍 Detecting environment..."
    
    if ! is_offline_environment; then
        echo "🌐 Online environment detected"
        echo "📦 Running build with dependency refresh..."
        
        # Try build with dependency refresh
        if ./gradlew "${build_args[@]}" --refresh-dependencies; then
            echo "✅ Build completed successfully!"
        else
            local exit_code=$?
            echo "❌ Build failed with exit code $exit_code"
            
            # Check for KAPT-specific errors
            if grep -q "kaptGenerateStubs.*FAILED" ~/.gradle/daemon/*/daemon-*.out.log 2>/dev/null ||
               grep -q "Could not load module" ~/.gradle/daemon/*/daemon-*.out.log 2>/dev/null; then
                echo ""
                echo "🚨 KAPT (Kotlin Annotation Processing) Error Detected!"
                echo "This is typically caused by:"
                echo "1. Version compatibility issues between Android Gradle Plugin and Kotlin"
                echo "2. Missing annotation processing dependencies"
                echo "3. Corrupted Gradle cache"
                echo ""
                echo "🔧 Suggested fixes:"
                echo "1. Clean build and clear cache:"
                echo "   ./gradlew clean"
                echo "   rm -rf ~/.gradle/caches/"
                echo "   ./build_wrapper.sh"
                echo ""
                echo "2. Try building without KAPT cache:"
                echo "   ./gradlew clean build --no-build-cache"
                echo ""
                echo "3. Check BUILD_TROUBLESHOOTING.md for detailed solutions"
            fi
            
            return $exit_code
        fi
    else
        echo "📴 Offline environment detected"
        
        if has_cached_dependencies; then
            echo "✅ Cached dependencies found"
            echo "📦 Running offline build..."
            ./gradlew "${build_args[@]}" --offline
        else
            echo "❌ No cached dependencies found"
            echo ""
            echo "🚨 Cannot build in offline mode without cached dependencies!"
            echo ""
            echo "Missing dependencies detected:"
            
            # Run the dependency check again to show what's missing
            has_cached_dependencies
            
            echo ""
            echo "Solutions:"
            echo "1. 🌐 Run this build on a machine with internet access first:"
            echo "   ./gradlew build --refresh-dependencies"
            echo ""
            echo "2. 📦 Copy the complete Gradle cache from a machine that has built this project:"
            echo "   # On connected machine:"
            echo "   tar -czf gradle-cache.tar.gz ~/.gradle/caches"
            echo "   # On this machine:"
            echo "   tar -xzf gradle-cache.tar.gz -C ~/"
            echo ""
            echo "3. 🔧 Try a minimal build to check what's available:"
            echo "   ./gradlew tasks --offline"
            echo ""
            echo "4. 🏢 Use a corporate proxy if available (see BUILD_TROUBLESHOOTING.md)"
            echo ""
            echo "5. 📞 Contact your system administrator about network access"
            echo ""
            echo "For detailed troubleshooting, see: BUILD_TROUBLESHOOTING.md"
            echo "Or run: ./check_connectivity.sh"
            exit 1
        fi
    fi
}

# Main execution
main() {
    # Default to 'build' if no arguments provided
    if [ $# -eq 0 ]; then
        set -- "build"
    fi
    
    # Handle special commands
    case "$1" in
        "help"|"--help"|"-h")
            echo ""
            echo "Usage: $0 [gradle-task...]"
            echo ""
            echo "This wrapper automatically detects your environment and runs Gradle with"
            echo "appropriate flags for online/offline scenarios."
            echo ""
            echo "Examples:"
            echo "  $0                    # Same as './gradlew build'"
            echo "  $0 clean build       # Clean and build"
            echo "  $0 assembleDebug     # Build debug APK"
            echo "  $0 test              # Run tests"
            echo ""
            echo "Special commands:"
            echo "  $0 check             # Check connectivity and diagnostics"
            echo "  $0 cache-info        # Show cache information"
            echo "  $0 force-offline     # Force offline build (bypass detection)"
            echo "  $0 fix-kapt          # Clean build to fix KAPT issues"
            echo ""
            echo "For troubleshooting connectivity issues:"
            echo "  ./check_connectivity.sh"
            echo ""
            echo "For detailed troubleshooting:"
            echo "  cat BUILD_TROUBLESHOOTING.md"
            echo ""
            exit 0
            ;;
        "check")
            echo "🔍 Running connectivity check..."
            ./check_connectivity.sh
            exit $?
            ;;
        "cache-info")
            echo "📦 Gradle Cache Information"
            echo "========================="
            echo ""
            cache_dir="$HOME/.gradle/caches"
            if [ -d "$cache_dir" ]; then
                echo "Cache directory: $cache_dir"
                echo "Cache size: $(du -sh "$cache_dir" 2>/dev/null | cut -f1)"
                echo "Total files: $(find "$cache_dir" -type f | wc -l)"
                echo "JAR/POM files: $(find "$cache_dir" -name "*.jar" -o -name "*.pom" | wc -l)"
                echo ""
                echo "Key buildscript dependencies:"
                modules_dir="$cache_dir/modules-2/files-2.1"
                if [ -d "$modules_dir" ]; then
                    [ -d "$modules_dir/com.android.tools.build/gradle" ] && echo "✅ Android Gradle Plugin" || echo "❌ Android Gradle Plugin"
                    [ -d "$modules_dir/org.jetbrains.kotlin/kotlin-gradle-plugin" ] && echo "✅ Kotlin Gradle Plugin" || echo "❌ Kotlin Gradle Plugin"
                    [ -d "$modules_dir/com.google.dagger/hilt-android-gradle-plugin" ] && echo "✅ Hilt Gradle Plugin" || echo "❌ Hilt Gradle Plugin"
                    [ -d "$modules_dir/com.google.dagger/hilt-compiler" ] && echo "✅ Hilt Compiler (KAPT)" || echo "❌ Hilt Compiler (KAPT)"
                else
                    echo "❌ No modules cache found"
                fi
            else
                echo "❌ No Gradle cache found"
            fi
            echo ""
            exit 0
            ;;
        "force-offline")
            echo "🔍 Forcing offline mode..."
            echo "📴 Offline mode (forced)"
            echo "📦 Running offline build..."
            shift  # Remove 'force-offline' from arguments
            ./gradlew "$@" --offline
            exit $?
            ;;
        "fix-kapt")
            echo "🔧 KAPT Error Fix Procedure"
            echo "========================="
            echo ""
            echo "This will clean the project and clear KAPT caches to resolve module issues."
            echo ""
            read -p "Continue? (y/N): " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                echo "1. 🧹 Cleaning project..."
                ./gradlew clean
                
                echo "2. 🗑️  Clearing KAPT cache..."
                rm -rf ~/.gradle/caches/transforms-*
                rm -rf build/generated/source/kapt/
                rm -rf app/build/generated/source/kapt/
                
                echo "3. 🔄 Rebuilding without build cache..."
                if ! is_offline_environment; then
                    echo "📦 Online build with dependency refresh..."
                    ./gradlew build --no-build-cache --refresh-dependencies
                else
                    echo "📦 Offline build..."
                    ./gradlew build --no-build-cache --offline
                fi
                
                echo "✅ KAPT fix procedure completed!"
            else
                echo "❌ Cancelled"
                exit 1
            fi
            exit $?
            ;;
        *)
            # Run regular build with all arguments
            run_build "$@"
            ;;
    esac
    
    echo ""
    echo "✅ Build completed successfully!"
    echo ""
    
    # Show APK location if build was successful
    if [ "$1" = "build" ] || [ "$1" = "assembleDebug" ] || [ "$1" = "assembleRelease" ]; then
        echo "📱 APK files can be found in:"
        find app/build/outputs/apk -name "*.apk" 2>/dev/null | head -3 || echo "   app/build/outputs/apk/"
        echo ""
    fi
}

# Run main function with all arguments
main "$@"