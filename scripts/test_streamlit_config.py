#!/usr/bin/env python3
"""
Script de test pour la configuration Streamlit Android.
Teste la configuration headless, CORS, et les styles mobiles.
"""

import sys
import tomllib
from pathlib import Path

# Ajouter le répertoire racine au path pour les imports
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


def test_streamlit_config():
    """Teste la configuration Streamlit pour Android"""
    print("⚙️ Test de la configuration Streamlit Android\n")

    config_path = root_dir / ".streamlit" / "config.toml"

    if not config_path.exists():
        print("❌ Fichier config.toml non trouvé")
        return False

    # Charger et vérifier la configuration
    with open(config_path, "rb") as f:
        config = tomllib.load(f)

    print("📊 Configuration chargée:")

    # Vérifications spécifiques Android
    checks = {
        "Headless activé": config.get("server", {}).get("headless", False),
        "CORS désactivé": not config.get("server", {}).get("enableCORS", True),
        "XSRF désactivé": not config.get("server", {}).get("enableXsrfProtection", True),
        "Port configuré": config.get("server", {}).get("port") == 8501,
        "Mode dev désactivé": not config.get("global", {}).get("developmentMode", True),
        "Stats désactivées": not config.get("browser", {}).get("gatherUsageStats", True),
    }

    for check_name, result in checks.items():
        status = "✅" if result else "❌"
        print(f"  {status} {check_name}: {result}")

    # Vérifier sections importantes
    print("\n📋 Sections de configuration:")
    sections = ["server", "browser", "client", "global", "theme", "logger"]
    for section in sections:
        if section in config:
            print(f"  ✅ Section '{section}': {len(config[section])} paramètres")
        else:
            print(f"  ❌ Section '{section}': manquante")

    return all(checks.values())


def test_mobile_styles():
    """Teste les styles mobiles"""
    print("\n🎨 Test des styles mobiles:")

    css_path = root_dir / ".streamlit" / "static" / "mobile.css"

    if not css_path.exists():
        print("  ❌ Fichier mobile.css non trouvé")
        return False

    css_content = css_path.read_text(encoding="utf-8")

    # Vérifications CSS importantes
    css_checks = {
        "Media queries mobile": "@media screen and (max-width: 768px)" in css_content,
        "Boutons tactiles": "min-height: 44px" in css_content,
        "Touch action": "touch-action: manipulation" in css_content,
        "Responsive containers": ".main .block-container" in css_content,
        "Font size mobile": "font-size: 16px" in css_content,
        "Responsive images": ".stImage" in css_content,
        "File uploader": ".stFileUploader" in css_content,
    }

    for check_name, result in css_checks.items():
        status = "✅" if result else "❌"
        print(f"  {status} {check_name}: {result}")

    print(f"  📏 Taille CSS: {len(css_content)} caractères")
    print(f"  📱 Règles @media: {css_content.count('@media')}")

    return all(css_checks.values())


def test_mobile_style_manager():
    """Teste le gestionnaire de styles mobiles"""
    print("\n🛠️ Test du gestionnaire de styles mobiles:")

    try:
        from app.utils.mobile_styles import MobileStyleManager

        # Créer une instance du gestionnaire
        style_manager = MobileStyleManager()

        print("  ✅ Import MobileStyleManager réussi")
        print(f"  ✅ Répertoire styles: {style_manager.styles_dir}")
        print(f"  ✅ Fichier CSS: {style_manager.mobile_css_path}")

        # Tester le chargement CSS
        css_content = style_manager.load_mobile_styles()
        if css_content:
            print(f"  ✅ CSS chargé: {len(css_content)} caractères")
        else:
            print("  ❌ Échec chargement CSS")
            return False

        # Tester les fonctions utilitaires
        print("  ✅ Gestionnaire de styles opérationnel")

        return True

    except ImportError as e:
        print(f"  ❌ Erreur import: {e}")
        return False
    except Exception as e:
        print(f"  ❌ Erreur test: {e}")
        return False


def test_android_configuration_integration():
    """Teste l'intégration avec la configuration Android"""
    print("\n🤖 Test intégration configuration Android:")

    try:
        from config.android_config import get_android_config

        config = get_android_config()

        # Vérifier configuration Streamlit dans AndroidConfig
        streamlit_config = getattr(config, "streamlit_config", {})

        if streamlit_config:
            print("  ✅ Configuration Streamlit dans AndroidConfig")
            print(f"  • Port: {streamlit_config.get('server.port', 'non défini')}")
            print(f"  • Headless: {streamlit_config.get('server.headless', 'non défini')}")
            print(f"  • CORS: {streamlit_config.get('server.enableCORS', 'non défini')}")
            print(f"  • Max upload: {streamlit_config.get('server.maxUploadSize', 'non défini')} MB")
        else:
            print("  ⚠️ Pas de configuration Streamlit spécifique dans AndroidConfig")

        return True

    except Exception as e:
        print(f"  ❌ Erreur test configuration: {e}")
        return False


def test_directory_structure():
    """Teste la structure des répertoires de configuration"""
    print("\n📁 Test structure répertoires:")

    paths_to_check = [
        (".streamlit", "Répertoire config Streamlit"),
        (".streamlit/config.toml", "Fichier config principal"),
        (".streamlit/static", "Répertoire assets statiques"),
        (".streamlit/static/mobile.css", "Fichier styles mobiles"),
        ("app/utils/mobile_styles.py", "Module styles Python"),
    ]

    all_exist = True
    for path_str, description in paths_to_check:
        path = root_dir / path_str
        exists = path.exists()
        status = "✅" if exists else "❌"
        print(f"  {status} {description}: {path}")
        if not exists:
            all_exist = False

    return all_exist


if __name__ == "__main__":
    try:
        print("🧪 Tests de configuration Streamlit pour Android\n")

        tests = [
            ("Structure répertoires", test_directory_structure),
            ("Configuration Streamlit", test_streamlit_config),
            ("Styles mobiles", test_mobile_styles),
            ("Gestionnaire styles", test_mobile_style_manager),
            ("Intégration Android", test_android_configuration_integration),
        ]

        results = []
        for test_name, test_func in tests:
            print(f"{'=' * 50}")
            result = test_func()
            results.append((test_name, result))
            print()

        # Résumé final
        print("🎯 Résumé des tests:")
        for test_name, result in results:
            status = "✅" if result else "❌"
            print(f"  {status} {test_name}")

        success_count = sum(1 for _, result in results if result)
        total_count = len(results)

        if success_count == total_count:
            print(f"\n🎉 Tous les tests réussis ({success_count}/{total_count}) !")
            print("Configuration Streamlit Android prête pour déploiement 🚀")
        else:
            print(f"\n⚠️ {success_count}/{total_count} tests réussis")
            print("Vérifier les configurations manquantes")

    except Exception as e:
        print(f"\n💥 Erreur lors des tests: {e}")
        sys.exit(1)
