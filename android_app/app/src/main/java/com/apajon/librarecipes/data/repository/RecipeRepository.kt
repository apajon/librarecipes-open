package com.apajon.librarecipes.data.repository

import com.apajon.librarecipes.data.api.LibraRecipesApiService
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.model.RecipeCreate
import com.apajon.librarecipes.data.model.RecipeResponse
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
     * Create a new recipe.
     * @param recipe Recipe data to create
     * @return Result with created recipe or error
     */
    suspend fun createRecipe(recipe: RecipeCreate): Result<RecipeResponse> {
        return try {
            val response = apiService.createRecipe(recipe)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search recipes with filters.
     * @param nom Recipe name filter
     * @param ingredients List of ingredients to search for
     * @param ingredientsMode Search mode: "ANY" or "ALL"
     * @param tags List of tags to filter by
     * @param categories List of categories to filter by
     * @return Flow of filtered recipe list
     */
    fun searchRecipes(
        nom: String? = null,
        ingredients: List<String>? = null,
        ingredientsMode: String = "ANY",
        tags: List<String>? = null,
        categories: List<String>? = null
    ): Flow<List<RecipeListItem>> = flow {
        try {
            val recipes = apiService.searchRecipes(
                nom = nom,
                ingredients = ingredients?.joinToString(","),
                ingredientsMode = ingredientsMode,
                tags = tags?.joinToString(","),
                categories = categories?.joinToString(",")
            )
            emit(recipes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}