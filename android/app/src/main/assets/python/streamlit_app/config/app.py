"""Configuration principale de l'application LibraRecipes"""

import streamlit as st


def setup_page_config():
    """Configure la page Streamlit principale"""
    st.set_page_config(page_title="LibraRecipes", page_icon="🍲", layout="wide", initial_sidebar_state="expanded")


def setup_navigation(pages):
    """Configure et retourne la navigation de l'application"""
    return st.navigation(pages, position="top")


def run_app(navigation):
    """Lance l'application principale"""
    if navigation:
        navigation.run()
