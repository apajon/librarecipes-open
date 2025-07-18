#!/usr/bin/env python3
"""
Script principal pour exécuter tous les tests fonctionnels Android
Comprend les tests de démarrage, fonctionnalités et intégration
"""

import sys
import subprocess
from pathlib import Path

# Ajouter le répertoire racine au path
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


def run_test_suite(test_module, description):
    """Exécute une suite de tests spécifique"""
    print(f"\n🚀 Exécution: {description}")
    print("=" * 60)

    try:
        # Exécuter le module de test
        result = subprocess.run(
            [sys.executable, str(root_dir / "tests" / f"{test_module}.py")],
            capture_output=True,
            text=True,
            timeout=300,
        )

        print(result.stdout)

        if result.stderr:
            print("⚠️  Avertissements/Erreurs:")
            print(result.stderr)

        success = result.returncode == 0
        status = "✅ RÉUSSI" if success else "❌ ÉCHOUÉ"
        print(f"\n{status}: {description}")

        return success

    except subprocess.TimeoutExpired:
        print(f"❌ TIMEOUT: {description} a pris trop de temps")
        return False
    except Exception as e:
        print(f"❌ ERREUR: {description} - {e}")
        return False


def run_all_functional_tests():
    """Exécute tous les tests fonctionnels Android"""
    print("🧪 Suite complète de tests fonctionnels LibraRecipes Android")
    print("=" * 70)

    # Définir les suites de tests
    test_suites = [
        ("test_functional_android", "Tests fonctionnels principaux"),
        ("test_integration_android", "Tests d'intégration WebView-Streamlit"),
    ]

    # Compteurs de résultats
    total_suites = len(test_suites)
    passed_suites = 0

    # Exécuter chaque suite
    for test_module, description in test_suites:
        success = run_test_suite(test_module, description)
        if success:
            passed_suites += 1

    # Résumé final
    print("\n" + "=" * 70)
    print("📊 RÉSUMÉ FINAL DES TESTS FONCTIONNELS")
    print("=" * 70)
    print(f"Suites de tests exécutées: {total_suites}")
    print(f"Suites réussies: {passed_suites}")
    print(f"Suites échouées: {total_suites - passed_suites}")

    if passed_suites == total_suites:
        print("\n🎉 TOUS LES TESTS FONCTIONNELS SONT PASSÉS !")
        print("✅ L'application Android LibraRecipes est prête pour les tests")
        return True
    else:
        print(f"\n⚠️  {total_suites - passed_suites} suite(s) de tests ont échoué")
        print("❌ Des corrections sont nécessaires avant de continuer")
        return False


def run_quick_validation():
    """Exécute une validation rapide des composants critiques"""
    print("\n🔍 Validation rapide des composants critiques")
    print("=" * 50)

    validations = []

    # 1. Vérification de la structure Android
    android_structure_ok = True
    android_paths = [
        "android/app/src/main/java/com/librarecipes/MainActivity.kt",
        "android/app/src/main/assets/python/android_bridge.py",
        "android/app/build.gradle",
        ".streamlit/config.toml",
    ]

    for path in android_paths:
        file_path = root_dir / path
        if file_path.exists():
            print(f"  ✅ {path}")
        else:
            print(f"  ❌ {path} - MANQUANT")
            android_structure_ok = False

    validations.append(("Structure Android", android_structure_ok))

    # 2. Vérification des modules Python critiques
    python_modules_ok = True
    critical_modules = ["src.db", "src.model", "app.Home", "app.utils.photos_manager"]

    for module in critical_modules:
        try:
            __import__(module)
            print(f"  ✅ Module {module}")
        except ImportError as e:
            print(f"  ❌ Module {module} - ERREUR: {e}")
            python_modules_ok = False

    validations.append(("Modules Python", python_modules_ok))

    # 3. Vérification des scripts de test
    test_scripts_ok = True
    test_scripts = ["scripts/test_android_bridge.py", "scripts/test_android_db.py", "scripts/test_android_photos.py"]

    for script in test_scripts:
        script_path = root_dir / script
        if script_path.exists():
            print(f"  ✅ {script}")
        else:
            print(f"  ❌ {script} - MANQUANT")
            test_scripts_ok = False

    validations.append(("Scripts de test", test_scripts_ok))

    # Résumé de la validation
    print("\n📋 Résumé de la validation:")
    all_valid = True
    for component, is_valid in validations:
        status = "✅ OK" if is_valid else "❌ PROBLÈME"
        print(f"  {component}: {status}")
        if not is_valid:
            all_valid = False

    return all_valid


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Tests fonctionnels LibraRecipes Android")
    parser.add_argument("--quick", "-q", action="store_true", help="Validation rapide uniquement")
    parser.add_argument("--verbose", "-v", action="store_true", help="Mode verbeux")

    args = parser.parse_args()

    if args.quick:
        # Validation rapide seulement
        success = run_quick_validation()
    else:
        # Validation rapide + tests complets
        print("Phase 1: Validation rapide")
        validation_ok = run_quick_validation()

        if validation_ok:
            print("\n✅ Validation rapide réussie, lancement des tests complets")
            success = run_all_functional_tests()
        else:
            print("\n❌ Validation rapide échouée, tests complets ignorés")
            success = False

    # Code de sortie
    sys.exit(0 if success else 1)
