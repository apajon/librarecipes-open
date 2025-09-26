package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
import com.apajon.librarecipes.data.model.ExecutionCreate
import com.apajon.librarecipes.data.model.ExecutionWithDetails
import com.apajon.librarecipes.data.model.PhotoDetail
import com.apajon.librarecipes.data.model.RecipeDetail
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the recipe detail screen.
 */
data class RecipeDetailUiState(
    val isLoading: Boolean = false,
    val recipe: RecipeDetail? = null,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null,
    val executions: List<ExecutionWithDetails> = emptyList(),
    val isAddingExecution: Boolean = false,
    val addExecutionSuccess: Boolean = false,
    val addExecutionError: String? = null,
    val availableConvives: List<ConviveEntity> = emptyList(),
    val isLoadingConvives: Boolean = false,
    // Photo management state
    val selectedPhotoIndex: Int = 0,
    val isPhotoManagementVisible: Boolean = false,
    val isAddingPhoto: Boolean = false,
    val addPhotoError: String? = null
)

/**
 * ViewModel for the recipe detail screen.
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeDetailUiState())
    val uiState: StateFlow<RecipeDetailUiState> = _uiState.asStateFlow()

    /**
     * Load recipe details by ID.
     * @param recipeId ID of the recipe to load
     */
    fun loadRecipeDetails(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            recipeRepository.getRecipeDetails(recipeId)
                .onSuccess { recipe ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recipe = recipe,
                        errorMessage = null
                    )
                    // Load executions for this recipe
                    loadExecutions(recipeId)
                    // Load available convives
                    loadAvailableConvives()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        recipe = null,
                        errorMessage = error.message ?: "Erreur lors du chargement de la recette"
                    )
                }
        }
    }

    /**
     * Retry loading the recipe details.
     */
    fun retry(recipeId: String) {
        loadRecipeDetails(recipeId)
    }
    
    /**
     * Delete the current recipe.
     * @param recipeId ID of the recipe to delete
     */
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteError = null)
            
            recipeRepository.deleteRecipe(recipeId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteSuccess = true,
                        deleteError = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteError = error.message ?: "Erreur lors de la suppression de la recette"
                    )
                }
        }
    }
    
    /**
     * Clear delete success state.
     */
    fun clearDeleteSuccess() {
        _uiState.value = _uiState.value.copy(deleteSuccess = false)
    }
    
    /**
     * Clear delete error state.
     */
    fun clearDeleteError() {
        _uiState.value = _uiState.value.copy(deleteError = null)
    }
    
    /**
     * Load executions for the current recipe.
     * @param recipeId ID of the recipe
     */
    private fun loadExecutions(recipeId: String) {
        viewModelScope.launch {
            recipeRepository.getExecutionsWithDetailsForRecipe(recipeId).collect { executions ->
                _uiState.value = _uiState.value.copy(executions = executions)
            }
        }
    }
    
    /**
     * Load available convives from database.
     */
    fun loadAvailableConvives() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingConvives = true)
            recipeRepository.getAllConvives().collect { convives ->
                _uiState.value = _uiState.value.copy(
                    availableConvives = convives,
                    isLoadingConvives = false
                )
            }
        }
    }
    
    /**
     * Add a new execution for the recipe with convives and feedback.
     * @param executionCreate Data for creating the execution
     */
    fun addExecution(executionCreate: ExecutionCreate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingExecution = true, 
                addExecutionError = null
            )
            
            recipeRepository.addExecution(executionCreate)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isAddingExecution = false,
                        addExecutionSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isAddingExecution = false,
                        addExecutionError = error.message ?: "Erreur lors de l'ajout de l'exécution"
                    )
                }
        }
    }
    
    /**
     * Clear add execution success state.
     */
    fun clearAddExecutionSuccess() {
        _uiState.value = _uiState.value.copy(addExecutionSuccess = false)
    }
    
    /**
     * Clear add execution error state.
     */
    fun clearAddExecutionError() {
        _uiState.value = _uiState.value.copy(addExecutionError = null)
    }
    
    /**
     * Delete an execution.
     * @param executionId ID of the execution to delete
     */
    fun deleteExecution(executionId: String) {
        viewModelScope.launch {
            recipeRepository.deleteExecution(executionId)
                .onSuccess {
                    // Execution deleted successfully, list will be updated automatically via flow
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        addExecutionError = error.message ?: "Erreur lors de la suppression de l'exécution"
                    )
                }
        }
    }
    
    /**
     * Update an existing execution.
     * @param executionCreate Execution data with ID for update
     */
    fun updateExecution(executionCreate: ExecutionCreate) {
        val executionId = executionCreate.executionId 
            ?: throw IllegalArgumentException("ExecutionId must be provided for updates")
        updateExecution(executionId, executionCreate)
    }
    
    /**
     * Update an existing execution.
     * @param executionId ID of the execution to update
     * @param executionCreate New execution data
     */
    fun updateExecution(executionId: String, executionCreate: ExecutionCreate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingExecution = true, 
                addExecutionError = null
            )
            
            recipeRepository.updateExecution(executionId, executionCreate)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isAddingExecution = false,
                        addExecutionSuccess = true
                    )
                    // Force refresh of executions after successful update
                    _uiState.value.recipe?.let { recipe ->
                        loadExecutions(recipe.id)
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isAddingExecution = false,
                        addExecutionError = error.message ?: "Erreur lors de la modification de l'exécution"
                    )
                }
        }
    }
    
    // Photo management methods
    
    /**
     * Navigate to next photo in the gallery.
     */
    fun nextPhoto() {
        val photos = _uiState.value.recipe?.photos
        if (photos != null && photos.isNotEmpty()) {
            val currentIndex = _uiState.value.selectedPhotoIndex
            val nextIndex = if (currentIndex >= photos.size - 1) 0 else currentIndex + 1
            _uiState.value = _uiState.value.copy(selectedPhotoIndex = nextIndex)
        }
    }
    
    /**
     * Navigate to previous photo in the gallery.
     */
    fun previousPhoto() {
        val photos = _uiState.value.recipe?.photos
        if (photos != null && photos.isNotEmpty()) {
            val currentIndex = _uiState.value.selectedPhotoIndex
            val previousIndex = if (currentIndex <= 0) photos.size - 1 else currentIndex - 1
            _uiState.value = _uiState.value.copy(selectedPhotoIndex = previousIndex)
        }
    }
    
    /**
     * Select a specific photo by index.
     */
    fun selectPhoto(index: Int) {
        val photos = _uiState.value.recipe?.photos
        if (photos != null && index in 0 until photos.size) {
            _uiState.value = _uiState.value.copy(selectedPhotoIndex = index)
        }
    }
    
    /**
     * Toggle photo management panel visibility.
     */
    fun togglePhotoManagement() {
        _uiState.value = _uiState.value.copy(
            isPhotoManagementVisible = !_uiState.value.isPhotoManagementVisible
        )
    }
    
    /**
     * Add a new photo to the recipe.
     * @param recipeId ID of the recipe
     * @param photoPath Path to the photo file
     * @param category Photo category
     */
    fun addPhoto(recipeId: String, photoPath: String, category: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingPhoto = true, addPhotoError = null)
            
            recipeRepository.addPhoto(recipeId, photoPath, category)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isAddingPhoto = false)
                    // Refresh recipe details to get updated photos
                    loadRecipeDetails(recipeId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isAddingPhoto = false,
                        addPhotoError = error.message ?: "Erreur lors de l'ajout de la photo"
                    )
                }
        }
    }
    
    /**
     * Update photo category.
     * @param photoId ID of the photo
     * @param category New category
     */
    fun updatePhotoCategory(photoId: String, category: String?) {
        viewModelScope.launch {
            recipeRepository.updatePhotoCategory(photoId, category)
                .onSuccess {
                    // Refresh recipe details to get updated photos
                    _uiState.value.recipe?.let { recipe ->
                        loadRecipeDetails(recipe.id)
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        addPhotoError = error.message ?: "Erreur lors de la mise à jour de la catégorie"
                    )
                }
        }
    }
    
    /**
     * Delete a photo.
     * @param photoId ID of the photo to delete
     */
    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            recipeRepository.deletePhoto(photoId)
                .onSuccess {
                    // Refresh recipe details to get updated photos
                    _uiState.value.recipe?.let { recipe ->
                        loadRecipeDetails(recipe.id)
                        // Reset photo index if needed
                        val photos = _uiState.value.recipe?.photos
                        if (photos != null && _uiState.value.selectedPhotoIndex >= photos.size) {
                            _uiState.value = _uiState.value.copy(selectedPhotoIndex = 0)
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        addPhotoError = error.message ?: "Erreur lors de la suppression de la photo"
                    )
                }
        }
    }
    
    /**
     * Move photo up in order.
     * @param photoId ID of the photo to move
     */
    fun movePhotoUp(photoId: String) {
        val photos = _uiState.value.recipe?.photos ?: return
        val currentIndex = photos.indexOfFirst { it.id == photoId }
        if (currentIndex > 0) {
            val reorderedPhotos = photos.toMutableList()
            val photo = reorderedPhotos.removeAt(currentIndex)
            reorderedPhotos.add(currentIndex - 1, photo)
            reorderPhotos(reorderedPhotos)
        }
    }
    
    /**
     * Move photo down in order.
     * @param photoId ID of the photo to move
     */
    fun movePhotoDown(photoId: String) {
        val photos = _uiState.value.recipe?.photos ?: return
        val currentIndex = photos.indexOfFirst { it.id == photoId }
        if (currentIndex < photos.size - 1) {
            val reorderedPhotos = photos.toMutableList()
            val photo = reorderedPhotos.removeAt(currentIndex)
            reorderedPhotos.add(currentIndex + 1, photo)
            reorderPhotos(reorderedPhotos)
        }
    }
    
    /**
     * Reorder photos based on new list.
     * @param reorderedPhotos New photo order
     */
    private fun reorderPhotos(reorderedPhotos: List<PhotoDetail>) {
        val recipeId = _uiState.value.recipe?.id ?: return
        val photoOrders = reorderedPhotos.mapIndexed { index, photo ->
            photo.id to index + 1
        }.toMap()
        
        viewModelScope.launch {
            recipeRepository.reorderPhotos(recipeId, photoOrders)
                .onSuccess {
                    // Refresh recipe details to get updated order
                    loadRecipeDetails(recipeId)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        addPhotoError = error.message ?: "Erreur lors du réordonnancement des photos"
                    )
                }
        }
    }
    
    /**
     * Clear photo error.
     */
    fun clearPhotoError() {
        _uiState.value = _uiState.value.copy(addPhotoError = null)
    }
}