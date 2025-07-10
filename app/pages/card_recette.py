import streamlit as st

from app.pages.photos_recette import photo_viewer
from src.crud.recettes import get_recette_by_id
from src.db import get_db_session


def main():
    st.set_page_config(page_title="Détail recette", page_icon="📖")

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
    for etape in sorted(recette.etapes, key=lambda e: e.ordre):
        st.markdown(f"**Étape {etape.ordre}** : {etape.description}")

    # Source
    st.markdown("### 📚 Source")
    if recette.source.type == "homemade":
        st.write("Recette maison")
    elif recette.source.type == "url":
        st.write(f"[Lien source]({recette.source.url})")
    elif recette.source.type == "book":
        st.write(f"{recette.source.book_title} — {recette.source.book_authors}")

    # (Facultatif) Ajoute ici les photos et l’historique
    # TODO


if __name__ == "__main__":
    main()
