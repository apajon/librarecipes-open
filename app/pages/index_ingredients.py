from collections import defaultdict
from urllib.parse import quote

import streamlit as st

from src.db import get_db_session
from src.model import Recette


def main():
    st.set_page_config(page_title="Index des recettes", page_icon="📚", layout="wide")

    st.title("📚 Index des recettes par ingrédients")
    st.markdown("*Comme dans un beau livre de recettes*")

    # Ancre pour le haut de page
    st.markdown('<div id="top"></div>', unsafe_allow_html=True)

    with get_db_session() as session:
        # Récupérer toutes les recettes avec leurs ingrédients
        recettes = session.query(Recette).all()

        if not recettes:
            st.info("Aucune recette trouvée.")
            return

        # Créer un dictionnaire des ingrédients avec leurs recettes
        ingredients_recettes = defaultdict(list)

        for recette in recettes:
            for ingredient in recette.ingredients:
                # Normaliser le nom de l'ingrédient (première lettre en majuscule)
                nom_ingredient = ingredient.nom.strip().capitalize()
                ingredients_recettes[nom_ingredient].append({"recette": recette, "ingredient": ingredient})

        # Trier les ingrédients par ordre alphabétique
        ingredients_tries = dict(sorted(ingredients_recettes.items()))

        # Grouper par initiale
        par_initiale = defaultdict(list)
        for ingredient_nom in ingredients_tries.keys():
            initiale = ingredient_nom[0].upper()
            if initiale.isalpha():
                par_initiale[initiale].append(ingredient_nom)
            else:
                par_initiale["#"].append(ingredient_nom)

        # Afficher le sommaire des initiales disponibles
        st.subheader("🔍 Navigation rapide")
        initiales_disponibles = sorted(par_initiale.keys())

        # Créer des boutons pour navigation rapide
        cols = st.columns(min(len(initiales_disponibles), 8))  # Max 8 colonnes
        for i, initiale in enumerate(initiales_disponibles):
            col_index = i % 8
            with cols[col_index]:
                # Utiliser markdown pour créer des liens d'ancrage
                st.markdown(f"[**{initiale}**](#{initiale.lower()})")

        st.markdown("---")

        # Afficher l'index complet
        for initiale in initiales_disponibles:
            # Créer une ancre pour la navigation
            st.markdown(f'<div id="{initiale.lower()}"></div>', unsafe_allow_html=True)

            # Afficher la lettre avec un bouton retour en haut
            col_lettre, col_bouton = st.columns([4, 1])
            with col_lettre:
                st.markdown(f"## {initiale}")
            with col_bouton:
                # Lien vers le haut de page
                st.markdown("[⬆️ Haut](#top)")

            ingredients_de_cette_initiale = sorted(par_initiale[initiale])

            for ingredient_nom in ingredients_de_cette_initiale:
                # Afficher le nom de l'ingrédient avec style
                st.markdown(f"### - {ingredient_nom}")

                # Récupérer et trier les recettes pour cet ingrédient
                recettes_ingredient = ingredients_recettes[ingredient_nom]
                recettes_triees = sorted(recettes_ingredient, key=lambda x: x["recette"].nom.lower())

                # Afficher les recettes avec indentation
                for item in recettes_triees:
                    recette = item["recette"]
                    ingredient = item["ingredient"]

                    # Construire l'URL vers la recette
                    url_recette = f"./card_recette?recette_id={quote(str(recette.id))}"

                    # Afficher la recette avec des détails sur l'ingrédient
                    quantite_text = ""
                    if ingredient.quantite:
                        # Gérer le cas où quantite peut être str ou float/int
                        try:
                            if isinstance(ingredient.quantite, str):
                                quantite_num = float(ingredient.quantite)
                            else:
                                quantite_num = ingredient.quantite

                            if quantite_num > 0:
                                unite_text = f" {ingredient.unite}" if ingredient.unite else ""
                                quantite_text = f" *({ingredient.quantite}{unite_text})*"
                        except (ValueError, TypeError):
                            # Si la conversion échoue, afficher quand même la quantité
                            unite_text = f" {ingredient.unite}" if ingredient.unite else ""
                            quantite_text = f" *({ingredient.quantite}{unite_text})*"

                    st.markdown(f"&nbsp;&nbsp;&nbsp;&nbsp;° [{recette.nom}]({url_recette}){quantite_text}")

            st.markdown("")  # Espacement entre les sections

        # Statistiques en bas de page
        st.markdown("---")
        st.subheader("📊 Statistiques")

        col1, col2, col3 = st.columns(3)
        with col1:
            st.metric("Nombre de recettes", len(recettes))
        with col2:
            st.metric("Ingrédients uniques", len(ingredients_tries))
        with col3:
            st.metric("Initiales", len(initiales_disponibles))

        # Lien pour retourner en haut
        st.markdown("---")
        st.markdown("[⬆️ Retour en haut](#top)")


if __name__ == "__main__":
    main()
