"""
Bridge Python-Android pour LibraRecipes
Ce module fait le lien entre l'application Android (Kotlin) et le serveur Streamlit Python.
"""

import os
import sys
import threading
import time
import socket
import logging
from pathlib import Path
from typing import Optional, Dict, Any

# Configuration du logging pour Android
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("LibraRecipes-Bridge")


class AndroidBridge:
    """Bridge principal pour gérer Streamlit sur Android via Chaquopy"""

    def __init__(self):
        self.streamlit_server = None
        self.server_thread = None
        self.server_port = None
        self.is_running = False

        # Configurer l'environnement Android
        self._setup_android_environment()

    def _setup_android_environment(self):
        """Configure l'environnement Python pour Android"""
        try:
            logger.info("Configuration de l'environnement Android...")

            # Récupérer le chemin de stockage Android depuis les propriétés système
            android_storage = os.environ.get("ANDROID_STORAGE") or os.getcwd()
            logger.info(f"Stockage Android configuré: {android_storage}")

            # Créer les dossiers nécessaires
            self._create_android_directories(android_storage)

            # Configurer les variables d'environnement pour LibraRecipes
            os.environ["LIBRARECIPES_DATA_DIR"] = os.path.join(android_storage, "data")
            os.environ["LIBRARECIPES_PHOTOS_DIR"] = os.path.join(android_storage, "data", "photos")
            os.environ["ANDROID_MODE"] = "true"

            # Ajouter le chemin du code LibraRecipes au Python path
            app_path = os.path.join(android_storage, "librarecipes")
            if os.path.exists(app_path):
                sys.path.insert(0, app_path)
                logger.info(f"Chemin LibraRecipes ajouté: {app_path}")

        except Exception as e:
            logger.error(f"Erreur lors de la configuration Android: {e}")
            raise

    def _create_android_directories(self, storage_path: str):
        """Crée la structure de dossiers nécessaire sur Android"""
        directories = [
            "data",
            "data/photos",
            "data/android_test",
            "data/android_test/logs",
            "data/android_test/photos",
        ]

        for directory in directories:
            dir_path = Path(storage_path) / directory
            dir_path.mkdir(parents=True, exist_ok=True)
            logger.info(f"Dossier créé/vérifié: {dir_path}")

    def _find_available_port(self, start_port: int = 8501) -> int:
        """Trouve un port disponible pour Streamlit"""
        for port in range(start_port, start_port + 100):
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                    s.bind(("localhost", port))
                    logger.info(f"Port disponible trouvé: {port}")
                    return port
            except OSError:
                continue

        raise RuntimeError("Aucun port disponible trouvé pour Streamlit")

    def _setup_streamlit_config(self):
        """Configure Streamlit pour Android"""
        try:
            # Configuration Streamlit pour Android
            streamlit_config = {
                "server.headless": True,
                "server.port": self.server_port,
                "server.address": "localhost",
                "server.enableCORS": False,
                "server.enableXsrfProtection": False,
                "browser.gatherUsageStats": False,
                "logger.level": "info",
                "client.showErrorDetails": True,
                "runner.magicEnabled": False,
                "server.maxUploadSize": 50,  # MB
            }

            # Appliquer la configuration Streamlit
            import streamlit as st

            for key, value in streamlit_config.items():
                st.config.set_option(key, value)

            logger.info("Configuration Streamlit appliquée pour Android")

        except Exception as e:
            logger.error(f"Erreur lors de la configuration Streamlit: {e}")
            raise

    def _initialize_database(self):
        """Initialise la base de données LibraRecipes sur Android"""
        try:
            logger.info("Initialisation de la base de données...")

            # Importer les modules de base de données
            from src.db import get_database_url, create_tables

            # Vérifier la configuration de la base de données
            db_url = get_database_url()
            logger.info(f"URL de base de données: {db_url}")

            # Créer les tables si nécessaire
            create_tables()
            logger.info("Base de données initialisée avec succès")

        except Exception as e:
            logger.error(f"Erreur lors de l'initialisation de la base de données: {e}")
            raise

    def _run_streamlit_server(self):
        """Lance le serveur Streamlit dans un thread séparé"""
        try:
            logger.info(f"Démarrage du serveur Streamlit sur le port {self.server_port}...")

            # Importer et configurer Streamlit
            import streamlit.web.bootstrap

            # Configuration pour le serveur
            server_config = {"headless": True, "port": self.server_port, "host": "localhost"}

            # Démarrer le serveur Streamlit
            # Le point d'entrée principal de l'application
            app_script = os.path.join(os.environ.get("ANDROID_STORAGE", ""), "librarecipes", "app", "Home.py")

            if not os.path.exists(app_script):
                raise FileNotFoundError(f"Script principal non trouvé: {app_script}")

            # Utiliser la méthode bootstrap de Streamlit
            streamlit.web.bootstrap.run(app_script, is_hello=False, args=[], flag_options=server_config)

        except Exception as e:
            logger.error(f"Erreur lors du démarrage du serveur Streamlit: {e}")
            self.is_running = False
            raise

    def start_streamlit_server(self) -> str:
        """
        Démarre le serveur Streamlit et retourne l'URL d'accès

        Returns:
            str: URL du serveur Streamlit (ex: "http://localhost:8501")
        """
        try:
            logger.info("Démarrage du bridge Android-Python...")

            # Trouver un port disponible
            self.server_port = self._find_available_port()

            # Configurer Streamlit
            self._setup_streamlit_config()

            # Initialiser la base de données
            self._initialize_database()

            # Démarrer le serveur dans un thread séparé
            self.server_thread = threading.Thread(target=self._run_streamlit_server, daemon=True)
            self.server_thread.start()

            # Attendre que le serveur soit prêt
            self._wait_for_server_ready()

            self.is_running = True
            server_url = f"http://localhost:{self.server_port}"

            logger.info(f"Serveur Streamlit démarré avec succès: {server_url}")
            return server_url

        except Exception as e:
            logger.error(f"Erreur critique lors du démarrage: {e}")
            raise

    def _wait_for_server_ready(self, timeout: int = 30):
        """Attend que le serveur Streamlit soit prêt à accepter des connexions"""
        start_time = time.time()

        while time.time() - start_time < timeout:
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                    s.settimeout(1)
                    result = s.connect_ex(("localhost", self.server_port))
                    if result == 0:
                        logger.info("Serveur Streamlit prêt")
                        return
            except Exception:
                pass

            time.sleep(0.5)

        raise TimeoutError(f"Le serveur n'a pas démarré dans les {timeout} secondes")

    def stop_server(self):
        """Arrête le serveur Streamlit"""
        try:
            logger.info("Arrêt du serveur Streamlit...")
            self.is_running = False

            if self.streamlit_server:
                self.streamlit_server.stop()

            logger.info("Serveur arrêté")

        except Exception as e:
            logger.error(f"Erreur lors de l'arrêt du serveur: {e}")

    def get_server_status(self) -> Dict[str, Any]:
        """Retourne le statut du serveur"""
        return {
            "is_running": self.is_running,
            "port": self.server_port,
            "url": f"http://localhost:{self.server_port}" if self.server_port else None,
        }


# Instance globale du bridge
_bridge_instance: Optional[AndroidBridge] = None


def get_bridge() -> AndroidBridge:
    """Retourne l'instance singleton du bridge"""
    global _bridge_instance
    if _bridge_instance is None:
        _bridge_instance = AndroidBridge()
    return _bridge_instance


def start_streamlit_server() -> str:
    """
    Point d'entrée principal appelé depuis Android (Kotlin)

    Returns:
        str: URL du serveur Streamlit
    """
    bridge = get_bridge()
    return bridge.start_streamlit_server()


def stop_streamlit_server():
    """Arrête le serveur Streamlit"""
    bridge = get_bridge()
    bridge.stop_server()


def get_server_status() -> Dict[str, Any]:
    """Retourne le statut du serveur"""
    bridge = get_bridge()
    return bridge.get_server_status()


# Fonctions utilitaires pour Android
def check_android_environment() -> bool:
    """Vérifie si l'environnement Android est correctement configuré"""
    try:
        android_storage = os.environ.get("ANDROID_STORAGE")
        if not android_storage:
            return False

        required_paths = [os.path.join(android_storage, "data"), os.path.join(android_storage, "librarecipes")]

        return all(os.path.exists(path) for path in required_paths)

    except Exception:
        return False


def setup_android_logging():
    """Configure le logging pour Android"""
    # Le logging est déjà configuré au début du module
    logger.info("Logging Android configuré")


if __name__ == "__main__":
    # Test du bridge en mode développement
    try:
        logger.info("Test du bridge Android-Python...")
        url = start_streamlit_server()
        logger.info(f"Serveur de test démarré: {url}")

        # Garder le serveur actif pour les tests
        while True:
            time.sleep(1)

    except KeyboardInterrupt:
        logger.info("Arrêt du serveur de test")
        stop_streamlit_server()
    except Exception as e:
        logger.error(f"Erreur lors du test: {e}")
