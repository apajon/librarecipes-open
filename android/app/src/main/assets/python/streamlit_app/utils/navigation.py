"""
Utilitaires de navigation et de redirection entre les pages
"""

import streamlit as st


def navigate_to_recipe_detail(recette_id: str) -> None:
    """Navigate vers la page de détail d'une recette"""
    st.session_state.selected_recette_id = str(recette_id)
    st.query_params.recette_id = str(recette_id)
    if "card_recette_page_obj" in st.session_state:
        st.switch_page(st.session_state.card_recette_page_obj)


def navigate_to_recipe_modify(recette_id: str) -> None:
    """Navigate vers la page de modification d'une recette"""
    st.session_state.selected_recette_id = str(recette_id)
    st.query_params.recette_id = str(recette_id)
    if "modify_recette_page_obj" in st.session_state:
        st.switch_page(st.session_state.modify_recette_page_obj)


def navigate_to_photos_manage(recette_id: str) -> None:
    """Navigate vers la page de gestion des photos"""
    st.session_state.selected_recette_id = str(recette_id)
    st.query_params.recette_id = str(recette_id)
    if "photos_recette_page_obj" in st.session_state:
        st.switch_page(st.session_state.photos_recette_page_obj)


def show_recipe_not_found_help() -> None:
    """Affiche l'aide pour naviguer vers une recette"""
    st.info("💡 **Comment voir une recette :**")
    st.markdown(
        """
    1. Allez dans **'Recherche & Exploration'** pour trouver une recette
    2. Cliquez sur **'👀 Voir'** sur la recette qui vous intéresse
    3. Vous serez automatiquement redirigé vers cette page avec les détails !

    Ou utilisez l'**Index A-Z** pour parcourir toutes vos recettes !
    """
    )
