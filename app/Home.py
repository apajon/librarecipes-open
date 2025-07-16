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

# Import pour l'affichage des recettes
from app.utils.recipe_display import format_recette_display_name
from src.crud.recettes import get_recette_by_id, list_recettes

# Import pour la base de données
from src.db import get_db_session

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


# Fonction pour afficher le résumé de la recette
def display_recette_summary(recette):
    """Affiche le résumé de la recette sélectionnée"""
    st.sidebar.header("📝 Résumé de la recette")

    # Nom de la recette
    st.sidebar.subheader(recette.nom)

    # Informations de base
    col1, col2 = st.sidebar.columns(2)
    with col1:
        if recette.preparation:
            st.write(f"⏱️ Préparation: {recette.preparation}min")
        if recette.cuisson:
            st.write(f"🔥 Cuisson: {recette.cuisson}min")
    with col2:
        if recette.portions:
            st.write(f"👥 Portions: {recette.portions}")

    # Total du temps
    if recette.preparation and recette.cuisson:
        total_time = recette.preparation + recette.cuisson
        st.sidebar.write(f"⏰ **Temps total: {total_time}min**")

    # Catégories
    if recette.categories:
        categories = [cat.nom for cat in recette.categories]
        st.sidebar.write(f"🏷️ **Catégories:** {', '.join(categories)}")

    # Tags
    if recette.tags:
        tags = [tag.nom for tag in recette.tags]
        st.sidebar.write(f"🔖 **Tags:** {', '.join(tags)}")

    # Source
    if recette.source:
        if recette.source.type == "url" and recette.source.url:
            st.sidebar.write(f"🌐 **Source:** [Lien]({recette.source.url})")
        elif recette.source.type == "book":
            book_info = recette.source.book_title
            if recette.source.book_authors:
                book_info += f" - {recette.source.book_authors}"
            if recette.source.book_page:
                book_info += f" (p. {recette.source.book_page})"
            st.sidebar.write(f"📚 **Livre:** {book_info}")
        elif recette.source.type == "homemade":
            st.sidebar.write("🏠 **Recette maison**")

    # Statistiques d'exécution
    if recette.executions:
        nb_executions = len(recette.executions)
        last_execution = max(recette.executions, key=lambda x: x.date_execution)
        last_date = last_execution.date_execution.strftime("%d/%m/%Y")
        st.sidebar.write(f"📊 **Exécutions:** {nb_executions} fois")
        st.sidebar.write(f"📅 **Dernière fois:** {last_date}")

    # Boutons d'action
    st.sidebar.markdown("---")
    col1, col2 = st.sidebar.columns(2)
    with col1:
        if st.button("👁️ Voir détail", key=f"voir_sidebar_{recette.id}"):
            st.session_state.selected_recette_id = recette.id
            st.switch_page(st.session_state.card_recette_page_obj)
    with col2:
        if st.button("✏️ Modifier", key=f"modifier_sidebar_{recette.id}"):
            st.session_state.selected_recette_id = recette.id
            st.switch_page(st.session_state.modify_recette_page_obj)


# Sidebar avec sélecteur de recettes
with st.sidebar:
    st.header("🔍 Sélecteur de recettes")

    # Récupération des recettes
    try:
        with get_db_session() as session:
            recettes = list_recettes(session)

            if recettes:
                # Création du dictionnaire pour le selectbox
                recette_options = {}
                for recette in recettes:
                    display_name = format_recette_display_name(recette)
                    recette_options[display_name] = recette.id

                # Déterminer l'index de la recette sélectionnée
                selected_index = None
                if "selected_recette_id" in st.session_state and st.session_state.selected_recette_id:
                    # Trouver l'index de la recette sélectionnée
                    for i, (display_name, recette_id) in enumerate(recette_options.items()):
                        if recette_id == st.session_state.selected_recette_id:
                            selected_index = i
                            break

                # Selectbox avec gestion de l'index
                selected_display_name = st.selectbox(
                    "Choisir une recette:",
                    options=[None] + list(recette_options.keys()),
                    index=selected_index + 1 if selected_index is not None else 0,
                    format_func=lambda x: "-- Sélectionner une recette --" if x is None else x,
                    help="Sélectionnez une recette pour voir son résumé",
                )

                # Récupération de la recette sélectionnée
                if selected_display_name and selected_display_name in recette_options:
                    selected_recette_id = recette_options[selected_display_name]
                    selected_recette = get_recette_by_id(session, selected_recette_id)

                    if selected_recette:
                        # Stockage dans la session state
                        st.session_state.selected_recette_id = selected_recette_id

                        # Affichage du résumé
                        display_recette_summary(selected_recette)
                elif selected_display_name is None:
                    # Réinitialiser la sélection si None est choisi
                    if "selected_recette_id" in st.session_state:
                        del st.session_state.selected_recette_id
            else:
                st.info("Aucune recette trouvée. Ajoutez votre première recette !")

    except Exception as e:
        st.error(f"Erreur lors du chargement des recettes : {str(e)}")

st.sidebar.markdown("---")

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
