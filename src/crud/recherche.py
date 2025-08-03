from enum import Enum
from typing import Optional

from sqlalchemy import func
from sqlalchemy.orm import Session, joinedload

from src.model import Categorie, Ingredient, Recette, Tag


class IngredientsMode(str, Enum):
    ANY = "any"
    ALL = "all"


def rechercher_recettes(
    session: Session,
    nom: Optional[str] = None,
    ingredients: Optional[list[str]] = None,
    tags: Optional[list[str]] = None,
    categories: Optional[list[str]] = None,
    ingredients_mode: IngredientsMode = IngredientsMode.ANY,
) -> list[Recette]:
    query = session.query(Recette)

    if nom:
        query = query.filter(func.lower(Recette.nom).like(f"%{nom.lower()}%"))

    if ingredients:
        query = query.join(Recette.ingredients)

        if ingredients_mode == IngredientsMode.ANY:
            # Recettes qui contiennent au moins un des ingrédients
            query = query.filter(Ingredient.nom.in_(ingredients))

        elif ingredients_mode == IngredientsMode.ALL:
            # Recettes qui contiennent tous les ingrédients
            query = (
                query.filter(Ingredient.nom.in_(ingredients))
                .group_by(Recette.id)
                .having(func.count(func.distinct(Ingredient.nom)) == len(set(ingredients)))
            )
        else:
            raise ValueError(f"ingredients_mode must be {list(IngredientsMode.__members__.keys())}")

    if tags:
        query = query.join(Recette.tags).filter(Tag.nom.in_(tags)).group_by(Recette.id)

    if categories:
        query = query.join(Recette.categories).filter(Categorie.nom.in_(categories)).group_by(Recette.id)

    return query.options(
        joinedload(Recette.categories),
        joinedload(Recette.tags),
        joinedload(Recette.ingredients),
        joinedload(Recette.etapes),
        joinedload(Recette.photos),
        joinedload(Recette.source),
        joinedload(Recette.executions)
    ).all()
