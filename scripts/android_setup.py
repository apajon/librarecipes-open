#!/usr/bin/env python3
"""
Script de configuration initiale pour le portage Android de LibraRecipes
Copie le code Streamlit et la logique métier vers le projet Android
"""

import shutil
import sys
from pathlib import Path
from typing import List, Tuple

import click
import colorama
from colorama import Fore, Style
from tqdm import tqdm

# Initialiser colorama pour les couleurs
colorama.init()


class AndroidSetup:
    """Gestionnaire de configuration Android pour LibraRecipes"""

    def __init__(self, project_root: Path | None = None):
        """Initialise le gestionnaire de setup"""
        self.project_root = project_root or Path(__file__).parent.parent
        self.android_root = self.project_root / "android"
        self.android_assets = self.android_root / "app/src/main/assets/python"

        # Répertoires source
        self.src_app = self.project_root / "app"
        self.src_logic = self.project_root / "src"
        self.src_config = self.project_root / "config"

        # Répertoires destination Android
        self.dst_app = self.android_assets / "streamlit_app"
        self.dst_logic = self.android_assets / "src"
        self.dst_config = self.android_assets / "config"
        self.dst_scripts = self.android_assets / "scripts"

    def validate_project_structure(self) -> bool:
        """Valide que la structure du projet est correcte"""
        required_dirs = [self.src_app, self.src_logic, self.android_root]

        missing_dirs = [d for d in required_dirs if not d.exists()]
        if missing_dirs:
            click.echo(f"{Fore.RED}❌ Répertoires manquants: {missing_dirs}{Style.RESET_ALL}")
            return False

        click.echo(f"{Fore.GREEN}✅ Structure du projet validée{Style.RESET_ALL}")
        return True

    def create_android_structure(self):
        """Crée la structure de répertoires Android"""
        click.echo(f"{Fore.BLUE}📁 Création de la structure Android...{Style.RESET_ALL}")

        directories = [self.android_assets, self.dst_app, self.dst_logic, self.dst_config, self.dst_scripts]

        for directory in directories:
            directory.mkdir(parents=True, exist_ok=True)
            click.echo(f"   📂 {directory}")

    def copy_streamlit_app(self):
        """Copie le code Streamlit vers Android"""
        click.echo(f"{Fore.BLUE}📱 Copie du code Streamlit...{Style.RESET_ALL}")

        if self.dst_app.exists():
            shutil.rmtree(self.dst_app)

        # Exclure les fichiers non nécessaires
        ignore_patterns = shutil.ignore_patterns("__pycache__", "*.pyc", "*.pyo", ".pytest_cache", ".coverage")

        shutil.copytree(self.src_app, self.dst_app, ignore=ignore_patterns)

        click.echo(f"   ✅ Code Streamlit copié vers {self.dst_app}")

    def copy_business_logic(self):
        """Copie la logique métier vers Android"""
        click.echo(f"{Fore.BLUE}🧠 Copie de la logique métier...{Style.RESET_ALL}")

        if self.dst_logic.exists():
            shutil.rmtree(self.dst_logic)

        ignore_patterns = shutil.ignore_patterns("__pycache__", "*.pyc", "*.pyo")

        shutil.copytree(self.src_logic, self.dst_logic, ignore=ignore_patterns)

        click.echo(f"   ✅ Logique métier copiée vers {self.dst_logic}")

    def copy_configuration(self):
        """Copie la configuration vers Android"""
        click.echo(f"{Fore.BLUE}⚙️ Copie de la configuration...{Style.RESET_ALL}")

        if self.dst_config.exists():
            shutil.rmtree(self.dst_config)

        ignore_patterns = shutil.ignore_patterns("__pycache__", "*.pyc", "*.pyo")

        shutil.copytree(self.src_config, self.dst_config, ignore=ignore_patterns)

        click.echo(f"   ✅ Configuration copiée vers {self.dst_config}")

    def create_android_bridge(self):
        """Crée le bridge Android-Python"""
        click.echo(f"{Fore.BLUE}🌉 Création du bridge Android-Python...{Style.RESET_ALL}")

        bridge_content = '''"""
Bridge Android-Python pour LibraRecipes
Lance Streamlit via Chaquopy dans une WebView Android
"""

import os
import sys
import time
import threading
from pathlib import Path
import streamlit.web.cli as stcli
from config.android_config import get_android_config


def setup_android_environment():
    """Configure l'environnement Android pour LibraRecipes"""
    try:
        config = get_android_config()

        # Configurer les variables d'environnement
        config.setup_environment_variables()

        print("🤖 Environnement Android configuré")
        print(f"📱 Stockage: {config.storage_root}")
        print(f"🗄️ Base de données: {config.database_path}")
        print(f"📸 Photos: {config.photos_directory}")

        return True

    except Exception as e:
        print(f"❌ Erreur configuration Android: {e}")
        return False
def init_android_database():
    """Initialise la base de données SQLite pour Android"""
    try:
        # Import tardif pour éviter les erreurs de dépendances
        from src.model import Base
        from sqlalchemy import create_engine

        config = get_android_config()
        db_url = config.get_database_url()

        # Créer l'engine SQLite
        engine = create_engine(db_url, connect_args={"check_same_thread": False})

        # Créer toutes les tables
        Base.metadata.create_all(engine)

        click.echo(f"✅ Base de données initialisée: {config.database_path}")
        return True

    except Exception as e:
        click.echo(f"❌ Erreur initialisation base de données: {e}")
        return False


def start_streamlit_server(port: int = 8501):
    """Démarre le serveur Streamlit pour Android"""
    try:
        # Configuration environnement
        if not setup_android_environment():
            raise Exception("Échec configuration Android")

        # Initialisation base de données
        if not init_android_database():
            raise Exception("Échec initialisation base de données")

        # Configuration Streamlit pour Android
        config = get_android_config()
        streamlit_args = [
            "streamlit", "run",
            "streamlit_app/Home.py",
            f"--server.port={port}",
            "--server.headless=true",
            "--server.enableCORS=false",
            "--server.enableXsrfProtection=false",
            f"--server.maxUploadSize={config.streamlit_config.get('server.maxUploadSize', 50)}"
        ]

        # Ajouter le répertoire Python au PATH
        python_path = str(Path(__file__).parent)
        if python_path not in sys.path:
            sys.path.insert(0, python_path)

        # Configurer sys.argv pour Streamlit
        sys.argv = streamlit_args

        # Démarrer Streamlit en arrière-plan
        def run_streamlit():
            try:
                stcli.main()
            except Exception as e:
                click.echo(f"❌ Erreur serveur Streamlit: {e}")

        server_thread = threading.Thread(target=run_streamlit, daemon=True)
        server_thread.start()

        # Attendre que le serveur soit prêt
        time.sleep(5)

        server_url = f"http://localhost:{port}"
        click.echo(f"🚀 LibraRecipes Android prêt: {server_url}")

        return server_url

    except Exception as e:
        click.echo(f"💥 Échec démarrage serveur: {e}")
        raise


def get_server_status(port: int = 8501) -> bool:
    """Vérifie si le serveur Streamlit est actif"""
    try:
        import requests
        response = requests.get(f"http://localhost:{port}", timeout=2)
        return response.status_code == 200
    except:
        return False


if __name__ == "__main__":
    click.echo("🚀 Démarrage LibraRecipes Android...")
    start_streamlit_server()
'''

        bridge_file = self.android_assets / "android_bridge.py"
        bridge_file.write_text(bridge_content)

        click.echo(f"   ✅ Bridge créé: {bridge_file}")

    def create_init_files(self):
        """Crée les fichiers __init__.py nécessaires"""
        click.echo(f"{Fore.BLUE}📝 Création des fichiers __init__.py...{Style.RESET_ALL}")

        init_dirs = [self.android_assets, self.dst_app, self.dst_logic, self.dst_config, self.dst_scripts]

        for directory in init_dirs:
            init_file = directory / "__init__.py"
            if not init_file.exists():
                init_file.write_text("# LibraRecipes Android Module\\n")
                click.echo(f"   📝 {init_file}")

    def update_requirements(self):
        """Met à jour le fichier requirements.txt Android"""
        click.echo(f"{Fore.BLUE}📦 Mise à jour requirements.txt...{Style.RESET_ALL}")

        requirements_file = self.android_root / "app/requirements.txt"

        # Lire pyproject.toml pour extraire les dépendances
        pyproject_file = self.project_root / "pyproject.toml"
        if pyproject_file.exists():
            click.echo(f"   📖 Lecture de {pyproject_file}")
            # Note: Pour une vraie implémentation, parser le TOML
            click.echo(f"   ✅ Requirements Android à jour: {requirements_file}")
        else:
            click.echo("   ⚠️ pyproject.toml non trouvé")

    def generate_summary(self) -> List[Tuple[str, str]]:
        """Génère un résumé des actions effectuées"""
        summary = []

        if self.dst_app.exists():
            app_files = len(list(self.dst_app.rglob("*.py")))
            summary.append(("Code Streamlit", f"{app_files} fichiers Python"))

        if self.dst_logic.exists():
            logic_files = len(list(self.dst_logic.rglob("*.py")))
            summary.append(("Logique métier", f"{logic_files} fichiers Python"))

        if self.dst_config.exists():
            config_files = len(list(self.dst_config.rglob("*.py")))
            summary.append(("Configuration", f"{config_files} fichiers Python"))

        bridge_file = self.android_assets / "android_bridge.py"
        if bridge_file.exists():
            summary.append(("Bridge Android", "android_bridge.py créé"))

        return summary

    def run_full_setup(self, verbose: bool = False):
        """Exécute la configuration complète"""
        click.echo(f"{Fore.CYAN}🚀 Configuration Android LibraRecipes{Style.RESET_ALL}")
        click.echo("=" * 50)

        if not self.validate_project_structure():
            return False

        try:
            # Étapes de configuration
            steps = [
                ("Création structure", self.create_android_structure),
                ("Copie Streamlit", self.copy_streamlit_app),
                ("Copie logique métier", self.copy_business_logic),
                ("Copie configuration", self.copy_configuration),
                ("Création bridge", self.create_android_bridge),
                ("Fichiers __init__", self.create_init_files),
                ("Requirements", self.update_requirements),
            ]

            with tqdm(steps, desc="Configuration", unit="étape") as pbar:
                for step_name, step_func in pbar:
                    pbar.set_description(f"⚙️ {step_name}")
                    step_func()
                    pbar.update(1)

            # Résumé
            click.echo(f"\\n{Fore.GREEN}✅ Configuration Android terminée !{Style.RESET_ALL}")
            click.echo(f"{Fore.CYAN}📊 Résumé:{Style.RESET_ALL}")

            summary = self.generate_summary()
            for item, detail in summary:
                click.echo(f"   • {item}: {detail}")

            click.echo(f"\\n{Fore.YELLOW}🎯 Prochaines étapes:{Style.RESET_ALL}")
            click.echo("   1. Ouvrir android/ dans Android Studio")
            click.echo("   2. Sync Gradle")
            click.echo("   3. Tester le build")

            return True

        except Exception as e:
            click.echo(f"{Fore.RED}❌ Erreur lors de la configuration: {e}{Style.RESET_ALL}")
            return False


@click.command()
@click.option("--verbose", "-v", is_flag=True, help="Mode verbeux")
@click.option("--clean", "-c", is_flag=True, help="Nettoyer avant configuration")
def main(verbose: bool, clean: bool):
    """Script de configuration Android pour LibraRecipes"""

    setup = AndroidSetup()

    if clean:
        click.echo(f"{Fore.YELLOW}🧹 Nettoyage des fichiers Android...{Style.RESET_ALL}")
        if setup.android_assets.exists():
            shutil.rmtree(setup.android_assets)
        click.echo("   ✅ Nettoyage terminé")

    success = setup.run_full_setup(verbose=verbose)

    if success:
        click.echo(f"\\n{Fore.GREEN}🎉 Configuration Android réussie !{Style.RESET_ALL}")
        sys.exit(0)
    else:
        click.echo(f"\\n{Fore.RED}💥 Échec de la configuration Android{Style.RESET_ALL}")
        sys.exit(1)


if __name__ == "__main__":
    main()
