package com.apajon.librarecipes.data.repository

import com.apajon.librarecipes.data.local.AppDatabase
import com.apajon.librarecipes.data.local.EntityMapper
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.model.RecipeCreate
import com.apajon.librarecipes.data.model.RecipeResponse
import com.apajon.librarecipes.data.model.RecipeDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed class representing the result of a recipe operation.
 */
sealed class RecipeResult {
    data class Success(val recipes: List<RecipeListItem>) : RecipeResult()
    data class Error(val message: String) : RecipeResult()
}

/**
 * Repository for recipe data operations.
 * Handles data operations using local Room database.
 */
@Singleton
class RecipeRepository @Inject constructor(
    private val database: AppDatabase
) {
    
    /**
     * Get all recipes from the local database.
     * @return Flow of recipe list with error information
     */
    fun getRecipes(): Flow<RecipeResult> = flow {
        try {
            database.recipeDao().getAllRecipes().map { recipes ->
                val recipeListItems = recipes.map { recipe ->
                    EntityMapper.recipeEntityToListItem(recipe)
                }
                RecipeResult.Success(recipeListItems)
            }.collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            // Log the error for debugging
            android.util.Log.e("RecipeRepository", "Error fetching recipes", e)
            emit(RecipeResult.Error("Erreur lors de la récupération des recettes depuis la base de données locale."))
        }
    }
    
    /**
     * Create a new recipe in the local database.
     * @param recipe Recipe data to create
     * @return Result with created recipe or error
     */
    suspend fun createRecipe(recipe: RecipeCreate): Result<RecipeResponse> {
        return try {
            val recipeWithEntities = EntityMapper.recipeCreateToEntities(recipe)
            
            // Insert recipe
            database.recipeDao().insertRecipe(recipeWithEntities.recipe)
            
            // Insert related entities
            database.ingredientDao().insertIngredients(recipeWithEntities.ingredients)
            database.etapeDao().insertEtapes(recipeWithEntities.etapes)
            database.categorieDao().insertCategories(recipeWithEntities.categories)
            database.tagDao().insertTags(recipeWithEntities.tags)
            recipeWithEntities.source?.let { source ->
                database.sourceDao().insertSource(source)
            }
            
            // Return response
            val response = RecipeResponse(
                id = recipeWithEntities.recipe.id,
                nom = recipeWithEntities.recipe.nom,
                preparation = recipeWithEntities.recipe.preparation,
                cuisson = recipeWithEntities.recipe.cuisson,
                portions = recipeWithEntities.recipe.portions,
                dateAjout = EntityMapper.dateFormat.format(recipeWithEntities.recipe.dateAjout),
                derniereExecution = null,
                source = recipeWithEntities.source?.let { source ->
                    com.apajon.librarecipes.data.model.SourceResponse(
                        type = source.type,
                        valeur = when (source.type) {
                            "url" -> source.url
                            "book" -> source.bookTitle
                            else -> null
                        }
                    )
                }
            )
            
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error creating recipe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get recipe details by ID from the local database.
     * @param recipeId ID of the recipe to fetch
     * @return Result with recipe details or error
     */
    suspend fun getRecipeDetails(recipeId: String): Result<RecipeDetail> {
        return try {
            val recipeWithDetails = database.recipeDao().getRecipeWithDetails(recipeId)
            if (recipeWithDetails != null) {
                val recipeDetail = EntityMapper.recipeWithDetailsToDetail(recipeWithDetails)
                Result.success(recipeDetail)
            } else {
                Result.failure(Exception("Recette non trouvée"))
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error fetching recipe details", e)
            Result.failure(e)
        }
    }

    /**
     * Search recipes with filters in the local database.
     * @param nom Recipe name filter
     * @param ingredients List of ingredients to search for
     * @param ingredientsMode Search mode: "ANY" or "ALL" (not fully implemented)
     * @param tags List of tags to filter by
     * @param categories List of categories to filter by
     * @return Flow of filtered recipe list with error information
     */
    fun searchRecipes(
        nom: String? = null,
        ingredients: List<String>? = null,
        ingredientsMode: String = "ANY",
        tags: List<String>? = null,
        categories: List<String>? = null
    ): Flow<RecipeResult> = flow {
        try {
            val recipes = database.recipeDao().searchRecipes(
                nom = nom,
                ingredients = ingredients,
                categories = categories,
                tags = tags
            )
            val recipeListItems = recipes.map { recipe ->
                EntityMapper.recipeEntityToListItem(recipe)
            }
            emit(RecipeResult.Success(recipeListItems))
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error searching recipes", e)
            emit(RecipeResult.Error("Erreur lors de la recherche dans la base de données locale."))
        }
    }
}