from sqlalchemy import func
from sqlalchemy.orm import Session

from src.model import Categorie, Ingredient, Tag


def get_all_ingredients(session: Session) -> list[str]:
    return [row[0] for row in session.query(func.distinct(Ingredient.nom)).order_by(Ingredient.nom).all()]


def get_all_tags(session: Session) -> list[str]:
    return [row[0] for row in session.query(func.distinct(Tag.nom)).order_by(Tag.nom).all()]


def get_all_categories(session: Session) -> list[str]:
    return [row[0] for row in session.query(func.distinct(Categorie.nom)).order_by(Categorie.nom).all()]


# Aliases for backend compatibility
def lister_ingredients(session: Session) -> list[str]:
    """List all unique ingredients (alias for get_all_ingredients)."""
    return get_all_ingredients(session)


def lister_tags(session: Session) -> list[str]:
    """List all tags (alias for get_all_tags)."""
    return get_all_tags(session)


def lister_categories(session: Session) -> list[str]:
    """List all categories (alias for get_all_categories)."""
    return get_all_categories(session)
