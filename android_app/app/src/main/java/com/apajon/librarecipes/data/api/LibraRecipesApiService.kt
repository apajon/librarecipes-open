package com.apajon.librarecipes.data.api

import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.model.RecipeCreate
import com.apajon.librarecipes.data.model.RecipeResponse
import com.apajon.librarecipes.data.model.SearchFilters
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
    @GET("recipes")
    suspend fun getRecipes(): List<RecipeListItem>
    
    /**
     * Create a new recipe.
     * @param recipe Recipe data to create
     * @return Created recipe with ID and metadata
     */
    @POST("recipes")
    suspend fun createRecipe(@Body recipe: RecipeCreate): RecipeResponse
    
    /**
     * Search recipes with filters.
     * @param searchFilters Search filters request body
     * @return Filtered list of recipes
     */
    @POST("recipes/search")
    suspend fun searchRecipes(@Body searchFilters: SearchFilters): List<RecipeListItem>
}