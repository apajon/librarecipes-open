import streamlit as st

from src.crud.recettes import get_recette_by_id
from src.db import get_db_session


def modify_recette_page():
    """Page de modification d'une recette"""
    # Essayer d'abord session_state, puis query_params
    recette_id = st.session_state.get("selected_recette_id") or st.query_params.get("recette_id")

    if not recette_id:
        st.info("💡 **Comment modifier une recette :**")
        st.markdown(
            """
        1. Allez dans **'Recherche & Exploration'** pour trouver une recette
        2. Cliquez sur **'👀 Voir'** sur la recette qui vous intéresse
        3. Dans la page de détail, cliquez sur **'✏️ Modifier cette recette'**

        Ou utilisez l'**Index A-Z** pour parcourir toutes vos recettes !
        """
        )
        return

    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)
    if not recette:
        st.error("Recette introuvable.")
        return

    st.title(f"✏️ Modifier : {recette.nom}")

    # Bouton pour revenir aux détails
    if st.button("⬅️ Retour aux détails", type="secondary"):
        st.switch_page(st.session_state.get("card_recette_page_obj"))

    st.info(
        "Cette fonctionnalité sera bientôt disponible. "
        "Pour l'instant, vous pouvez consulter les détails de la recette "
        "dans le menu 'Gestion > Détail'."
    )
