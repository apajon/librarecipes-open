import streamlit as st

from app.pages_functions.photos_recette import photo_viewer
from app.utils.navigation import navigate_to_photos_manage, navigate_to_recipe_modify, show_recipe_not_found_help

# Import des utilitaires
from app.utils.session_manager import get_recette_id_from_state
from app.utils.ui_components import create_recipe_navigation_buttons, show_recipe_metrics
from src.crud.recettes import delete_recette, get_recette_by_id
from src.db import get_db_session


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
    """Affiche les détails de la recette"""
    st.title(recette.nom)

    # Photos
    st.subheader("📷 Photos")
    photo_viewer(recette)

    # Catégories
    _show_categories(recette)

    # Ingrédients
    _show_ingredients(recette)

    # Étapes
    _show_etapes(recette)

    # Métriques
    show_recipe_metrics(recette)

    # Tags
    _show_tags(recette)

    # Source
    _show_source(recette)


def _show_categories(recette):
    """Affiche les catégories de la recette"""
    st.markdown("### 🏷️ Catégories")
    if recette.categories:
        st.write(", ".join([c.nom for c in recette.categories]))
    else:
        st.write("Aucune catégorie")


def _show_ingredients(recette):
    """Affiche les ingrédients de la recette"""
    st.markdown("### 🧂 Ingrédients")
    for ing in recette.ingredients:
        ligne = f"- {ing.nom} : {ing.quantite} {ing.unite or ''}"
        if ing.indispensable:
            ligne += " (🟢 indispensable)"
        elif ing.alternatives:
            ligne += f" (🔄 alternatives : {ing.alternatives})"
        st.markdown(ligne)


def _show_etapes(recette):
    """Affiche les étapes de la recette"""
    st.markdown("### 📝 Étapes")
    for etape in recette.etapes:
        st.markdown(f"**{etape.ordre}.** {etape.description}")


def _show_tags(recette):
    """Affiche les tags de la recette"""
    if recette.tags:
        st.markdown("### 🏷️ Tags")
        st.write(", ".join([t.nom for t in recette.tags]))


def _show_source(recette):
    """Affiche la source de la recette"""
    if recette.source:
        st.markdown("### 📚 Source")
        source = recette.source

        if source.type == "homemade":
            st.info("🏠 **Recette maison** - Création originale")

        elif source.type == "url" and source.url:
            st.markdown(f"🌐 **Source web :** {source.url}")

        elif source.type == "book":
            _show_book_source(source)


def _show_book_source(source):
    """Affiche les informations d'une source livre"""
    livre_info = []
    if source.book_title:
        livre_info.append(f"📖 **{source.book_title}**")
    if source.book_authors:
        livre_info.append(f"✍️ {source.book_authors}")
    if source.book_page:
        livre_info.append(f"📄 Page {source.book_page}")

    if livre_info:
        st.markdown(" | ".join(livre_info))
    else:
        st.info("📚 **Source livre** - Informations incomplètes")


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
