"""
Pydantic schemas for LibraRecipes API.

These schemas define the request and response models for the API endpoints.
"""

from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime
from enum import Enum


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

    class Config:
        from_attributes = True


class SourceCreate(BaseModel):
    """Schema for creating a source."""
    type: str = Field(..., description="Source type (homemade, url, book)")
    valeur: Optional[str] = Field(None, description="Source value")


class SourceResponse(BaseModel):
    """Schema for source response."""
    type: str
    valeur: Optional[str] = None

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