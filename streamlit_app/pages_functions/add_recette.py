import streamlit as st
from streamlit_tags import st_tags

from src.crud.metadata import get_all_categories, get_all_tags
from src.crud.recettes import create_recette
from src.db import get_db_session
from streamlit_app.utils.etapes_manager import EtapesManager
from streamlit_app.utils.ingredients_manager import IngredientsManager
from streamlit_app.utils.recipe_validator import prepare_recipe_data, validate_recipe_data

# Import des utilitaires
from streamlit_app.utils.session_manager import clear_recette_session_state, init_recette_session_state
from streamlit_app.utils.ui_components import create_recipe_info_form, create_source_form
from streamlit_app.utils.ui_helpers import show_add_recipe_help, show_banner


def add_recette_page():
    """Page d'ajout d'une nouvelle recette"""
    # Affichage du banner
    show_banner("banner_recettes.png")

    st.title("➕ Ajout d'une nouvelle recette")

    # Section d'aide
    show_add_recipe_help()

    # Initialiser les variables de session
    init_recette_session_state()

    # Gestionnaires
    ingredients_manager = IngredientsManager()
    etapes_manager = EtapesManager()

    # Formulaire principal
    _render_general_info_form()

    # Classification
    categories, tags = _render_classification_form()

    # Source
    source_data = _render_source_form()

    # Gestion des ingrédients et étapes
    ingredients_manager.render_complete()
    etapes_manager.render_complete()

    # Bouton d'enregistrement
    _render_save_button(categories, tags, source_data, ingredients_manager, etapes_manager)


def _render_general_info_form():
    """Affiche le formulaire d'informations générales"""
    st.subheader("ℹ️ Informations générales")

    defaults = {
        "nom": st.session_state.get("recette_nom", ""),
        "preparation": st.session_state.get("recette_preparation", 15),
        "cuisson": st.session_state.get("recette_cuisson", 0),
        "portions": st.session_state.get("recette_portions", 4),
    }

    nom, preparation, cuisson, portions = create_recipe_info_form(defaults)

    # Mettre à jour session state
    st.session_state.recette_nom = nom
    st.session_state.recette_preparation = preparation
    st.session_state.recette_cuisson = cuisson
    st.session_state.recette_portions = portions


def _render_classification_form():
    """Affiche le formulaire de classification et retourne les valeurs"""
    st.subheader("🏷️ Classification")

    with get_db_session() as session:
        existing_categories = get_all_categories(session)
        existing_tags = get_all_tags(session)

    categories = st_tags(
        label="Catégories",
        text="Appuyez sur Entrée pour ajouter",
        suggestions=existing_categories,
        key="categories_input",
    )

    tags = st_tags(label="Tags", text="Appuyez sur Entrée pour ajouter", suggestions=existing_tags, key="tags_input")

    return categories, tags


def _render_source_form():
    """Affiche le formulaire de source et retourne les données"""
    st.subheader("📚 Source")
    return create_source_form()


def _render_save_button(categories, tags, source_data, ingredients_manager, etapes_manager):
    """Affiche le bouton de sauvegarde et gère l'enregistrement"""
    st.divider()

    if st.button("💾 Enregistrer la recette", type="primary"):
        _save_recipe(categories, tags, source_data, ingredients_manager, etapes_manager)


def _save_recipe(categories, tags, source_data, ingredients_manager, etapes_manager):
    """Sauvegarde la recette après validation"""
    nom = st.session_state.recette_nom
    ingredients = ingredients_manager.get_ingredients()
    etapes = etapes_manager.get_etapes()

    # Validation
    is_valid, errors = validate_recipe_data(nom, ingredients, etapes)

    if not is_valid:
        for error in errors:
            st.error(error)
        return

    try:
        with get_db_session() as session:
            recette_data = prepare_recipe_data(
                nom=nom,
                preparation=st.session_state.recette_preparation,
                cuisson=st.session_state.recette_cuisson,
                portions=st.session_state.recette_portions,
                categories=categories,
                tags=tags,
                source_data=source_data,
                ingredients=ingredients,
                etapes=etapes,
            )

            nouvelle_recette = create_recette(session, recette_data)
            st.success(f"✅ Recette '{nouvelle_recette.nom}' créée avec succès !")

            # Réinitialiser le formulaire
            clear_recette_session_state()
            st.rerun()

    except Exception as e:
        st.error(f"Erreur lors de la création de la recette : {str(e)}")
