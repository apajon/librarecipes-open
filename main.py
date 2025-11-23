#!/usr/bin/env python3
"""
Main entry point for LibraRecipes Application (desktop)
"""

if __name__ == "__main__":
    # Lancer l'app desktop (Streamlit)
    import streamlit.web.bootstrap as st_bootstrap

    st_bootstrap.run("streamlit_app/Home.py", "", [], flag_options={})
