from collections import defaultdict

import streamlit as st
from sqlalchemy.orm import joinedload

from src.db import get_db_session
from src.model import Ingredient, Recette
from streamlit_app.utils.recipe_display import format_recette_display_name


def index_ingredients_page():
    """Page d'index des ingrédients par ordre alphabétique"""
    st.title("🥕 Index des ingrédients par ordre alphabétique")
    st.markdown("*Tous les ingrédients classés de A à Z avec leurs recettes*")

    # Ancre pour le haut de page
    st.markdown('<div id="top"></div>', unsafe_allow_html=True)

    with get_db_session() as session:
        # Récupérer tous les ingrédients avec leurs recettes
        ingredients = session.query(Ingredient).join(Recette).all()

        if not ingredients:
            st.info("Aucun ingrédient trouvé. Ajoutez d'abord quelques recettes !")
            return

        # Grouper les ingrédients par nom, puis par initiale
        ingredients_par_nom = defaultdict(list)
        for ingredient in ingredients:
            ingredients_par_nom[ingredient.nom].append(ingredient.recette_id)

        # Grouper par initiale
        par_initiale = defaultdict(list)
        for nom_ingredient in ingredients_par_nom.keys():
            initiale = nom_ingredient[0].upper()
            # Récupérer les recettes pour cet ingrédient
            recette_ids = list(set(ingredients_par_nom[nom_ingredient]))  # supprimer les doublons
            recettes = (
                session.query(Recette)
                .filter(Recette.id.in_(recette_ids))
                .options(
                    joinedload(Recette.source),
                    joinedload(Recette.executions),
                )
                .all()
            )
            par_initiale[initiale].append({"nom": nom_ingredient, "recettes": recettes})

        # Trier par initiale
        par_initiale = dict(sorted(par_initiale.items()))

        # Navigation alphabétique
        st.subheader("Navigation rapide")
        cols = st.columns(13)  # A-M dans la première ligne
        for i, lettre in enumerate(sorted(par_initiale.keys())[:13]):
            with cols[i]:
                if st.button(lettre, key=f"nav_{lettre}"):
                    st.markdown(f'<a href="#{lettre.lower()}">Aller à {lettre}</a>', unsafe_allow_html=True)

        if len(par_initiale) > 13:
            cols2 = st.columns(13)  # N-Z dans la deuxième ligne
            for i, lettre in enumerate(sorted(par_initiale.keys())[13:]):
                with cols2[i]:
                    if st.button(lettre, key=f"nav2_{lettre}"):
                        st.markdown(f'<a href="#{lettre.lower()}">Aller à {lettre}</a>', unsafe_allow_html=True)

        st.divider()

        # Affichage des ingrédients par initiale
        for initiale in sorted(par_initiale.keys()):
            st.markdown(f'<h3 id="{initiale.lower()}">{initiale}</h3>', unsafe_allow_html=True)

            # Afficher les ingrédients de cette initiale
            ingredients_initiale = par_initiale[initiale]
            ingredients_initiale.sort(key=lambda x: x["nom"].lower())

            for ingredient_data in ingredients_initiale:
                nom_ingredient = ingredient_data["nom"]
                recettes = ingredient_data["recettes"]

                st.markdown(f"**🥕 {nom_ingredient}**")
                st.caption(f"Utilisé dans {len(recettes)} recette(s)")

                # Afficher les recettes qui utilisent cet ingrédient
                cols = st.columns(3)
                for i, recette in enumerate(recettes):
                    with cols[i % 3]:
                        with st.container():
                            # Utiliser le nom formaté
                            formatted_name = format_recette_display_name(recette)
                            st.markdown(f"• {formatted_name}")

                            # Bouton pour voir la recette
                            if st.button("👀", key=f"voir_recette_{recette.id}_{nom_ingredient}"):
                                # Stocker l'ID de la recette dans session_state ET query_params
                                st.session_state.selected_recette_id = str(recette.id)
                                st.query_params.recette_id = str(recette.id)
                                # Navigation automatique vers la page de détail
                                if "card_recette_page_obj" in st.session_state:
                                    st.switch_page(st.session_state.card_recette_page_obj)

                st.markdown("---")

        # Bouton retour en haut
        st.markdown("---")
        if st.button("⬆️ Retour en haut"):
            st.markdown('<a href="#top">Retour en haut</a>', unsafe_allow_html=True)

        # Statistiques
        st.markdown("---")
        st.subheader("📈 Statistiques")

        col1, col2 = st.columns(2)
        with col1:
            total_ingredients = len(ingredients_par_nom)
            st.metric("Total ingrédients uniques", total_ingredients)

        with col2:
            # Ingrédients commençant par des lettres différentes
            initiales = set(ing[0].upper() for ing in ingredients_par_nom.keys())
            st.metric("Lettres de l'alphabet utilisées", len(initiales))
