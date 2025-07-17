import os
import sys
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker


# Configuration de base de données avec fallback
def get_database_url():
    """
    Obtient l'URL de la base de données adaptée à l'environnement.
    Utilise une configuration par défaut pour le développement.

    Returns:
        str: URL de la base de données SQLite
    """
    try:
        # Essayer d'importer la configuration Android si disponible
        project_root = Path(__file__).parent.parent
        sys.path.insert(0, str(project_root))

        from config.android_config import get_database_url as _get_android_database_url

        return _get_android_database_url()
    except ImportError:
        # Configuration par défaut pour l'environnement de développement
        return "sqlite:///./data/recettes.db"


# URL de base de données adaptative (développement/Android)
DATABASE_URL = get_database_url()

# Création de l'engine SQLite
engine = create_engine(
    DATABASE_URL, connect_args={"check_same_thread": False}  # important pour usage avec Streamlit ou threads
)

# Factory de sessions
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@contextmanager
def get_db_session():
    session = SessionLocal()
    try:
        yield session
    finally:
        session.close()


def init_database_for_android():
    """
    Initialise la base de données pour Android.
    Teste la création avec os.path.join(os.getcwd(), "my_local_db.db") comme spécifié.
    """
    from src.model import Base

    # Créer les tables si elles n'existent pas
    Base.metadata.create_all(bind=engine)

    # Test de connectivité
    with get_db_session() as session:
        # Test simple de la base
        result = session.execute(text("SELECT 1"))
        row = result.fetchone()
        assert row is not None and row[0] == 1

    return True


def test_android_database_path():
    """
    Teste la création d'une base avec le pattern Android recommandé.
    """
    test_db_path = os.path.join(os.getcwd(), "my_local_db.db")
    test_url = f"sqlite:///{test_db_path}"

    # Test de création d'engine avec ce chemin
    test_engine = create_engine(test_url, connect_args={"check_same_thread": False})

    # Test de connexion
    try:
        connection = test_engine.connect()
        connection.execute(text("SELECT 1"))
        connection.close()
        return True, test_db_path
    except Exception as e:
        return False, str(e)


def get_current_database_info():
    """
    Retourne les informations sur la base de données actuelle.
    """
    return {
        "database_url": DATABASE_URL,
        "engine_url": str(engine.url),
        "is_android": "android" in DATABASE_URL.lower(),
        "working_directory": os.getcwd(),
    }
