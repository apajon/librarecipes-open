"""
Gestionnaire pour les photos des recettes
Support Android avec configuration adaptative
"""

import uuid
from pathlib import Path
from typing import Any, Dict

import streamlit as st


def get_android_config():
    """
    Configuration Android adaptative avec fallback pour développement.

    Returns:
        dict: Configuration Android ou par défaut
    """
    try:
        # Essayer d'importer la configuration Android si disponible
        import sys

        sys.path.insert(0, str(Path(__file__).parent.parent.parent))
        from config.android_config import get_android_config as _get_android_config

        return _get_android_config()
    except ImportError:
        # Configuration par défaut pour développement
        return {
            "photos_path": "./data/photos",
            "is_android": False,
            "android_data_path": "/data/data/com.librarecipes/files",
        }


class PhotosManager:
    """Gestionnaire pour les photos d'une recette"""

    def __init__(self, recette_id: str):
        self.recette_id = recette_id

        # Utiliser la configuration Android pour le répertoire photos
        config = get_android_config()
        self.photos_base_dir = config.get_photos_directory()

        # Créer le répertoire spécifique à la recette
        self.photos_dir = self.photos_base_dir / recette_id
        self.photos_dir.mkdir(parents=True, exist_ok=True)

    def save_uploaded_photo(self, uploaded_file, categorie: str = "final") -> Dict[str, Any]:
        """Sauvegarde un fichier uploadé et retourne les métadonnées"""
        if not uploaded_file:
            return {}

        # Générer un nom unique pour le fichier
        file_extension = uploaded_file.name.split(".")[-1]
        unique_filename = f"{uuid.uuid4()}.{file_extension}"
        file_path = self.photos_dir / unique_filename

        # Sauvegarder le fichier
        with open(file_path, "wb") as f:
            f.write(uploaded_file.getbuffer())

        return {"chemin": str(file_path), "categorie": categorie, "nom_original": uploaded_file.name}

    def get_upload_session_key(self) -> str:
        """Retourne la clé de session pour les uploads"""
        return f"uploaded_file_{self.recette_id}"

    def is_file_already_uploaded(self, filename: str) -> bool:
        """Vérifie si un fichier a déjà été uploadé dans cette session"""
        uploaded_files = st.session_state.get(self.get_upload_session_key(), set())
        return filename in uploaded_files

    def mark_file_as_uploaded(self, filename: str) -> None:
        """Marque un fichier comme uploadé dans cette session"""
        session_key = self.get_upload_session_key()
        if session_key not in st.session_state:
            st.session_state[session_key] = set()
        st.session_state[session_key].add(filename)

    def delete_photo_file(self, file_path: str) -> bool:
        """Supprime un fichier photo du disque"""
        try:
            path = Path(file_path)
            if path.exists():
                path.unlink()
                return True
        except Exception:
            pass
        return False

    def render_photo_upload_form(self) -> tuple:
        """Affiche le formulaire d'upload de photo et retourne (fichier, catégorie)"""
        upload_key = f"photo_upload_{self.recette_id}"
        uploaded = st.file_uploader("Photo", type=["png", "jpg", "jpeg"], key=upload_key)
        categorie = st.selectbox("Catégorie", ["final", "cuisson", "ingrédient", "préparation", "autre"])

        return uploaded, categorie

    def get_photos_directory_info(self) -> Dict[str, Any]:
        """Retourne les informations sur le répertoire photos pour débogage"""
        config = get_android_config()
        return {
            "recette_id": self.recette_id,
            "photos_base_dir": str(self.photos_base_dir),
            "photos_dir": str(self.photos_dir),
            "photos_dir_exists": self.photos_dir.exists(),
            "is_android": config.is_android,
            "android_storage_root": str(config.storage_root),
        }

    def test_photo_storage(self) -> Dict[str, Any]:
        """Teste les capacités de stockage pour Android"""
        test_results = {
            "directory_creation": False,
            "file_write": False,
            "file_read": False,
            "file_delete": False,
            "error": None,
        }

        try:
            # Test 1: Création de répertoire
            test_dir = self.photos_dir / "test"
            test_dir.mkdir(parents=True, exist_ok=True)
            test_results["directory_creation"] = test_dir.exists()

            # Test 2: Écriture de fichier
            test_file = test_dir / "test_photo.txt"
            test_content = "Test photo storage for Android"
            test_file.write_text(test_content)
            test_results["file_write"] = test_file.exists()

            # Test 3: Lecture de fichier
            read_content = test_file.read_text()
            test_results["file_read"] = read_content == test_content

            # Test 4: Suppression de fichier
            test_file.unlink()
            test_dir.rmdir()
            test_results["file_delete"] = not test_file.exists()

        except Exception as e:
            test_results["error"] = str(e)

        return test_results

    def migrate_photos_to_android(self, old_photos_dir: Path) -> Dict[str, Any]:
        """Migre les photos existantes vers le nouveau stockage Android"""
        migration_results = {
            "total_photos": 0,
            "migrated_photos": 0,
            "failed_photos": 0,
            "errors": [],
        }

        if not old_photos_dir.exists():
            return migration_results

        try:
            for photo_file in old_photos_dir.rglob("*"):
                if photo_file.is_file() and photo_file.suffix.lower() in [".jpg", ".jpeg", ".png"]:
                    migration_results["total_photos"] += 1

                    try:
                        # Déterminer le chemin de destination
                        relative_path = photo_file.relative_to(old_photos_dir)
                        new_path = self.photos_dir / relative_path

                        # Créer le répertoire de destination
                        new_path.parent.mkdir(parents=True, exist_ok=True)

                        # Copier le fichier
                        new_path.write_bytes(photo_file.read_bytes())

                        migration_results["migrated_photos"] += 1

                    except Exception as e:
                        migration_results["failed_photos"] += 1
                        migration_results["errors"].append(f"{photo_file}: {str(e)}")

        except Exception as e:
            migration_results["errors"].append(f"Erreur globale: {str(e)}")

        return migration_results
