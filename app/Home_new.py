import streamlit as st
from pages_functions.add_recette import add_recette_page
from pages_functions.card_recette import card_recette_page

# Import des fonctions de pages
from pages_functions.home import home_page
from pages_functions.index_recettes import index_recettes_page
from pages_functions.recherche_recette import recherche_recette_page

# Configuration de la page
st.set_page_config(page_title="LibraRecipes", page_icon="🍲", layout="wide", initial_sidebar_state="expanded")

# Définition des pages avec st.Page
pages = {
    "🏠 Accueil": [
        st.Page(home_page, title="Accueil", icon="🏠", default=True),
    ],
    "🔍 Recherche": [
        st.Page(recherche_recette_page, title="Rechercher", icon="🔍"),
        st.Page(index_recettes_page, title="Index A-Z", icon="📖"),
    ],
    "➕ Gestion": [
        st.Page(add_recette_page, title="Ajouter", icon="➕"),
        st.Page(card_recette_page, title="Détail", icon="📄"),
    ],
}

# Configuration de la navigation
pg = st.navigation(pages)

# Exécution de la page sélectionnée
pg.run()
