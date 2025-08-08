#!/usr/bin/env python3
"""
Main entry point for LibraRecipes Application (desktop + mobile)
"""

import sys


def is_android() -> bool:
    try:
        return hasattr(sys, "getandroidapilevel")
    except Exception:
        return False


if __name__ == "__main__":
    if is_android():
        # Lancer l'app mobile Kivy
        from mobile_app.main import LibraRecipesApp

        app = LibraRecipesApp()
        app.run()
    else:
        # Lancer l'app desktop (Streamlit)
        try:
            import streamlit.web.bootstrap as st_bootstrap

            st_bootstrap.run("app/Home.py", "", [], flag_options={})
        except Exception:
            # Fallback: exécuter la Kivy app en local si Streamlit indisponible
            from mobile_app.main import LibraRecipesApp

            app = LibraRecipesApp()
            app.run()
