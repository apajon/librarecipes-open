"""Module principal pour la gestion de la sidebar"""

import streamlit as st

from streamlit_app.utils.recipe_selector import render_recipe_selector


def setup_sidebar():
    """Configure et affiche la sidebar complète"""
    with st.sidebar:
        render_recipe_selector()
        st.markdown("---")
