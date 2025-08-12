package com.apajon.librarecipes.data.repository

import com.apajon.librarecipes.data.api.LibraRecipesApiService
import com.apajon.librarecipes.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for recipe data operations.
 * Handles data fetching from the API and provides data to ViewModels.
 */
@Singleton
class RecipeRepository @Inject constructor(
    private val apiService: LibraRecipesApiService
) {
    
    /**
     * Get all recipes from the API.
     * @return Flow of recipe list
     */
    fun getRecipes(): Flow<List<RecipeListItem>> = flow {
        try {
            val recipes = apiService.getRecipes()
            emit(recipes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get specific recipe by ID.
     * @param recipeId Recipe ID
     * @return Flow of recipe details
     */
    fun getRecipe(recipeId: String): Flow<RecipeDetailResponse?> = flow {
        try {
            val recipe = apiService.getRecipe(recipeId)
            emit(recipe)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    /**
     * Create a new recipe.
     * @param recipe Recipe data to create
     * @return Flow of created recipe details or null if failed
     */
    fun createRecipe(recipe: RecipeCreateRequest): Flow<RecipeDetailResponse?> = flow {
        try {
            val createdRecipe = apiService.createRecipe(recipe)
            emit(createdRecipe)
        } catch (e: Exception) {
            emit(null)
        }
    }
    
    /**
     * Search recipes with filters.
     * @param searchFilters Search criteria
     * @return Flow of filtered recipe list
     */
    fun searchRecipes(searchFilters: SearchFilters): Flow<List<RecipeListItem>> = flow {
        try {
            val recipes = apiService.searchRecipes(searchFilters)
            emit(recipes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get all available ingredients for autocomplete.
     * @return Flow of ingredient names
     */
    fun getIngredients(): Flow<List<String>> = flow {
        try {
            val ingredients = apiService.getIngredients()
            emit(ingredients.map { it.nom })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get all available categories.
     * @return Flow of category names
     */
    fun getCategories(): Flow<List<String>> = flow {
        try {
            val categories = apiService.getCategories()
            emit(categories.map { it.nom })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Get all available tags.
     * @return Flow of tag names
     */
    fun getTags(): Flow<List<String>> = flow {
        try {
            val tags = apiService.getTags()
            emit(tags.map { it.nom })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}