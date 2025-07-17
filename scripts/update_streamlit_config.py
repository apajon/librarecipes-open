"""
Configuration dynamique Streamlit pour Android
Génère config.toml adaptatif selon l'environnement
"""

import sys
from pathlib import Path

# Ajouter le répertoire racine au path pour les imports
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


def generate_streamlit_config():
    """Génère la configuration Streamlit adaptée à l'environnement"""
    from config.android_config import get_android_config

    config = get_android_config()

    # Configuration de base
    base_config = {
        "server": {
            "headless": config.is_android,
            "port": 8501,
            "address": "127.0.0.1",
            "enableCORS": not config.is_android,
            "enableXsrfProtection": not config.is_android,
            "maxUploadSize": 50 if config.is_android else 200,
            "maxMessageSize": 200,
            "enableWebsocketCompression": False,
        },
        "browser": {
            "gatherUsageStats": False,
            "serverAddress": "127.0.0.1",
            "serverPort": 8501,
        },
        "client": {
            "caching": True,
            "displayEnabled": True,
            "showErrorDetails": not config.is_android,
        },
        "global": {
            "developmentMode": not config.is_android,
            "suppressDeprecationWarnings": config.is_android,
            "disableWatchdogWarning": config.is_android,
        },
        "theme": {
            "base": "light",
            "primaryColor": "#FF6B6B",
            "backgroundColor": "#FFFFFF",
            "secondaryBackgroundColor": "#F0F2F6",
            "textColor": "#262730",
            "font": "sans serif",
        },
        "logger": {
            "level": "INFO" if config.is_android else "DEBUG",
            "enableLogging": True,
        },
        "deprecation": {
            "showPyplotGlobalUse": False,
            "showfileUploaderEncoding": False,
        },
    }

    return base_config


def write_streamlit_config():
    """Écrit la configuration Streamlit dans le fichier config.toml"""
    config_data = generate_streamlit_config()
    config_path = root_dir / ".streamlit" / "config.toml"

    # Créer le répertoire si nécessaire
    config_path.parent.mkdir(parents=True, exist_ok=True)

    # Générer le contenu TOML
    toml_content = "# Configuration Streamlit pour LibraRecipes Android\n"
    toml_content += "# Générée automatiquement selon l'environnement\n\n"

    for section_name, section_data in config_data.items():
        toml_content += f"[{section_name}]\n"
        for key, value in section_data.items():
            if isinstance(value, bool):
                toml_content += f"{key} = {str(value).lower()}\n"
            elif isinstance(value, str):
                toml_content += f'{key} = "{value}"\n'
            else:
                toml_content += f"{key} = {value}\n"
        toml_content += "\n"

    # Écrire le fichier
    config_path.write_text(toml_content, encoding="utf-8")

    return config_path


def update_streamlit_config_for_android():
    """Met à jour la configuration Streamlit pour Android"""
    from config.android_config import get_android_config

    print("⚙️ Mise à jour configuration Streamlit pour Android...")

    config = get_android_config()
    config_path = write_streamlit_config()

    print(f"✅ Configuration générée: {config_path}")
    print(f"📱 Mode Android: {config.is_android}")
    print(f"🔧 Headless: {config.is_android}")
    print(f"🌐 CORS: {not config.is_android}")

    return config_path


if __name__ == "__main__":
    update_streamlit_config_for_android()
