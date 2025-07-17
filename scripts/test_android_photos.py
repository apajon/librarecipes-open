#!/usr/bin/env python3
"""
Script de test pour la gestion des photos sur Android.
Teste le stockage, l'upload et la migration des photos.
"""

import sys
from pathlib import Path

# Ajouter le répertoire racine au path pour les imports
root_dir = Path(__file__).parent.parent
sys.path.insert(0, str(root_dir))


def test_photos_manager():
    """Teste le PhotosManager pour Android"""
    from app.utils.photos_manager import PhotosManager
    from config.android_config import get_android_config

    print("📸 Test du gestionnaire de photos Android\n")

    # Configuration Android
    config = get_android_config()
    print("📊 Configuration photos:")
    print(f"  • Mode Android: {'✅' if config.is_android else '❌'}")
    print(f"  • Répertoire photos: {config.get_photos_directory()}")
    print()

    # Test avec une recette d'exemple
    test_recette_id = "test_recette_android_photos"
    photos_manager = PhotosManager(test_recette_id)

    # 1. Informations sur le répertoire
    print("📁 Informations répertoire:")
    info = photos_manager.get_photos_directory_info()
    for key, value in info.items():
        print(f"  • {key}: {value}")
    print()

    # 2. Test de stockage
    print("🔧 Test de stockage:")
    storage_test = photos_manager.test_photo_storage()
    for test_name, result in storage_test.items():
        if test_name == "error" and result:
            print(f"  ❌ Erreur: {result}")
        elif test_name != "error":
            status = "✅" if result else "❌"
            print(f"  {status} {test_name}: {result}")
    print()

    # 3. Test de migration (simulation)
    print("📦 Test de migration:")
    old_photos_dir = Path("data/photos") / test_recette_id

    # Créer quelques fichiers de test pour la migration
    if not old_photos_dir.exists():
        old_photos_dir.mkdir(parents=True, exist_ok=True)
        test_photo = old_photos_dir / "test_migration.jpg"
        test_photo.write_bytes(b"fake_photo_content_for_migration_test")
        print("  📝 Fichier de test créé pour la migration")

    migration_result = photos_manager.migrate_photos_to_android(old_photos_dir)
    print(f"  • Total photos: {migration_result['total_photos']}")
    print(f"  • Photos migrées: {migration_result['migrated_photos']}")
    print(f"  • Photos échouées: {migration_result['failed_photos']}")

    if migration_result["errors"]:
        print("  ❌ Erreurs de migration:")
        for error in migration_result["errors"]:
            print(f"    - {error}")
    else:
        print("  ✅ Migration réussie sans erreurs")

    # Nettoyer les fichiers de test
    try:
        if old_photos_dir.exists():
            for file in old_photos_dir.rglob("*"):
                if file.is_file():
                    file.unlink()
            old_photos_dir.rmdir()
            print("  🧹 Fichiers de test nettoyés")

        # Nettoyer aussi le nouveau répertoire de test
        new_test_dir = photos_manager.photos_dir
        if new_test_dir.exists():
            for file in new_test_dir.rglob("*"):
                if file.is_file():
                    file.unlink()
            new_test_dir.rmdir()
            print("  🧹 Répertoire de test Android nettoyé")
    except Exception as e:
        print(f"  ⚠️ Erreur de nettoyage: {e}")

    return True


def test_photo_paths_compatibility():
    """Teste la compatibilité des chemins entre développement et Android"""
    from config.android_config import get_android_config

    print("\n🔗 Test de compatibilité des chemins:")

    config = get_android_config()

    # Test des chemins relatifs vs absolus
    print(f"  • Stockage racine: {config.storage_root}")
    print(f"  • Répertoire photos: {config.get_photos_directory()}")
    print(f"  • Existe: {'✅' if config.get_photos_directory().exists() else '❌'}")

    # Test de création de sous-répertoires
    test_subdir = config.get_photos_directory() / "test_compatibility"
    try:
        test_subdir.mkdir(parents=True, exist_ok=True)
        print(f"  ✅ Création sous-répertoire: {test_subdir}")

        # Test d'écriture
        test_file = test_subdir / "test.txt"
        test_file.write_text("test compatibility")
        print(f"  ✅ Écriture fichier: {test_file}")

        # Nettoyage
        test_file.unlink()
        test_subdir.rmdir()
        print("  🧹 Nettoyage effectué")

    except Exception as e:
        print(f"  ❌ Erreur de compatibilité: {e}")


if __name__ == "__main__":
    try:
        test_photos_manager()
        test_photo_paths_compatibility()
        print("\n🎉 Tests photos Android terminés avec succès!")
    except Exception as e:
        print(f"\n💥 Erreur lors des tests photos: {e}")
        sys.exit(1)
