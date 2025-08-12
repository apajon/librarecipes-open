#!/bin/bash

# LibraRecipes Android Build Connectivity Check Script
# This script helps diagnose and resolve Gradle build connectivity issues

set -e

echo "🔍 LibraRecipes Android Build Connectivity Checker"
echo "=================================================="

# Function to check connectivity to a host
check_host() {
    local host=$1
    local description=$2
    echo -n "Checking $description ($host)... "
    
    if ping -c 1 -W 3 "$host" >/dev/null 2>&1; then
        echo "✅ Connected"
        return 0
    else
        echo "❌ No connection"
        return 1
    fi
}

# Function to check if running in a restricted environment
check_environment() {
    echo ""
    echo "🌐 Network Connectivity Check"
    echo "------------------------"
    
    local connected=0
    
    # Check key repositories
    check_host "dl.google.com" "Google Maven Repository" && connected=1
    check_host "maven.google.com" "Alternative Google Maven" && connected=1
    check_host "repo1.maven.org" "Maven Central" && connected=1
    check_host "jcenter.bintray.com" "JCenter Repository" && connected=1
    check_host "plugins.gradle.org" "Gradle Plugin Portal" && connected=1
    
    if [ $connected -eq 0 ]; then
        echo ""
        echo "⚠️  WARNING: No internet connectivity detected!"
        echo "This appears to be a restricted/offline environment."
        echo ""
        echo "📋 Possible solutions:"
        echo "1. Use --offline mode if dependencies are cached"
        echo "2. Set up a local Maven repository mirror"
        echo "3. Use a corporate proxy if available"
        echo "4. Pre-download dependencies in a connected environment"
        echo ""
        return 1
    else
        echo ""
        echo "✅ Internet connectivity available"
        return 0
    fi
}

# Function to suggest Gradle solutions
suggest_solutions() {
    echo "🔧 Suggested Solutions for Gradle Build Issues"
    echo "============================================="
    echo ""
    
    echo "1. 📥 Try building with cached dependencies (offline mode):"
    echo "   ./gradlew build --offline"
    echo ""
    
    echo "2. 🔄 Clean and retry the build:"
    echo "   ./gradlew clean build --refresh-dependencies"
    echo ""
    
    echo "3. 🌐 Use alternative repository configurations:"
    echo "   (Already configured in build.gradle with fallback repos)"
    echo ""
    
    echo "4. 🔧 Clear Gradle cache and retry:"
    echo "   rm -rf ~/.gradle/caches/"
    echo "   ./gradlew build"
    echo ""
    
    echo "5. 📦 For completely offline environments:"
    echo "   a) Pre-download dependencies on a connected machine"
    echo "   b) Copy ~/.gradle/caches/ to the offline environment"
    echo "   c) Use --offline mode"
    echo ""
}

# Function to check Gradle daemon status
check_gradle_daemon() {
    echo "🔄 Gradle Daemon Status"
    echo "---------------------"
    
    if ./gradlew --status >/dev/null 2>&1; then
        echo "✅ Gradle daemon accessible"
        ./gradlew --status
    else
        echo "❌ Gradle daemon issues detected"
        echo "Try: ./gradlew --stop && ./gradlew build"
    fi
    echo ""
}

# Main execution
main() {
    cd "$(dirname "$0")"
    
    if ! check_environment; then
        echo "🚨 OFFLINE ENVIRONMENT DETECTED"
        echo ""
        echo "This build environment has no internet access."
        echo "The Gradle build errors are due to network connectivity issues."
        echo ""
        suggest_solutions
        echo ""
        echo "💡 Quick fixes to try:"
        echo "1. ./gradlew build --offline"
        echo "2. Check if dependencies are pre-cached"
        echo "3. Contact your system administrator about proxy settings"
        echo ""
        exit 1
    fi
    
    check_gradle_daemon
    suggest_solutions
    
    echo "🚀 Ready to build! Try: ./gradlew build"
}

# Run the main function
main "$@"