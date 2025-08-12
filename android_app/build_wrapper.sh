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
    ping -c 1 -W 2 google.com >/dev/null 2>&1
    return $?
}

# Function to check if Gradle cache has dependencies
has_cached_dependencies() {
    local cache_dir="$HOME/.gradle/caches"
    if [ -d "$cache_dir" ] && [ "$(find "$cache_dir" -name "*.jar" -o -name "*.pom" | wc -l)" -gt 10 ]; then
        return 0
    else
        return 1
    fi
}

# Function to run build with appropriate flags
run_build() {
    local build_args=("$@")
    
    echo "🔍 Detecting environment..."
    
    if ! is_offline_environment; then
        echo "🌐 Online environment detected"
        echo "📦 Running build with dependency refresh..."
        ./gradlew "${build_args[@]}" --refresh-dependencies
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
            echo "Solutions:"
            echo "1. Run this build on a machine with internet access first"
            echo "2. Copy the Gradle cache from a machine that has built this project"
            echo "3. Contact your system administrator about network access"
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
    esac
    
    # Run the build
    run_build "$@"
    
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