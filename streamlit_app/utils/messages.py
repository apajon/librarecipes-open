"""
Utilitaires pour l'affichage des erreurs et des messages
"""

from typing import List

import streamlit as st


def show_validation_errors(errors: List[str]) -> None:
    """Affiche une liste d'erreurs de validation"""
    for error in errors:
        st.error(error)


def show_success_message(message: str) -> None:
    """Affiche un message de succès"""
    st.success(f"✅ {message}")


def show_error_message(message: str) -> None:
    """Affiche un message d'erreur"""
    st.error(f"❌ {message}")


def show_info_message(message: str) -> None:
    """Affiche un message d'information"""
    st.info(f"💡 {message}")


def show_warning_message(message: str) -> None:
    """Affiche un message d'avertissement"""
    st.warning(f"⚠️ {message}")


def handle_exception(e: Exception, context: str = "Opération") -> None:
    """Gère l'affichage d'une exception"""
    show_error_message(f"Erreur lors de {context.lower()} : {str(e)}")


def show_empty_state(message: str, help_text: str = "") -> None:
    """Affiche un état vide avec message d'aide optionnel"""
    st.info(message)
    if help_text:
        st.markdown(help_text)
