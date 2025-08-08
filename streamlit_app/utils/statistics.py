"""
Utilitaires pour les statistiques et métriques des recettes
"""

import streamlit as st

from src.db import get_db_session
from src.model import Categorie, Recette, Tag


def get_recipe_statistics():
    """Récupère les statistiques des recettes"""
    with get_db_session() as session:
        total_recettes = session.query(Recette).count()
        total_categories = session.query(Categorie).count()
        total_tags = session.query(Tag).count()

    return {"total_recettes": total_recettes, "total_categories": total_categories, "total_tags": total_tags}


def display_home_statistics():
    """Affiche les statistiques sur la page d'accueil"""
    stats = get_recipe_statistics()

    col1, col2, col3 = st.columns(3)

    with col1:
        st.metric("Recettes totales", stats["total_recettes"], help="Nombre total de recettes dans votre bibliothèque")

    with col2:
        st.metric("Catégories", stats["total_categories"], help="Nombre de catégories différentes")

    with col3:
        st.metric("Tags", stats["total_tags"], help="Nombre de tags utilisés")
