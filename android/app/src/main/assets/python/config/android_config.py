"""
Configuration Android pour LibraRecipes
Support Chaquopy + WebView avec stockage SQLite local
"""

import os
import sys
from pathlib import Path
from typing import Dict, List

from dotenv import load_dotenv
from pathvalidate import sanitize_filename, sanitize_filepath


class AndroidConfig:
    """Configuration centralisée pour le portage Android de LibraRecipes"""

    def __init__(self):
        """Initialise la configuration Android"""
        # Charger les variables d'environnement
        load_dotenv()

        # Détecter l'environnement
        self.is_android = self._detect_android_environment()
        self.is_development = not self.is_android

        # Configurer les chemins
        self._setup_paths()

    def _detect_android_environment(self) -> bool:
        """Détecte si on est dans un environnement Android/Chaquopy"""
        android_indicators = [
            # Utiliser des indicateurs plus fiables
            "ANDROID_ROOT" in os.environ,
            "ANDROID_DATA" in os.environ,
            "com.termux" in os.getenv("PREFIX", ""),
            "chaquo" in str(sys.modules.keys()),
        ]
        return any(android_indicators)

    def _setup_paths(self):
        """Configure les chemins selon l'environnement"""
        if self.is_android:
            self._setup_android_paths()
        else:
            self._setup_development_paths()

    def _setup_android_paths(self):
        """Configuration des chemins pour Android"""
        # Stockage Android (défini par MainActivity.kt)
        android_storage = os.getenv("ANDROID_STORAGE", "/data/data/com.librarecipes/files")
        self.storage_root = Path(android_storage)

        # Chemins spécifiques Android
        self.database_path = self.storage_root / "recettes.db"
        self.photos_directory = self.storage_root / "photos"
        self.logs_directory = self.storage_root / "logs"

        # Configuration Streamlit pour Android
        self.streamlit_config = {
            "server.port": 8501,
            "server.headless": True,
            "server.enableCORS": False,
            "server.enableXsrfProtection": False,
            "server.maxUploadSize": 50,
            "global.developmentMode": False,
        }

    def _setup_development_paths(self):
        """Configuration des chemins pour développement"""
        # Chemins de développement (comme actuellement)
        project_root = Path(__file__).parent.parent
        self.storage_root = project_root / "data"

        # Simulation Android pour développement - utiliser un dossier local
        android_sim = os.getenv("ANDROID_STORAGE")
        if android_sim and Path(android_sim).exists():
            self.storage_root = Path(android_sim)

        self.database_path = self.storage_root / "recettes.db"
        self.photos_directory = self.storage_root / "photos"
        self.logs_directory = self.storage_root / "logs"

        # Configuration Streamlit pour développement
        self.streamlit_config = {
            "server.port": 8501,
            "server.headless": False,
            "server.enableCORS": True,
            "server.enableXsrfProtection": True,
            "server.maxUploadSize": 200,
            "global.developmentMode": True,
        }

    def get_database_url(self) -> str:
        """Retourne l'URL de la base de données SQLite"""
        # Assurer que le répertoire existe
        self.database_path.parent.mkdir(parents=True, exist_ok=True)

        return f"sqlite:///{self.database_path}"

    def get_photos_directory(self) -> Path:
        """Retourne le répertoire des photos avec création si nécessaire"""
        self.photos_directory.mkdir(parents=True, exist_ok=True)
        return self.photos_directory

    def get_logs_directory(self) -> Path:
        """Retourne le répertoire des logs avec création si nécessaire"""
        self.logs_directory.mkdir(parents=True, exist_ok=True)
        return self.logs_directory

    def get_safe_filename(self, filename: str) -> str:
        """Retourne un nom de fichier sécurisé pour Android/Linux"""
        return sanitize_filename(filename)

    def get_safe_filepath(self, filepath: str) -> str:
        """Retourne un chemin de fichier sécurisé"""
        return sanitize_filepath(filepath)

    def get_streamlit_args(self) -> List[str]:
        """Retourne les arguments Streamlit selon l'environnement"""
        args = []
        for key, value in self.streamlit_config.items():
            args.append(f"--{key}={value}")
        return args

    def setup_environment_variables(self):
        """Configure les variables d'environnement pour l'application"""
        # Définir les chemins pour le code existant
        os.environ["LIBRARECIPES_DATABASE_URL"] = self.get_database_url()
        os.environ["LIBRARECIPES_PHOTOS_DIR"] = str(self.get_photos_directory())
        os.environ["LIBRARECIPES_LOGS_DIR"] = str(self.get_logs_directory())
        os.environ["LIBRARECIPES_IS_ANDROID"] = str(self.is_android)

        # Configuration Python pour Android
        if self.is_android:
            os.environ["PYTHONUNBUFFERED"] = "1"
            os.environ["PYTHONIOENCODING"] = "utf-8"

    def get_app_info(self) -> Dict[str, str]:
        """Retourne les informations de l'application"""
        return {
            "name": "LibraRecipes",
            "version": "2.4.0",
            "environment": "Android" if self.is_android else "Development",
            "database": str(self.database_path),
            "photos": str(self.photos_directory),
            "storage_root": str(self.storage_root),
        }

    def validate_setup(self) -> bool:
        """Valide que la configuration est correcte"""
        try:
            # Vérifier que les répertoires peuvent être créés
            self.get_photos_directory()
            self.get_logs_directory()

            # Vérifier que le chemin de base de données est valide
            db_dir = self.database_path.parent
            db_dir.mkdir(parents=True, exist_ok=True)

            return True
        except Exception as e:
            print(f"❌ Erreur de validation AndroidConfig: {e}")
            return False


# Instance globale de configuration
android_config = AndroidConfig()


def get_android_config() -> AndroidConfig:
    """Retourne l'instance de configuration Android"""
    return android_config


# Fonctions utilitaires pour compatibilité
def get_database_url() -> str:
    """Fonction utilitaire pour obtenir l'URL de la base de données"""
    return android_config.get_database_url()


def get_photos_directory() -> Path:
    """Fonction utilitaire pour obtenir le répertoire des photos"""
    return android_config.get_photos_directory()


def is_android_environment() -> bool:
    """Fonction utilitaire pour détecter l'environnement Android"""
    return android_config.is_android


if __name__ == "__main__":
    # Test de la configuration
    config = get_android_config()

    print("🔧 Configuration AndroidConfig")
    print("=" * 40)

    app_info = config.get_app_info()
    for key, value in app_info.items():
        print(f"{key}: {value}")

    print("\n🧪 Test de validation...")
    if config.validate_setup():
        print("✅ Configuration Android valide")
    else:
        print("❌ Configuration Android invalide")

    print(f"\n📱 Environnement: {'Android' if config.is_android else 'Développement'}")
    print(f"🗄️ Base de données: {config.get_database_url()}")
    print(f"📸 Photos: {config.get_photos_directory()}")
