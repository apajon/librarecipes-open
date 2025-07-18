"""
Module d'initialisation pour LibraRecipes Android
Point d'entrée principal pour l'application Android
"""

from android_bridge import start_streamlit_server, stop_streamlit_server, get_server_status

__all__ = ["start_streamlit_server", "stop_streamlit_server", "get_server_status"]

# Version du bridge
__version__ = "2.6.0"
