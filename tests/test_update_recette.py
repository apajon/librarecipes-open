from src.crud.recettes import update_recette
from src.db import SessionLocal

updated_data = {
    "nom": "Pâtes pesto revisitées",
    "ingredients": [
        {"nom": "Pâtes complètes", "quantite": 400, "unite": "g"},
        {"nom": "Pesto rosso", "quantite": 120, "unite": "g"},
    ],
    "etapes": ["Faire bouillir l’eau.", "Cuire les pâtes.", "Ajouter le pesto rosso hors du feu."],
    "source": {"type": "homemade"},
}

with SessionLocal() as session:
    update_recette(session, recette_id="id-de-ta-recette", data=updated_data)
