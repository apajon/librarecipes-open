from src.crud.recettes import create_recette
from src.db import get_db_session

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
    create_recette(session, data)
