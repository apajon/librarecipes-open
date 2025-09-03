"""Constantes et fonctions utilitaires pour la navigation et les actions communes"""

import streamlit as st

# Constantes pour les clés de session state
SESSION_KEYS = {
    "selected_recette_id": "selected_recette_id",
    "card_recette_page": "card_recette_page_obj",
    "modify_recette_page": "modify_recette_page_obj",
    "photos_recette_page": "photos_recette_page_obj",
}


def navigate_to_detail_page(recette_id: str) -> None:
    """Navigue vers la page de détail avec gestion de session state"""
    st.session_state[SESSION_KEYS["selected_recette_id"]] = recette_id
    if SESSION_KEYS["card_recette_page"] in st.session_state:
        st.switch_page(st.session_state[SESSION_KEYS["card_recette_page"]])


def navigate_to_modify_page(recette_id: str) -> None:
    """Navigue vers la page de modification avec gestion de session state"""
    st.session_state[SESSION_KEYS["selected_recette_id"]] = recette_id
    if SESSION_KEYS["modify_recette_page"] in st.session_state:
        st.switch_page(st.session_state[SESSION_KEYS["modify_recette_page"]])


def set_selected_recipe(recette_id: str) -> None:
    """Met à jour la recette sélectionnée dans session state"""
    st.session_state[SESSION_KEYS["selected_recette_id"]] = recette_id


def clear_selected_recipe() -> None:
    """Efface la recette sélectionnée du session state"""
    if SESSION_KEYS["selected_recette_id"] in st.session_state:
        del st.session_state[SESSION_KEYS["selected_recette_id"]]


def get_selected_recipe_id() -> str | None:
    """Récupère l'ID de la recette sélectionnée"""
    return st.session_state.get(SESSION_KEYS["selected_recette_id"])


def setup_page_config():
    """Configure la page pour l'utilisation mobile/Android"""
    st.set_page_config(page_title="LibraRecipes", page_icon="📚", layout="wide", initial_sidebar_state="collapsed")


def get_mobile_navigation():
    """Retourne la configuration de navigation mobile"""
    return {
        "home": {"title": "Accueil", "icon": "🏠"},
        "add": {"title": "Ajouter", "icon": "➕"},
        "search": {"title": "Rechercher", "icon": "🔍"},
        "cook": {"title": "Que cuisiner", "icon": "👨‍🍳"},
        "index": {"title": "Index A-Z", "icon": "📖"},
    }


def setup_mobile_sidebar():
    """Configure la sidebar pour mobile Android"""
    with st.sidebar:
        st.title("📚 LibraRecipes")
        nav_config = get_mobile_navigation()

        for key, config in nav_config.items():
            if st.button(f"{config['icon']} {config['title']}", key=f"nav_{key}"):
                st.session_state.current_page = key
