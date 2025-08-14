package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val errorMessage: String? = null
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
}