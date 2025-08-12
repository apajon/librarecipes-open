import os
from contextlib import contextmanager
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Determine database path based on platform
try:
    from kivy.utils import platform
    is_android = platform == "android"
except ImportError:
    # kivy not available, assume desktop/development environment
    is_android = False

if is_android:
    from android.storage import app_storage_path
    db_path = os.path.join(app_storage_path(), "recettes.db")
else:
    # For desktop/development
    db_path = "data/recettes.db"
    # Ensure directory exists
    Path("data").mkdir(exist_ok=True)

DATABASE_URL = f"sqlite:///{db_path}"

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
