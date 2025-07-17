"""
Gestionnaire de styles pour LibraRecipes Android
Injection CSS pour responsive design et optimisation mobile
"""

from pathlib import Path

import streamlit as st

from config.android_config import get_android_config


class MobileStyleManager:
    """Gestionnaire des styles mobiles pour LibraRecipes"""

    def __init__(self):
        """Initialise le gestionnaire de styles"""
        self.config = get_android_config()
        self.styles_dir = Path(__file__).parent.parent.parent / ".streamlit" / "static"
        self.mobile_css_path = self.styles_dir / "mobile.css"

    def load_mobile_styles(self) -> str:
        """Charge les styles CSS mobiles"""
        try:
            if self.mobile_css_path.exists():
                return self.mobile_css_path.read_text(encoding="utf-8")
            else:
                st.warning("⚠️ Fichier CSS mobile non trouvé")
                return ""
        except Exception as e:
            st.error(f"❌ Erreur chargement CSS: {e}")
            return ""

    def inject_mobile_styles(self):
        """Injecte les styles CSS mobiles dans la page Streamlit"""
        css_content = self.load_mobile_styles()

        if css_content:
            # Injection CSS avec style tag
            st.markdown(
                f"""
                <style>
                {css_content}
                </style>
                """,
                unsafe_allow_html=True,
            )

    def add_viewport_meta(self):
        """Ajoute la balise viewport pour responsive design"""
        st.markdown(
            """
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            """,
            unsafe_allow_html=True,
        )

    def add_mobile_optimizations(self):
        """Ajoute diverses optimisations mobiles"""
        mobile_optimizations = """
        <style>
        /* Optimisations spécifiques Android */
        body {
            -webkit-tap-highlight-color: transparent;
            -webkit-touch-callout: none;
            -webkit-user-select: none;
            user-select: none;
        }

        /* Éviter le zoom automatique sur iOS */
        input, textarea, select {
            font-size: 16px !important;
        }

        /* Améliorer les performances de scroll */
        * {
            -webkit-overflow-scrolling: touch;
            transform: translateZ(0);
        }

        /* Masquer la barre d'adresse sur mobile */
        @media screen and (max-width: 768px) {
            .stApp {
                height: 100vh;
                overflow-y: auto;
            }
        }
        </style>

        <script>
        // Script pour masquer la barre d'adresse mobile
        window.addEventListener('load', function() {
            setTimeout(function() {
                window.scrollTo(0, 1);
            }, 1000);
        });

        // Désactiver le zoom par pincement
        document.addEventListener('gesturestart', function (e) {
            e.preventDefault();
        });

        // Améliorer les performances tactiles
        document.addEventListener('touchstart', function() {}, {passive: true});
        document.addEventListener('touchmove', function() {}, {passive: true});
        </script>
        """

        st.markdown(mobile_optimizations, unsafe_allow_html=True)

    def setup_mobile_layout(self):
        """Configure la mise en page mobile complète"""
        # Injection des styles
        self.inject_mobile_styles()

        # Optimisations mobiles
        self.add_mobile_optimizations()

        # Configuration responsive
        st.markdown(
            """
            <style>
            /* Configuration layout responsive */
            .main .block-container {
                padding-top: 2rem;
                padding-bottom: 2rem;
            }

            /* Android specific optimizations */
            .stApp {
                background-color: #f8f9fa;
            }

            @media screen and (max-width: 768px) {
                .main .block-container {
                    padding: 1rem 0.5rem;
                    max-width: 100%;
                }
            }
            </style>
            """,
            unsafe_allow_html=True,
        )

    def add_recipe_card_styles(self):
        """Ajoute les styles pour les cartes de recettes"""
        recipe_styles = """
        <style>
        .recipe-card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            margin: 1rem 0;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border: 1px solid #e0e0e0;
            transition: all 0.3s ease;
        }

        .recipe-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 16px rgba(0,0,0,0.15);
        }

        .recipe-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .recipe-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #2c3e50;
            margin: 0;
        }

        .recipe-meta {
            display: flex;
            gap: 1rem;
            color: #7f8c8d;
            font-size: 0.9rem;
            margin-bottom: 1rem;
        }

        .recipe-actions {
            display: flex;
            gap: 0.5rem;
            justify-content: flex-end;
            margin-top: 1rem;
        }

        @media screen and (max-width: 768px) {
            .recipe-card {
                padding: 1rem;
                margin: 0.5rem 0;
            }

            .recipe-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
            }

            .recipe-meta {
                flex-direction: column;
                gap: 0.25rem;
            }

            .recipe-actions {
                justify-content: center;
                flex-wrap: wrap;
            }
        }
        </style>
        """

        st.markdown(recipe_styles, unsafe_allow_html=True)

    def get_mobile_detection_script(self) -> str:
        """Retourne un script de détection mobile"""
        return """
        <script>
        function isMobileDevice() {
            return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        }

        function isAndroidWebView() {
            return navigator.userAgent.includes('wv') && navigator.userAgent.includes('Android');
        }

        // Ajouter classes CSS selon le device
        if (isMobileDevice()) {
            document.body.classList.add('mobile-device');
        }

        if (isAndroidWebView()) {
            document.body.classList.add('android-webview');
        }
        </script>
        """


# Instance globale pour faciliter l'usage
mobile_styles = MobileStyleManager()


def setup_mobile_interface():
    """Fonction utilitaire pour configurer l'interface mobile"""
    mobile_styles.setup_mobile_layout()


def setup_recipe_interface():
    """Fonction utilitaire pour les pages de recettes"""
    mobile_styles.setup_mobile_layout()
    mobile_styles.add_recipe_card_styles()


def get_responsive_columns(mobile_cols: int = 1, desktop_cols: int = 3):
    """Retourne des colonnes adaptatives selon le device"""
    # Sur mobile, utiliser moins de colonnes
    return st.columns(mobile_cols if mobile_styles.config.is_android else desktop_cols)


def mobile_container():
    """Container optimisé pour mobile"""
    return st.container()


def mobile_expander(label: str, expanded: bool = False):
    """Expander optimisé mobile"""
    return st.expander(label, expanded=expanded)
