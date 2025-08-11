package com.apajon.librarecipes.data.api

import com.apajon.librarecipes.data.model.RecipeListItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API service interface for LibraRecipes backend.
 * Defines the API endpoints for recipe management operations.
 */
interface LibraRecipesApiService {
    
    /**
     * Get all recipes (list view).
     * @return List of recipe summaries
     */
    @GET("recettes/")
    suspend fun getRecipes(): Response<List<RecipeListItem>>
    
    /**
     * Search recipes with filters.
     * @param nom Recipe name filter
     * @param ingredients Comma-separated list of ingredients
     * @param ingredientsMode Search mode: "ANY" or "ALL"
     * @param tags Comma-separated list of tags
     * @param categories Comma-separated list of categories
     * @return Filtered list of recipes
     */
    @GET("recettes/recherche")
    suspend fun searchRecipes(
        @Query("nom") nom: String? = null,
        @Query("ingredients") ingredients: String? = null,
        @Query("ingredients_mode") ingredientsMode: String? = null,
        @Query("tags") tags: String? = null,
        @Query("categories") categories: String? = null
    ): Response<List<RecipeListItem>>
}