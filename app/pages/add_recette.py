import os
import uuid
from pathlib import Path

import streamlit as st
from PIL import Image
from streamlit_tags import st_tags

from src.crud.metadata import get_all_categories, get_all_tags
from src.crud.recettes import create_recette
from src.db import get_db_session


def initialize_session_state():
    """Initialise les variables de session nécessaires"""
    if "ingredients_list" not in st.session_state:
        st.session_state.ingredients_list = []
    if "etapes_list" not in st.session_state:
        st.session_state.etapes_list = []
    if "photos_list" not in st.session_state:
        st.session_state.photos_list = []


def gestion_ingredients():
    """Gestion des ingrédients de la recette"""
    st.subheader("🧂 Ingrédients")

    # Formulaire pour ajouter un ingrédient
    with st.expander("➕ Ajouter un ingrédient", expanded=len(st.session_state.ingredients_list) == 0):
        col1, col2, col3 = st.columns([3, 1, 1])

        with col1:
            nom_ingredient = st.text_input("Nom de l'ingrédient", key="new_ingredient_nom")
        with col2:
            quantite = st.number_input("Quantité", min_value=0.0, step=0.1, key="new_ingredient_quantite")
        with col3:
            unite = st.selectbox(
                "Unité",
                ["", "g", "kg", "ml", "cl", "l", "c. à c.", "c. à s.", "pièce(s)", "gousse(s)", "pincée(s)"],
                key="new_ingredient_unite",
            )

        col4, col5 = st.columns([1, 1])
        with col4:
            indispensable = st.checkbox("Indispensable", value=True, key="new_ingredient_indispensable")
        with col5:
            alternatives = st.text_input("Alternatives (séparées par ;)", key="new_ingredient_alternatives")

        if st.button("Ajouter l'ingrédient"):
            if nom_ingredient.strip():
                ingredient = {
                    "nom": nom_ingredient.strip(),
                    "quantite": quantite if quantite > 0 else None,
                    "unite": unite if unite else None,
                    "indispensable": indispensable,
                    "alternatives": alternatives.strip() if alternatives.strip() else None,
                }
                st.session_state.ingredients_list.append(ingredient)
                st.rerun()
            else:
                st.error("Le nom de l'ingrédient est obligatoire")

    # Affichage de la liste des ingrédients
    if st.session_state.ingredients_list:
        st.markdown("**Liste des ingrédients :**")
        for i, ing in enumerate(st.session_state.ingredients_list):
            col1, col2 = st.columns([4, 1])
            with col1:
                quantite_text = f"{ing['quantite']} {ing['unite'] or ''}" if ing["quantite"] else ""
                indispensable_text = " (🟢 indispensable)" if ing["indispensable"] else " (🔄 optionnel)"
                alternatives_text = f" - Alternatives: {ing['alternatives']}" if ing["alternatives"] else ""
                st.write(f"• {ing['nom']} {quantite_text}{indispensable_text}{alternatives_text}")
            with col2:
                if st.button("🗑️", key=f"del_ingredient_{i}"):
                    st.session_state.ingredients_list.pop(i)
                    st.rerun()


def gestion_etapes():
    """Gestion des étapes de la recette"""
    st.subheader("📝 Étapes de préparation")

    # Formulaire pour ajouter une étape
    with st.expander("➕ Ajouter une étape", expanded=len(st.session_state.etapes_list) == 0):
        description_etape = st.text_area("Description de l'étape", key="new_etape_description")

        if st.button("Ajouter l'étape"):
            if description_etape.strip():
                st.session_state.etapes_list.append(description_etape.strip())
                st.rerun()
            else:
                st.error("La description de l'étape est obligatoire")

    # Affichage de la liste des étapes
    if st.session_state.etapes_list:
        st.markdown("**Étapes de préparation :**")
        for i, etape in enumerate(st.session_state.etapes_list):
            col1, col2, col3 = st.columns([3, 1, 1])
            with col1:
                st.write(f"**Étape {i+1}** : {etape}")  # noqa E226
            with col2:
                if i > 0 and st.button("⬆️", key=f"up_etape_{i}"):
                    st.session_state.etapes_list[i], st.session_state.etapes_list[i - 1] = (
                        st.session_state.etapes_list[i - 1],
                        st.session_state.etapes_list[i],
                    )
                    st.rerun()
            with col3:
                if st.button("🗑️", key=f"del_etape_{i}"):
                    st.session_state.etapes_list.pop(i)
                    st.rerun()


def gestion_photos():
    """Gestion des photos de la recette"""
    st.subheader("📷 Photos")

    uploaded_photos = st.file_uploader(
        "Ajouter des photos", type=["png", "jpg", "jpeg"], accept_multiple_files=True, key="photos_uploader"
    )

    if uploaded_photos:
        for uploaded_photo in uploaded_photos:
            if uploaded_photo not in [p["file"] for p in st.session_state.photos_list]:
                categorie = st.selectbox(
                    f"Catégorie pour {uploaded_photo.name}",
                    ["final", "cuisson", "ingrédient", "préparation", "autre"],
                    key=f"cat_{uploaded_photo.name}",
                )

                photo_data = {"file": uploaded_photo, "categorie": categorie, "name": uploaded_photo.name}
                st.session_state.photos_list.append(photo_data)

    # Affichage des photos ajoutées
    if st.session_state.photos_list:
        st.markdown("**Photos ajoutées :**")
        for i, photo in enumerate(st.session_state.photos_list):
            col1, col2, col3 = st.columns([2, 2, 1])
            with col1:
                st.image(photo["file"], width=150, caption=photo["name"])
            with col2:
                st.write(f"Catégorie : {photo['categorie']}")
            with col3:
                if st.button("🗑️", key=f"del_photo_{i}"):
                    st.session_state.photos_list.pop(i)
                    st.rerun()


def sauvegarder_photos(recette_id: str) -> list[dict]:
    """Sauvegarde les photos sur le disque et retourne la liste des chemins"""
    photos_data = []

    if st.session_state.photos_list:
        # Créer le dossier pour les photos de cette recette
        photos_dir = Path("data") / "photos" / str(recette_id)
        photos_dir.mkdir(parents=True, exist_ok=True)

        for i, photo in enumerate(st.session_state.photos_list):
            # Générer un nom de fichier unique
            file_extension = Path(photo["name"]).suffix
            filename = f"{uuid.uuid4()}{file_extension}"
            filepath = photos_dir / filename

            # Sauvegarder le fichier
            with open(filepath, "wb") as f:
                f.write(photo["file"].read())

            photos_data.append({"chemin": str(filepath), "categorie": photo["categorie"]})

    return photos_data


def main():
    st.set_page_config(page_title="Ajout de recette", page_icon="🍽️", layout="wide", initial_sidebar_state="expanded")

    # Affichage du banner
    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_recettes.png")
    if os.path.exists(image_path):
        img = Image.open(image_path)
        st.image(img, use_container_width=True)

    st.sidebar.title("Navigation")
    st.sidebar.markdown("Bienvenue dans l'application d'ajout de recettes !")

    st.title("➕ Ajout d'une nouvelle recette")

    # Section d'aide
    with st.expander("ℹ️ Aide - Comment ajouter une recette", expanded=False):
        st.markdown(
            """
        **Étapes pour ajouter une recette :**

        1. **Informations générales** : Remplissez le nom (obligatoire), temps de préparation/cuisson et portions
        2. **Classification** : Ajoutez des catégories (Plat, Dessert, etc.) et des tags (végé, rapide, etc.)
        3. **Source** : Indiquez l'origine de la recette (maison, URL, livre)
        4. **Ingrédients** : Listez tous les ingrédients avec quantités et unités
        5. **Étapes** : Décrivez chaque étape de préparation dans l'ordre
        6. **Photos** : Ajoutez des photos (optionnel) avec leur catégorie
        7. **Enregistrement** : Cliquez sur "Enregistrer" pour créer la recette

        💡 **Astuces :**
        - Les suggestions de catégories/tags sont basées sur vos recettes existantes
        - Vous pouvez réorganiser les étapes avec les boutons ⬆️
        - Les ingrédients peuvent être marqués comme optionnels avec des alternatives
        """
        )

    # Initialiser les variables de session
    initialize_session_state()

    # Récupération des métadonnées existantes
    with get_db_session() as session:
        existing_categories = get_all_categories(session)
        existing_tags = get_all_tags(session)

    # Formulaire principal
    with st.form("recette_form", clear_on_submit=False):

        # Informations de base
        st.subheader("📋 Informations générales")

        col1, col2 = st.columns([2, 1])
        with col1:
            nom = st.text_input("Nom de la recette *", placeholder="Ex: Pâtes à la carbonara")
        with col2:
            portions = st.number_input("Nombre de portions", min_value=1, value=4)

        col3, col4 = st.columns([1, 1])
        with col3:
            preparation = st.number_input("Temps de préparation (minutes)", min_value=0, value=15)
        with col4:
            cuisson = st.number_input("Temps de cuisson (minutes)", min_value=0, value=0)

        # Catégories et tags
        st.subheader("🏷️ Classification")

        col5, col6 = st.columns([1, 1])
        with col5:
            categories = st_tags(
                label="Catégories",
                text="Appuyez sur Entrée pour ajouter",
                suggestions=existing_categories,
                maxtags=10,
                key="categories_input",
            )
        with col6:
            tags = st_tags(
                label="Tags",
                text="Appuyez sur Entrée pour ajouter",
                suggestions=existing_tags,
                maxtags=15,
                key="tags_input",
            )

        # Source
        st.subheader("📚 Source")
        type_source = st.selectbox("Type de source", ["homemade", "url", "book"])

        source_data = {"type": type_source}

        if type_source == "url":
            url = st.text_input("URL de la recette")
            if url:
                source_data["url"] = url
        elif type_source == "book":
            col7, col8, col9 = st.columns([2, 2, 1])
            with col7:
                book_title = st.text_input("Titre du livre")
            with col8:
                book_authors = st.text_input("Auteur(s)")
            with col9:
                book_page = st.text_input("Page")

            if book_title:
                source_data["book_title"] = book_title
            if book_authors:
                source_data["book_authors"] = book_authors
            if book_page:
                source_data["book_page"] = book_page

        # Bouton de soumission du formulaire
        submitted = st.form_submit_button("💾 Enregistrer la recette", type="primary")

    # Gestion des ingrédients (en dehors du formulaire pour permettre les interactions)
    gestion_ingredients()

    # Gestion des étapes (en dehors du formulaire pour permettre les interactions)
    gestion_etapes()

    # Gestion des photos (en dehors du formulaire pour permettre les interactions)
    gestion_photos()

    # Résumé avant soumission
    if st.session_state.ingredients_list and st.session_state.etapes_list:
        with st.expander("📋 Résumé de la recette à créer", expanded=False):
            st.markdown(f"**Ingrédients :** {len(st.session_state.ingredients_list)} ingrédient(s)")
            for ing in st.session_state.ingredients_list:
                quantite_text = f" - {ing['quantite']} {ing['unite'] or ''}" if ing["quantite"] else ""
                st.markdown(f"  • {ing['nom']}{quantite_text}")

            st.markdown(f"**Étapes :** {len(st.session_state.etapes_list)} étape(s)")
            for i, etape in enumerate(st.session_state.etapes_list):
                st.markdown(f"  {i+1}. {etape[:80]}{'...' if len(etape) > 80 else ''}")  # noqa E226

            if st.session_state.photos_list:
                st.markdown(f"**Photos :** {len(st.session_state.photos_list)} photo(s) prêtes")

    # Traitement de la soumission
    if submitted:
        if not nom.strip():
            st.error("Le nom de la recette est obligatoire !")
        elif not st.session_state.ingredients_list:
            st.error("Au moins un ingrédient est requis !")
        elif not st.session_state.etapes_list:
            st.error("Au moins une étape de préparation est requise !")
        else:
            try:
                # Préparation des données
                recette_data = {
                    "nom": nom.strip(),
                    "preparation": preparation if preparation > 0 else None,
                    "cuisson": cuisson if cuisson > 0 else None,
                    "portions": portions,
                    "ingredients": st.session_state.ingredients_list,
                    "etapes": st.session_state.etapes_list,
                    "categories": categories,
                    "tags": tags,
                    "source": source_data,
                }

                # Création de la recette en base
                with get_db_session() as session:
                    nouvelle_recette = create_recette(session, recette_data)

                    # Sauvegarde des photos si il y en a
                    if st.session_state.photos_list:
                        photos_data = sauvegarder_photos(str(nouvelle_recette.id))
                        # Mise à jour de la recette avec les photos
                        if photos_data:
                            recette_data["photos"] = photos_data
                            from src.crud.recettes import update_recette

                            update_recette(session, str(nouvelle_recette.id), {"photos": photos_data})

                st.success(f"✅ Recette '{nom}' créée avec succès !")

                # Lien vers la recette créée
                from urllib.parse import quote

                url_recette = f"./card_recette?recette_id={quote(str(nouvelle_recette.id))}"
                st.markdown(f"[🔍 Voir la recette créée]({url_recette})")

                # Réinitialiser les variables de session
                st.session_state.ingredients_list = []
                st.session_state.etapes_list = []
                st.session_state.photos_list = []

                # Proposer de créer une nouvelle recette
                if st.button("➕ Créer une nouvelle recette"):
                    st.rerun()

            except Exception as e:
                st.error(f"Erreur lors de la création de la recette : {str(e)}")
                st.exception(e)


if __name__ == "__main__":
    main()
