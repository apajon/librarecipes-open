import uuid
from datetime import datetime

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, String, Table, Text
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


def generate_uuid():
    return str(uuid.uuid4())


# ------------------------
# TABLES ASSOCIATIVES
# ------------------------

recette_categories = Table(
    "recette_categories",
    Base.metadata,
    Column("recette_id", String, ForeignKey("recettes.id"), primary_key=True),
    Column("categorie", String, primary_key=True),
)

recette_tags = Table(
    "recette_tags",
    Base.metadata,
    Column("recette_id", String, ForeignKey("recettes.id"), primary_key=True),
    Column("tag", String, primary_key=True),
)

# ------------------------
# ENTITÉS PRINCIPALES
# ------------------------


class Recette(Base):
    __tablename__ = "recettes"

    id = Column(String, primary_key=True, default=generate_uuid)
    nom = Column(String, nullable=False)
    preparation = Column(Integer)  # en minutes
    cuisson = Column(Integer)  # en minutes
    portions = Column(Integer)
    date_ajout = Column(DateTime, default=datetime.utcnow)

    # Relations
    ingredients = relationship("Ingredient", cascade="all, delete-orphan")
    etapes = relationship("Etape", cascade="all, delete-orphan")
    photos = relationship("Photo", cascade="all, delete-orphan")
    executions = relationship("Execution", cascade="all, delete-orphan")

    # Tags et catégories
    categories = relationship("Categorie", cascade="all, delete-orphan")
    tags = relationship("Tag", cascade="all, delete-orphan")

    # Source
    source = relationship("Source", uselist=False, cascade="all, delete-orphan")


class Ingredient(Base):
    __tablename__ = "ingredients"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    nom = Column(String, nullable=False)
    quantite = Column(String)
    unite = Column(String)
    indispensable = Column(Boolean, default=True)
    alternatives = Column(Text)  # Liste séparée par ;


class Etape(Base):
    __tablename__ = "etapes"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    ordre = Column(Integer)
    description = Column(Text)


class Photo(Base):
    __tablename__ = "photos"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    chemin = Column(String, nullable=False)  # chemin local
    categorie = Column(String)  # final, cuisson, ingrédient...
    ordre = Column(Integer, default=0)


class Categorie(Base):
    __tablename__ = "categories"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    nom = Column(String)


class Tag(Base):
    __tablename__ = "tags"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    nom = Column(String)


# ------------------------
# CONVIVES & FEEDBACK
# ------------------------


class Convive(Base):
    __tablename__ = "convives"

    id = Column(String, primary_key=True, default=generate_uuid)
    nom = Column(String, unique=True, nullable=False)
    groupe = Column(String)  # famille, amis...

    feedbacks = relationship("FeedbackExecution", cascade="all, delete-orphan")


class Execution(Base):
    __tablename__ = "executions"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"))
    date_execution = Column(DateTime, default=datetime.utcnow)

    feedbacks = relationship("FeedbackExecution", cascade="all, delete-orphan")


class FeedbackExecution(Base):
    __tablename__ = "feedback_execution"

    id = Column(String, primary_key=True, default=generate_uuid)
    execution_id = Column(String, ForeignKey("executions.id"))
    convive_id = Column(String, ForeignKey("convives.id"))
    statut = Column(String)  # "aimé", "partiellement", "rien mangé"


class Source(Base):
    __tablename__ = "sources"

    id = Column(String, primary_key=True, default=generate_uuid)
    recette_id = Column(String, ForeignKey("recettes.id"), unique=True)

    type = Column(String)  # 'homemade', 'url', 'book'

    # champs facultatifs selon le type
    url = Column(String, nullable=True)
    book_title = Column(String, nullable=True)
    book_authors = Column(String, nullable=True)
    book_page = Column(String, nullable=True)

    recette = relationship("Recette", back_populates="source")
