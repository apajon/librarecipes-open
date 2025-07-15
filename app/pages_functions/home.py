import os

import streamlit as st
from PIL import Image
from PIL.ImageFile import ImageFile


def home_page():
    """Page d'accueil de LibraRecipes"""
    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_librarecipes.png")
    img: ImageFile = Image.open(image_path)
    st.image(img, use_container_width=True)

    st.title("LibraRecipes 🍽️")

    st.markdown("Bienvenue dans votre bibliothèque de recettes personnelle.")

    st.subheader("📂 Accès rapide")

    st.info("Utilisez le menu de navigation pour explorer vos recettes.")

    # Statistiques rapides
    col1, col2, col3 = st.columns(3)

    with col1:
        st.metric("Recettes totales", "0", help="Nombre total de recettes dans votre bibliothèque")

    with col2:
        st.metric("Catégories", "0", help="Nombre de catégories différentes")

    with col3:
        st.metric("Tags", "0", help="Nombre de tags utilisés")

    st.subheader("🚀 Actions rapides")

    st.info("Utilisez le menu de navigation à gauche pour accéder aux différentes fonctionnalités de LibraRecipes.")

    # Section d'aide
    with st.expander("ℹ️ Comment utiliser LibraRecipes"):
        st.markdown(
            """
        **LibraRecipes** est votre assistant personnel pour gérer vos recettes de cuisine.

        ### 🔍 **Rechercher des recettes**
        - Recherchez par nom, ingrédients, catégories ou tags
        - Filtrez selon vos préférences alimentaires
        - Trouvez rapidement ce que vous voulez cuisiner

        ### ➕ **Ajouter de nouvelles recettes**
        - Saisissez facilement vos recettes personnelles
        - Organisez-les par catégories et tags
        - Ajoutez des photos pour les rendre plus attrayantes

        ### 📖 **Consulter vos recettes**
        - Accédez à l'index alphabétique de toutes vos recettes
        - Consultez les détails complets de chaque recette
        - Modifiez et mettez à jour vos recettes

        ### 🏷️ **Organiser votre collection**
        - Utilisez des catégories (Plat principal, Dessert, Apéritif, etc.)
        - Ajoutez des tags (végétarien, rapide, facile, etc.)
        - Créez votre propre système d'organisation
        """
        )
