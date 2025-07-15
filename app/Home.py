import streamlit as st
from pages_functions.add_recette import add_recette_page
from pages_functions.card_recette import card_recette_page

# Import des fonctions de pages
from pages_functions.home import home_page
from pages_functions.index_ingredients import index_ingredients_page
from pages_functions.index_recettes import index_recettes_page
from pages_functions.modify_recette import modify_recette_page
from pages_functions.photos_recette import photos_recette_page
from pages_functions.que_cuisiner import que_cuisiner_page
from pages_functions.recherche_recette import recherche_recette_page

# Configuration de la page
st.set_page_config(page_title="LibraRecipes", page_icon="🍲", layout="wide", initial_sidebar_state="expanded")

# Définition des pages avec st.Page
card_recette_page_obj = st.Page(card_recette_page, title="Détail", icon="📄")
modify_recette_page_obj = st.Page(modify_recette_page, title="Modifier", icon="✏️")
photos_recette_page_obj = st.Page(photos_recette_page, title="Photos", icon="📷")

pages = {
    "🏠 Accueil": [
        st.Page(home_page, title="Accueil", icon="🏠", default=True),
    ],
    "🔍 Recherche & Exploration": [
        st.Page(recherche_recette_page, title="Rechercher", icon="🔍"),
        st.Page(index_recettes_page, title="Index A-Z", icon="📖"),
        st.Page(index_ingredients_page, title="Ingrédients", icon="🥕"),
    ],
    "🎲 Que cuisiner ?": [
        st.Page(que_cuisiner_page, title="Que cuisiner ?", icon="🎲"),
    ],
    "➕ Ajouter recette": [
        st.Page(add_recette_page, title="Ajouter recette", icon="➕"),
    ],
    "🔧 Gestion des recettes": [
        card_recette_page_obj,
        modify_recette_page_obj,
        photos_recette_page_obj,
    ],
}

# Configuration de la navigation
pg = st.navigation(pages, position="top")

# Rendre l'objet page accessible globalement pour st.switch_page
if "card_recette_page_obj" not in st.session_state:
    st.session_state.card_recette_page_obj = card_recette_page_obj

if "modify_recette_page_obj" not in st.session_state:
    st.session_state.modify_recette_page_obj = modify_recette_page_obj

if "photos_recette_page_obj" not in st.session_state:
    st.session_state.photos_recette_page_obj = photos_recette_page_obj

# Exécution de la page sélectionnée
if pg:
    pg.run()
