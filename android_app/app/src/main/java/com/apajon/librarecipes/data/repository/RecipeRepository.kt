package com.apajon.librarecipes.data.repository

import com.apajon.librarecipes.data.local.AppDatabase
import com.apajon.librarecipes.data.local.EntityMapper
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
import com.apajon.librarecipes.data.local.entities.FeedbackExecutionEntity
import com.apajon.librarecipes.data.model.ConviveWithFeedback
import com.apajon.librarecipes.data.model.ExecutionCreate
import com.apajon.librarecipes.data.model.ExecutionWithDetails
import com.apajon.librarecipes.data.model.FeedbackStatus
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.model.RecipeCreate
import com.apajon.librarecipes.data.model.RecipeResponse
import com.apajon.librarecipes.data.model.RecipeDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
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
    
    /**
     * Delete a recipe from the local database.
     * @param recipeId ID of the recipe to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            // First delete related entities (foreign key constraints)
            database.ingredientDao().deleteByRecipeId(recipeId)
            database.etapeDao().deleteByRecipeId(recipeId)
            database.categorieDao().deleteByRecipeId(recipeId)
            database.tagDao().deleteByRecipeId(recipeId)
            database.sourceDao().deleteByRecipeId(recipeId)
            
            // Then delete the recipe itself
            database.recipeDao().deleteRecipeById(recipeId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error deleting recipe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing recipe in the local database.
     * @param recipeId ID of the recipe to update
     * @param recipe Updated recipe data
     * @return Result with updated recipe or error
     */
    suspend fun updateRecipe(recipeId: String, recipe: RecipeCreate): Result<RecipeResponse> {
        return try {
            val recipeWithEntities = EntityMapper.recipeCreateToEntities(recipe, recipeId)
            
            // Delete existing related entities
            database.ingredientDao().deleteByRecipeId(recipeId)
            database.etapeDao().deleteByRecipeId(recipeId)
            database.categorieDao().deleteByRecipeId(recipeId)
            database.tagDao().deleteByRecipeId(recipeId)
            database.sourceDao().deleteByRecipeId(recipeId)
            
            // Update recipe main record
            database.recipeDao().updateRecipe(recipeWithEntities.recipe)
            
            // Insert updated related entities
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
            android.util.Log.e("RecipeRepository", "Error updating recipe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a new execution for a recipe with convives and their feedback.
     * @param executionCreate Data for creating the execution
     * @return Result indicating success or failure
     */
    suspend fun addExecution(executionCreate: ExecutionCreate): Result<String> {
        return try {
            val executionId = UUID.randomUUID().toString()
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(currentDate)
            
            // Create execution
            val execution = ExecutionEntity(
                id = executionId,
                recetteId = executionCreate.recipeId,
                dateExecution = dateString
            )
            
            // Insert execution
            database.executionDao().insertExecution(execution)
            
            // Insert convives and feedback
            for (conviveWithFeedback in executionCreate.convivesWithFeedback) {
                // Ensure convive exists in database
                val existingConvive = database.conviveDao().getConvive(conviveWithFeedback.convive.id)
                if (existingConvive == null) {
                    database.conviveDao().insertConvive(conviveWithFeedback.convive)
                }
                
                // Create feedback entry
                val feedback = FeedbackExecutionEntity(
                    id = UUID.randomUUID().toString(),
                    executionId = executionId,
                    conviveId = conviveWithFeedback.convive.id,
                    statut = conviveWithFeedback.feedback.name
                )
                database.feedbackExecutionDao().insertFeedback(feedback)
            }
            
            Result.success(executionId)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error adding execution", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get executions with details for a specific recipe.
     * @param recipeId ID of the recipe
     * @return Flow of executions with convives and feedback
     */
    fun getExecutionsWithDetailsForRecipe(recipeId: String): Flow<List<ExecutionWithDetails>> {
        // For simplicity, we'll return executions without detailed feedback for now
        // In a production app, we'd implement proper Flow combination
        return database.executionDao().getExecutionsForRecipe(recipeId).map { executions ->
            executions.map { execution ->
                ExecutionWithDetails(
                    execution = execution,
                    convivesWithFeedback = emptyList() // TODO: Load convives and feedback properly
                )
            }
        }
    }
    
    /**
     * Get all convives from the database.
     * @return Flow of all convives
     */
    fun getAllConvives(): Flow<List<ConviveEntity>> {
        return database.conviveDao().getAllConvives()
    }
    
    /**
     * Add or update a convive.
     * @param convive The convive to add/update
     * @return Result indicating success or failure
     */
    suspend fun addOrUpdateConvive(convive: ConviveEntity): Result<String> {
        return try {
            database.conviveDao().insertConvive(convive)
            Result.success(convive.id)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error adding/updating convive", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get convives with same name to handle duplicates.
     * @param nom Name to search for
     * @return List of convives with the same name
     */
    suspend fun getConvivesWithSameName(nom: String): List<ConviveEntity> {
        return try {
            val allConvives = database.conviveDao().getAllConvives().first()
            allConvives.filter { it.nom.equals(nom, ignoreCase = true) }
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error getting convives with same name", e)
            emptyList()
        }
    }
    
    /**
     * Delete an execution.
     * @param executionId ID of the execution to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteExecution(executionId: String): Result<Unit> {
        return try {
            val execution = database.executionDao().getExecution(executionId)
            if (execution != null) {
                database.executionDao().deleteExecution(execution)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exécution non trouvée"))
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error deleting execution", e)
            Result.failure(e)
        }
    }
}