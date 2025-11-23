#!/usr/bin/env python3
"""
Main entry point for LibraRecipes Application (desktop)
"""

import sys

if __name__ == "__main__":
    # Lancer l'app desktop (Streamlit)
    try:
        import streamlit.web.bootstrap as st_bootstrap

        st_bootstrap.run("streamlit_app/Home.py", "", [], flag_options={})
    except ImportError:
        print("Error: Streamlit is not installed. Please install dependencies with 'poetry install'")
        sys.exit(1)
    except FileNotFoundError:
        print("Error: streamlit_app/Home.py not found. Please check the file path.")
        sys.exit(1)
    except Exception as e:
        print(f"Error starting Streamlit application: {e}")
        sys.exit(1)
