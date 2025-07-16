"""
Utilitaires pour l'affichage des bandeaux et de l'aide
"""

import os

import streamlit as st
from PIL import Image


def show_banner(banner_name: str) -> None:
    """Affiche un bandeau à partir du dossier assets"""
    image_path = os.path.join(os.path.dirname(__file__), "../assets", banner_name)
    if os.path.exists(image_path):
        img = Image.open(image_path)
        st.image(img, use_container_width=True)


def show_add_recipe_help() -> None:
    """Affiche l'aide pour l'ajout de recette"""
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


def show_photo_management_help() -> None:
    """Affiche l'aide pour la gestion des photos"""
    st.info("💡 **Comment gérer les photos d'une recette :**")
    st.markdown(
        """
    1. Allez dans **'Recherche & Exploration'** pour trouver une recette
    2. Cliquez sur **'👀 Voir'** sur la recette qui vous intéresse
    3. Dans la page de détail, cliquez sur **'📸 Gérer les photos'**

    Ou depuis la page de modification d'une recette, cliquez sur **'📷 Gérer les photos existantes'**
    """
    )
