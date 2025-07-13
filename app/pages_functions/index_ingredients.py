import streamlit as st

from src.crud.metadata import get_all_ingredients
from src.db import get_db_session


def index_ingredients_page():
    """Page d'index des ingrédients"""
    st.title("🥕 Index des ingrédients")
    st.markdown("*Tous les ingrédients utilisés dans vos recettes*")

    with get_db_session() as session:
        ingredients = get_all_ingredients(session)

        if not ingredients:
            st.info("Aucun ingrédient trouvé. Ajoutez d'abord quelques recettes !")
            return

        st.subheader(f"📊 {len(ingredients)} ingrédients répertoriés")

        # Recherche d'ingrédient
        recherche = st.text_input("🔍 Rechercher un ingrédient")

        # Filtrer selon la recherche
        if recherche:
            ingredients_filtres = [ing for ing in ingredients if recherche.lower() in ing.lower()]
        else:
            ingredients_filtres = ingredients

        if not ingredients_filtres:
            st.warning("Aucun ingrédient trouvé pour cette recherche.")
            return

        # Affichage en colonnes
        st.markdown(f"**{len(ingredients_filtres)} ingrédient(s) trouvé(s) :**")

        # Diviser en colonnes de 4
        cols = st.columns(4)
        for i, ingredient in enumerate(sorted(ingredients_filtres)):
            with cols[i % 4]:
                st.write(f"• {ingredient}")

        # Statistiques
        st.markdown("---")
        st.subheader("📈 Statistiques")

        col1, col2 = st.columns(2)
        with col1:
            st.metric("Total ingrédients", len(ingredients))

        with col2:
            # Ingrédients commençant par des lettres différentes
            initiales = set(ing[0].upper() for ing in ingredients)
            st.metric("Lettres de l'alphabet utilisées", len(initiales))
