"""
FastAPI backend for LibraRecipes.

This module provides REST API endpoints for all recipe management operations.
"""

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
import logging

from src.db import get_db_session
from src.crud import recettes as crud_recettes
from src.crud import recherche as crud_recherche
from src.crud import metadata as crud_metadata
from backend.schemas import (
    RecetteCreate, RecetteUpdate, RecetteResponse,
    SearchFilters, RecetteListResponse,
    IngredientResponse, CategorieResponse, TagResponse
)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="LibraRecipes API",
    description="REST API for LibraRecipes - Recipe Management System",
    version="2.3.18"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure as needed for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_db() -> Session:
    """Dependency to get database session."""
    with get_db_session() as session:
        yield session


@app.get("/")
async def root():
    """Root endpoint."""
    return {"message": "LibraRecipes API", "version": "2.3.18"}


@app.get("/health")
async def health_check():
    """Health check endpoint."""
    return {"status": "healthy"}


# Recipe endpoints
@app.post("/recipes", response_model=RecetteResponse)
async def create_recipe(
    recipe_data: RecetteCreate,
    db: Session = Depends(get_db)
):
    """Create a new recipe."""
    try:
        recipe = crud_recettes.create_recette(db, recipe_data.model_dump())
        return RecetteResponse.from_orm(recipe)
    except Exception as e:
        logger.error(f"Error creating recipe: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/recipes", response_model=List[RecetteListResponse])
async def list_recipes(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    """List all recipes with pagination."""
    try:
        recipes = crud_recettes.lister_recettes(db, skip=skip, limit=limit)
        return [RecetteListResponse.from_orm(recipe) for recipe in recipes]
    except Exception as e:
        logger.error(f"Error listing recipes: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/recipes/{recipe_id}", response_model=RecetteResponse)
async def get_recipe(recipe_id: str, db: Session = Depends(get_db)):
    """Get a recipe by ID."""
    try:
        recipe = crud_recettes.charger_recette(db, recipe_id)
        if not recipe:
            raise HTTPException(status_code=404, detail="Recipe not found")
        return RecetteResponse.from_orm(recipe)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting recipe {recipe_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/recipes/{recipe_id}", response_model=RecetteResponse)
async def update_recipe(
    recipe_id: str,
    recipe_data: RecetteUpdate,
    db: Session = Depends(get_db)
):
    """Update a recipe."""
    try:
        recipe = crud_recettes.update_recette(db, recipe_id, recipe_data.model_dump())
        if not recipe:
            raise HTTPException(status_code=404, detail="Recipe not found")
        return RecetteResponse.from_orm(recipe)
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error updating recipe {recipe_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/recipes/{recipe_id}")
async def delete_recipe(recipe_id: str, db: Session = Depends(get_db)):
    """Delete a recipe."""
    try:
        success = crud_recettes.supprimer_recette(db, recipe_id)
        if not success:
            raise HTTPException(status_code=404, detail="Recipe not found")
        return {"message": "Recipe deleted successfully"}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error deleting recipe {recipe_id}: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/recipes/search", response_model=List[RecetteListResponse])
async def search_recipes(
    search_filters: SearchFilters,
    db: Session = Depends(get_db)
):
    """Search recipes with filters."""
    try:
        recipes = crud_recherche.rechercher_recettes(
            db,
            nom=search_filters.nom,
            ingredients=search_filters.ingredients,
            ingredients_mode=search_filters.ingredients_mode,
            tags=search_filters.tags,
            categories=search_filters.categories
        )
        return [RecetteListResponse.from_orm(recipe) for recipe in recipes]
    except Exception as e:
        logger.error(f"Error searching recipes: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# Metadata endpoints
@app.get("/metadata/ingredients", response_model=List[IngredientResponse])
async def list_ingredients(db: Session = Depends(get_db)):
    """List all unique ingredients."""
    try:
        ingredients = crud_metadata.lister_ingredients(db)
        return [IngredientResponse(nom=ing) for ing in ingredients]
    except Exception as e:
        logger.error(f"Error listing ingredients: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/metadata/categories", response_model=List[CategorieResponse])
async def list_categories(db: Session = Depends(get_db)):
    """List all categories."""
    try:
        categories = crud_metadata.lister_categories(db)
        return [CategorieResponse(nom=cat) for cat in categories]
    except Exception as e:
        logger.error(f"Error listing categories: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/metadata/tags", response_model=List[TagResponse])
async def list_tags(db: Session = Depends(get_db)):
    """List all tags."""
    try:
        tags = crud_metadata.lister_tags(db)
        return [TagResponse(nom=tag) for tag in tags]
    except Exception as e:
        logger.error(f"Error listing tags: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)