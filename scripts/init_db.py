from sqlalchemy import create_engine
from src.model import Base

DATABASE_URL = "sqlite:///data/recettes.db"


def init_db():
    engine = create_engine(DATABASE_URL)
    Base.metadata.create_all(engine)
    print("✅ Base de données initialisée dans data/recettes.db")


if __name__ == "__main__":
    init_db()
