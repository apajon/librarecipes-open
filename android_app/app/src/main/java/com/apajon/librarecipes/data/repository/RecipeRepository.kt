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
 * Data class representing the execution status of a recipe.
 */
data class ExecutionStatus(
    val hasExecutions: Boolean,
    val lastExecutionDate: String?
)

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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(executionCreate.executionDate)
            
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
        return database.executionDao().getExecutionsForRecipe(recipeId).map { executions ->
            executions.map { execution ->
                // Get feedback for this execution
                val feedbackList = database.feedbackExecutionDao().getFeedbackForExecution(execution.id).first()
                
                // Get convives with their feedback
                val convivesWithFeedback = feedbackList.mapNotNull { feedback ->
                    val convive = database.conviveDao().getConvive(feedback.conviveId)
                    if (convive != null) {
                        val feedbackStatus = when (feedback.statut) {
                            "AIME" -> com.apajon.librarecipes.data.model.FeedbackStatus.AIME
                            "PARTIELLEMENT" -> com.apajon.librarecipes.data.model.FeedbackStatus.PARTIELLEMENT
                            "RIEN_MANGE" -> com.apajon.librarecipes.data.model.FeedbackStatus.RIEN_MANGE
                            else -> com.apajon.librarecipes.data.model.FeedbackStatus.AIME
                        }
                        com.apajon.librarecipes.data.model.ConviveWithFeedback(convive, feedbackStatus)
                    } else null
                }
                
                ExecutionWithDetails(
                    execution = execution,
                    convivesWithFeedback = convivesWithFeedback
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
                // Delete associated feedback first
                database.feedbackExecutionDao().deleteFeedbackForExecution(executionId)
                // Then delete the execution
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
    
    /**
     * Update an execution with new convives and feedback.
     * @param executionId ID of the execution to update
     * @param executionCreate New execution data
     * @return Result indicating success or failure
     */
    suspend fun updateExecution(executionId: String, executionCreate: ExecutionCreate): Result<Unit> {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val dateString = dateFormat.format(executionCreate.executionDate)
            
            // Update execution date
            val execution = database.executionDao().getExecution(executionId)
            if (execution != null) {
                val updatedExecution = execution.copy(dateExecution = dateString)
                database.executionDao().updateExecution(updatedExecution)
                
                // Delete existing feedback for this execution
                database.feedbackExecutionDao().deleteFeedbackForExecution(executionId)
                
                // Add new feedback
                for (conviveWithFeedback in executionCreate.convivesWithFeedback) {
                    // Ensure convive exists in database
                    val existingConvive = database.conviveDao().getConvive(conviveWithFeedback.convive.id)
                    if (existingConvive == null) {
                        database.conviveDao().insertConvive(conviveWithFeedback.convive)
                    }
                    
                    val feedback = FeedbackExecutionEntity(
                        id = UUID.randomUUID().toString(),
                        executionId = executionId,
                        conviveId = conviveWithFeedback.convive.id,
                        statut = conviveWithFeedback.feedback.name
                    )
                    database.feedbackExecutionDao().insertFeedback(feedback)
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exécution non trouvée"))
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error updating execution", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all unique ingredient names from the database.
     * @return Flow of unique ingredient names
     */
    fun getAllUniqueIngredients(): Flow<List<String>> {
        return flow {
            try {
                val ingredients = database.ingredientDao().getAllUniqueIngredientNames()
                emit(ingredients)
            } catch (e: Exception) {
                android.util.Log.e("RecipeRepository", "Error getting unique ingredients", e)
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get recipes that contain a specific ingredient.
     * @param ingredientName Name of the ingredient to search for
     * @return Flow of recipes containing the ingredient
     */
    fun getRecipesByIngredient(ingredientName: String): Flow<List<RecipeListItem>> {
        return flow {
            try {
                val recipeIds = database.ingredientDao().getRecipeIdsWithIngredient(ingredientName)
                val allRecipes = database.recipeDao().getAllRecipes().first()
                val filteredRecipes = allRecipes.filter { recipe -> recipe.id in recipeIds }
                val recipeListItems = filteredRecipes.map { EntityMapper.recipeEntityToListItem(it) }
                emit(recipeListItems)
            } catch (e: Exception) {
                android.util.Log.e("RecipeRepository", "Error getting recipes by ingredient", e)
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get recipes with their execution status (whether they have executions and last execution date).
     * @return Flow of recipes with execution information
     */
    fun getRecipesWithExecutionStatus(): Flow<Map<String, ExecutionStatus>> {
        return flow {
            try {
                val recipes = database.recipeDao().getAllRecipes().first()
                val executionStatus = mutableMapOf<String, ExecutionStatus>()
                
                for (recipe in recipes) {
                    val executions = database.executionDao().getExecutionsForRecipe(recipe.id).first()
                    
                    if (executions.isEmpty()) {
                        executionStatus[recipe.id] = ExecutionStatus(
                            hasExecutions = false,
                            lastExecutionDate = null
                        )
                    } else {
                        // Find the most recent execution
                        val latestExecution = executions.maxByOrNull { 
                            try {
                                EntityMapper.dateFormat.parse(it.dateExecution)?.time ?: 0L
                            } catch (e: Exception) {
                                0L
                            }
                        }
                        
                        executionStatus[recipe.id] = ExecutionStatus(
                            hasExecutions = true,
                            lastExecutionDate = latestExecution?.dateExecution
                        )
                    }
                }
                
                emit(executionStatus)
            } catch (e: Exception) {
                android.util.Log.e("RecipeRepository", "Error getting execution status", e)
                emit(emptyMap())
            }
        }
    }
    
    /**
     * Get all unique convives from executions.
     * @return Flow of unique convive names from executions
     */
    fun getAllUniqueConvivesFromExecutions(): Flow<List<String>> {
        return flow {
            try {
                val allFeedback = database.feedbackExecutionDao().getAllFeedback().first()
                val conviveIds = allFeedback.map { it.conviveId }.distinct()
                val conviveNames = mutableListOf<String>()
                
                for (conviveId in conviveIds) {
                    val convive = database.conviveDao().getConvive(conviveId)
                    convive?.let { conviveNames.add(it.nom) }
                }
                
                emit(conviveNames.distinct().sorted())
            } catch (e: Exception) {
                android.util.Log.e("RecipeRepository", "Error getting unique convives from executions", e)
                emit(emptyList())
            }
        }
    }
    
    /**
     * Get recipes where a specific convive participated in executions.
     * @param conviveName Name of the convive to search for
     * @return Flow of recipes where the convive participated
     */
    fun getRecipesByConvive(conviveName: String): Flow<List<RecipeListItem>> {
        return flow {
            try {
                // Find the convive by name
                val convive = database.conviveDao().getConviveByName(conviveName)
                if (convive == null) {
                    emit(emptyList())
                    return@flow
                }
                
                // Get all feedback for this convive
                val allFeedback = database.feedbackExecutionDao().getAllFeedback().first()
                val conviveFeedback = allFeedback.filter { it.conviveId == convive.id }
                
                // Get execution IDs where this convive participated
                val executionIds = conviveFeedback.map { it.executionId }.distinct()
                
                // Get recipe IDs from these executions
                val recipeIds = mutableSetOf<String>()
                for (executionId in executionIds) {
                    val execution = database.executionDao().getExecution(executionId)
                    execution?.let { recipeIds.add(it.recetteId) }
                }
                
                // Get the actual recipes
                val allRecipes = database.recipeDao().getAllRecipes().first()
                val filteredRecipes = allRecipes.filter { recipe -> recipe.id in recipeIds }
                val recipeListItems = filteredRecipes.map { EntityMapper.recipeEntityToListItem(it) }
                
                emit(recipeListItems)
            } catch (e: Exception) {
                android.util.Log.e("RecipeRepository", "Error getting recipes by convive", e)
                emit(emptyList())
            }
        }
    }
    
    // Photo management methods
    
    /**
     * Get photos for a recipe.
     * @param recipeId ID of the recipe
     * @return Flow of photos
     */
    fun getPhotosForRecipe(recipeId: String): Flow<List<com.apajon.librarecipes.data.local.entities.PhotoEntity>> {
        return database.photoDao().getPhotosForRecipeFlow(recipeId)
    }
    
    /**
     * Add a photo to a recipe.
     * @param recipeId ID of the recipe
     * @param photoPath Path to the photo file
     * @param category Photo category (préparation, ingrédient, cuisson, final)
     * @return Result with success or error
     */
    suspend fun addPhoto(recipeId: String, photoPath: String, category: String?): Result<String> {
        return try {
            val existingPhotos = database.photoDao().getPhotosForRecipe(recipeId)
            val nextOrder = (existingPhotos.maxOfOrNull { it.ordre } ?: 0) + 1
            
            val photoEntity = com.apajon.librarecipes.data.local.entities.PhotoEntity(
                id = UUID.randomUUID().toString(),
                recetteId = recipeId,
                chemin = photoPath,
                categorie = category,
                ordre = nextOrder
            )
            
            database.photoDao().insertPhoto(photoEntity)
            Result.success(photoEntity.id)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error adding photo", e)
            Result.failure(Exception("Erreur lors de l'ajout de la photo"))
        }
    }
    
    /**
     * Update photo category.
     * @param photoId ID of the photo
     * @param category New category
     * @return Result with success or error
     */
    suspend fun updatePhotoCategory(photoId: String, category: String?): Result<Unit> {
        return try {
            database.photoDao().updatePhotoCategory(photoId, category)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error updating photo category", e)
            Result.failure(Exception("Erreur lors de la mise à jour de la catégorie"))
        }
    }
    
    /**
     * Reorder photos.
     * @param recipeId ID of the recipe
     * @param photoOrders Map of photo ID to new order
     * @return Result with success or error
     */
    suspend fun reorderPhotos(recipeId: String, photoOrders: Map<String, Int>): Result<Unit> {
        return try {
            photoOrders.forEach { (photoId, order) ->
                database.photoDao().updatePhotoOrder(photoId, order)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error reordering photos", e)
            Result.failure(Exception("Erreur lors du réordonnancement des photos"))
        }
    }
    
    /**
     * Delete a photo.
     * @param photoId ID of the photo to delete
     * @return Result with success or error
     */
    suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            database.photoDao().deletePhotoById(photoId)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RecipeRepository", "Error deleting photo", e)
            Result.failure(Exception("Erreur lors de la suppression de la photo"))
        }
    }
}