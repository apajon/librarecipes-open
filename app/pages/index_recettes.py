from collections import defaultdict
from urllib.parse import quote

import streamlit as st

from src.db import get_db_session
from src.model import Recette


def main():
    st.set_page_config(page_title="Index des recettes", page_icon="📖", layout="wide")

    st.title("📖 Index des recettes par ordre alphabétique")
    st.markdown("*Toutes vos recettes classées de A à Z*")

    # Ancre pour le haut de page
    st.markdown('<div id="top"></div>', unsafe_allow_html=True)

    with get_db_session() as session:
        # Récupérer toutes les recettes
        recettes = session.query(Recette).all()

        if not recettes:
            st.info("Aucune recette trouvée.")
            return

        # Grouper les recettes par initiale
        par_initiale = defaultdict(list)

        for recette in recettes:
            # Normaliser le nom de la recette (première lettre en majuscule)
            nom_recette = recette.nom.strip()
            initiale = nom_recette[0].upper()

            if initiale.isalpha():
                par_initiale[initiale].append(recette)
            else:
                par_initiale["#"].append(recette)

        # Trier les recettes dans chaque initiale
        for initiale in par_initiale:
            par_initiale[initiale].sort(key=lambda r: r.nom.lower())

        # Afficher le sommaire des initiales disponibles
        st.subheader("🔍 Navigation rapide")
        initiales_disponibles = sorted(par_initiale.keys())

        # Créer des liens pour navigation rapide
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

            # Afficher la lettre avec un lien retour en haut
            col_lettre, col_bouton = st.columns([4, 1])
            with col_lettre:
                st.markdown(f"## {initiale}")
            with col_bouton:
                # Lien vers le haut de page
                st.markdown("[⬆️ Haut](#top)")

            recettes_de_cette_initiale = par_initiale[initiale]

            for recette in recettes_de_cette_initiale:
                # Construire l'URL vers la recette
                url_recette = f"./card_recette?recette_id={quote(str(recette.id))}"

                # Afficher des informations complémentaires sur la recette
                infos_recette = []

                # Temps de préparation et cuisson
                if recette.preparation:
                    infos_recette.append(f"Prép: {recette.preparation}min")
                if recette.cuisson:
                    infos_recette.append(f"Cuisson: {recette.cuisson}min")
                if recette.portions:
                    infos_recette.append(f"{recette.portions} pers.")

                # Catégories
                if recette.categories:
                    categories_noms = [cat.nom for cat in recette.categories]
                    infos_recette.append(f"({', '.join(categories_noms)})")

                infos_text = " • ".join(infos_recette) if infos_recette else ""

                # Afficher la recette avec style livre
                if infos_text:
                    st.markdown(f"• [{recette.nom}]({url_recette}) *{infos_text}*")
                else:
                    st.markdown(f"• [{recette.nom}]({url_recette})")

            st.markdown("")  # Espacement entre les sections

        # Statistiques en bas de page
        st.markdown("---")
        st.subheader("📊 Statistiques")

        col1, col2, col3 = st.columns(3)
        with col1:
            st.metric("Nombre de recettes", len(recettes))
        with col2:
            # Calculer le nombre moyen de recettes par initiale
            moyenne = len(recettes) / len(initiales_disponibles) if initiales_disponibles else 0
            st.metric("Moyenne par lettre", f"{moyenne:.1f}")
        with col3:
            st.metric("Initiales", len(initiales_disponibles))

        # Informations supplémentaires
        st.markdown("---")
        st.subheader("ℹ️ Informations")

        # Répartition par initiale
        with st.expander("📊 Répartition par lettre", expanded=False):
            for initiale in initiales_disponibles:
                nb_recettes = len(par_initiale[initiale])
                pourcentage = (nb_recettes / len(recettes)) * 100
                st.write(f"**{initiale}** : {nb_recettes} recettes ({pourcentage:.1f}%)")

        # Lien pour retourner en haut
        st.markdown("---")
        st.markdown("[⬆️ Retour en haut](#top)")


if __name__ == "__main__":
    main()
