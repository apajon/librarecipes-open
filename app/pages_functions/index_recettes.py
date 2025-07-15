from collections import defaultdict

import streamlit as st

from src.db import get_db_session
from src.model import Recette


def index_recettes_page():
    """Page d'index des recettes par ordre alphabétique"""
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
            initiale = recette.nom[0].upper()
            par_initiale[initiale].append(recette)

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

        # Affichage des recettes par initiale
        for initiale in sorted(par_initiale.keys()):
            st.markdown(f'<h3 id="{initiale.lower()}">{initiale}</h3>', unsafe_allow_html=True)

            # Afficher les recettes de cette initiale
            recettes_initiale = par_initiale[initiale]
            recettes_initiale.sort(key=lambda r: r.nom.lower())

            cols = st.columns(3)
            for i, recette in enumerate(recettes_initiale):
                with cols[i % 3]:
                    with st.container():
                        st.markdown(f"**{recette.nom}**")

                        # Informations rapides
                        temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
                        st.caption(f"⏱️ {temps_total}min | 👥 {recette.portions or 0} portions")

                        # Catégories
                        if recette.categories:
                            categories_str = ", ".join([c.nom for c in recette.categories[:2]])
                            if len(recette.categories) > 2:
                                categories_str += f" +{len(recette.categories)-2}"  # noqa E226
                            st.caption(f"🏷️ {categories_str}")

                        # Bouton vers la recette
                        if st.button("👀 Voir", key=f"voir_{recette.id}"):
                            # Stocker l'ID de la recette dans session_state ET query_params
                            st.session_state.selected_recette_id = str(recette.id)
                            st.query_params.recette_id = str(recette.id)
                            # Navigation automatique vers la page de détail
                            if "card_recette_page_obj" in st.session_state:
                                st.switch_page(st.session_state.card_recette_page_obj)

        # Bouton retour en haut
        st.markdown("---")
        if st.button("⬆️ Retour en haut"):
            st.markdown('<a href="#top">Retour en haut</a>', unsafe_allow_html=True)
