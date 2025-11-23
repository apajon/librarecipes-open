"""
Pydantic schemas for LibraRecipes API.

These schemas define the request and response models for the API endpoints.
"""

from datetime import datetime
from enum import Enum
from typing import List, Optional

from pydantic import BaseModel, Field


class IngredientCreate(BaseModel):
    """Schema for creating an ingredient."""

    nom: str = Field(..., description="Ingredient name")
    quantite: Optional[float] = Field(None, description="Quantity")
    unite: Optional[str] = Field(None, description="Unit of measurement")
    indispensable: bool = Field(True, description="Whether ingredient is essential")
    alternatives: Optional[str] = Field(None, description="Alternative ingredients")


class IngredientResponse(BaseModel):
    """Schema for ingredient response."""

    nom: str
    quantite: Optional[float] = None
    unite: Optional[str] = None
    indispensable: bool = True
    alternatives: Optional[str] = None

    @classmethod
    def from_orm(cls, ingredient):
        """Create from SQLAlchemy model."""
        return cls(
            nom=ingredient.nom,
            quantite=ingredient.quantite,
            unite=ingredient.unite,
            indispensable=ingredient.indispensable,
            alternatives=ingredient.alternatives,
        )

    class Config:
        from_attributes = True


class EtapeCreate(BaseModel):
    """Schema for creating a step."""

    description: str = Field(..., description="Step description")
    ordre: int = Field(..., description="Step order")


class EtapeResponse(BaseModel):
    """Schema for step response."""

    description: str
    ordre: int

    @classmethod
    def from_orm(cls, etape):
        """Create from SQLAlchemy model."""
        return cls(description=etape.description, ordre=etape.ordre)

    class Config:
        from_attributes = True


class PhotoCreate(BaseModel):
    """Schema for creating a photo."""

    chemin: str = Field(..., description="Photo file path")
    categorie: Optional[str] = Field(None, description="Photo category")
    description: Optional[str] = Field(None, description="Photo description")


class PhotoResponse(BaseModel):
    """Schema for photo response."""

    chemin: str
    categorie: Optional[str] = None
    description: Optional[str] = None

    @classmethod
    def from_orm(cls, photo):
        """Create from SQLAlchemy model. Note: model has no `description` field; it's ignored if absent."""
        return cls(chemin=photo.chemin, categorie=photo.categorie, description=getattr(photo, "description", None))

    class Config:
        from_attributes = True


class SourceCreate(BaseModel):
    """Schema for creating a source.

    Standardized to explicit fields matching the SQLAlchemy model: `url` or `book_*`.
    """

    type: str = Field(..., description="Source type (homemade, url, book)")
    url: Optional[str] = Field(None, description="URL for source type 'url'")
    book_title: Optional[str] = Field(None, description="Book title for source type 'book'")
    book_authors: Optional[str] = Field(None, description="Book authors for source type 'book'")
    book_page: Optional[str] = Field(None, description="Book page for source type 'book'")


class SourceResponse(BaseModel):
    """Schema for source response (explicit fields)."""

    type: str
    url: Optional[str] = None
    book_title: Optional[str] = None
    book_authors: Optional[str] = None
    book_page: Optional[str] = None

    @classmethod
    def from_orm(cls, source):
        """Create from SQLAlchemy model."""
        return cls(
            type=source.type,
            url=source.url,
            book_title=source.book_title,
            book_authors=source.book_authors,
            book_page=source.book_page,
        )

    class Config:
        from_attributes = True


class RecetteCreate(BaseModel):
    """Schema for creating a recipe."""

    nom: str = Field(..., description="Recipe name")
    preparation: Optional[int] = Field(None, description="Preparation time in minutes")
    cuisson: Optional[int] = Field(None, description="Cooking time in minutes")
    portions: Optional[int] = Field(None, description="Number of servings")
    ingredients: List[IngredientCreate] = Field(default_factory=list, description="Recipe ingredients")
    etapes: List[str] = Field(default_factory=list, description="Recipe steps")
    categories: List[str] = Field(default_factory=list, description="Recipe categories")
    tags: List[str] = Field(default_factory=list, description="Recipe tags")
    photos: List[PhotoCreate] = Field(default_factory=list, description="Recipe photos")
    source: Optional[SourceCreate] = Field(None, description="Recipe source")


class RecetteUpdate(BaseModel):
    """Schema for updating a recipe."""

    nom: Optional[str] = Field(None, description="Recipe name")
    preparation: Optional[int] = Field(None, description="Preparation time in minutes")
    cuisson: Optional[int] = Field(None, description="Cooking time in minutes")
    portions: Optional[int] = Field(None, description="Number of servings")
    ingredients: Optional[List[IngredientCreate]] = Field(None, description="Recipe ingredients")
    etapes: Optional[List[str]] = Field(None, description="Recipe steps")
    categories: Optional[List[str]] = Field(None, description="Recipe categories")
    tags: Optional[List[str]] = Field(None, description="Recipe tags")
    photos: Optional[List[PhotoCreate]] = Field(None, description="Recipe photos")
    source: Optional[SourceCreate] = Field(None, description="Recipe source")


class RecetteListResponse(BaseModel):
    """Schema for recipe list response (summary)."""

    id: str
    nom: str
    preparation: Optional[int] = None
    cuisson: Optional[int] = None
    portions: Optional[int] = None
    date_ajout: datetime
    categories: List[str] = Field(default_factory=list)
    tags: List[str] = Field(default_factory=list)

    @classmethod
    def from_orm(cls, recipe):
        """Create from SQLAlchemy model."""
        return cls(
            id=recipe.id,
            nom=recipe.nom,
            preparation=recipe.preparation,
            cuisson=recipe.cuisson,
            portions=recipe.portions,
            date_ajout=recipe.date_ajout,
            categories=[cat.nom for cat in recipe.categories],
            tags=[tag.nom for tag in recipe.tags],
        )

    class Config:
        from_attributes = True


class RecetteResponse(BaseModel):
    """Schema for full recipe response."""

    id: str
    nom: str
    preparation: Optional[int] = None
    cuisson: Optional[int] = None
    portions: Optional[int] = None
    date_ajout: datetime
    ingredients: List[IngredientResponse] = Field(default_factory=list)
    etapes: List[EtapeResponse] = Field(default_factory=list)
    categories: List[str] = Field(default_factory=list)
    tags: List[str] = Field(default_factory=list)
    photos: List[PhotoResponse] = Field(default_factory=list)
    source: Optional[SourceResponse] = None

    @classmethod
    def from_orm(cls, recipe):
        """Create from SQLAlchemy model."""
        return cls(
            id=recipe.id,
            nom=recipe.nom,
            preparation=recipe.preparation,
            cuisson=recipe.cuisson,
            portions=recipe.portions,
            date_ajout=recipe.date_ajout,
            ingredients=[IngredientResponse.from_orm(ing) for ing in recipe.ingredients],
            etapes=[EtapeResponse.from_orm(etape) for etape in recipe.etapes],
            categories=[cat.nom for cat in recipe.categories],
            tags=[tag.nom for tag in recipe.tags],
            photos=[PhotoResponse.from_orm(photo) for photo in recipe.photos],
            source=SourceResponse.from_orm(recipe.source) if recipe.source else None,
        )

    class Config:
        from_attributes = True


class IngredientsMode(str, Enum):
    """Enum for ingredients search mode."""

    ANY = "ANY"
    ALL = "ALL"


class SearchFilters(BaseModel):
    """Schema for recipe search filters."""

    nom: Optional[str] = Field(None, description="Recipe name filter")
    ingredients: Optional[List[str]] = Field(None, description="Ingredients filter")
    ingredients_mode: IngredientsMode = Field(IngredientsMode.ANY, description="Ingredients search mode")
    tags: Optional[List[str]] = Field(None, description="Tags filter")
    categories: Optional[List[str]] = Field(None, description="Categories filter")


class CategorieResponse(BaseModel):
    """Schema for category response."""

    nom: str

    class Config:
        from_attributes = True


class TagResponse(BaseModel):
    """Schema for tag response."""

    nom: str

    class Config:
        from_attributes = True


class ErrorResponse(BaseModel):
    """Schema for error response."""

    detail: str
    status_code: int
    status_code: int
