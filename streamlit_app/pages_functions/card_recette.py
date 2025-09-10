import streamlit as st

from src.crud.recettes import delete_recette, get_recette_by_id
from src.db import get_db_session
from streamlit_app.utils.navigation import (
    navigate_to_photos_manage,
    navigate_to_recipe_modify,
    show_recipe_not_found_help,
)

# Import des utilitaires
from streamlit_app.utils.session_manager import get_recette_id_from_state
from streamlit_app.utils.ui_components import create_recipe_navigation_buttons
from streamlit_app.utils.card_components import (
    create_photos_card,
    create_categories_card,
    create_ingredients_card,
    create_steps_card,
    create_metrics_card,
    create_tags_card,
    create_source_card,
)


def card_recette_page():
    """Page de détail d'une recette"""
    recette_id = get_recette_id_from_state()

    if not recette_id:
        show_recipe_not_found_help()
        return

    recette = _get_recipe_or_show_error(recette_id)
    if not recette:
        return

    _render_recipe_details(recette)
    _render_recipe_actions(recette)


def _get_recipe_or_show_error(recette_id):
    """Récupère la recette ou affiche une erreur"""
    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)

    if not recette:
        st.error("Recette introuvable.")
        return None

    return recette


def _render_recipe_details(recette):
    """Affiche les détails de la recette avec un layout en cartes"""
    # Main title (not in a card)
    st.title(recette.nom)
    
    # Card 1: Photos (as requested - second card after title)
    create_photos_card(recette)
    
    # Card 2: Categories 
    create_categories_card(recette)
    
    # Card 3: Ingredients
    create_ingredients_card(recette)
    
    # Card 4: Steps
    create_steps_card(recette)
    
    # Card 5: Metrics/Timing
    create_metrics_card(recette)
    
    # Card 6: Tags (collapsed by default)
    create_tags_card(recette)
    
    # Card 7: Source (collapsed by default)  
    create_source_card(recette)




def _render_recipe_actions(recette):
    """Affiche les actions disponibles pour la recette"""
    st.divider()

    # Boutons d'action
    modify_clicked, photos_clicked, delete_clicked = create_recipe_navigation_buttons(str(recette.id))

    if modify_clicked:
        navigate_to_recipe_modify(str(recette.id))

    if photos_clicked:
        navigate_to_photos_manage(str(recette.id))

    if delete_clicked:
        st.session_state.confirm_delete_recette = str(recette.id)
        st.rerun()

    # Gestion de la confirmation de suppression
    _handle_delete_confirmation(recette)

    st.info("Utilisez le menu de navigation pour accéder aux autres fonctionnalités.")


def _handle_delete_confirmation(recette):
    """Gère la confirmation de suppression"""
    if st.session_state.get("confirm_delete_recette") == str(recette.id):
        st.markdown("---")
        st.error("⚠️ **Confirmation de suppression**")

        st.write(f"Êtes-vous sûr(e) de vouloir supprimer la recette **'{recette.nom}'** ?")
        st.write("Cette action supprimera définitivement :")
        st.markdown(
            """
        - La recette et toutes ses informations
        - Tous les ingrédients associés
        - Toutes les étapes de préparation
        - Toutes les photos associées
        - Les catégories et tags liés
        """
        )

        col1, col2 = st.columns([1, 1])

        with col1:
            if st.button("❌ Oui, supprimer définitivement", type="primary"):
                _delete_recipe(recette)

        with col2:
            if st.button("🔙 Annuler", type="secondary"):
                if "confirm_delete_recette" in st.session_state:
                    del st.session_state.confirm_delete_recette
                st.rerun()


def _delete_recipe(recette):
    """Supprime la recette"""
    try:
        with get_db_session() as session:
            delete_recette(session, recette.id)

        st.success(f"✅ Recette '{recette.nom}' supprimée avec succès")

        # Nettoyer session state
        if "confirm_delete_recette" in st.session_state:
            del st.session_state.confirm_delete_recette
        if "selected_recette_id" in st.session_state:
            del st.session_state.selected_recette_id

        st.rerun()

    except Exception as e:
        st.error(f"Erreur lors de la suppression : {str(e)}")
