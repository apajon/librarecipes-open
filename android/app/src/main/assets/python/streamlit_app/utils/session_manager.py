"""
Utilitaires pour l'initialisation et la gestion des états de session Streamlit
"""

from typing import Any

import streamlit as st


def init_session_key(key: str, default_value: Any) -> None:
    """Initialise une clé de session si elle n'existe pas"""
    if key not in st.session_state:
        st.session_state[key] = default_value


def init_recette_session_state() -> None:
    """Initialise les variables de session pour l'ajout de recette"""
    init_session_key("ingredients_list", [])
    init_session_key("etapes_list", [])
    init_session_key("photos_list", [])
    init_session_key("recette_nom", "")
    init_session_key("recette_preparation", 15)
    init_session_key("recette_cuisson", 0)
    init_session_key("recette_portions", 4)


def init_modify_session_state(recette) -> None:
    """Initialise les variables de session pour la modification de recette"""
    # Informations générales
    st.session_state.modify_recette_id = recette.id
    st.session_state.modify_recette_nom = recette.nom
    st.session_state.modify_recette_preparation = recette.preparation or 15
    st.session_state.modify_recette_cuisson = recette.cuisson or 0
    st.session_state.modify_recette_portions = recette.portions or 4

    # Catégories et tags
    st.session_state.modify_recette_categories = [c.nom for c in recette.categories]
    st.session_state.modify_recette_tags = [t.nom for t in recette.tags]

    # Source
    _init_source_session_state(recette)
    _init_ingredients_session_state(recette)
    _init_etapes_session_state(recette)

    # Photos
    init_session_key("modify_photos_list", [])


def _init_source_session_state(recette) -> None:
    """Initialise les données de source dans session state"""
    if recette.source:
        source_data = {"type": recette.source.type}
        if recette.source.url:
            source_data["url"] = recette.source.url
        if recette.source.book_title:
            source_data["book_title"] = recette.source.book_title
        if recette.source.book_authors:
            source_data["book_authors"] = recette.source.book_authors
        if recette.source.book_page:
            source_data["book_page"] = recette.source.book_page
        st.session_state.modify_recette_source = source_data
    else:
        st.session_state.modify_recette_source = {"type": "homemade"}


def _init_ingredients_session_state(recette) -> None:
    """Initialise les ingrédients dans session state"""
    st.session_state.modify_ingredients_list = []
    for ing in recette.ingredients:
        ingredient = {
            "nom": ing.nom,
            "quantite": ing.quantite,
            "unite": ing.unite,
            "indispensable": ing.indispensable,
            "alternatives": ing.alternatives,
        }
        st.session_state.modify_ingredients_list.append(ingredient)


def _init_etapes_session_state(recette) -> None:
    """Initialise les étapes dans session state"""
    st.session_state.modify_etapes_list = []
    for etape in sorted(recette.etapes, key=lambda e: e.ordre):
        st.session_state.modify_etapes_list.append(etape.description)


def clear_recette_session_state() -> None:
    """Nettoie les variables de session liées aux recettes"""
    keys_to_remove = [
        key
        for key in st.session_state.keys()
        if key.startswith(("ingredients_list", "etapes_list", "photos_list", "recette_"))
    ]
    for key in keys_to_remove:
        del st.session_state[key]


def get_recette_id_from_state() -> str:
    """Récupère l'ID de recette depuis session_state ou query_params"""
    return st.session_state.get("selected_recette_id") or st.query_params.get("recette_id")


def set_selected_recette(recette_id: str) -> None:
    """Définit la recette sélectionnée dans session state et query params"""
    st.session_state.selected_recette_id = str(recette_id)
    st.query_params.recette_id = str(recette_id)
