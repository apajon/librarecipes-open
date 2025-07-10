import os
from urllib.parse import quote

import streamlit as st
from PIL import Image
from PIL.ImageFile import ImageFile

from src.crud.metadata import get_all_categories, get_all_ingredients, get_all_tags
from src.crud.recherche import IngredientsMode, rechercher_recettes
from src.db import SessionLocal


def main():
    st.set_page_config(page_title="Recherche de recettes", page_icon="🍽️", layout="wide")

    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_recettes.png")
    img: ImageFile = Image.open(image_path)
    st.image(img, use_container_width=True)

    st.sidebar.title("Navigation")
    st.sidebar.markdown("Bienvenue dans l'application de recherche de recettes !")

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
                st.markdown(f"### {recette.nom}")
                st.markdown(f"[➡️ Voir la recette détaillée](./card_recette?recette_id={quote(str(recette.id))})")

                recette_id = str(recette.id)
                url = f"?recette_id={recette_id}"
                st.markdown(f"[Voir la recette détaillée](./card_recette{url})")


if __name__ == "__main__":
    main()
    # Pour exécuter l'application, utilisez la commande suivante dans le terminal :
    # PYTHONPATH=. streamlit run app/main.py
