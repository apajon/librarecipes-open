"""
Components for photo management and display in recipes
"""
import os
import uuid
from pathlib import Path

import streamlit as st

from src.crud.recettes import get_recette_by_id
from src.db import get_db_session
from src.model import Photo


def enhanced_photo_viewer(recette):
    """Enhanced photo viewer with sliding navigation and inline management"""
    photos = sorted(recette.photos, key=lambda p: p.ordre) if recette.photos else []
    
    if not photos:
        st.info("📷 Aucune photo enregistrée pour cette recette.")
        _render_add_photo_section(recette)
        return
    
    # Create columns for photo display
    col1, col2, col3 = st.columns([1, 8, 1])
    
    # Session state for current photo
    photo_key = f"current_photo_{recette.id}"
    if photo_key not in st.session_state:
        st.session_state[photo_key] = 0
    
    current_index = st.session_state[photo_key]
    total_photos = len(photos)
    
    # Navigation buttons
    with col1:
        if st.button("◀", key=f"prev_{recette.id}", disabled=current_index == 0):
            st.session_state[photo_key] = max(0, current_index - 1)
            st.rerun()
    
    with col3:
        if st.button("▶", key=f"next_{recette.id}", disabled=current_index == total_photos - 1):
            st.session_state[photo_key] = min(total_photos - 1, current_index + 1)
            st.rerun()
    
    # Main photo display
    with col2:
        current_photo = photos[current_index]
        
        # Check if photo file exists
        if os.path.exists(current_photo.chemin):
            st.image(
                current_photo.chemin, 
                caption=f"📷 {current_index + 1}/{total_photos} - {current_photo.categorie}",
                use_container_width=True
            )
            
            # Display photo category as badge
            if current_photo.categorie:
                category_colors = {
                    "préparation": "🟡",
                    "ingrédient": "🟢", 
                    "cuisson": "🟠",
                    "final": "🔵",
                    "autre": "⚪"
                }
                emoji = category_colors.get(current_photo.categorie, "⚪")
                st.markdown(f"**{emoji} {current_photo.categorie.title()}**")
        else:
            st.error(f"❌ Photo non trouvée: {current_photo.chemin}")
    
    # Thumbnails navigation
    if total_photos > 1:
        st.markdown("**📸 Autres photos:**")
        
        # Create thumbnail columns (max 6 per row)
        thumb_cols = st.columns(min(total_photos, 6))
        
        for i, photo in enumerate(photos):
            col_index = i % 6
            with thumb_cols[col_index]:
                if os.path.exists(photo.chemin):
                    if st.button(
                        f"📷", 
                        key=f"thumb_{recette.id}_{i}",
                        help=f"{photo.categorie} - {i+1}/{total_photos}",
                        use_container_width=True
                    ):
                        st.session_state[photo_key] = i
                        st.rerun()
                    
                    # Small thumbnail
                    st.image(photo.chemin, width=80)
                    st.caption(f"{i+1}. {photo.categorie}")
    
    # Photo management section
    _render_photo_management_section(recette, photos, current_index)


def _render_add_photo_section(recette):
    """Render the add photo section"""
    st.markdown("**➕ Ajouter une photo**")
    
    # Upload widget
    upload_key = f"photo_upload_{recette.id}"
    uploaded = st.file_uploader(
        "Choisir une photo",
        type=["png", "jpg", "jpeg"],
        key=upload_key,
        help="Formats supportés: PNG, JPG, JPEG"
    )
    
    # Category selection
    col1, col2 = st.columns([2, 1])
    with col1:
        categorie = st.selectbox(
            "Catégorie de la photo",
            ["préparation", "ingrédient", "cuisson", "final", "autre"],
            key=f"photo_cat_{recette.id}"
        )
    
    with col2:
        if uploaded and st.button("💾 Enregistrer", key=f"save_photo_{recette.id}"):
            _save_uploaded_photo(recette, uploaded, categorie)
            st.rerun()


def _render_photo_management_section(recette, photos, current_index):
    """Render photo management controls"""
    if not photos:
        return
    
    st.markdown("---")
    st.markdown("**🔧 Gestion des photos**")
    
    current_photo = photos[current_index]
    
    # Photo controls in columns
    col1, col2, col3, col4 = st.columns(4)
    
    with col1:
        # Category change
        new_cat = st.selectbox(
            "Catégorie",
            ["préparation", "ingrédient", "cuisson", "final", "autre"],
            index=["préparation", "ingrédient", "cuisson", "final", "autre"].index(current_photo.categorie),
            key=f"edit_cat_{current_photo.id}"
        )
        
        if new_cat != current_photo.categorie:
            if st.button("💾 Sauver", key=f"save_cat_{current_photo.id}"):
                _update_photo_category(current_photo, new_cat)
                st.success("Catégorie mise à jour!")
                st.rerun()
    
    with col2:
        # Move up/down
        if st.button("⬆️ Monter", key=f"up_{current_photo.id}", disabled=current_index == 0):
            _move_photo_up(recette, current_index, photos)
            st.rerun()
        
        if st.button("⬇️ Descendre", key=f"down_{current_photo.id}", disabled=current_index == len(photos) - 1):
            _move_photo_down(recette, current_index, photos)
            st.rerun()
    
    with col3:
        # Delete photo
        if st.button("🗑️ Supprimer", key=f"del_{current_photo.id}"):
            _delete_photo(recette, current_photo)
            st.rerun()
    
    with col4:
        # Add new photo
        _render_add_photo_section(recette)


def _save_uploaded_photo(recette, uploaded_file, categorie):
    """Save an uploaded photo"""
    try:
        # Create photos directory
        photos_dir = Path("data") / "photos" / str(recette.id)
        photos_dir.mkdir(parents=True, exist_ok=True)
        
        # Generate unique filename
        file_extension = uploaded_file.name.split('.')[-1].lower()
        filename = f"{uuid.uuid4()}.{file_extension}"
        filepath = photos_dir / filename
        
        # Save file
        with open(filepath, "wb") as f:
            f.write(uploaded_file.read())
        
        # Add to database
        with get_db_session() as session:
            # Get max order
            max_ordre = session.query(Photo).filter(Photo.recette_id == recette.id).count()
            
            photo = Photo(
                id=str(uuid.uuid4()),
                recette_id=recette.id,
                chemin=str(filepath),
                categorie=categorie,
                ordre=max_ordre
            )
            session.add(photo)
            session.commit()
        
        st.success("📷 Photo ajoutée avec succès!")
        
    except Exception as e:
        st.error(f"Erreur lors de l'ajout de la photo: {str(e)}")


def _update_photo_category(photo, new_category):
    """Update photo category"""
    with get_db_session() as session:
        photo_obj = session.query(Photo).filter(Photo.id == photo.id).first()
        if photo_obj:
            photo_obj.categorie = new_category
            session.commit()


def _move_photo_up(recette, current_index, photos):
    """Move photo up in order"""
    if current_index > 0:
        with get_db_session() as session:
            photo1 = session.query(Photo).filter(Photo.id == photos[current_index].id).first()
            photo2 = session.query(Photo).filter(Photo.id == photos[current_index - 1].id).first()
            
            if photo1 and photo2:
                photo1.ordre, photo2.ordre = photo2.ordre, photo1.ordre
                session.commit()


def _move_photo_down(recette, current_index, photos):
    """Move photo down in order"""
    if current_index < len(photos) - 1:
        with get_db_session() as session:
            photo1 = session.query(Photo).filter(Photo.id == photos[current_index].id).first()
            photo2 = session.query(Photo).filter(Photo.id == photos[current_index + 1].id).first()
            
            if photo1 and photo2:
                photo1.ordre, photo2.ordre = photo2.ordre, photo1.ordre
                session.commit()


def _delete_photo(recette, photo):
    """Delete a photo"""
    try:
        with get_db_session() as session:
            # Remove from database
            photo_obj = session.query(Photo).filter(Photo.id == photo.id).first()
            if photo_obj:
                session.delete(photo_obj)
                session.commit()
            
            # Remove file
            if os.path.exists(photo.chemin):
                os.remove(photo.chemin)
            
            # Reorganize orders
            _reorganize_photo_orders(session, recette.id)
        
        st.success("📷 Photo supprimée avec succès!")
        
    except Exception as e:
        st.error(f"Erreur lors de la suppression: {str(e)}")


def _reorganize_photo_orders(session, recette_id):
    """Reorganize photo orders to be sequential"""
    photos = session.query(Photo).filter(Photo.recette_id == recette_id).order_by(Photo.ordre).all()
    for i, photo in enumerate(photos):
        photo.ordre = i
    session.commit()