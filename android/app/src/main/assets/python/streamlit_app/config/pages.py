"""Configuration des pages de l'application LibraRecipes"""

import streamlit as st
from app.pages_functions.add_recette import add_recette_page
from app.pages_functions.card_recette import card_recette_page
from app.pages_functions.home import home_page
from app.pages_functions.index_ingredients import index_ingredients_page
from app.pages_functions.index_recettes import index_recettes_page
from app.pages_functions.modify_recette import modify_recette_page
from app.pages_functions.photos_recette import photos_recette_page
from app.pages_functions.que_cuisiner import que_cuisiner_page
from app.pages_functions.recherche_recette import recherche_recette_page


def create_page_objects():
    """Crée les objets de pages pour la navigation"""
    card_recette_page_obj = st.Page(card_recette_page, title="Détail", icon="📄")
    modify_recette_page_obj = st.Page(modify_recette_page, title="Modifier", icon="✏️")
    photos_recette_page_obj = st.Page(photos_recette_page, title="Photos", icon="📷")

    return {
        "card_recette": card_recette_page_obj,
        "modify_recette": modify_recette_page_obj,
        "photos_recette": photos_recette_page_obj,
    }


def get_pages_config(page_objects):
    """Retourne la configuration complète des pages"""
    return {
        "🏠 Accueil": [
            st.Page(home_page, title="Accueil", icon="🏠", default=True),
        ],
        "🔍 Recherche & Exploration": [
            st.Page(recherche_recette_page, title="Rechercher", icon="🔍"),
            st.Page(index_recettes_page, title="Index A-Z", icon="📖"),
            st.Page(index_ingredients_page, title="Ingrédients", icon="🥕"),
        ],
        "🎲 Que cuisiner ?": [
            st.Page(que_cuisiner_page, title="Que cuisiner ?", icon="🎲"),
        ],
        "➕ Ajouter recette": [
            st.Page(add_recette_page, title="Ajouter recette", icon="➕"),
        ],
        "🔧 Gestion des recettes": [
            page_objects["card_recette"],
            page_objects["modify_recette"],
            page_objects["photos_recette"],
        ],
    }


def setup_session_state_pages(page_objects):
    """Configure les objets de pages dans session_state"""
    for key, page_obj in page_objects.items():
        session_key = f"{key}_page_obj"
        if session_key not in st.session_state:
            st.session_state[session_key] = page_obj
