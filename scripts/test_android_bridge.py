"""
Script de test pour le bridge Android-Python
Teste la fonctionnalité du bridge en mode développement
"""

import sys
import os
from pathlib import Path

# Ajouter le chemin du bridge aux imports
bridge_path = Path(__file__).parent.parent / "android_bridge.py"
if bridge_path.exists():
    sys.path.insert(0, str(bridge_path.parent))
else:
    # Fallback vers le chemin Android assets
    bridge_path = Path(__file__).parent.parent / "android" / "app" / "src" / "main" / "assets" / "python"
    sys.path.insert(0, str(bridge_path))


def test_bridge_import():
    """Test d'import du bridge"""
    try:
        import android_bridge  # noqa: F401

        print("✅ Import du bridge réussi")
        return True
    except ImportError as e:
        print(f"❌ Erreur d'import du bridge: {e}")
        return False


def test_bridge_environment():
    """Test de la configuration d'environnement"""
    try:
        from android_bridge import check_android_environment, setup_android_logging

        setup_android_logging()
        print("✅ Configuration du logging réussie")

        # Simuler l'environnement Android
        os.environ["ANDROID_STORAGE"] = str(Path.cwd())

        if check_android_environment():
            print("✅ Environnement Android validé")
        else:
            print("⚠️  Environnement Android non configuré (normal en développement)")

        return True
    except Exception as e:
        print(f"❌ Erreur de configuration: {e}")
        return False


def test_bridge_initialization():
    """Test d'initialisation du bridge"""
    try:
        from android_bridge import AndroidBridge

        bridge = AndroidBridge()
        print("✅ Initialisation du bridge réussie")

        status = bridge.get_server_status()
        print(f"✅ Statut du serveur: {status}")

        return True
    except Exception as e:
        print(f"❌ Erreur d'initialisation: {e}")
        return False


def main():
    """Fonction principale de test"""
    print("🧪 Test du bridge Android-Python LibraRecipes")
    print("=" * 50)

    tests = [test_bridge_import, test_bridge_environment, test_bridge_initialization]

    passed = 0
    for test in tests:
        print(f"\n🔍 {test.__doc__}")
        if test():
            passed += 1

    print(f"\n📊 Résultats: {passed}/{len(tests)} tests réussis")

    if passed == len(tests):
        print("🎉 Tous les tests sont passés !")
        return True
    else:
        print("⚠️  Certains tests ont échoué")
        return False


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
