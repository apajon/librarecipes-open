package com.apajon.librarecipes.data.api

import com.apajon.librarecipes.data.model.*
import retrofit2.http.*

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
    suspend fun getRecipes(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): List<RecipeListItem>
    
    /**
     * Get specific recipe by ID.
     * @param recipeId Recipe ID
     * @return Complete recipe details
     */
    @GET("recipes/{recipe_id}")
    suspend fun getRecipe(@Path("recipe_id") recipeId: String): RecipeDetailResponse
    
    /**
     * Create a new recipe.
     * @param recipe Recipe data to create
     * @return Created recipe details
     */
    @POST("recipes")
    suspend fun createRecipe(@Body recipe: RecipeCreateRequest): RecipeDetailResponse
    
    /**
     * Search recipes with filters.
     * @param searchFilters Search criteria
     * @return Filtered list of recipes
     */
    @POST("recipes/search")
    suspend fun searchRecipes(@Body searchFilters: SearchFilters): List<RecipeListItem>
    
    // Metadata endpoints for form data
    
    /**
     * Get all available ingredients.
     * @return List of ingredient names
     */
    @GET("metadata/ingredients")
    suspend fun getIngredients(): List<IngredientMetadata>
    
    /**
     * Get all available categories.
     * @return List of category names
     */
    @GET("metadata/categories")
    suspend fun getCategories(): List<CategoryMetadata>
    
    /**
     * Get all available tags.
     * @return List of tag names
     */
    @GET("metadata/tags")
    suspend fun getTags(): List<TagMetadata>
}