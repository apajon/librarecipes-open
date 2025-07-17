#!/usr/bin/env python3
"""
Script de synchronisation automatique pour le développement Android LibraRecipes
Synchronise le code Streamlit et la logique métier vers Android
"""

import shutil
import sys
import time
from pathlib import Path
from typing import List

import click
import colorama
from colorama import Fore, Style
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

# Initialiser colorama
colorama.init()


class LibraRecipesSyncHandler(FileSystemEventHandler):
    """Gestionnaire d'événements pour la synchronisation automatique"""

    def __init__(self, syncer):
        """Initialise le gestionnaire"""
        self.syncer = syncer
        self.last_sync = time.time()
        self.sync_delay = 2  # Délai en secondes avant synchronisation

    def on_modified(self, event):
        """Déclenché quand un fichier est modifié"""
        if not event.is_directory and self._should_sync(event.src_path):
            current_time = time.time()
            if current_time - self.last_sync > self.sync_delay:
                self.syncer.sync_changes()
                self.last_sync = current_time

    def on_created(self, event):
        """Déclenché quand un fichier est créé"""
        if not event.is_directory and self._should_sync(event.src_path):
            self.syncer.sync_changes()
            self.last_sync = time.time()

    def on_deleted(self, event):
        """Déclenché quand un fichier est supprimé"""
        if not event.is_directory:
            self.syncer.sync_changes()
            self.last_sync = time.time()

    def _should_sync(self, file_path: str) -> bool:
        """Vérifie si le fichier doit déclencher une synchronisation"""
        file_path_obj = Path(file_path)

        # Extensions à synchroniser
        sync_extensions = {".py", ".toml", ".yaml", ".yml", ".json", ".md"}

        # Répertoires à ignorer
        ignore_dirs = {"__pycache__", ".pytest_cache", ".coverage", "node_modules", ".git"}

        # Vérifier l'extension
        if file_path_obj.suffix not in sync_extensions:
            return False

        # Vérifier les répertoires à ignorer
        if any(ignore_dir in file_path_obj.parts for ignore_dir in ignore_dirs):
            return False

        return True


class AndroidSyncer:
    """Gestionnaire de synchronisation vers Android"""

    def __init__(self, project_root: Path | None = None):
        """Initialise le synchronisateur"""
        self.project_root = project_root or Path(__file__).parent.parent
        self.android_assets = self.project_root / "android/app/src/main/assets/python"

        # Répertoires source à surveiller
        self.watch_dirs = [self.project_root / "app", self.project_root / "src", self.project_root / "config"]

        # Mappings source -> destination
        self.sync_mappings = {"app": "streamlit_app", "src": "src", "config": "config"}

        self.sync_count = 0

    def validate_setup(self) -> bool:
        """Valide que la configuration Android existe"""
        if not self.android_assets.exists():
            click.echo(f"{Fore.RED}❌ Répertoire Android non trouvé: {self.android_assets}{Style.RESET_ALL}")
            click.echo(f"{Fore.YELLOW}💡 Exécutez d'abord: python scripts/android_setup.py{Style.RESET_ALL}")
            return False

        missing_dirs = [d for d in self.watch_dirs if not d.exists()]
        if missing_dirs:
            click.echo(f"{Fore.RED}❌ Répertoires source manquants: {missing_dirs}{Style.RESET_ALL}")
            return False

        return True

    def sync_directory(self, source_name: str) -> bool:
        """Synchronise un répertoire spécifique"""
        try:
            source_dir = self.project_root / source_name
            dest_name = self.sync_mappings.get(source_name, source_name)
            dest_dir = self.android_assets / dest_name

            if not source_dir.exists():
                return False

            # Supprimer l'ancien répertoire
            if dest_dir.exists():
                shutil.rmtree(dest_dir)

            # Copier avec exclusions
            ignore_patterns = shutil.ignore_patterns("__pycache__", "*.pyc", "*.pyo", ".pytest_cache", ".coverage")

            shutil.copytree(source_dir, dest_dir, ignore=ignore_patterns)

            # Compter les fichiers Python
            py_files = len(list(dest_dir.rglob("*.py")))
            click.echo(f"   📂 {source_name} → {dest_name} ({py_files} fichiers Python)")

            return True

        except Exception as e:
            click.echo(f"   ❌ Erreur sync {source_name}: {e}")
            return False

    def sync_changes(self):
        """Synchronise tous les changements"""
        self.sync_count += 1
        timestamp = time.strftime("%H:%M:%S")

        click.echo(f"{Fore.BLUE}🔄 Sync #{self.sync_count} - {timestamp}{Style.RESET_ALL}")

        success_count = 0
        for source_name in self.sync_mappings.keys():
            if self.sync_directory(source_name):
                success_count += 1

        if success_count == len(self.sync_mappings):
            click.echo(f"{Fore.GREEN}   ✅ Synchronisation complète ({success_count} répertoires){Style.RESET_ALL}")
        else:
            click.echo(
                f"{Fore.YELLOW}   ⚠️ Synchronisation partielle "
                f"({success_count}/{len(self.sync_mappings)}){Style.RESET_ALL}"
            )

    def get_watch_paths(self) -> List[str]:
        """Retourne la liste des chemins à surveiller"""
        return [str(d) for d in self.watch_dirs if d.exists()]

    def start_watch_mode(self):
        """Démarre le mode surveillance"""
        click.echo(f"{Fore.CYAN}👀 Mode surveillance activé{Style.RESET_ALL}")
        click.echo("📁 Répertoires surveillés:")
        for watch_dir in self.watch_dirs:
            if watch_dir.exists():
                click.echo(f"   • {watch_dir}")

        click.echo(f"🎯 Destination: {self.android_assets}")
        click.echo(f"{Fore.YELLOW}⚡ Synchronisation automatique en cours...{Style.RESET_ALL}")
        click.echo("   (Ctrl+C pour arrêter)")

        # Synchronisation initiale
        self.sync_changes()

        # Configurer la surveillance
        event_handler = LibraRecipesSyncHandler(self)
        observer = Observer()

        for watch_path in self.get_watch_paths():
            observer.schedule(event_handler, watch_path, recursive=True)

        observer.start()

        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            click.echo(f"\\n{Fore.CYAN}🛑 Arrêt de la surveillance...{Style.RESET_ALL}")
            observer.stop()

        observer.join()
        click.echo(f"{Fore.GREEN}✅ Surveillance terminée{Style.RESET_ALL}")

    def run_single_sync(self):
        """Exécute une synchronisation unique"""
        click.echo(f"{Fore.CYAN}🔄 Synchronisation unique{Style.RESET_ALL}")

        self.sync_changes()

        click.echo(f"{Fore.GREEN}✅ Synchronisation terminée{Style.RESET_ALL}")

    def show_status(self):
        """Affiche le statut de synchronisation"""
        click.echo(f"{Fore.CYAN}📊 Statut de synchronisation{Style.RESET_ALL}")
        click.echo("=" * 40)

        # Informations générales
        click.echo(f"📁 Projet: {self.project_root}")
        click.echo(f"📱 Android: {self.android_assets}")

        # Vérifier chaque mapping
        click.echo("\\n📂 Répertoires synchronisés:")
        for source_name, dest_name in self.sync_mappings.items():
            source_dir = self.project_root / source_name
            dest_dir = self.android_assets / dest_name

            if source_dir.exists() and dest_dir.exists():
                source_files = len(list(source_dir.rglob("*.py")))
                dest_files = len(list(dest_dir.rglob("*.py")))

                status = "✅" if source_files == dest_files else "⚠️"
                click.echo(f"   {status} {source_name} → {dest_name} " f"({source_files}→{dest_files} fichiers)")
            elif source_dir.exists():
                click.echo(f"   ❌ {source_name} → {dest_name} (non synchronisé)")
            else:
                click.echo(f"   ❌ {source_name} (source manquante)")

        # Dernière modification
        if self.android_assets.exists():
            last_mod = max(self.android_assets.rglob("*.py"), key=lambda p: p.stat().st_mtime, default=None)
            if last_mod:
                mod_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(last_mod.stat().st_mtime))
                click.echo(f"\\n🕒 Dernière modification: {mod_time}")


@click.command()
@click.option("--watch", "-w", is_flag=True, help="Mode surveillance continue")
@click.option("--status", "-s", is_flag=True, help="Afficher le statut")
@click.option("--clean", "-c", is_flag=True, help="Nettoyer avant synchronisation")
def main(watch: bool, status: bool, clean: bool):
    """Script de synchronisation Android pour LibraRecipes

    Exemples:
        python scripts/sync_to_android.py           # Sync unique
        python scripts/sync_to_android.py --watch   # Mode surveillance
        python scripts/sync_to_android.py --status  # Afficher statut
    """

    syncer = AndroidSyncer()

    # Vérifier la configuration
    if not syncer.validate_setup():
        click.echo(f"{Fore.RED}💥 Configuration Android invalide{Style.RESET_ALL}")
        sys.exit(1)

    # Nettoyage si demandé
    if clean:
        click.echo(f"{Fore.YELLOW}🧹 Nettoyage des fichiers Android...{Style.RESET_ALL}")
        if syncer.android_assets.exists():
            for item in syncer.android_assets.iterdir():
                if item.name in syncer.sync_mappings.values():
                    if item.is_dir():
                        shutil.rmtree(item)
                    else:
                        item.unlink()
        click.echo("   ✅ Nettoyage terminé")

    # Exécuter l'action demandée
    if status:
        syncer.show_status()
    elif watch:
        syncer.start_watch_mode()
    else:
        syncer.run_single_sync()


if __name__ == "__main__":
    main()
