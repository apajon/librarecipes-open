import pytest
from src.crud.recettes import create_recette
from src.db import get_db_session


def test_create_recette():
    """Test creating a new recipe."""
    data = {
        "nom": "Pâtes pesto maison",
        "preparation": 10,
        "cuisson": 12,
        "portions": 4,
        "ingredients": [
            {"nom": "Pâtes", "quantite": 400, "unite": "g", "indispensable": True},
            {"nom": "Pesto", "quantite": 100, "unite": "g", "indispensable": True},
        ],
        "etapes": ["Faire bouillir l'eau.", "Cuire les pâtes.", "Mélanger avec le pesto."],
        "categories": ["Plat", "Rapide"],
        "tags": ["végé"],
        "source": {"type": "homemade"},
    }

    with get_db_session() as session:
        recette = create_recette(session, data)
        assert recette is not None
        assert recette.nom == "Pâtes pesto maison"
        assert recette.preparation == 10
        assert recette.cuisson == 12
        assert recette.portions == 4
        assert len(recette.ingredients) == 2
        assert len(recette.etapes) == 3
