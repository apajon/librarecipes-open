"""Module pour l'affichage du résumé des recettes dans la sidebar"""

import streamlit as st

from streamlit_app.utils.constants import BUTTON_LABELS, ICONS, MESSAGES
from streamlit_app.utils.navigation_helpers import navigate_to_detail_page, navigate_to_modify_page


def _display_basic_info(recette):
    """Affiche les informations de base de la recette"""
    col1, col2 = st.sidebar.columns(2)
    with col1:
        if recette.preparation:
            st.write(f"{ICONS['time_prep']} Préparation: {recette.preparation}min")
        if recette.cuisson:
            st.write(f"{ICONS['time_cook']} Cuisson: {recette.cuisson}min")
    with col2:
        if recette.portions:
            st.write(f"{ICONS['portions']} Portions: {recette.portions}")


def _display_total_time(recette):
    """Affiche le temps total si disponible"""
    if recette.preparation and recette.cuisson:
        total_time = recette.preparation + recette.cuisson
        st.sidebar.write(f"{ICONS['time_total']} **Temps total: {total_time}min**")


def _display_categories_and_tags(recette):
    """Affiche les catégories et tags"""
    if recette.categories:
        categories = [cat.nom for cat in recette.categories]
        st.sidebar.write(f"{ICONS['categories']} **Catégories:** {', '.join(categories)}")

    if recette.tags:
        tags = [tag.nom for tag in recette.tags]
        st.sidebar.write(f"{ICONS['tags']} **Tags:** {', '.join(tags)}")


def _display_source_info(recette):
    """Affiche les informations de source"""
    if not recette.source:
        return

    if recette.source.type == "url" and recette.source.url:
        st.sidebar.write(f"{ICONS['source_web']} **Source:** [Lien]({recette.source.url})")
    elif recette.source.type == "book":
        book_info = recette.source.book_title
        if recette.source.book_authors:
            book_info += f" - {recette.source.book_authors}"
        if recette.source.book_page:
            book_info += f" (p. {recette.source.book_page})"
        st.sidebar.write(f"{ICONS['source_book']} **Livre:** {book_info}")
    elif recette.source.type == "homemade":
        st.sidebar.write(f"{ICONS['source_home']} **Recette maison**")


def _display_execution_stats(recette):
    """Affiche les statistiques d'exécution"""
    if recette.executions:
        nb_executions = len(recette.executions)
        last_execution = max(recette.executions, key=lambda x: x.date_execution)
        last_date = last_execution.date_execution.strftime("%d/%m/%Y")
        st.sidebar.write(f"{ICONS['executions']} **Exécutions:** {nb_executions} fois")
        st.sidebar.write(f"{ICONS['last_execution']} **Dernière fois:** {last_date}")


def _display_action_buttons(recette):
    """Affiche les boutons d'action"""
    st.sidebar.markdown("---")
    col1, col2 = st.sidebar.columns(2)
    with col1:
        if st.button(BUTTON_LABELS["view_detail"], key=f"voir_sidebar_{recette.id}"):
            navigate_to_detail_page(recette.id)
    with col2:
        if st.button(BUTTON_LABELS["modify"], key=f"modifier_sidebar_{recette.id}"):
            navigate_to_modify_page(recette.id)


def display_recette_summary(recette):
    """Affiche le résumé complet de la recette sélectionnée"""
    st.sidebar.header(MESSAGES["recipe_summary_title"])
    st.sidebar.subheader(recette.nom)

    _display_basic_info(recette)
    _display_total_time(recette)
    _display_categories_and_tags(recette)
    _display_source_info(recette)
    _display_execution_stats(recette)
    _display_action_buttons(recette)
