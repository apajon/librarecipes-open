"""
Gestionnaire pour les photos des recettes
"""

import uuid
from pathlib import Path
from typing import Any, Dict

import streamlit as st


class PhotosManager:
    """Gestionnaire pour les photos d'une recette"""

    def __init__(self, recette_id: str):
        self.recette_id = recette_id
        self.photos_dir = Path("data/photos") / recette_id
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
