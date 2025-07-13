import os
import uuid
from pathlib import Path
from urllib.parse import unquote

import streamlit as st
from PIL import Image
from streamlit_tags import st_tags

from src.crud.metadata import get_all_categories, get_all_tags
from src.crud.recettes import get_recette_by_id, update_recette
from src.db import get_db_session


def initialize_session_state_for_modification(recette):
    """Initialise les variables de session avec les données de la recette existante"""
    # Informations générales
    st.session_state.modify_recette_id = recette.id
    st.session_state.modify_recette_nom = recette.nom
    st.session_state.modify_recette_preparation = recette.preparation or 15
    st.session_state.modify_recette_cuisson = recette.cuisson or 0
    st.session_state.modify_recette_portions = recette.portions or 4

    # Catégories et tags
    st.session_state.modify_recette_categories = [c.nom for c in recette.categories]
    st.session_state.modify_recette_tags = [t.nom for t in recette.tags]

    # Source
    if recette.source:
        source_data = {"type": recette.source.type}
        if recette.source.url:
            source_data["url"] = recette.source.url
        if recette.source.book_title:
            source_data["book_title"] = recette.source.book_title
        if recette.source.book_authors:
            source_data["book_authors"] = recette.source.book_authors
        if recette.source.book_page:
            source_data["book_page"] = recette.source.book_page
        st.session_state.modify_recette_source = source_data
    else:
        st.session_state.modify_recette_source = {"type": "homemade"}

    # Ingrédients - Force la réinitialisation avec les données de la recette
    st.session_state.modify_ingredients_list = []
    for ing in recette.ingredients:
        ingredient = {
            "nom": ing.nom,
            "quantite": ing.quantite,
            "unite": ing.unite,
            "indispensable": ing.indispensable,
            "alternatives": ing.alternatives,
        }
        st.session_state.modify_ingredients_list.append(ingredient)

    # Étapes - Force la réinitialisation avec les données de la recette
    st.session_state.modify_etapes_list = []
    for etape in sorted(recette.etapes, key=lambda e: e.ordre):
        st.session_state.modify_etapes_list.append(etape.description)

    # Photos - Initialise seulement si pas déjà présent (pour les nouvelles photos)
    if "modify_photos_list" not in st.session_state:
        st.session_state.modify_photos_list = []


def gestion_ingredients_modification():
    """Gestion des ingrédients pour la modification de recette"""
    st.subheader("🧂 Ingrédients")

    # Protection : s'assurer que la liste existe
    if "modify_ingredients_list" not in st.session_state:
        st.session_state.modify_ingredients_list = []

    # Formulaire pour ajouter un ingrédient
    with st.expander("➕ Ajouter un ingrédient", expanded=False):
        col1, col2, col3 = st.columns([3, 1, 1])

        with col1:
            nom_ingredient = st.text_input("Nom de l'ingrédient", key="modify_new_ingredient_nom")
        with col2:
            quantite = st.number_input("Quantité", min_value=0.0, step=0.1, key="modify_new_ingredient_quantite")
        with col3:
            unite = st.selectbox(
                "Unité",
                ["", "g", "kg", "ml", "cl", "l", "c. à c.", "c. à s.", "pièce(s)", "gousse(s)", "pincée(s)"],
                key="modify_new_ingredient_unite",
            )

        col4, col5 = st.columns([1, 1])
        with col4:
            indispensable = st.checkbox("Indispensable", value=True, key="modify_new_ingredient_indispensable")
        with col5:
            alternatives = st.text_input("Alternatives (séparées par ;)", key="modify_new_ingredient_alternatives")

        if st.button("Ajouter l'ingrédient", key="modify_add_ingredient"):
            if nom_ingredient.strip():
                ingredient = {
                    "nom": nom_ingredient.strip(),
                    "quantite": quantite if quantite > 0 else None,
                    "unite": unite if unite else None,
                    "indispensable": indispensable,
                    "alternatives": alternatives.strip() if alternatives.strip() else None,
                }
                st.session_state.modify_ingredients_list.append(ingredient)
                st.rerun()
            else:
                st.error("Le nom de l'ingrédient est obligatoire")

    # Affichage de la liste des ingrédients
    if st.session_state.modify_ingredients_list:
        st.markdown("**Liste des ingrédients :**")
        for i, ing in enumerate(st.session_state.modify_ingredients_list):
            col1, col2 = st.columns([4, 1])
            with col1:
                quantite_text = f"{ing['quantite']} {ing['unite'] or ''}" if ing["quantite"] else ""
                indispensable_text = " (🟢 indispensable)" if ing["indispensable"] else " (🔄 optionnel)"
                alternatives_text = f" - Alternatives: {ing['alternatives']}" if ing["alternatives"] else ""
                st.write(f"• {ing['nom']} {quantite_text}{indispensable_text}{alternatives_text}")
            with col2:
                if st.button("🗑️", key=f"modify_del_ingredient_{i}"):
                    st.session_state.modify_ingredients_list.pop(i)
                    st.rerun()


def gestion_etapes_modification():
    """Gestion des étapes pour la modification de recette"""
    st.subheader("📝 Étapes de préparation")

    # Protection : s'assurer que la liste existe
    if "modify_etapes_list" not in st.session_state:
        st.session_state.modify_etapes_list = []

    # Formulaire pour ajouter une étape
    with st.expander("➕ Ajouter une étape", expanded=False):
        description_etape = st.text_area("Description de l'étape", key="modify_new_etape_description")

        if st.button("Ajouter l'étape", key="modify_add_etape"):
            if description_etape.strip():
                st.session_state.modify_etapes_list.append(description_etape.strip())
                st.rerun()
            else:
                st.error("La description de l'étape est obligatoire")

    # Affichage de la liste des étapes
    if st.session_state.modify_etapes_list:
        st.markdown("**Étapes de préparation :**")
        for i, etape in enumerate(st.session_state.modify_etapes_list):
            col1, col2, col3 = st.columns([3, 1, 1])
            with col1:
                st.write(f"**Étape {i+1}** : {etape}")  # noqa E226
            with col2:
                if i > 0 and st.button("⬆️", key=f"modify_up_etape_{i}"):
                    st.session_state.modify_etapes_list[i], st.session_state.modify_etapes_list[i - 1] = (
                        st.session_state.modify_etapes_list[i - 1],
                        st.session_state.modify_etapes_list[i],
                    )
                    st.rerun()
            with col3:
                if st.button("🗑️", key=f"modify_del_etape_{i}"):
                    st.session_state.modify_etapes_list.pop(i)
                    st.rerun()


def gestion_photos_modification():
    """Gestion des photos pour la modification de recette"""
    st.subheader("📷 Photos")

    # Protection : s'assurer que la liste existe
    if "modify_photos_list" not in st.session_state:
        st.session_state.modify_photos_list = []

    uploaded_photos = st.file_uploader(
        "Ajouter des photos", type=["png", "jpg", "jpeg"], accept_multiple_files=True, key="modify_photos_uploader"
    )

    if uploaded_photos:
        for uploaded_photo in uploaded_photos:
            if uploaded_photo not in [p["file"] for p in st.session_state.modify_photos_list]:
                categorie = st.selectbox(
                    f"Catégorie pour {uploaded_photo.name}",
                    ["final", "cuisson", "ingrédient", "préparation", "autre"],
                    key=f"modify_cat_{uploaded_photo.name}",
                )

                photo_data = {"file": uploaded_photo, "categorie": categorie, "name": uploaded_photo.name}
                st.session_state.modify_photos_list.append(photo_data)

    # Affichage des photos ajoutées
    if st.session_state.modify_photos_list:
        st.markdown("**Nouvelles photos à ajouter :**")
        for i, photo in enumerate(st.session_state.modify_photos_list):
            col1, col2, col3 = st.columns([2, 2, 1])
            with col1:
                st.image(photo["file"], width=150, caption=photo["name"])
            with col2:
                st.write(f"Catégorie : {photo['categorie']}")
            with col3:
                if st.button("🗑️", key=f"modify_del_photo_{i}"):
                    st.session_state.modify_photos_list.pop(i)
                    st.rerun()


def sauvegarder_photos_modification(recette_id: str) -> list[dict]:
    """Sauvegarde les nouvelles photos sur le disque et retourne la liste des chemins"""
    photos_data = []

    if st.session_state.modify_photos_list:
        # Créer le dossier pour les photos de cette recette
        photos_dir = Path("data") / "photos" / str(recette_id)
        photos_dir.mkdir(parents=True, exist_ok=True)

        for i, photo in enumerate(st.session_state.modify_photos_list):
            # Générer un nom de fichier unique
            file_extension = Path(photo["name"]).suffix
            filename = f"{uuid.uuid4()}{file_extension}"
            filepath = photos_dir / filename

            # Sauvegarder le fichier
            with open(filepath, "wb") as f:
                f.write(photo["file"].read())

            photos_data.append({"chemin": str(filepath), "categorie": photo["categorie"]})

    return photos_data


def traiter_modification_recette():
    """Traite la modification de la recette en utilisant les données du session state"""
    try:
        # Modification de la recette en base
        with get_db_session() as session:
            # Préparation des photos : récupérer les existantes + ajouter les nouvelles
            photos_finales = []

            # Récupérer les photos existantes
            recette_actuelle = get_recette_by_id(session, st.session_state.modify_recette_id)
            if recette_actuelle:
                photos_finales = [{"chemin": p.chemin, "categorie": p.categorie} for p in recette_actuelle.photos]

            # Ajouter les nouvelles photos si il y en a
            if st.session_state.modify_photos_list:
                nouvelles_photos = sauvegarder_photos_modification(st.session_state.modify_recette_id)
                photos_finales.extend(nouvelles_photos)

            # Préparation des données depuis le session state (avec toutes les photos)
            recette_data = {
                "nom": st.session_state.modify_recette_nom.strip(),
                "preparation": (
                    st.session_state.modify_recette_preparation
                    if st.session_state.modify_recette_preparation > 0
                    else None
                ),
                "cuisson": (
                    st.session_state.modify_recette_cuisson if st.session_state.modify_recette_cuisson > 0 else None
                ),
                "portions": st.session_state.modify_recette_portions,
                "ingredients": st.session_state.modify_ingredients_list,
                "etapes": st.session_state.modify_etapes_list,
                "categories": st.session_state.modify_recette_categories,
                "tags": st.session_state.modify_recette_tags,
                "source": st.session_state.modify_recette_source,
                "photos": photos_finales,  # Inclure toutes les photos dès le départ
            }

            # Modification de la recette en base (UN SEUL APPEL)
            recette_modifiee = update_recette(session, st.session_state.modify_recette_id, recette_data)

        if recette_modifiee:
            st.success(f"✅ Recette '{st.session_state.modify_recette_nom}' modifiée avec succès !")

            # Lien vers la recette modifiée
            from urllib.parse import quote

            url_recette = f"./card_recette?recette_id={quote(str(recette_modifiee.id))}"
            st.markdown(f"[🔍 Voir la recette modifiée]({url_recette})")
        else:
            st.error("Erreur lors de la modification de la recette")
            return False

        # Réinitialiser les variables de session de modification
        keys_to_clear = [k for k in st.session_state.keys() if k.startswith("modify_")]
        for key in keys_to_clear:
            del st.session_state[key]

        return True

    except Exception as e:
        st.error(f"Erreur lors de la modification de la recette : {str(e)}")
        st.exception(e)
        return False


def main():
    st.set_page_config(
        page_title="Modification de recette", page_icon="✏️", layout="wide", initial_sidebar_state="expanded"
    )

    # Affichage du banner
    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_recettes.png")
    if os.path.exists(image_path):
        img = Image.open(image_path)
        st.image(img, use_container_width=True)

    st.sidebar.title("Navigation")
    st.sidebar.markdown("Bienvenue dans l'application de modification de recettes !")

    # Récupération de l'ID de la recette depuis les paramètres d'URL
    params = st.query_params
    recette_id = params.get("recette_id")

    if not recette_id:
        st.error("Aucune recette sélectionnée pour modification.")
        st.markdown("[🔙 Retour à l'accueil](./)")
        return

    # Décodage de l'URL
    recette_id = unquote(recette_id)

    # Récupération de la recette
    with get_db_session() as session:
        recette = get_recette_by_id(session, recette_id)

    if not recette:
        st.error("Recette introuvable.")
        st.markdown("[🔙 Retour à l'accueil](./)")
        return

    st.title(f"✏️ Modification de la recette : {recette.nom}")

    # Navigation - lien de retour vers la recette
    st.markdown("### 🔙 Navigation")
    from urllib.parse import quote

    url_card = f"./card_recette?recette_id={quote(str(recette.id))}"
    st.markdown(f"[📖 Retour à la recette]({url_card})")
    st.markdown("---")

    # Initialiser les variables de session avec les données de la recette SEULEMENT si c'est une nouvelle recette
    # ou si les données n'existent pas encore
    if "modify_recette_id" not in st.session_state or st.session_state.modify_recette_id != recette.id:
        initialize_session_state_for_modification(recette)

    # Section d'aide
    with st.expander("ℹ️ Aide - Comment modifier une recette", expanded=False):
        st.markdown(
            """
        **Étapes pour modifier une recette :**

        1. **Informations générales** : Modifiez le nom, temps de préparation/cuisson et portions
        2. **Classification** : Ajustez les catégories et tags
        3. **Source** : Changez l'origine de la recette
        4. **Ingrédients** : Ajoutez, supprimez ou modifiez les ingrédients
        5. **Étapes** : Modifiez, réorganisez ou ajoutez des étapes
        6. **Photos** : Ajoutez de nouvelles photos (pour gérer les existantes, utilisez la page Photos)
        7. **Enregistrement** : Cliquez sur "Modifier" pour sauvegarder les changements

        💡 **Astuces :**
        - Les données actuelles sont pré-remplies dans le formulaire
        - Pour gérer les photos existantes, utilisez la page "Gérer les photos"
        - Les modifications seront appliquées immédiatement en base de données
        """
        )

    # Récupération des métadonnées existantes
    with get_db_session() as session:
        existing_categories = get_all_categories(session)
        existing_tags = get_all_tags(session)

    # Formulaire principal
    with st.form("modify_recette_form", clear_on_submit=False):

        # Informations de base
        st.subheader("📋 Informations générales")

        col1, col2 = st.columns([2, 1])
        with col1:
            nom = st.text_input(
                "Nom de la recette *",
                placeholder="Ex: Pâtes à la carbonara",
                value=st.session_state.modify_recette_nom,
                key="modify_form_nom",
            )
            # Mettre à jour le session state
            if nom != st.session_state.modify_recette_nom:
                st.session_state.modify_recette_nom = nom
        with col2:
            portions = st.number_input(
                "Nombre de portions",
                min_value=1,
                value=st.session_state.modify_recette_portions,
                key="modify_form_portions",
            )
            if portions != st.session_state.modify_recette_portions:
                st.session_state.modify_recette_portions = portions

        col3, col4 = st.columns([1, 1])
        with col3:
            preparation = st.number_input(
                "Temps de préparation (minutes)",
                min_value=0,
                value=st.session_state.modify_recette_preparation,
                key="modify_form_preparation",
            )
            if preparation != st.session_state.modify_recette_preparation:
                st.session_state.modify_recette_preparation = preparation
        with col4:
            cuisson = st.number_input(
                "Temps de cuisson (minutes)",
                min_value=0,
                value=st.session_state.modify_recette_cuisson,
                key="modify_form_cuisson",
            )
            if cuisson != st.session_state.modify_recette_cuisson:
                st.session_state.modify_recette_cuisson = cuisson

        # Catégories et tags
        st.subheader("🏷️ Classification")

        col5, col6 = st.columns([1, 1])
        with col5:
            categories = st_tags(
                label="Catégories",
                text="Appuyez sur Entrée pour ajouter",
                suggestions=existing_categories,
                maxtags=10,
                key="modify_categories_input",
                value=st.session_state.modify_recette_categories,
            )
            st.session_state.modify_recette_categories = categories
        with col6:
            tags = st_tags(
                label="Tags",
                text="Appuyez sur Entrée pour ajouter",
                suggestions=existing_tags,
                maxtags=15,
                key="modify_tags_input",
                value=st.session_state.modify_recette_tags,
            )
            st.session_state.modify_recette_tags = tags

        # Source
        st.subheader("📚 Source")
        type_source = st.selectbox(
            "Type de source",
            ["homemade", "url", "book"],
            index=["homemade", "url", "book"].index(st.session_state.modify_recette_source.get("type", "homemade")),
        )

        source_data = {"type": type_source}

        if type_source == "url":
            url = st.text_input("URL de la recette", value=st.session_state.modify_recette_source.get("url", ""))
            if url:
                source_data["url"] = url
        elif type_source == "book":
            col7, col8, col9 = st.columns([2, 2, 1])
            with col7:
                book_title = st.text_input(
                    "Titre du livre", value=st.session_state.modify_recette_source.get("book_title", "")
                )
            with col8:
                book_authors = st.text_input(
                    "Auteur(s)", value=st.session_state.modify_recette_source.get("book_authors", "")
                )
            with col9:
                book_page = st.text_input("Page", value=st.session_state.modify_recette_source.get("book_page", ""))

            if book_title:
                source_data["book_title"] = book_title
            if book_authors:
                source_data["book_authors"] = book_authors
            if book_page:
                source_data["book_page"] = book_page

        st.session_state.modify_recette_source = source_data

        # Bouton de soumission du formulaire
        submitted = st.form_submit_button("✏️ Modifier la recette", type="primary")

    # Traitement de la soumission - Messages d'erreur/succès juste après le formulaire
    if submitted:
        # Vérifier que les éléments essentiels sont présents
        if not (st.session_state.modify_recette_nom and st.session_state.modify_recette_nom.strip()):
            st.error("Le nom de la recette est obligatoire !")
        elif not st.session_state.modify_ingredients_list:
            st.error("Au moins un ingrédient est requis !")
        elif not st.session_state.modify_etapes_list:
            st.error("Au moins une étape de préparation est requise !")
        else:
            traiter_modification_recette()

    # Gestion des ingrédients (en dehors du formulaire pour permettre les interactions)
    gestion_ingredients_modification()

    # Gestion des étapes (en dehors du formulaire pour permettre les interactions)
    gestion_etapes_modification()

    # Gestion des photos (en dehors du formulaire pour permettre les interactions)
    gestion_photos_modification()

    # Lien vers la gestion des photos existantes
    st.markdown("---")
    st.subheader("🖼️ Gestion des photos existantes")
    from urllib.parse import quote

    url_photos = f"./photos_recette?recette_id={quote(str(recette.id))}"
    st.markdown(f"[📷 Gérer les photos existantes]({url_photos})")

    # Résumé avant modification
    if st.session_state.modify_ingredients_list and st.session_state.modify_etapes_list:
        with st.expander("📋 Résumé de la recette modifiée", expanded=False):
            st.markdown(f"**Nom :** {st.session_state.modify_recette_nom}")
            st.markdown(f"**Préparation :** {st.session_state.modify_recette_preparation} min")
            st.markdown(f"**Cuisson :** {st.session_state.modify_recette_cuisson} min")
            st.markdown(f"**Portions :** {st.session_state.modify_recette_portions}")

            if st.session_state.modify_recette_categories:
                st.markdown(f"**Catégories :** {', '.join(st.session_state.modify_recette_categories)}")
            if st.session_state.modify_recette_tags:
                st.markdown(f"**Tags :** {', '.join(st.session_state.modify_recette_tags)}")

            st.markdown(f"**Ingrédients :** {len(st.session_state.modify_ingredients_list)} ingrédient(s)")
            for ing in st.session_state.modify_ingredients_list:
                quantite_text = f" - {ing['quantite']} {ing['unite'] or ''}" if ing["quantite"] else ""
                st.markdown(f"  • {ing['nom']}{quantite_text}")

            st.markdown(f"**Étapes :** {len(st.session_state.modify_etapes_list)} étape(s)")
            for i, etape in enumerate(st.session_state.modify_etapes_list):
                st.markdown(f"  {i+1}. {etape[:80]}{'...' if len(etape) > 80 else ''}")  # noqa E226

            if st.session_state.modify_photos_list:
                st.markdown(f"**Nouvelles photos :** {len(st.session_state.modify_photos_list)} photo(s) prêtes")

    # Bouton de modification final en bas de page
    st.markdown("---")
    st.subheader("✏️ Finaliser la modification")

    st.info(
        "💡 Vous pouvez modifier votre recette ici après avoir ajusté les éléments "
        "souhaités dans le formulaire principal en haut de page."
    )

    # Affichage d'un résumé rapide des données saisies
    col1, col2, col3 = st.columns([1, 1, 1])
    with col1:
        st.metric("Ingrédients", len(st.session_state.modify_ingredients_list))
    with col2:
        st.metric("Étapes", len(st.session_state.modify_etapes_list))
    with col3:
        st.metric("Nouvelles photos", len(st.session_state.modify_photos_list))

    # Récupérer les données du formulaire depuis les variables de session (si elles existent)
    with st.form("modify_recette_form_final", clear_on_submit=False):

        # Bouton de soumission final
        submitted_final = st.form_submit_button(
            "🚀 Finaliser et enregistrer les modifications", type="primary", use_container_width=True
        )

    # Traitement de la soumission finale
    if submitted_final:
        # Vérifier que les éléments essentiels sont présents
        if not (st.session_state.modify_recette_nom and st.session_state.modify_recette_nom.strip()):
            st.error("❌ Le nom de la recette est obligatoire ! Remplissez le formulaire principal en haut de page.")
        elif not st.session_state.modify_ingredients_list:
            st.error("❌ Au moins un ingrédient est requis !")
        elif not st.session_state.modify_etapes_list:
            st.error("❌ Au moins une étape de préparation est requise !")
        else:
            # Utiliser la même fonction de traitement
            traiter_modification_recette()

    # Navigation en bas de page
    st.markdown("---")
    st.markdown("### 🔙 Navigation")
    from urllib.parse import quote

    url_card_bottom = f"./card_recette?recette_id={quote(str(recette.id))}"
    st.markdown(f"[📖 Retour à la recette]({url_card_bottom})")


if __name__ == "__main__":
    main()
