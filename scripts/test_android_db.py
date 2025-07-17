#!/usr/bin/env python3
"""
Script de test pour la compatibilité de la base de données avec Android.
Teste la création de base avec le pattern Android et vérifie les modèles SQLAlchemy.
"""

import os
import sys
from pathlib import Path

# Ajouter le répertoire racine au path pour les imports
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


def test_database_compatibility():
    """Teste la compatibilité de la base de données pour Android"""
    from src.db import (
        get_current_database_info,
        init_database_for_android,
        test_android_database_path,
    )
    from src.model import Recette, Ingredient, Etape, Photo

    print("🧪 Test de compatibilité de la base de données Android\n")

    # 1. Informations sur la configuration actuelle
    print("📊 Configuration actuelle:")
    db_info = get_current_database_info()

    for key, value in db_info.items():
        print(f"  • {key}: {value}")
    print()

    # 2. Test du pattern Android recommandé
    print("🔧 Test du pattern Android (os.path.join(os.getcwd(), 'my_local_db.db')):")
    success, result = test_android_database_path()
    if success:
        print(f"  ✅ Pattern Android testé avec succès: {result}")
        # Nettoyer le fichier de test
        if os.path.exists(result):
            os.remove(result)
            print(f"  🧹 Fichier de test supprimé: {result}")
    else:
        print(f"  ❌ Erreur lors du test Android: {result}")
    print()

    # 3. Test d'initialisation de la base
    print("🚀 Test d'initialisation de la base Android:")
    try:
        init_success = init_database_for_android()
        if init_success:
            print("  ✅ Initialisation réussie")
        else:
            print("  ❌ Échec de l'initialisation")
    except Exception as e:
        print(f"  ❌ Erreur lors de l'initialisation: {e}")
    print()

    # 4. Vérification des modèles
    print("📋 Vérification des modèles SQLAlchemy:")
    models = [Recette, Ingredient, Etape, Photo]
    for model in models:
        try:
            table_name = model.__tablename__
            columns = [col.name for col in model.__table__.columns]
            print(f"  ✅ {model.__name__} ({table_name}): {len(columns)} colonnes")
        except Exception as e:
            print(f"  ❌ Erreur avec {model.__name__}: {e}")

    return True


def test_android_environment():
    """Teste l'environnement Android"""
    from config.android_config import get_android_config

    print("\n🤖 Test de l'environnement Android:")

    config = get_android_config()

    print(f"  • Mode Android: {'✅' if config.is_android else '❌'}")
    print(f"  • Répertoire de stockage: {config.storage_root}")
    print(f"  • Base de données: {config.get_database_url()}")
    print(f"  • Répertoire photos: {config.get_photos_directory()}")


if __name__ == "__main__":
    try:
        test_database_compatibility()
        test_android_environment()
        print("\n🎉 Tests terminés avec succès!")
    except Exception as e:
        print(f"\n💥 Erreur lors des tests: {e}")
        sys.exit(1)
