"""Utilitaires pour l'affichage des recettes"""

from streamlit_app.utils.constants import SOURCE_TYPES


def format_recette_display_name(recette):
    """Formate le nom d'affichage de la recette pour éviter les doublons"""
    display_name = recette.nom

    # Ajouter le type de source si disponible
    if recette.source and recette.source.type:
        source_type = SOURCE_TYPES.get(recette.source.type, recette.source.type)
        display_name += f" ({source_type})"

    # Ajouter la date d'ajout
    if recette.date_ajout:
        date_str = recette.date_ajout.strftime("%d/%m/%Y")
        display_name += f" - {date_str}"

    # Ajouter la dernière exécution si disponible
    if recette.executions:
        last_execution = max(recette.executions, key=lambda x: x.date_execution)
        last_exec_str = last_execution.date_execution.strftime("%d/%m/%Y")
        display_name += f" (dernière: {last_exec_str})"

    return display_name
