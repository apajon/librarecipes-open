"""Module pour le sélecteur de recettes dans la sidebar"""

import streamlit as st

from app.utils.constants import MESSAGES
from app.utils.navigation_helpers import clear_selected_recipe, get_selected_recipe_id, set_selected_recipe
from app.utils.recipe_display import format_recette_display_name
from app.utils.sidebar_summary import display_recette_summary
from src.crud.recettes import get_recette_by_id, list_recettes
from src.db import get_db_session


def _create_recipe_options(recettes):
    """Crée le dictionnaire des options pour le selectbox"""
    recette_options = {}
    for recette in recettes:
        display_name = format_recette_display_name(recette)
        recette_options[display_name] = recette.id
    return recette_options


def _find_selected_index(recette_options):
    """Trouve l'index de la recette sélectionnée"""
    selected_id = get_selected_recipe_id()
    if not selected_id:
        return None

    for i, (display_name, recette_id) in enumerate(recette_options.items()):
        if recette_id == selected_id:
            return i
    return None


def _create_selectbox(recette_options, selected_index):
    """Crée le selectbox avec gestion de l'index"""
    return st.selectbox(
        "Choisir une recette:",
        options=[None] + list(recette_options.keys()),
        index=selected_index + 1 if selected_index is not None else 0,
        format_func=lambda x: MESSAGES["select_recipe"] if x is None else x,
        help=MESSAGES["select_recipe_help"],
    )


def _handle_recipe_selection(selected_display_name, recette_options, session):
    """Gère la sélection de recette"""
    if selected_display_name and selected_display_name in recette_options:
        selected_recette_id = recette_options[selected_display_name]
        selected_recette = get_recette_by_id(session, selected_recette_id)

        if selected_recette:
            set_selected_recipe(selected_recette_id)
            display_recette_summary(selected_recette)
    elif selected_display_name is None:
        clear_selected_recipe()


def render_recipe_selector():
    """Affiche le sélecteur de recettes dans la sidebar"""
    st.header(MESSAGES["recipe_selector_title"])

    try:
        with get_db_session() as session:
            recettes = list_recettes(session)

            if recettes:
                recette_options = _create_recipe_options(recettes)
                selected_index = _find_selected_index(recette_options)
                selected_display_name = _create_selectbox(recette_options, selected_index)
                _handle_recipe_selection(selected_display_name, recette_options, session)
            else:
                st.info(MESSAGES["no_recipes"])

    except Exception as e:
        st.error(MESSAGES["loading_error"].format(error=str(e)))
