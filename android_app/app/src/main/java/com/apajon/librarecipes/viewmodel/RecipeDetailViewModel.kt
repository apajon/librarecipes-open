package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
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
    val executions: List<ExecutionEntity> = emptyList(),
    val isAddingExecution: Boolean = false,
    val addExecutionSuccess: Boolean = false,
    val addExecutionError: String? = null
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
            recipeRepository.getExecutionsForRecipe(recipeId).collect { executions ->
                _uiState.value = _uiState.value.copy(executions = executions)
            }
        }
    }
    
    /**
     * Add a new execution for the recipe.
     * @param recipeId ID of the recipe
     * @param nombreConvives Number of people served (optional)
     */
    fun addExecution(recipeId: String, nombreConvives: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAddingExecution = true, 
                addExecutionError = null
            )
            
            recipeRepository.addExecution(recipeId, nombreConvives)
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
}