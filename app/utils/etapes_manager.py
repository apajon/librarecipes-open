"""
Gestionnaire pour les étapes d'une recette
"""

from typing import List

import streamlit as st

from app.utils.ui_components import create_etape_form, display_etapes_list


class EtapesManager:
    """Gestionnaire pour les étapes d'une recette"""

    def __init__(self, session_key: str = "etapes_list"):
        self.session_key = session_key
        self._ensure_list_exists()

    def _ensure_list_exists(self) -> None:
        """S'assure que la liste d'étapes existe dans session state"""
        if self.session_key not in st.session_state:
            st.session_state[self.session_key] = []

    def get_etapes(self) -> List[str]:
        """Récupère la liste des étapes"""
        return st.session_state[self.session_key]

    def add_etape(self, description: str) -> bool:
        """Ajoute une étape à la liste"""
        if not description.strip():
            st.error("La description de l'étape est obligatoire")
            return False

        st.session_state[self.session_key].append(description.strip())
        return True

    def remove_etape(self, index: int) -> None:
        """Supprime une étape par index"""
        if 0 <= index < len(st.session_state[self.session_key]):
            st.session_state[self.session_key].pop(index)

    def move_etape_up(self, index: int) -> None:
        """Déplace une étape vers le haut"""
        if index > 0:
            etapes = st.session_state[self.session_key]
            etapes[index], etapes[index - 1] = etapes[index - 1], etapes[index]

    def move_etape_down(self, index: int) -> None:
        """Déplace une étape vers le bas"""
        etapes = st.session_state[self.session_key]
        if index < len(etapes) - 1:
            etapes[index], etapes[index + 1] = etapes[index + 1], etapes[index]

    def render_form(self, key_prefix: str = "") -> None:
        """Affiche le formulaire d'ajout d'étape"""
        st.subheader("📝 Étapes de préparation")

        with st.expander("➕ Ajouter une étape", expanded=len(self.get_etapes()) == 0):
            description = create_etape_form(key_prefix)

            if st.button("Ajouter l'étape", key=f"{key_prefix}add_etape"):
                if self.add_etape(description):
                    st.rerun()

    def render_list(self, key_prefix: str = "", allow_reorder: bool = False) -> None:
        """Affiche la liste des étapes"""
        display_etapes_list(self.get_etapes(), key_prefix, allow_reorder)

    def render_complete(self, key_prefix: str = "", allow_reorder: bool = False) -> None:
        """Affiche le formulaire et la liste des étapes"""
        self.render_form(key_prefix)
        self.render_list(key_prefix, allow_reorder)
