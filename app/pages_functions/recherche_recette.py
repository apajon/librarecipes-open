import streamlit as st

from app.utils.navigation import navigate_to_recipe_detail

# Import des utilitaires
from app.utils.ui_helpers import show_banner
from src.crud.metadata import get_all_categories, get_all_ingredients, get_all_tags
from src.crud.recherche import IngredientsMode, rechercher_recettes
from src.db import get_db_session


def recherche_recette_page():
    """Page de recherche de recettes"""
    show_banner("banner_recettes.png")

    st.title("Recherche de recettes")

    # Formulaires de recherche
    search_params = _render_search_forms()

    # Bouton de recherche et gestion des résultats
    _handle_search_and_results(search_params)


def _render_search_forms():
    """Affiche les formulaires de recherche et retourne les paramètres"""
    with get_db_session() as session:
        nom = st.text_input("Nom de la recette")

        all_ingredients = get_all_ingredients(session)
        ingredients = st.multiselect("Ingrédients", all_ingredients)

        ingredients_mode = st.radio(
            "Mode de recherche des ingrédients",
            options=[IngredientsMode.ANY, IngredientsMode.ALL],
            format_func=lambda x: "Au moins un" if x == IngredientsMode.ANY else "Tous requis",
        )

        all_tags = get_all_tags(session)
        tags = st.multiselect("Tags", all_tags)

        all_categories = get_all_categories(session)
        categories = st.multiselect("Catégories", all_categories)

    return {
        "nom": nom,
        "ingredients": ingredients,
        "ingredients_mode": ingredients_mode,
        "tags": tags,
        "categories": categories,
    }


def _handle_search_and_results(search_params):
    """Gère la recherche et l'affichage des résultats"""
    if st.button("Rechercher"):
        with get_db_session() as session:
            recettes = rechercher_recettes(session, **search_params)
            st.session_state.search_results = recettes

    # Affichage des résultats stockés
    if "search_results" in st.session_state:
        _display_search_results(st.session_state.search_results)


def _display_search_results(recettes):
    """Affiche les résultats de recherche"""
    st.subheader(f"{len(recettes)} recette(s) trouvée(s)")

    for recette in recettes:
        _display_recipe_card(recette)


def _display_recipe_card(recette):
    """Affiche une carte de recette dans les résultats"""
    with st.container():
        st.markdown(f"### {recette.nom}")

        # Informations rapides
        temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
        st.caption(f"⏱️ {temps_total}min | 👥 {recette.portions or 0} portions")

        if recette.categories:
            categories_str = ", ".join([c.nom for c in recette.categories[:3]])
            st.caption(f"🏷️ {categories_str}")

        # Bouton vers la recette
        if st.button("👀 Voir", key=f"recherche_{recette.id}"):
            navigate_to_recipe_detail(str(recette.id))

    st.divider()
