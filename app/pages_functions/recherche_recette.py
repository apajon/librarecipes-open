import os

import streamlit as st
from PIL import Image
from PIL.ImageFile import ImageFile

from src.crud.metadata import get_all_categories, get_all_ingredients, get_all_tags
from src.crud.recherche import IngredientsMode, rechercher_recettes
from src.db import SessionLocal


def recherche_recette_page():
    """Page de recherche de recettes"""
    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_recettes.png")
    img: ImageFile = Image.open(image_path)
    st.image(img, use_container_width=True)

    st.title("Recherche de recettes")

    with SessionLocal() as session:
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

        if st.button("Rechercher"):
            recettes = rechercher_recettes(
                session,
                nom=nom,
                ingredients=ingredients,
                ingredients_mode=ingredients_mode,
                tags=tags,
                categories=categories,
            )

            st.subheader(f"{len(recettes)} recette(s) trouvée(s)")

            for recette in recettes:
                with st.container():
                    st.markdown(f"### {recette.nom}")

                    # Informations rapides
                    col1, col2 = st.columns([3, 1])
                    with col1:
                        temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
                        st.caption(f"⏱️ {temps_total}min | 👥 {recette.portions or 0} portions")

                        if recette.categories:
                            categories_str = ", ".join([c.nom for c in recette.categories[:3]])
                            st.caption(f"🏷️ {categories_str}")

                    with col2:
                        if st.button("👀 Voir", key=f"recherche_{recette.id}"):
                            # Stocker l'ID de la recette dans session_state ET query_params
                            st.session_state.selected_recette_id = str(recette.id)
                            st.query_params.recette_id = str(recette.id)
                            # Navigation automatique vers la page de détail
                            if "card_recette_page_obj" in st.session_state:
                                st.switch_page(st.session_state.card_recette_page_obj)
                            else:
                                st.session_state.navigate_to = "detail"
                                st.rerun()

                st.divider()
