import streamlit as st

from streamlit_app.utils.statistics import display_home_statistics

# Import des utilitaires
from streamlit_app.utils.ui_helpers import show_banner


def home_page():
    """Page d'accueil de LibraRecipes"""
    show_banner("banner_librarecipes.png")

    st.title("LibraRecipes 🍽️")

    st.markdown("Bienvenue dans votre bibliothèque de recettes personnelle.")

    st.subheader("📂 Accès rapide")

    st.info("Utilisez le menu de navigation pour explorer vos recettes.")

    # Statistiques rapides
    display_home_statistics()

    st.subheader("🚀 Actions rapides")

    st.info("Utilisez le menu de navigation à gauche pour accéder aux différentes fonctionnalités de LibraRecipes.")

    # Section d'aide
    _show_help_section()


def _show_help_section():
    """Affiche la section d'aide"""
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
