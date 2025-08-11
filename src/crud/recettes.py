import uuid
from datetime import datetime

from sqlalchemy.orm import Session, joinedload

from src.model import Categorie, Etape, Execution, FeedbackExecution, Ingredient, Photo, Recette, Source, Tag


def create_recette(session: Session, data: dict) -> Recette:
    recette = Recette(
        id=str(uuid.uuid4()),
        nom=data["nom"],
        preparation=data.get("preparation"),
        cuisson=data.get("cuisson"),
        portions=data.get("portions"),
        date_ajout=datetime.utcnow(),
    )

    # Ingrédients
    for ing in data.get("ingredients", []):
        recette.ingredients.append(
            Ingredient(
                id=str(uuid.uuid4()),
                nom=ing["nom"],
                quantite=ing.get("quantite"),
                unite=ing.get("unite"),
                indispensable=ing.get("indispensable", True),
                alternatives=ing.get("alternatives"),
            )
        )

    # Étapes
    for i, etape in enumerate(data.get("etapes", [])):
        recette.etapes.append(Etape(id=str(uuid.uuid4()), ordre=i + 1, description=etape))

    # Catégories
    for cat in data.get("categories", []):
        recette.categories.append(Categorie(id=str(uuid.uuid4()), nom=cat))

    # Tags
    for tag in data.get("tags", []):
        recette.tags.append(Tag(id=str(uuid.uuid4()), nom=tag))

    # Photos
    for photo in data.get("photos", []):
        recette.photos.append(Photo(id=str(uuid.uuid4()), chemin=photo["chemin"], categorie=photo.get("categorie")))

    # Source
    if "source" in data:
        src = data["source"]
        recette.source = Source(
            id=str(uuid.uuid4()),
            type=src["type"],
            url=src.get("url"),
            book_title=src.get("book_title"),
            book_authors=src.get("book_authors"),
            book_page=src.get("book_page"),
        )

    session.add(recette)
    session.commit()
    session.refresh(recette)
    return recette


def get_recette_by_id(session: Session, recette_id: str) -> Recette | None:
    # return session.query(Recette).filter(Recette.id == recette_id).first()
    return (
        session.query(Recette)
        .options(
            joinedload(Recette.categories),
            joinedload(Recette.tags),
            joinedload(Recette.ingredients),
            joinedload(Recette.etapes),
            joinedload(Recette.source),
            joinedload(Recette.executions).joinedload(Execution.feedbacks).joinedload(FeedbackExecution.convive),
            joinedload(Recette.photos),
        )
        .filter(Recette.id == recette_id)
        .first()
    )


def list_recettes(session: Session) -> list[Recette]:
    return (
        session.query(Recette)
        .options(
            joinedload(Recette.categories),
            joinedload(Recette.tags),
            joinedload(Recette.ingredients),
            joinedload(Recette.etapes),
            joinedload(Recette.photos),
            joinedload(Recette.source)
        )
        .order_by(Recette.date_ajout.desc())
        .all()
    )


def lister_recettes(session: Session, skip: int = 0, limit: int = 100) -> list[Recette]:
    """Liste les recettes avec pagination."""
    return (
        session.query(Recette)
        .options(
            joinedload(Recette.categories),
            joinedload(Recette.tags),
            joinedload(Recette.ingredients),
            joinedload(Recette.etapes),
            joinedload(Recette.photos),
            joinedload(Recette.source)
        )
        .order_by(Recette.date_ajout.desc())
        .offset(skip)
        .limit(limit)
        .all()
    )


def charger_recette(session: Session, recette_id: str) -> Recette | None:
    """Charge une recette par son ID avec toutes ses relations."""
    return get_recette_by_id(session, recette_id)


def supprimer_recette(session: Session, recette_id: str) -> bool:
    """Supprime une recette par son ID."""
    return delete_recette(session, recette_id)


def delete_recette(session: Session, recette_id: str) -> bool:
    recette = get_recette_by_id(session, recette_id)
    if not recette:
        return False
    session.delete(recette)
    session.commit()
    return True


def update_recette(session: Session, recette_id: str, data: dict) -> Recette | None:
    recette = session.query(Recette).filter(Recette.id == recette_id).first()
    print("Recette récupérée :", recette.nom if recette else "introuvable")

    if not recette:
        return None

    # Mise à jour des champs simples
    recette.nom = data.get("nom", recette.nom)
    recette.preparation = data.get("preparation", recette.preparation)
    recette.cuisson = data.get("cuisson", recette.cuisson)
    recette.portions = data.get("portions", recette.portions)

    # Ingrédients : suppression complète + recréation (only if provided and not None)
    if "ingredients" in data and data["ingredients"] is not None:
        recette.ingredients.clear()
        session.flush()
        for ing in data.get("ingredients", []):
            recette.ingredients.append(
                Ingredient(
                    id=str(uuid.uuid4()),
                    nom=ing["nom"],
                    quantite=ing.get("quantite"),
                    unite=ing.get("unite"),
                    indispensable=ing.get("indispensable", True),
                    alternatives=ing.get("alternatives"),
                )
            )

    # Étapes (only if provided and not None)
    if "etapes" in data and data["etapes"] is not None:
        recette.etapes.clear()
        session.flush()
        for i, etape in enumerate(data.get("etapes", [])):
            recette.etapes.append(Etape(id=str(uuid.uuid4()), ordre=i + 1, description=etape))

    # Catégories (only if provided and not None)
    if "categories" in data and data["categories"] is not None:
        recette.categories.clear()
        session.flush()
        for cat in data.get("categories", []):
            recette.categories.append(Categorie(id=str(uuid.uuid4()), nom=cat))

    # Tags (only if provided and not None)
    if "tags" in data and data["tags"] is not None:
        recette.tags.clear()
        session.flush()
        for tag in data.get("tags", []):
            recette.tags.append(Tag(id=str(uuid.uuid4()), nom=tag))

    # Photos (only if provided and not None)
    if "photos" in data and data["photos"] is not None:
        recette.photos.clear()
        session.flush()
        for photo in data.get("photos", []):
            recette.photos.append(Photo(id=str(uuid.uuid4()), chemin=photo["chemin"], categorie=photo.get("categorie")))

    # Source
    if recette.source:
        session.delete(recette.source)
        session.flush()  # 🔥 obligé pour appliquer la suppression avant insert

    if "source" in data:
        src = data["source"]
        recette.source = Source(
            id=str(uuid.uuid4()),
            type=src["type"],
            url=src.get("url"),
            book_title=src.get("book_title"),
            book_authors=src.get("book_authors"),
            book_page=src.get("book_page"),
        )

    session.commit()
    session.refresh(recette)
    return recette
