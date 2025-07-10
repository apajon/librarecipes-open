import os
import uuid
from pathlib import Path
from urllib.parse import unquote

import streamlit as st

# from PIL import Image
from sqlalchemy.orm import joinedload

from src.db import SessionLocal
from src.model import Photo, Recette


def photo_viewer(recette):

    # photos = recette.photos
    # if not photos:
    #     st.info("Aucune photo enregistrée pour cette recette.")
    # else:
    #     for photo in photos:
    #         st.image(photo.chemin, caption=f"Catégorie : {photo.categorie}", use_container_width=True)

    photos = recette.photos
    if not photos:
        st.info("Aucune photo enregistrée pour cette recette.")
        return
    photos_sorted = sorted(photos, key=lambda p: p.ordre)

    # Liste des chemins d'image
    paths = [photo.chemin for photo in photos_sorted]
    categories = [photo.categorie for photo in photos_sorted]

    # ID unique pour chaque recette
    id_session_key = f"selected_photo_{recette.id}"

    # Initialiser l'index de la photo sélectionnée
    if id_session_key not in st.session_state:
        st.session_state[id_session_key] = 0

    selected_index = st.session_state[id_session_key]

    # 📸 Afficher la photo principale
    st.image(paths[selected_index], caption=f"Catégorie : {categories[selected_index]}", use_container_width=True)

    # 🎚️ Afficher les miniatures
    st.markdown("### Aperçu des autres photos")
    cols = st.columns(min(len(paths), 5))  # max 5 colonnes par ligne

    for i, col in enumerate(cols):
        with col:
            if st.button("📷", key=f"thumb_{recette.id}_{i}"):
                st.session_state[id_session_key] = i
            st.image(paths[i], caption=categories[i], use_container_width=True)


def main():
    st.set_page_config(page_title="Photos de recette", page_icon="📷")
    recette_id = st.query_params.get("recette_id")

    if not recette_id:
        st.error("Aucune recette spécifiée.")
        return

    with SessionLocal() as session:
        recette = (
            session.query(Recette)
            .options(joinedload(Recette.photos))
            .filter(Recette.id == unquote(recette_id))
            .first()
        )

        if not recette:
            st.error("Recette introuvable.")
            return

        st.title(f"📸 Photos : {recette.nom}")

        # --- 📤 Upload ---
        st.subheader("Ajouter une photo")
        uploaded = st.file_uploader("Photo", type=["png", "jpg", "jpeg"])
        categorie = st.selectbox("Catégorie", ["final", "cuisson", "ingrédient", "préparation", "autre"])

        if uploaded:
            path_dir = Path("data") / "photos" / str(recette.id)
            path_dir.mkdir(parents=True, exist_ok=True)

            filename = f"{uuid.uuid4()}.jpg"
            filepath = path_dir / filename

            with open(filepath, "wb") as f:
                f.write(uploaded.read())

            photo = Photo(
                id=str(uuid.uuid4()),
                recette_id=recette.id,
                chemin=str(filepath),
                categorie=categorie,
                ordre=len(recette.photos),
            )
            session.add(photo)
            session.commit()
            st.success("📷 Photo enregistrée")
            st.rerun()

        # --- 🖼️ Liste des photos ---
        st.subheader("Photos existantes")
        photos = sorted(recette.photos, key=lambda p: p.ordre)

        for i, photo in enumerate(photos):
            col1, col2 = st.columns([1, 3])
            with col1:
                st.image(photo.chemin, width=150)
            with col2:
                st.markdown(f"**Catégorie :** {photo.categorie}")
                new_cat = st.selectbox(
                    "Modifier la catégorie",
                    ["final", "cuisson", "ingrédient", "préparation", "autre"],
                    index=["final", "cuisson", "ingrédient", "préparation", "autre"].index(photo.categorie),
                    key=f"cat_{photo.id}",
                )
                if new_cat != photo.categorie:
                    photo.categorie = new_cat
                    session.commit()
                    st.success("Catégorie mise à jour")

                col_up, col_down, col_del = st.columns(3)
                with col_up:
                    if st.button("⬆️", key=f"up_{photo.id}") and i > 0:
                        photos[i].ordre, photos[i - 1].ordre = photos[i - 1].ordre, photos[i].ordre
                        session.commit()
                        st.rerun()
                with col_down:
                    if st.button("⬇️", key=f"down_{photo.id}") and i < len(photos) - 1:
                        photos[i].ordre, photos[i + 1].ordre = photos[i + 1].ordre, photos[i].ordre
                        session.commit()
                        st.rerun()
                with col_del:
                    if st.button("🗑️ Supprimer", key=f"del_{photo.id}"):
                        try:
                            os.remove(photo.chemin)
                        except Exception:
                            pass
                        session.delete(photo)
                        session.commit()
                        st.success("Photo supprimée")
                        st.rerun()


if __name__ == "__main__":
    main()
