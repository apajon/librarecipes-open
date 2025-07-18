"""
Utilitaires pour la validation des données de recette
"""

from typing import Any, Dict, List, Tuple


def validate_recipe_data(nom: str, ingredients: List[Dict[str, Any]], etapes: List[str]) -> Tuple[bool, List[str]]:
    """Valide les données d'une recette et retourne les erreurs"""
    errors = []

    if not nom.strip():
        errors.append("Le nom de la recette est obligatoire")

    if not ingredients:
        errors.append("Au moins un ingrédient est requis")

    if not etapes:
        errors.append("Au moins une étape est requise")

    return len(errors) == 0, errors


def prepare_recipe_data(
    nom: str,
    preparation: int,
    cuisson: int,
    portions: int,
    categories: List[str],
    tags: List[str],
    source_data: Dict[str, Any],
    ingredients: List[Dict[str, Any]],
    etapes: List[str],
) -> Dict[str, Any]:
    """Prépare les données de recette pour la sauvegarde"""
    # Convertir les étapes en format attendu par le modèle
    etapes_formatted = []
    for i, description in enumerate(etapes):
        etapes_formatted.append({"numero": i + 1, "description": description})

    return {
        "nom": nom.strip(),
        "preparation": preparation,
        "cuisson": cuisson,
        "portions": portions,
        "categories": categories,
        "tags": tags,
        "source": source_data if source_data.get("type") != "homemade" or len(source_data) > 1 else None,
        "ingredients": ingredients,
        "etapes": etapes_formatted,
    }


class RecipeValidator:
    """Classe pour la validation des recettes"""

    @staticmethod
    def validate_recipe_data(nom: str, ingredients: List[Dict[str, Any]], etapes: List[str]) -> Tuple[bool, List[str]]:
        """Valide les données d'une recette et retourne les erreurs"""
        return validate_recipe_data(nom, ingredients, etapes)

    @staticmethod
    def prepare_recipe_data(
        nom: str,
        preparation: int,
        cuisson: int,
        portions: int,
        categories: List[str],
        tags: List[str],
        ingredients: List[Dict[str, Any]],
        etapes: List[str],
    ) -> Dict[str, Any]:
        """Prépare les données de recette pour la sauvegarde"""
        return prepare_recipe_data(nom, preparation, cuisson, portions, categories, tags, ingredients, etapes)
