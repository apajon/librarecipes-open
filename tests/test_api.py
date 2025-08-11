"""
Tests for the FastAPI backend endpoints.
"""

import pytest
from fastapi.testclient import TestClient
from backend.main import app
from src.db import get_db_session

client = TestClient(app)


def test_root_endpoint():
    """Test the root endpoint."""
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert data["message"] == "LibraRecipes API"
    assert data["version"] == "2.3.18"


def test_health_check():
    """Test the health check endpoint."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"


def test_create_recipe():
    """Test creating a new recipe via API."""
    recipe_data = {
        "nom": "Test API Recipe",
        "preparation": 15,
        "cuisson": 20,
        "portions": 4,
        "ingredients": [
            {"nom": "Flour", "quantite": 200, "unite": "g", "indispensable": True},
            {"nom": "Eggs", "quantite": 2, "unite": "pieces", "indispensable": True}
        ],
        "etapes": ["Mix flour", "Add eggs", "Bake"],
        "categories": ["Dessert"],
        "tags": ["easy", "quick"],
        "source": {"type": "homemade"}
    }
    
    response = client.post("/recipes", json=recipe_data)
    assert response.status_code == 200
    data = response.json()
    assert data["nom"] == "Test API Recipe"
    assert data["preparation"] == 15
    assert data["cuisson"] == 20
    assert data["portions"] == 4
    assert len(data["ingredients"]) == 2
    assert len(data["etapes"]) == 3
    return data["id"]  # Return the created recipe ID for other tests


def test_list_recipes():
    """Test listing recipes via API."""
    response = client.get("/recipes")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_get_recipe():
    """Test getting a specific recipe via API."""
    # First create a recipe
    recipe_id = test_create_recipe()
    
    # Then get it
    response = client.get(f"/recipes/{recipe_id}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == recipe_id
    assert data["nom"] == "Test API Recipe"


def test_update_recipe():
    """Test updating a recipe via API."""
    # First create a recipe
    recipe_id = test_create_recipe()
    
    # Update data
    update_data = {
        "nom": "Updated Test API Recipe",
        "preparation": 25,
        "ingredients": [
            {"nom": "Updated Flour", "quantite": 250, "unite": "g", "indispensable": True}
        ]
    }
    
    response = client.put(f"/recipes/{recipe_id}", json=update_data)
    assert response.status_code == 200
    data = response.json()
    assert data["nom"] == "Updated Test API Recipe"
    assert data["preparation"] == 25
    assert len(data["ingredients"]) == 1
    assert data["ingredients"][0]["nom"] == "Updated Flour"


def test_search_recipes():
    """Test searching recipes via API."""
    search_filters = {
        "nom": "Test",
        "ingredients_mode": "ANY"
    }
    
    response = client.post("/recipes/search", json=search_filters)
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_metadata_endpoints():
    """Test metadata endpoints."""
    # Test ingredients
    response = client.get("/metadata/ingredients")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    
    # Test categories
    response = client.get("/metadata/categories")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    
    # Test tags
    response = client.get("/metadata/tags")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


def test_delete_recipe():
    """Test deleting a recipe via API."""
    # First create a recipe
    recipe_id = test_create_recipe()
    
    # Then delete it
    response = client.delete(f"/recipes/{recipe_id}")
    assert response.status_code == 200
    data = response.json()
    assert data["message"] == "Recipe deleted successfully"
    
    # Verify it's deleted
    response = client.get(f"/recipes/{recipe_id}")
    assert response.status_code == 404


if __name__ == "__main__":
    pytest.main([__file__, "-v"])