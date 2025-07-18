"""
Composants UI réutilisables pour les formulaires de recettes
"""

from typing import Any, Dict, List, Optional, Tuple

import streamlit as st


def create_ingredient_form(key_prefix: str = "") -> Tuple[str, Optional[float], Optional[str], bool, str]:
    """Crée un formulaire d'ajout d'ingrédient réutilisable"""
    col1, col2, col3 = st.columns([3, 1, 1])

    with col1:
        nom = st.text_input("Nom de l'ingrédient", key=f"{key_prefix}ingredient_nom")
    with col2:
        quantite = st.number_input("Quantité", min_value=0.0, step=0.1, key=f"{key_prefix}ingredient_quantite")
    with col3:
        unite = st.selectbox(
            "Unité",
            ["", "g", "kg", "ml", "cl", "l", "c. à c.", "c. à s.", "pièce(s)", "gousse(s)", "pincée(s)"],
            key=f"{key_prefix}ingredient_unite",
        )

    col4, col5 = st.columns([1, 1])
    with col4:
        indispensable = st.checkbox("Indispensable", value=True, key=f"{key_prefix}ingredient_indispensable")
    with col5:
        alternatives = st.text_input("Alternatives (séparées par ;)", key=f"{key_prefix}ingredient_alternatives")

    return nom, quantite if quantite > 0 else None, unite if unite else None, indispensable, alternatives


def display_ingredient_list(ingredients: List[Dict[str, Any]], key_prefix: str = "") -> None:
    """Affiche la liste des ingrédients avec boutons de suppression"""
    if not ingredients:
        return

    st.markdown("**Liste des ingrédients :**")
    for i, ing in enumerate(ingredients):
        col1, col2 = st.columns([4, 1])
        with col1:
            quantite_text = f"{ing['quantite']} {ing['unite'] or ''}" if ing["quantite"] else ""
            indispensable_text = " (🟢 indispensable)" if ing["indispensable"] else " (🔄 optionnel)"
            alternatives_text = f" - Alternatives: {ing['alternatives']}" if ing["alternatives"] else ""
            st.write(f"• {ing['nom']} {quantite_text}{indispensable_text}{alternatives_text}")
        with col2:
            if st.button("🗑️", key=f"{key_prefix}del_ingredient_{i}"):
                ingredients.pop(i)
                st.rerun()


def create_etape_form(key_prefix: str = "") -> str:
    """Crée un formulaire d'ajout d'étape réutilisable"""
    return st.text_area("Description de l'étape", key=f"{key_prefix}etape_description")


def display_etapes_list(etapes: List[str], key_prefix: str = "", allow_reorder: bool = False) -> None:
    """Affiche la liste des étapes avec boutons de suppression et réordonnancement optionnel"""
    if not etapes:
        return

    st.markdown("**Étapes de préparation :**")
    for i, etape in enumerate(etapes):
        if allow_reorder:
            col1, col2, col3, col4 = st.columns([3, 0.5, 0.5, 0.5])
        else:
            col1, col4 = st.columns([4, 1])

        with col1:
            st.write(f"**Étape {i+1}** : {etape}")  # noqa E226

        if allow_reorder:
            with col2:
                if i > 0 and st.button("⬆️", key=f"{key_prefix}up_etape_{i}"):
                    etapes[i], etapes[i - 1] = etapes[i - 1], etapes[i]
                    st.rerun()
            with col3:
                if i < len(etapes) - 1 and st.button("⬇️", key=f"{key_prefix}down_etape_{i}"):
                    etapes[i], etapes[i + 1] = etapes[i + 1], etapes[i]
                    st.rerun()

        with col4:
            if st.button("🗑️", key=f"{key_prefix}del_etape_{i}"):
                etapes.pop(i)
                st.rerun()


def create_recipe_info_form(default_values: Optional[Dict[str, Any]] = None) -> Tuple[str, int, int, int]:
    """Crée le formulaire d'informations générales de recette"""
    defaults = default_values or {}

    nom = st.text_input("Nom de la recette *", value=defaults.get("nom", ""))

    col1, col2, col3 = st.columns(3)
    with col1:
        preparation = st.number_input("Temps de préparation (min)", min_value=0, value=defaults.get("preparation", 15))
    with col2:
        cuisson = st.number_input("Temps de cuisson (min)", min_value=0, value=defaults.get("cuisson", 0))
    with col3:
        portions = st.number_input("Nombre de portions", min_value=1, value=defaults.get("portions", 4))

    return nom, preparation, cuisson, portions


def create_source_form(default_source: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
    """Crée le formulaire de source de recette"""
    defaults = default_source or {"type": "Maison"}

    # Mapper les types de base de données vers l'affichage UI
    type_mapping = {"homemade": "Maison", "url": "URL", "book": "Livre/Magazine"}

    default_type = type_mapping.get(defaults.get("type", "homemade"), "Maison")
    source_type = st.radio(
        "Type de source",
        ["Maison", "URL", "Livre/Magazine"],
        index=["Maison", "URL", "Livre/Magazine"].index(default_type),
    )

    source_data = {"type": {"Maison": "homemade", "URL": "url", "Livre/Magazine": "book"}[source_type]}

    if source_type == "URL":
        url = st.text_input("URL de la recette", value=defaults.get("url", ""))
        if url:
            source_data["url"] = url
    elif source_type == "Livre/Magazine":
        book_title = st.text_input("Titre du livre/magazine", value=defaults.get("book_title", ""))
        book_authors = st.text_input("Auteur(s)", value=defaults.get("book_authors", ""))
        book_page = st.text_input("Page (optionnel)", value=defaults.get("book_page", ""))

        if book_title:
            source_data["book_title"] = book_title
        if book_authors:
            source_data["book_authors"] = book_authors
        if book_page:
            source_data["book_page"] = book_page

    return source_data


def show_recipe_metrics(recette) -> None:
    """Affiche les métriques d'une recette (temps, portions)"""
    col1, col2, col3 = st.columns(3)
    with col1:
        st.metric("Temps de préparation", f"{recette.preparation or 0} min")
    with col2:
        st.metric("Temps de cuisson", f"{recette.cuisson or 0} min")
    with col3:
        st.metric("Portions", recette.portions or 0)


def create_recipe_navigation_buttons(recette_id: str) -> Tuple[bool, bool, bool]:
    """Crée les boutons de navigation pour une recette et retourne les états des clics"""
    col1, col2, col3 = st.columns([1, 1, 1])

    with col1:
        modify_clicked = st.button("✏️ Modifier cette recette", type="primary", use_container_width=True)

    with col2:
        photos_clicked = st.button("📸 Gérer les photos", type="secondary", use_container_width=True)

    with col3:
        delete_clicked = st.button("🗑️ Supprimer cette recette", type="secondary", use_container_width=True)

    return modify_clicked, photos_clicked, delete_clicked


def create_mobile_button(text: str, key: str, icon: str = "") -> str:
    """Crée un bouton optimisé pour mobile"""
    button_style = """
    <style>
    .mobile-button {
        background-color: #ff6b6b;
        color: white;
        border: none;
        padding: 12px 24px;
        font-size: 16px;
        border-radius: 8px;
        cursor: pointer;
        width: 100%;
        margin: 8px 0;
        touch-action: manipulation;
    }
    .mobile-button:hover {
        background-color: #ff5252;
    }
    </style>
    """

    button_html = f"""
    {button_style}
    <button class="mobile-button" id="{key}">
        {icon} {text}
    </button>
    """

    return button_html


def create_mobile_form(title: str) -> str:
    """Crée un formulaire optimisé pour mobile"""
    form_style = """
    <style>
    .mobile-form {
        background: white;
        border-radius: 12px;
        padding: 20px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        margin: 16px 0;
    }
    .mobile-form h3 {
        margin-top: 0;
        color: #333;
        font-size: 1.2em;
    }
    </style>
    """

    form_html = f"""
    {form_style}
    <div class="mobile-form">
        <h3>{title}</h3>
    </div>
    """

    return form_html
