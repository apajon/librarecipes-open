import os
import sys

from sqlalchemy import create_engine

# Pour pouvoir importer depuis le dossier parent (src/)
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from src.models import Base

DATABASE_URL = "sqlite:///data/recettes.db"


def init_db():
    engine = create_engine(DATABASE_URL)
    Base.metadata.create_all(engine)
    print("✅ Base de données initialisée dans data/recettes.db")


if __name__ == "__main__":
    init_db()
