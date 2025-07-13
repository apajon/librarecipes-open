from model import Recette
from src.crud.recettes import update_recette
from src.db import get_db_session

updated_data = {
    "nom": "Pâtes pesto revisitées",
    "ingredients": [
        {"nom": "Pâtes complètes", "quantite": 400, "unite": "g"},
        {"nom": "Pesto rosso", "quantite": 120, "unite": "g"},
    ],
    "etapes": ["Faire bouillir l’eau.", "Cuire les pâtes.", "Ajouter le pesto rosso hors du feu."],
    "source": {"type": "homemade"},
}

with get_db_session() as session:
    recette = session.query(Recette).first()
    if recette:
        # Update the recette with the new data
        updated_recette = update_recette(session, recette_id=str(recette.id), data=updated_data)
        if updated_recette:
            print(f"Recette '{updated_recette.nom}' mise à jour avec succès.")
        else:
            print("Échec de la mise à jour de la recette.")
    else:
        print("Aucune recette trouvée pour mise à jour.")
