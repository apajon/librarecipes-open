"""
Android-compatible database layer using sqlite3 directly
This replaces SQLAlchemy for Android builds to avoid compatibility issues
"""

import json
import os
import sqlite3
from contextlib import contextmanager
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from kivy.utils import platform


def get_db_path():
    """Get the database path based on platform"""
    if platform == "android":
        from android.storage import app_storage_path

        db_path = os.path.join(app_storage_path(), "recettes.db")
    else:
        # For desktop/development
        db_path = "data/recettes.db"
        # Ensure directory exists
        Path("data").mkdir(exist_ok=True)
    return db_path


@contextmanager
def get_db_connection():
    """Get database connection with proper cleanup"""
    conn = sqlite3.connect(get_db_path())
    conn.row_factory = sqlite3.Row  # Enable dict-like access to rows
    try:
        yield conn
    finally:
        conn.close()


def init_database():
    """Initialize the database with required tables"""
    with get_db_connection() as conn:
        cursor = conn.cursor()

        # Create recipes table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS recettes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                description TEXT,
                temps_preparation INTEGER,
                nombre_personnes INTEGER,
                ingredients TEXT,  -- JSON string
                etapes TEXT,       -- JSON string
                date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                source_type TEXT DEFAULT 'homemade',
                source_url TEXT,
                source_livre TEXT,
                source_page INTEGER
            )
        """
        )

        # Create executions table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS executions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                recette_id INTEGER NOT NULL,
                date_execution TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                commentaire TEXT,
                note INTEGER,
                FOREIGN KEY (recette_id) REFERENCES recettes (id)
            )
        """
        )

        # Create categories table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL UNIQUE
            )
        """
        )

        # Create recipe_categories junction table
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS recipe_categories (
                recette_id INTEGER,
                category_id INTEGER,
                PRIMARY KEY (recette_id, category_id),
                FOREIGN KEY (recette_id) REFERENCES recettes (id),
                FOREIGN KEY (category_id) REFERENCES categories (id)
            )
        """
        )

        conn.commit()


class Recipe:
    """Simple recipe model for Android compatibility"""

    def __init__(
        self,
        id=None,
        nom="",
        description="",
        temps_preparation=0,
        nombre_personnes=4,
        ingredients=None,
        etapes=None,
        date_ajout=None,
        date_modification=None,
        source_type="homemade",
        source_url="",
        source_livre="",
        source_page=None,
    ):
        self.id = id
        self.nom = nom
        self.description = description
        self.temps_preparation = temps_preparation
        self.nombre_personnes = nombre_personnes
        self.ingredients = ingredients or []
        self.etapes = etapes or []
        self.date_ajout = date_ajout or datetime.now()
        self.date_modification = date_modification or datetime.now()
        self.source_type = source_type
        self.source_url = source_url
        self.source_livre = source_livre
        self.source_page = source_page
        self.executions = []
        self.categories = []

    @classmethod
    def from_row(cls, row):
        """Create Recipe instance from database row"""
        recipe = cls(
            id=row["id"],
            nom=row["nom"],
            description=row["description"],
            temps_preparation=row["temps_preparation"],
            nombre_personnes=row["nombre_personnes"],
            ingredients=json.loads(row["ingredients"]) if row["ingredients"] else [],
            etapes=json.loads(row["etapes"]) if row["etapes"] else [],
            date_ajout=datetime.fromisoformat(row["date_ajout"]) if row["date_ajout"] else None,
            date_modification=datetime.fromisoformat(row["date_modification"]) if row["date_modification"] else None,
            source_type=row["source_type"],
            source_url=row["source_url"],
            source_livre=row["source_livre"],
            source_page=row["source_page"],
        )
        return recipe


def get_all_recipes() -> List[Recipe]:
    """Get all recipes from database"""
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM recettes ORDER BY nom")
        rows = cursor.fetchall()
        return [Recipe.from_row(row) for row in rows]


def get_recipe_by_id(recipe_id: int) -> Optional[Recipe]:
    """Get a specific recipe by ID"""
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("SELECT * FROM recettes WHERE id = ?", (recipe_id,))
        row = cursor.fetchone()
        return Recipe.from_row(row) if row else None


def save_recipe(recipe: Recipe) -> Recipe:
    """Save or update a recipe"""
    with get_db_connection() as conn:
        cursor = conn.cursor()

        ingredients_json = json.dumps(recipe.ingredients)
        etapes_json = json.dumps(recipe.etapes)

        if recipe.id:
            # Update existing recipe
            cursor.execute(
                """
                UPDATE recettes SET
                    nom = ?, description = ?, temps_preparation = ?,
                    nombre_personnes = ?, ingredients = ?, etapes = ?,
                    date_modification = ?, source_type = ?, source_url = ?,
                    source_livre = ?, source_page = ?
                WHERE id = ?
            """,
                (
                    recipe.nom,
                    recipe.description,
                    recipe.temps_preparation,
                    recipe.nombre_personnes,
                    ingredients_json,
                    etapes_json,
                    datetime.now().isoformat(),
                    recipe.source_type,
                    recipe.source_url,
                    recipe.source_livre,
                    recipe.source_page,
                    recipe.id,
                ),
            )
        else:
            # Create new recipe
            cursor.execute(
                """
                INSERT INTO recettes (
                    nom, description, temps_preparation, nombre_personnes,
                    ingredients, etapes, date_ajout, date_modification,
                    source_type, source_url, source_livre, source_page
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
                (
                    recipe.nom,
                    recipe.description,
                    recipe.temps_preparation,
                    recipe.nombre_personnes,
                    ingredients_json,
                    etapes_json,
                    recipe.date_ajout.isoformat(),
                    datetime.now().isoformat(),
                    recipe.source_type,
                    recipe.source_url,
                    recipe.source_livre,
                    recipe.source_page,
                ),
            )
            recipe.id = cursor.lastrowid

        conn.commit()
        return recipe


def delete_recipe(recipe_id: int):
    """Delete a recipe and its executions"""
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute("DELETE FROM executions WHERE recette_id = ?", (recipe_id,))
        cursor.execute("DELETE FROM recipe_categories WHERE recette_id = ?", (recipe_id,))
        cursor.execute("DELETE FROM recettes WHERE id = ?", (recipe_id,))
        conn.commit()


def search_recipes(query: str) -> List[Recipe]:
    """Search recipes by name or description"""
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute(
            """
            SELECT * FROM recettes
            WHERE nom LIKE ? OR description LIKE ?
            ORDER BY nom
        """,
            (f"%{query}%", f"%{query}%"),
        )
        rows = cursor.fetchall()
        return [Recipe.from_row(row) for row in rows]
