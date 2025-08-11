package com.apajon.librarecipes.data.repository

import com.apajon.librarecipes.data.api.LibraRecipesApiService
import com.apajon.librarecipes.data.model.RecipeListItem
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
            val response = apiService.getRecipes()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
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
            val response = apiService.searchRecipes(
                nom = nom,
                ingredients = ingredients?.joinToString(","),
                ingredientsMode = ingredientsMode,
                tags = tags?.joinToString(","),
                categories = categories?.joinToString(",")
            )
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}