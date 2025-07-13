import random

import streamlit as st

from src.db import get_db_session
from src.model import Recette


def que_cuisiner_page():
    """Page de suggestions de recettes - Que cuisiner ?"""
    st.title("🎲 Que cuisiner aujourd'hui ?")
    st.markdown("*Laissez-nous vous inspirer avec des suggestions de recettes !*")

    with get_db_session() as session:
        # Récupérer toutes les recettes
        toutes_recettes = session.query(Recette).all()

        if not toutes_recettes:
            st.info("Aucune recette trouvée. Ajoutez d'abord quelques recettes !")
            return

        st.subheader("🎯 Suggestions personnalisées")

        # Filtres de suggestion
        col1, col2, col3 = st.columns(3)

        with col1:
            temps_max = st.selectbox(
                "Temps de préparation maximum",
                [15, 30, 45, 60, 90, 120, float("inf")],
                format_func=lambda x: f"{int(x)} min" if x != float("inf") else "Peu importe",
                index=2,
            )

        with col2:
            type_plat = st.selectbox(
                "Type de plat", ["Tous", "Plat principal", "Entrée", "Dessert", "Apéritif", "Accompagnement"]
            )

        with col3:
            st.selectbox("Difficulté", ["Toutes", "Facile", "Moyen", "Difficile"])

        # Bouton de suggestion
        if st.button("🎲 Suggérer une recette", type="primary", use_container_width=True):
            # Filtrer les recettes selon les critères
            recettes_filtrees = []

            for recette in toutes_recettes:
                # Filtre temps - utiliser les bons attributs du modèle
                temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
                if temps_max != float("inf") and temps_total > temps_max:
                    continue

                # Filtre type (basé sur les catégories)
                if type_plat != "Tous":
                    categories_noms = [c.nom.lower() for c in recette.categories]
                    if type_plat.lower() not in categories_noms:
                        continue

                recettes_filtrees.append(recette)

            if recettes_filtrees:
                # Choisir une recette au hasard
                recette_suggeree = random.choice(recettes_filtrees)

                st.success("🎉 Voici notre suggestion !")

                # Afficher la recette suggérée
                with st.container():
                    st.markdown(f"### {recette_suggeree.nom}")

                    col1, col2, col3 = st.columns(3)
                    with col1:
                        temps_total = (recette_suggeree.preparation or 0) + (recette_suggeree.cuisson or 0)
                        st.metric("Temps total", f"{temps_total} min")
                    with col2:
                        st.metric("Portions", recette_suggeree.portions or 0)
                    with col3:
                        if recette_suggeree.categories:
                            categories_str = ", ".join([c.nom for c in recette_suggeree.categories[:2]])
                            st.metric("Catégorie", categories_str)

                    # Aperçu des ingrédients
                    if recette_suggeree.ingredients:
                        st.markdown("**Ingrédients principaux :**")
                        ingredients_apercu = recette_suggeree.ingredients[:5]
                        for ing in ingredients_apercu:
                            st.write(f"• {ing.nom}")
                        if len(recette_suggeree.ingredients) > 5:
                            st.write(f"• ... et {len(recette_suggeree.ingredients) - 5} autres ingrédients")

                    # Actions
                    st.markdown("---")
                    col1, col2 = st.columns(2)
                    with col1:
                        if st.button("👀 Voir la recette complète", use_container_width=True):
                            # Stocker l'ID de la recette dans session_state ET query_params
                            st.session_state.selected_recette_id = str(recette_suggeree.id)
                            st.query_params.recette_id = str(recette_suggeree.id)
                            # Navigation automatique vers la page de détail
                            if "card_recette_page_obj" in st.session_state:
                                st.switch_page(st.session_state.card_recette_page_obj)

                    with col2:
                        if st.button("🎲 Autre suggestion", use_container_width=True):
                            st.rerun()

            else:
                st.warning("Aucune recette ne correspond à vos critères. Essayez avec des filtres moins stricts !")

        # Section recettes populaires
        st.markdown("---")
        st.subheader("⭐ Recettes populaires")

        # Afficher quelques recettes au hasard
        if len(toutes_recettes) >= 3:
            recettes_populaires = random.sample(toutes_recettes, min(3, len(toutes_recettes)))

            cols = st.columns(3)
            for i, recette in enumerate(recettes_populaires):
                with cols[i]:
                    with st.container():
                        st.markdown(f"**{recette.nom}**")
                        temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
                        st.caption(f"⏱️ {temps_total}min | 👥 {recette.portions or 0} portions")

                        if recette.categories:
                            categories_str = ", ".join([c.nom for c in recette.categories[:2]])
                            st.caption(f"🏷️ {categories_str}")

                        if st.button("👀", key=f"populaire_{recette.id}"):
                            # Stocker l'ID de la recette dans session_state ET query_params
                            st.session_state.selected_recette_id = str(recette.id)
                            st.query_params.recette_id = str(recette.id)
                            # Navigation automatique vers la page de détail
                            if "card_recette_page_obj" in st.session_state:
                                st.switch_page(st.session_state.card_recette_page_obj)

        # Conseils du jour
        st.markdown("---")
        st.subheader("💡 Conseil du chef")

        conseils = [
            "🧂 Goûtez vos plats en cours de cuisson pour ajuster l'assaisonnement.",
            "🔥 Préchauffez toujours votre four avant d'y mettre vos plats.",
            "🥗 Préparez vos légumes à l'avance pour gagner du temps en cuisine.",
            "🍖 Laissez reposer la viande quelques minutes après cuisson.",
            "🧄 Émincez l'ail finement pour libérer plus de saveurs.",
            "🧅 Pour éviter de pleurer en coupant les oignons, mettez-les au frigo avant.",
            "🥄 Une pincée de sucre peut équilibrer l'acidité d'une sauce tomate.",
            "🧈 Sortez le beurre du frigo 30 minutes avant de l'utiliser en pâtisserie.",
        ]

        conseil_du_jour = random.choice(conseils)
        st.info(conseil_du_jour)
