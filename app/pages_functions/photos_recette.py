import os
import uuid

import streamlit as st

from config.android_config import get_android_config
from src.crud.recettes import get_recette_by_id
from src.db import get_db_session
from src.model import Photo


def photo_viewer(recette):
    """Afficheur de photos avec navigation"""
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


def reorganiser_ordres_photos(session, recette_id):
    """Réorganise les ordres des photos pour qu'ils soient séquentiels (0, 1, 2, ...)"""
    photos = session.query(Photo).filter(Photo.recette_id == recette_id).order_by(Photo.ordre).all()
    for i, photo in enumerate(photos):
        photo.ordre = i
    session.commit()


def photos_recette_page():
    """Page de gestion des photos d'une recette"""
    # Essayer d'abord session_state, puis query_params (comme les autres pages)
    recette_id = st.session_state.get("selected_recette_id") or st.query_params.get("recette_id")

    if not recette_id:
        st.error("Aucune recette sélectionnée.")
        st.info("💡 **Comment gérer les photos d'une recette :**")
        st.markdown(
            """
        1. Allez dans **'Recherche & Exploration'** pour trouver une recette
        2. Cliquez sur **'👀 Voir'** sur la recette qui vous intéresse
        3. Dans la page de détail, cliquez sur **'📸 Gérer les photos'**

        Ou depuis la page de modification d'une recette, cliquez sur **'📷 Gérer les photos existantes'**
        """
        )
        return

    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)

        if not recette:
            st.error("Recette introuvable.")
            return

        st.title(f"📸 Photos : {recette.nom}")

        # Navigation - liens de retour
        st.markdown("### 🔙 Navigation")
        col_nav1, col_nav2, col_nav3 = st.columns([1, 1, 2])

        with col_nav1:
            if st.button("⬅️ Retour aux détails", type="secondary", key="top_back_to_details"):
                if "card_recette_page_obj" in st.session_state:
                    st.switch_page(st.session_state.card_recette_page_obj)

        with col_nav2:
            if st.button("⬅️ Retour aux modifications", type="secondary", key="top_back_to_modifications"):
                if "modify_recette_page_obj" in st.session_state:
                    st.switch_page(st.session_state.modify_recette_page_obj)

        st.markdown("---")

        # --- 📤 Upload ---
        st.subheader("Ajouter une photo")

        # Utiliser une clé unique pour éviter les uploads multiples
        upload_key = f"photo_upload_{recette.id}"
        uploaded = st.file_uploader("Photo", type=["png", "jpg", "jpeg"], key=upload_key)
        categorie = st.selectbox("Catégorie", ["final", "cuisson", "ingrédient", "préparation", "autre"])

        # Vérifier si un fichier a été uploadé et qu'il n'a pas déjà été traité
        upload_session_key = f"uploaded_file_{recette.id}"
        if uploaded and uploaded.name not in st.session_state.get(upload_session_key, set()):
            # Utiliser la configuration Android pour le stockage des photos
            config = get_android_config()
            photos_base_dir = config.get_photos_directory()
            path_dir = photos_base_dir / str(recette.id)
            path_dir.mkdir(parents=True, exist_ok=True)

            filename = f"{uuid.uuid4()}.jpg"
            filepath = path_dir / filename

            with open(filepath, "wb") as f:
                f.write(uploaded.read())

            # Calculer le prochain ordre disponible
            max_ordre = session.query(Photo).filter(Photo.recette_id == recette.id).count()

            photo = Photo(
                id=str(uuid.uuid4()),
                recette_id=recette.id,
                chemin=str(filepath),
                categorie=categorie,
                ordre=max_ordre,  # Utiliser le nombre de photos existantes comme nouvel ordre
            )
            session.add(photo)
            session.commit()

            # Marquer ce fichier comme traité
            if upload_session_key not in st.session_state:
                st.session_state[upload_session_key] = set()
            st.session_state[upload_session_key].add(uploaded.name)

            st.success("📷 Photo enregistrée")
            st.rerun()

        # --- 🖼️ Liste des photos ---
        st.subheader("Photos existantes")

        # Réorganiser les ordres au cas où il y aurait des incohérences
        reorganiser_ordres_photos(session, recette.id)

        # Récupérer les photos triées par ordre
        photos = session.query(Photo).filter(Photo.recette_id == recette.id).order_by(Photo.ordre).all()

        if not photos:
            st.info("Aucune photo pour cette recette.")
            return

        for i, photo in enumerate(photos):
            col1, col2 = st.columns([1, 3])
            with col1:
                st.image(str(photo.chemin), width=150)
            with col2:
                st.markdown(f"**Catégorie :** {photo.categorie}")
                st.markdown(f"**Position :** {i+1}/{len(photos)}")  # noqa E226

                new_cat = st.selectbox(
                    "Modifier la catégorie",
                    ["final", "cuisson", "ingrédient", "préparation", "autre"],
                    index=["final", "cuisson", "ingrédient", "préparation", "autre"].index(str(photo.categorie)),
                    key=f"cat_{photo.id}",
                )
                if new_cat != photo.categorie:
                    if st.button("💾 Sauvegarder la catégorie", key=f"save_cat_{photo.id}"):
                        photo.categorie = new_cat
                        session.commit()
                        st.success("Catégorie mise à jour")
                        st.rerun()

                col_up, col_down, col_del = st.columns(3)

                with col_up:
                    # Monter la photo (diminuer l'ordre)
                    if st.button("⬆️", key=f"up_{photo.id}", disabled=(i == 0)):
                        if i > 0:
                            # Échanger avec la photo précédente
                            photo_precedente = photos[i - 1]
                            photo.ordre, photo_precedente.ordre = photo_precedente.ordre, photo.ordre
                            session.commit()
                            st.success("Photo montée !")
                            st.rerun()

                with col_down:
                    # Descendre la photo (augmenter l'ordre)
                    if st.button("⬇️", key=f"down_{photo.id}", disabled=(i == len(photos) - 1)):
                        if i < len(photos) - 1:
                            # Échanger avec la photo suivante
                            photo_suivante = photos[i + 1]
                            photo.ordre, photo_suivante.ordre = photo_suivante.ordre, photo.ordre
                            session.commit()
                            st.success("Photo descendue !")
                            st.rerun()

                with col_del:
                    if st.button("🗑️ Supprimer", key=f"del_{photo.id}"):
                        try:
                            os.remove(str(photo.chemin))
                        except Exception:
                            pass
                        session.delete(photo)
                        session.commit()
                        # Réorganiser les ordres après suppression
                        reorganiser_ordres_photos(session, recette.id)
                        st.success("Photo supprimée")
                        st.rerun()

        # Navigation en bas de page
        st.markdown("---")
        st.markdown("### 🔙 Navigation")
        col_nav_bottom1, col_nav_bottom2, col_nav_bottom3 = st.columns([1, 1, 2])

        with col_nav_bottom1:
            if st.button("⬅️ Retour aux détails", type="secondary", key="bottom_back_to_details"):
                if "card_recette_page_obj" in st.session_state:
                    st.switch_page(st.session_state.card_recette_page_obj)

        with col_nav_bottom2:
            if st.button("⬅️ Retour aux modifications", type="secondary", key="bottom_back_to_modifications"):
                if "modify_recette_page_obj" in st.session_state:
                    st.switch_page(st.session_state.modify_recette_page_obj)
