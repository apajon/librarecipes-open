"""
Module d'aide pour la configuration Android.
Gère l'import conditionnel du module android_config.
"""

import sys
from pathlib import Path


def get_android_config():
    """
    Obtient la configuration Android si disponible, sinon retourne une configuration par défaut.

    Returns:
        dict: Configuration Android ou configuration par défaut
    """
    try:
        # Essayer d'importer depuis le chemin correct
        project_root = Path(__file__).parent.parent.parent
        sys.path.insert(0, str(project_root))

        from config.android_config import get_android_config as _get_android_config

        return _get_android_config()
    except ImportError:
        # Configuration par défaut pour l'environnement de développement
        return {
            "app_name": "LibraRecipes",
            "database_path": "./data/recettes.db",
            "photos_path": "./data/photos",
            "android_data_path": "/data/data/com.librarecipes/files",
            "is_android": False,
            "streamlit_config": {"port": 8501, "headless": False, "enable_cors": False, "max_upload_size": 200},
        }


def get_database_url():
    """
    Obtient l'URL de la base de données adaptée à l'environnement.

    Returns:
        str: URL de la base de données SQLite
    """
    try:
        # Essayer d'importer depuis le chemin correct
        project_root = Path(__file__).parent.parent.parent
        sys.path.insert(0, str(project_root))

        from config.android_config import get_database_url as _get_database_url

        return _get_database_url()
    except ImportError:
        # Configuration par défaut pour l'environnement de développement
        return "sqlite:///./data/recettes.db"


def is_android_environment():
    """
    Vérifie si l'application s'exécute dans un environnement Android.

    Returns:
        bool: True si Android, False sinon
    """
    config = get_android_config()
    return config.get("is_android", False)


def get_photos_path():
    """
    Obtient le chemin de stockage des photos adapté à l'environnement.

    Returns:
        str: Chemin vers le répertoire des photos
    """
    config = get_android_config()
    return config.get("photos_path", "./data/photos")


def get_app_data_path():
    """
    Obtient le chemin de données de l'application adapté à l'environnement.

    Returns:
        str: Chemin vers le répertoire de données de l'app
    """
    config = get_android_config()
    return config.get("android_data_path", "./data")
