from collections import defaultdict

import streamlit as st
from sqlalchemy.orm import joinedload

from src.db import get_db_session
from src.model import Recette

# Import des utilitaires
from streamlit_app.utils.navigation import navigate_to_recipe_detail
from streamlit_app.utils.recipe_display import format_recette_display_name


def index_recettes_page():
    """Page d'index des recettes par ordre alphabétique"""
    st.title("📖 Index des recettes par ordre alphabétique")
    st.markdown("*Toutes vos recettes classées de A à Z*")

    # Ancre pour le haut de page
    st.markdown('<div id="top"></div>', unsafe_allow_html=True)

    with get_db_session() as session:
        recettes = (
            session.query(Recette)
            .options(
                joinedload(Recette.source),
                joinedload(Recette.executions),
                joinedload(Recette.categories),
            )
            .all()
        )

        if not recettes:
            st.info("Aucune recette trouvée.")
            return

        # Grouper et afficher les recettes
        par_initiale = _group_recipes_by_initial(recettes)
        _render_navigation_buttons(par_initiale)
        st.divider()
        _render_recipes_by_initial(par_initiale)
        _render_back_to_top_button()


def _group_recipes_by_initial(recettes):
    """Groupe les recettes par initiale"""
    par_initiale = defaultdict(list)

    for recette in recettes:
        initiale = recette.nom[0].upper()
        par_initiale[initiale].append(recette)

    # Trier par initiale et trier les recettes dans chaque groupe
    par_initiale = dict(sorted(par_initiale.items()))
    for initiale in par_initiale:
        par_initiale[initiale].sort(key=lambda r: r.nom.lower())

    return par_initiale


def _render_navigation_buttons(par_initiale):
    """Affiche les boutons de navigation alphabétique"""
    st.subheader("Navigation rapide")

    # Première ligne (A-M)
    cols = st.columns(13)
    for i, lettre in enumerate(sorted(par_initiale.keys())[:13]):
        with cols[i]:
            if st.button(lettre, key=f"nav_{lettre}"):
                st.markdown(f'<a href="#{lettre.lower()}">Aller à {lettre}</a>', unsafe_allow_html=True)

    # Deuxième ligne si nécessaire (N-Z)
    if len(par_initiale) > 13:
        cols2 = st.columns(13)
        for i, lettre in enumerate(sorted(par_initiale.keys())[13:]):
            with cols2[i]:
                if st.button(lettre, key=f"nav2_{lettre}"):
                    st.markdown(f'<a href="#{lettre.lower()}">Aller à {lettre}</a>', unsafe_allow_html=True)


def _render_recipes_by_initial(par_initiale):
    """Affiche les recettes groupées par initiale"""
    for initiale in sorted(par_initiale.keys()):
        st.markdown(f'<h3 id="{initiale.lower()}">{initiale}</h3>', unsafe_allow_html=True)

        recettes_initiale = par_initiale[initiale]
        _render_recipe_grid(recettes_initiale)


def _render_recipe_grid(recettes):
    """Affiche une grille de recettes"""
    cols = st.columns(3)
    for i, recette in enumerate(recettes):
        with cols[i % 3]:
            _render_recipe_card(recette)


def _render_recipe_card(recette):
    """Affiche une carte de recette"""
    with st.container():
        # Utiliser le nom formaté
        formatted_name = format_recette_display_name(recette)
        st.markdown(f"**{formatted_name}**")

        # Informations rapides
        temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
        st.caption(f"⏱️ {temps_total}min | 👥 {recette.portions or 0} portions")

        # Catégories
        if recette.categories:
            categories_str = ", ".join([c.nom for c in recette.categories[:2]])
            if len(recette.categories) > 2:
                categories_str += f" +{len(recette.categories)-2}"  # noqa: E226
            st.caption(f"🏷️ {categories_str}")

        # Bouton vers la recette
        if st.button("👀 Voir", key=f"voir_index_{recette.id}"):
            navigate_to_recipe_detail(str(recette.id))


def _render_back_to_top_button():
    """Affiche le bouton retour en haut"""
    st.markdown("---")
    if st.button("⬆️ Retour en haut"):
        st.markdown('<a href="#top">Retour en haut</a>', unsafe_allow_html=True)
