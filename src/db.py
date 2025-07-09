from contextlib import contextmanager

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "sqlite:///data/recettes.db"

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
