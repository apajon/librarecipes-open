import os

import streamlit as st
from PIL import Image
from PIL.ImageFile import ImageFile


def main():
    st.set_page_config(page_title="LibraRecipes", page_icon="🍲", layout="wide")

    image_path = os.path.join(os.path.dirname(__file__), "assets", "banner_librarecipes.png")
    img: ImageFile = Image.open(image_path)
    st.image(img, use_container_width=True)

    st.title("LibraRecipes 🍽️")

    st.markdown("Bienvenue dans votre bibliothèque de recettes personnelle.")

    st.subheader("📂 Accès rapide")

    st.info("Utilisez le menu latéral ou les liens ci-dessus pour naviguer.")

    st.page_link("pages/recherche_recette.py", label="🔍 Rechercher une recette")
    st.page_link("pages/add_recette.py", label="➕ Ajouter une recette")

    # st.markdown(
    #     """
    #     ## 📖 À propos de LibraRecipes
    #     LibraRecipes est une application pour gérer vos recettes de cuisine.
    #     Vous pouvez rechercher des recettes, en ajouter de nouvelles et les organiser par catégories.

    #     ### 🚀 Fonctionnalités à venir
    #     - Ajout de recettes avec photos
    #     - Gestion des ingrédients et des étapes
    #     - Recherche avancée par tags et catégories
    #     """
    # )


if __name__ == "__main__":
    main()
