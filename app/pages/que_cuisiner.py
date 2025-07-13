import random
from datetime import datetime
from urllib.parse import quote

import streamlit as st

from src.db import get_db_session
from src.model import Recette


def get_ingredient_season_score(ingredient_nom, current_month):
    """Retourne un score de saisonnalité pour un ingrédient (1.0 = très de saison, 0.3 = hors saison)"""

    # Dictionnaire des saisons par ingrédient (mois 1-12)
    ingredients_saison = {
        # Légumes d'été (juin-septembre)
        "tomate": [6, 7, 8, 9],
        "courgette": [6, 7, 8, 9],
        "aubergine": [7, 8, 9],
        "poivron": [7, 8, 9],
        "concombre": [6, 7, 8],
        "basilic": [6, 7, 8, 9],
        # Légumes d'automne (septembre-novembre)
        "potiron": [9, 10, 11],
        "courge": [9, 10, 11, 12],
        "champignon": [9, 10, 11],
        "châtaigne": [10, 11],
        # Légumes d'hiver (décembre-février)
        "chou": [11, 12, 1, 2],
        "poireau": [10, 11, 12, 1, 2],
        "endive": [11, 12, 1, 2],
        "navet": [10, 11, 12, 1],
        # Légumes de printemps (mars-mai)
        "asperge": [3, 4, 5],
        "artichaut": [4, 5, 6],
        "petit pois": [4, 5, 6],
        "radis": [3, 4, 5],
        "épinard": [3, 4, 5, 9, 10],
        # Fruits d'été
        "fraise": [5, 6, 7],
        "cerise": [6, 7],
        "abricot": [6, 7, 8],
        "pêche": [7, 8, 9],
        "melon": [7, 8, 9],
        # Fruits d'automne
        "pomme": [9, 10, 11, 12],
        "poire": [8, 9, 10, 11],
        "raisin": [8, 9, 10],
        # Fruits d'hiver
        "orange": [11, 12, 1, 2, 3],
        "citron": [11, 12, 1, 2],
        "mandarine": [11, 12, 1],
        # Ingrédients disponibles toute l'année (score neutre)
        "ail": list(range(1, 13)),
        "oignon": list(range(1, 13)),
        "carotte": list(range(1, 13)),
        "pomme de terre": list(range(1, 13)),
    }

    # Normaliser le nom de l'ingrédient
    ingredient_lower = ingredient_nom.lower().strip()

    # Chercher l'ingrédient dans le dictionnaire
    for ingredient_key, mois_saison in ingredients_saison.items():
        if ingredient_key in ingredient_lower:
            if current_month in mois_saison:
                return 1.5  # Très de saison
            else:
                return 0.7  # Hors saison

    # Si l'ingrédient n'est pas trouvé, score neutre
    return 1.0


def calculate_recipe_score(recette, current_date):
    """Calcule un score de probabilité pour une recette basé sur plusieurs critères"""

    score = 1.0

    # 1. Score basé sur la dernière exécution
    if hasattr(recette, "derniere_execution") and recette.derniere_execution:
        jours_depuis_execution = (current_date - recette.derniere_execution).days
        if jours_depuis_execution > 90:  # Plus de 3 mois
            score *= 3.0
        elif jours_depuis_execution > 30:  # Plus d'1 mois
            score *= 2.0
        elif jours_depuis_execution > 7:  # Plus d'1 semaine
            score *= 1.5
        else:  # Récemment exécutée
            score *= 0.3
    else:
        # Jamais exécutée = bonus important
        score *= 2.5

    # 2. Score de saisonnalité des ingrédients
    if recette.ingredients:
        scores_ingredients = []
        current_month = current_date.month

        for ingredient in recette.ingredients:
            ingredient_score = get_ingredient_season_score(ingredient.nom, current_month)
            scores_ingredients.append(ingredient_score)

        # Moyenne des scores des ingrédients
        if scores_ingredients:
            moyenne_saison = sum(scores_ingredients) / len(scores_ingredients)
            score *= moyenne_saison

    # 3. Bonus pour les recettes rapides (moins de 30 min total)
    temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
    if temps_total <= 30:
        score *= 1.3

    return score


def main():
    st.set_page_config(page_title="Que cuisiner ?", page_icon="🍽️", layout="wide")

    st.title("🍽️ Que cuisiner ?")
    st.markdown("*Laissez-vous inspirer par nos suggestions intelligentes*")

    with get_db_session() as session:
        # Récupérer toutes les recettes
        recettes = session.query(Recette).all()

        if not recettes:
            st.info("Aucune recette trouvée.")
            return

        # Interface utilisateur
        col1, col2 = st.columns([2, 1])

        with col1:
            st.subheader("🎲 Suggestion intelligente")
            st.markdown(
                """
            Notre algorithme prend en compte :
            - ⏰ Le temps écoulé depuis la dernière préparation
            - 🌱 La saisonnalité des ingrédients
            - ⚡ La rapidité de préparation
            """
            )

        with col2:
            # Filtres optionnels
            st.subheader("🔧 Filtres")

            temps_max = st.slider(
                "Temps max (minutes)", min_value=0, max_value=180, value=120, step=15, help="0 = pas de limite"
            )

            categories_disponibles = set()
            for recette in recettes:
                for cat in recette.categories:
                    categories_disponibles.add(cat.nom)

            categorie_filtre = st.selectbox("Catégorie", ["Toutes"] + sorted(list(categories_disponibles)))

        # Filtrer les recettes selon les critères
        recettes_filtrees = []
        current_date = datetime.now()

        for recette in recettes:
            # Filtre par temps
            if temps_max > 0:
                temps_total = (recette.preparation or 0) + (recette.cuisson or 0)
                if temps_total > temps_max:
                    continue

            # Filtre par catégorie
            if categorie_filtre != "Toutes":
                categories_recette = [cat.nom for cat in recette.categories]
                if categorie_filtre not in categories_recette:
                    continue

            recettes_filtrees.append(recette)

        if not recettes_filtrees:
            st.warning("Aucune recette ne correspond à vos critères.")
            return

        # Calculer les scores et créer une liste pondérée
        recettes_ponderees = []
        scores_info = {}

        for recette in recettes_filtrees:
            score = calculate_recipe_score(recette, current_date)
            scores_info[recette.id] = score

            # Ajouter la recette plusieurs fois selon son score
            nb_occurrences = max(1, int(score * 10))  # Score entre 0.1 et 10+
            recettes_ponderees.extend([recette] * nb_occurrences)

        # Boutons d'action
        st.markdown("---")
        col_btn1, col_btn2, col_btn3 = st.columns([1, 1, 2])

        with col_btn1:
            if st.button("🎲 Nouvelle suggestion", type="primary", use_container_width=True):
                st.rerun()

        with col_btn2:
            afficher_stats = st.checkbox("📊 Voir les détails")

        # Sélection aléatoire pondérée
        if recettes_ponderees:
            recette_suggeree = random.choice(recettes_ponderees)

            # Affichage de la recette suggérée
            st.markdown("---")
            st.subheader("🍽️ Suggestion du moment")

            # Informations principales
            col_info1, col_info2 = st.columns([2, 1])

            with col_info1:
                url_recette = f"./card_recette?recette_id={quote(str(recette_suggeree.id))}"
                st.markdown(f"## [{recette_suggeree.nom}]({url_recette})")

                # Temps et portions
                infos = []
                if recette_suggeree.preparation:
                    infos.append(f"⏱️ Préparation: {recette_suggeree.preparation} min")
                if recette_suggeree.cuisson:
                    infos.append(f"🔥 Cuisson: {recette_suggeree.cuisson} min")
                if recette_suggeree.portions:
                    infos.append(f"👥 Pour {recette_suggeree.portions} personnes")

                if infos:
                    st.markdown(" • ".join(infos))

                # Catégories
                if recette_suggeree.categories:
                    categories = [cat.nom for cat in recette_suggeree.categories]
                    st.markdown(f"🏷️ **Catégories:** {', '.join(categories)}")

            with col_info2:
                # Raisons de la suggestion
                st.markdown("**🤖 Pourquoi cette suggestion ?**")
                score = scores_info.get(recette_suggeree.id, 1.0)

                raisons = []
                if score > 2.0:
                    raisons.append("📅 Pas préparée récemment")
                if any(
                    get_ingredient_season_score(ing.nom, current_date.month) > 1.2
                    for ing in recette_suggeree.ingredients
                ):
                    raisons.append("🌱 Ingrédients de saison")

                temps_total = (recette_suggeree.preparation or 0) + (recette_suggeree.cuisson or 0)
                if temps_total <= 30:
                    raisons.append("⚡ Rapide à préparer")

                for raison in raisons:
                    st.markdown(f"• {raison}")

                if afficher_stats:
                    st.markdown(f"📊 Score: {score:.2f}")

            # Ingrédients principaux
            if recette_suggeree.ingredients:
                st.markdown("### 🧂 Ingrédients principaux")
                ingredients_affiches = []
                for ingredient in recette_suggeree.ingredients[:8]:  # Max 8 ingrédients
                    saison_score = get_ingredient_season_score(ingredient.nom, current_date.month)
                    emoji_saison = "🌱" if saison_score > 1.2 else "🟡" if saison_score < 0.8 else ""

                    if ingredient.quantite and ingredient.unite:
                        ingredients_affiches.append(
                            f"{emoji_saison} {ingredient.nom} ({ingredient.quantite} {ingredient.unite})"
                        )
                    else:
                        ingredients_affiches.append(f"{emoji_saison} {ingredient.nom}")

                # Afficher en colonnes
                cols_ing = st.columns(2)
                for i, ing in enumerate(ingredients_affiches):
                    with cols_ing[i % 2]:
                        st.markdown(f"• {ing}")

                if len(recette_suggeree.ingredients) > 8:
                    st.markdown(f"*... et {len(recette_suggeree.ingredients) - 8} autres ingrédients*")

            # Boutons d'action
            st.markdown("---")
            col_action1, col_action2, col_action3 = st.columns(3)

            with col_action1:
                url_modify = f"./modify_recette?recette_id={quote(str(recette_suggeree.id))}"
                st.markdown(f"[✏️ Modifier la recette]({url_modify})")

            with col_action2:
                url_photos = f"./photos_recette?recette_id={quote(str(recette_suggeree.id))}"
                st.markdown(f"[📷 Voir les photos]({url_photos})")

            with col_action3:
                st.markdown(f"[📖 Voir la recette complète]({url_recette})")

        # Statistiques si demandées
        if afficher_stats:
            st.markdown("---")
            st.subheader("📊 Statistiques des suggestions")

            # Top 5 des recettes les mieux scorées
            recettes_par_score = sorted(
                [(r, scores_info.get(r.id, 1.0)) for r in recettes_filtrees], key=lambda x: x[1], reverse=True
            )

            st.markdown("**🏆 Top 5 des recettes recommandées:**")
            for i, (recette, score) in enumerate(recettes_par_score[:5], 1):
                url = f"./card_recette?recette_id={quote(str(recette.id))}"
                st.markdown(f"{i}. [{recette.nom}]({url}) (score: {score:.2f})")


if __name__ == "__main__":
    main()
