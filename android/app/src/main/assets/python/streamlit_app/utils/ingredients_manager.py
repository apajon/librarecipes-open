"""
Gestionnaire pour les ingrédients d'une recette
"""

from typing import Any, Dict, List, Optional

import streamlit as st

from app.utils.ui_components import create_ingredient_form, display_ingredient_list


class IngredientsManager:
    """Gestionnaire pour les ingrédients d'une recette"""

    def __init__(self, session_key: str = "ingredients_list"):
        self.session_key = session_key
        self._ensure_list_exists()

    def _ensure_list_exists(self) -> None:
        """S'assure que la liste d'ingrédients existe dans session state"""
        if self.session_key not in st.session_state:
            st.session_state[self.session_key] = []

    def get_ingredients(self) -> List[Dict[str, Any]]:
        """Récupère la liste des ingrédients"""
        return st.session_state[self.session_key]

    def add_ingredient(
        self,
        nom: str,
        quantite: Optional[float],
        unite: Optional[str],
        indispensable: bool,
        alternatives: Optional[str],
    ) -> bool:
        """Ajoute un ingrédient à la liste"""
        if not nom.strip():
            st.error("Le nom de l'ingrédient est obligatoire")
            return False

        ingredient = {
            "nom": nom.strip(),
            "quantite": quantite,
            "unite": unite,
            "indispensable": indispensable,
            "alternatives": alternatives.strip() if alternatives and alternatives.strip() else None,
        }

        st.session_state[self.session_key].append(ingredient)
        return True

    def remove_ingredient(self, index: int) -> None:
        """Supprime un ingrédient par index"""
        if 0 <= index < len(st.session_state[self.session_key]):
            st.session_state[self.session_key].pop(index)

    def render_form(self, key_prefix: str = "") -> None:
        """Affiche le formulaire d'ajout d'ingrédient"""
        st.subheader("🥘 Ingrédients")

        with st.expander("➕ Ajouter un ingrédient", expanded=len(self.get_ingredients()) == 0):
            nom, quantite, unite, indispensable, alternatives = create_ingredient_form(key_prefix)

            if st.button("Ajouter l'ingrédient", key=f"{key_prefix}add_ingredient"):
                if self.add_ingredient(nom, quantite, unite, indispensable, alternatives):
                    st.rerun()

    def render_list(self, key_prefix: str = "") -> None:
        """Affiche la liste des ingrédients"""
        display_ingredient_list(self.get_ingredients(), key_prefix)

    def render_complete(self, key_prefix: str = "") -> None:
        """Affiche le formulaire et la liste des ingrédients"""
        self.render_form(key_prefix)
        self.render_list(key_prefix)
