import os

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
    # Nouvelles variables pour stocker les données du formulaire principal
    if "recette_nom" not in st.session_state:
        st.session_state.recette_nom = ""
    if "recette_preparation" not in st.session_state:
        st.session_state.recette_preparation = 15
    if "recette_cuisson" not in st.session_state:
        st.session_state.recette_cuisson = 0
    if "recette_portions" not in st.session_state:
        st.session_state.recette_portions = 4


def gestion_ingredients():
    """Gestion des ingrédients de la recette"""
    st.subheader("🥘 Ingrédients")

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
                etape = {
                    "numero": len(st.session_state.etapes_list) + 1,
                    "description": description_etape.strip(),
                }
                st.session_state.etapes_list.append(etape)
                st.rerun()
            else:
                st.error("La description de l'étape est obligatoire")

    # Affichage de la liste des étapes
    if st.session_state.etapes_list:
        st.markdown("**Liste des étapes :**")
        for i, etape in enumerate(st.session_state.etapes_list):
            col1, col2 = st.columns([4, 1])
            with col1:
                st.write(f"**{etape['numero']}.** {etape['description']}")
            with col2:
                if st.button("🗑️", key=f"del_etape_{i}"):
                    st.session_state.etapes_list.pop(i)
                    # Renuméroter les étapes
                    for j, e in enumerate(st.session_state.etapes_list):
                        e["numero"] = j + 1
                    st.rerun()


def add_recette_page():
    """Page d'ajout d'une nouvelle recette"""
    # Affichage du banner
    image_path = os.path.join(os.path.dirname(__file__), "../assets", "banner_recettes.png")
    if os.path.exists(image_path):
        img = Image.open(image_path)
        st.image(img, use_container_width=True)

    st.title("➕ Ajout d'une nouvelle recette")

    # Section d'aide
    with st.expander("ℹ️ Aide - Comment ajouter une recette", expanded=False):
        st.markdown(
            """
        **Étapes pour ajouter une recette :**

        1. **Informations générales** : Remplissez le nom (obligatoire), temps de préparation/cuisson et portions
        2. **Classification** : Ajoutez des catégories (Plat, Dessert, etc.) et des tags (végé, rapide, etc.)
        3. **Ingrédients** : Listez tous les ingrédients avec quantités et unités
        4. **Étapes** : Décrivez chaque étape de préparation dans l'ordre
        5. **Enregistrement** : Cliquez sur "Enregistrer" pour créer la recette

        💡 **Astuces :**
        - Les suggestions de catégories/tags sont basées sur vos recettes existantes
        - Vous pouvez supprimer des éléments avec les boutons 🗑️
        - Les ingrédients peuvent être marqués comme optionnels avec des alternatives
        """
        )

    # Initialiser les variables de session
    initialize_session_state()

    # Formulaire principal
    st.subheader("ℹ️ Informations générales")

    nom = st.text_input("Nom de la recette *", value=st.session_state.recette_nom)

    col1, col2, col3 = st.columns(3)
    with col1:
        temps_preparation = st.number_input(
            "Temps de préparation (min)", min_value=0, value=st.session_state.recette_preparation
        )
    with col2:
        temps_cuisson = st.number_input("Temps de cuisson (min)", min_value=0, value=st.session_state.recette_cuisson)
    with col3:
        nombre_portions = st.number_input("Nombre de portions", min_value=1, value=st.session_state.recette_portions)

    # Classification
    st.subheader("🏷️ Classification")

    with get_db_session() as session:
        existing_categories = get_all_categories(session)
        existing_tags = get_all_tags(session)

    categories = st_tags(
        label="Catégories",
        text="Appuyez sur Entrée pour ajouter",
        suggestions=existing_categories,
        key="categories_input",
    )

    tags = st_tags(label="Tags", text="Appuyez sur Entrée pour ajouter", suggestions=existing_tags, key="tags_input")

    # Source
    st.subheader("📚 Source")
    source_type = st.radio("Type de source", ["Maison", "URL", "Livre/Magazine"])

    source_url = ""
    book_title = ""
    book_authors = ""
    book_page = ""

    if source_type == "URL":
        source_url = st.text_input("URL de la recette")
    elif source_type == "Livre/Magazine":
        book_title = st.text_input("Titre du livre/magazine")
        book_authors = st.text_input("Auteur(s)")
        book_page = st.text_input("Page (optionnel)")

    # Gestion des ingrédients et étapes
    gestion_ingredients()
    gestion_etapes()

    # Bouton d'enregistrement
    st.divider()
    if st.button("💾 Enregistrer la recette", type="primary"):
        if not nom.strip():
            st.error("Le nom de la recette est obligatoire")
        elif not st.session_state.ingredients_list:
            st.error("Au moins un ingrédient est requis")
        elif not st.session_state.etapes_list:
            st.error("Au moins une étape est requise")
        else:
            try:
                with get_db_session() as session:
                    # Préparer les données de source
                    source_data = None
                    if source_type == "URL" and source_url:
                        source_data = {"type": "url", "url": source_url}
                    elif source_type == "Maison":
                        source_data = {"type": "homemade"}
                    elif source_type == "Livre/Magazine" and book_title:
                        source_data = {
                            "type": "book",
                            "book_title": book_title,
                            "book_authors": book_authors if book_authors else None,
                            "book_page": book_page if book_page else None,
                        }

                    recette_data = {
                        "nom": nom.strip(),
                        "preparation": temps_preparation,
                        "cuisson": temps_cuisson,
                        "portions": nombre_portions,
                        "categories": categories,
                        "tags": tags,
                        "source": source_data,
                        "ingredients": st.session_state.ingredients_list,
                        "etapes": st.session_state.etapes_list,
                    }

                    nouvelle_recette = create_recette(session, recette_data)
                    st.success(f"✅ Recette '{nouvelle_recette.nom}' créée avec succès !")

                    # Réinitialiser le formulaire
                    for key in list(st.session_state.keys()):
                        if key.startswith(("ingredients_list", "etapes_list", "photos_list", "recette_")):
                            del st.session_state[key]

                    st.rerun()

            except Exception as e:
                st.error(f"Erreur lors de la création de la recette : {str(e)}")
