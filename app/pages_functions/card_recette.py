import streamlit as st

from app.pages_functions.photos_recette import photo_viewer
from src.crud.recettes import delete_recette, get_recette_by_id
from src.db import get_db_session


def card_recette_page():
    """Page de détail d'une recette"""
    # Essayer d'abord session_state, puis query_params
    recette_id = st.session_state.get("selected_recette_id") or st.query_params.get("recette_id")

    if not recette_id:
        st.info("💡 **Comment voir une recette :**")
        st.markdown(
            """
        1. Allez dans **'Recherche & Exploration'** pour trouver une recette
        2. Cliquez sur **'👀 Voir'** sur la recette qui vous intéresse
        3. Vous serez automatiquement redirigé vers cette page avec les détails !

        Ou utilisez l'**Index A-Z** pour parcourir toutes vos recettes !
        """
        )
        return

    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)
    if not recette:
        st.error("Recette introuvable.")
        return

    st.title(recette.nom)

    # 📸 Affichage des photos
    st.subheader("📷 Photos")
    photo_viewer(recette)

    # Tags
    st.markdown("### 🏷️ Catégories")
    st.write(", ".join([c.nom for c in recette.categories]))

    # Ingrédients
    st.markdown("### 🧂 Ingrédients")
    for ing in recette.ingredients:
        ligne = f"- {ing.nom} : {ing.quantite} {ing.unite or ''}"
        if ing.indispensable:
            ligne += " (🟢 indispensable)"
        elif ing.alternatives:
            ligne += f" (🔄 alternatives : {ing.alternatives})"
        st.markdown(ligne)

    # Étapes
    st.markdown("### 📝 Étapes")
    for etape in recette.etapes:
        st.markdown(f"**{etape.ordre}.** {etape.description}")

    # Informations complémentaires
    col1, col2, col3 = st.columns(3)
    with col1:
        st.metric("Temps de préparation", f"{recette.preparation or 0} min")
    with col2:
        st.metric("Temps de cuisson", f"{recette.cuisson or 0} min")
    with col3:
        st.metric("Portions", recette.portions or 0)

    # Tags
    if recette.tags:
        st.markdown("### 🏷️ Tags")
        st.write(", ".join([t.nom for t in recette.tags]))

    # Source
    if recette.source:
        st.markdown("### 📚 Source")
        source = recette.source

        if source.type == "homemade":
            st.info("🏠 **Recette maison** - Création originale")

        elif source.type == "url" and source.url:
            st.markdown(f"🌐 **Source web :** {source.url}")

        elif source.type == "book":
            # Construire l'affichage du livre
            livre_info = []
            if source.book_title:
                livre_info.append(f"📖 **{source.book_title}**")
            if source.book_authors:
                livre_info.append(f"✍️ *{source.book_authors}*")
            if source.book_page:
                livre_info.append(f"📄 Page {source.book_page}")

            if livre_info:
                st.markdown("<br>".join(livre_info), unsafe_allow_html=True)
            else:
                st.info("📚 Source : Livre/Magazine")

    # Actions
    st.divider()

    # Boutons d'action
    col_action1, col_action2, col_action3 = st.columns([1, 1, 1])

    with col_action1:
        # Bouton pour modifier la recette
        if st.button("✏️ Modifier cette recette", type="primary", use_container_width=True):
            # Stocker l'ID de la recette dans session_state pour la page de modification
            st.session_state.selected_recette_id = recette_id
            st.switch_page(st.session_state.get("modify_recette_page_obj"))

    with col_action2:
        # Bouton pour gérer les photos
        if st.button("📸 Gérer les photos", type="secondary", use_container_width=True):
            # Stocker l'ID de la recette dans session_state ET query params
            st.session_state.selected_recette_id = recette_id
            st.query_params.recette_id = str(recette_id)
            if "photos_recette_page_obj" in st.session_state:
                st.switch_page(st.session_state.photos_recette_page_obj)

    with col_action3:
        # Bouton pour supprimer la recette
        if st.button("🗑️ Supprimer cette recette", type="secondary", use_container_width=True):
            # Marquer que l'utilisateur veut supprimer
            st.session_state.confirm_delete_recette = recette_id

    # Afficher la confirmation de suppression si demandée
    if st.session_state.get("confirm_delete_recette") == recette_id:
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

        col_confirm1, col_confirm2 = st.columns([1, 1])
        with col_confirm1:
            if st.button("❌ Annuler", type="secondary", use_container_width=True):
                # Supprimer la demande de confirmation
                if "confirm_delete_recette" in st.session_state:
                    del st.session_state.confirm_delete_recette
                st.rerun()

        with col_confirm2:
            if st.button("🗑️ Confirmer la suppression", type="primary", use_container_width=True):
                # Supprimer la recette
                with get_db_session() as session:
                    success = delete_recette(session, recette_id)

                if success:
                    st.success(f"✅ La recette '{recette.nom}' a été supprimée avec succès.")
                    st.info("💡 Redirection vers la page d'accueil...")
                    # Nettoyer le session state
                    if "selected_recette_id" in st.session_state:
                        del st.session_state.selected_recette_id
                    if "confirm_delete_recette" in st.session_state:
                        del st.session_state.confirm_delete_recette
                    # Rediriger vers la page d'accueil
                    if "home_page_obj" in st.session_state:
                        st.switch_page(st.session_state.home_page_obj)
                else:
                    st.error("❌ Erreur lors de la suppression de la recette.")
                    if "confirm_delete_recette" in st.session_state:
                        del st.session_state.confirm_delete_recette

    st.info("Utilisez le menu de navigation pour accéder aux autres fonctionnalités.")
