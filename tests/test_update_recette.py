import pytest
from src.model import Recette
from src.crud.recettes import update_recette, create_recette
from src.db import get_db_session


def test_update_recette():
    """Test updating an existing recipe."""
    # First create a recipe to update
    initial_data = {
        "nom": "Pâtes pesto basiques",
        "preparation": 10,
        "cuisson": 12,
        "portions": 4,
        "ingredients": [
            {"nom": "Pâtes", "quantite": 400, "unite": "g", "indispensable": True},
            {"nom": "Pesto", "quantite": 100, "unite": "g", "indispensable": True},
        ],
        "etapes": ["Faire bouillir l'eau.", "Cuire les pâtes.", "Mélanger avec le pesto."],
        "categories": ["Plat"],
        "tags": ["basique"],
        "source": {"type": "homemade"},
    }

    updated_data = {
        "nom": "Pâtes pesto revisitées",
        "ingredients": [
            {"nom": "Pâtes complètes", "quantite": 400, "unite": "g"},
            {"nom": "Pesto rosso", "quantite": 120, "unite": "g"},
        ],
        "etapes": ["Faire bouillir l'eau.", "Cuire les pâtes.", "Ajouter le pesto rosso hors du feu."],
        "source": {"type": "homemade"},
    }

    with get_db_session() as session:
        # Create initial recipe
        recette = create_recette(session, initial_data)
        recette_id = str(recette.id)
        
        # Update the recipe
        updated_recette = update_recette(session, recette_id=recette_id, data=updated_data)
        
        assert updated_recette is not None
        assert updated_recette.nom == "Pâtes pesto revisitées"
        assert len(updated_recette.ingredients) == 2
        assert len(updated_recette.etapes) == 3
        assert updated_recette.ingredients[0].nom == "Pâtes complètes"
        assert updated_recette.ingredients[1].nom == "Pesto rosso"