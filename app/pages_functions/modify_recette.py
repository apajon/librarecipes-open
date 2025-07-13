import streamlit as st

from src.crud.recettes import get_recette_by_id
from src.db import get_db_session


def modify_recette_page():
    """Page de modification d'une recette"""
    params = st.query_params
    recette_id = params.get("recette_id")

    if not recette_id:
        st.error("Aucune recette sélectionnée.")
        return

    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)
    if not recette:
        st.error("Recette introuvable.")
        return

    st.title(f"✏️ Modifier : {recette.nom}")

    st.info(
        "Cette fonctionnalité sera bientôt disponible. "
        "Pour l'instant, vous pouvez consulter les détails de la recette "
        "dans le menu 'Gestion > Détail'."
    )
