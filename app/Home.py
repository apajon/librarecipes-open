"""
Application principale LibraRecipes
Point d'entrée de l'application avec navigation et sidebar
"""

# Configuration et modules principaux
from app.config.app import run_app, setup_navigation, setup_page_config
from app.config.pages import create_page_objects, get_pages_config, setup_session_state_pages
from app.utils.sidebar import setup_sidebar


def main():
    """Fonction principale de l'application"""
    # Configuration de base
    setup_page_config()

    # Configuration des pages
    page_objects = create_page_objects()
    pages = get_pages_config(page_objects)
    setup_session_state_pages(page_objects)

    # Configuration de la sidebar
    setup_sidebar()

    # Navigation et lancement
    navigation = setup_navigation(pages)
    run_app(navigation)


# Exécution directe du script
main()
