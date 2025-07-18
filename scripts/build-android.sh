#!/bin/bash

# Script de build automatisé pour LibraRecipes Android
# Usage: ./build-android.sh [debug|release|clean]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ANDROID_DIR="$PROJECT_ROOT/android"

echo "🚀 LibraRecipes Android Build Script"
echo "======================================"

# Function to print colored output
print_status() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

print_success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

print_error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

# Vérifier que nous sommes dans le bon répertoire
if [ ! -d "$ANDROID_DIR" ]; then
    print_error "Répertoire Android non trouvé: $ANDROID_DIR"
    exit 1
fi

cd "$ANDROID_DIR"

# Vérifier les prérequis
print_status "Vérification des prérequis..."

if ! command -v python3 &> /dev/null; then
    print_error "Python3 n'est pas installé"
    exit 1
fi

if [ ! -f "$PROJECT_ROOT/scripts/sync_to_android.py" ]; then
    print_error "Script de synchronisation non trouvé"
    exit 1
fi

# Synchroniser les assets Python
print_status "Synchronisation des assets Python..."
cd "$PROJECT_ROOT"
python3 scripts/sync_to_android.py
cd "$ANDROID_DIR"

# Fonction pour build debug
build_debug() {
    print_status "Construction de l'APK debug..."
    ./gradlew assembleDebug

    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        print_success "APK debug créé: app/build/outputs/apk/debug/app-debug.apk"
    else
        print_error "Échec de la création de l'APK debug"
        exit 1
    fi
}

# Fonction pour build release
build_release() {
    print_status "Construction de l'APK release..."

    if [ ! -f "keystore.properties" ]; then
        print_error "Fichier keystore.properties manquant pour le build release"
        print_error "Exécutez d'abord: keytool -genkey -v -keystore release-key.keystore ..."
        exit 1
    fi

    ./gradlew assembleRelease

    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        print_success "APK release créé: app/build/outputs/apk/release/app-release.apk"
    else
        print_error "Échec de la création de l'APK release"
        exit 1
    fi
}

# Fonction pour nettoyer
clean_build() {
    print_status "Nettoyage du projet..."
    ./gradlew clean
    print_success "Nettoyage terminé"
}

# Fonction pour vérifier la configuration
check_config() {
    print_status "Vérification de la configuration..."
    ./gradlew checkConfig
}

# Parse command line arguments
case "${1:-debug}" in
    "debug")
        build_debug
        ;;
    "release")
        build_release
        ;;
    "clean")
        clean_build
        ;;
    "check")
        check_config
        ;;
    "all")
        clean_build
        build_debug
        build_release
        ;;
    *)
        echo "Usage: $0 [debug|release|clean|check|all]"
        echo ""
        echo "Options:"
        echo "  debug   - Build APK debug (défaut)"
        echo "  release - Build APK release (nécessite keystore.properties)"
        echo "  clean   - Nettoie le projet"
        echo "  check   - Vérifie la configuration"
        echo "  all     - Clean + build debug + build release"
        exit 1
        ;;
esac

print_success "Build terminé avec succès!"
