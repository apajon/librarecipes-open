import streamlit as st


def main():
    st.set_page_config(page_title="Ajout de recette", page_icon="🍽️", layout="wide", initial_sidebar_state="expanded")

    st.sidebar.title("Navigation")
    st.sidebar.markdown("Bienvenue dans l'application d'ajout de recettes !")

    st.title("Ajout de recette")

    st.markdown("Cette fonctionnalité n'est pas encore implémentée.")


if __name__ == "__main__":
    main()
    # Pour exécuter l'application, utilisez la commande suivante dans le terminal :
    # PYTHONPATH=. streamlit run app/main.py
