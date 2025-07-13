import streamlit as st

from app.pages.photos_recette import photo_viewer
from src.crud.recettes import get_recette_by_id
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
    st.info("Utilisez le menu de navigation pour accéder aux autres fonctionnalités.")
