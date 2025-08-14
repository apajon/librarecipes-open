package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.repository.RecipeRepository
import com.apajon.librarecipes.data.repository.RecipeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for search functionality.
 */
data class SearchUiState(
    val searchQuery: String = "",
    val selectedIngredients: List<String> = emptyList(),
    val selectedCategories: List<String> = emptyList(),
    val selectedTags: List<String> = emptyList(),
    val searchResults: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false
)

/**
 * ViewModel for search screen functionality.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * Update the search query text.
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Update the ingredients filter.
     */
    fun updateIngredients(ingredientsText: String) {
        val ingredients = if (ingredientsText.isBlank()) {
            emptyList()
        } else {
            ingredientsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
        _uiState.value = _uiState.value.copy(selectedIngredients = ingredients)
    }

    /**
     * Update the categories filter.
     */
    fun updateCategories(categoriesText: String) {
        val categories = if (categoriesText.isBlank()) {
            emptyList()
        } else {
            categoriesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
        _uiState.value = _uiState.value.copy(selectedCategories = categories)
    }

    /**
     * Update the tags filter.
     */
    fun updateTags(tagsText: String) {
        val tags = if (tagsText.isBlank()) {
            emptyList()
        } else {
            tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
        _uiState.value = _uiState.value.copy(selectedTags = tags)
    }

    /**
     * Perform search with current filters.
     */
    fun performSearch() {
        val currentState = _uiState.value
        
        // Don't search if all fields are empty
        if (currentState.searchQuery.isBlank() &&
            currentState.selectedIngredients.isEmpty() &&
            currentState.selectedCategories.isEmpty() &&
            currentState.selectedTags.isEmpty()
        ) {
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null,
            hasSearched = true
        )

        viewModelScope.launch {
            recipeRepository.searchRecipes(
                nom = currentState.searchQuery.takeIf { it.isNotBlank() },
                ingredients = currentState.selectedIngredients.takeIf { it.isNotEmpty() },
                categories = currentState.selectedCategories.takeIf { it.isNotEmpty() },
                tags = currentState.selectedTags.takeIf { it.isNotEmpty() }
            ).collect { result ->
                when (result) {
                    is RecipeResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = result.recipes,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is RecipeResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Clear all search filters and results.
     */
    fun clearSearch() {
        _uiState.value = SearchUiState()
    }

    /**
     * Clear only the error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Load all recipes (when no search filters applied).
     */
    fun loadAllRecipes() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            hasSearched = true
        )

        viewModelScope.launch {
            recipeRepository.getRecipes().collect { result ->
                when (result) {
                    is RecipeResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = result.recipes,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    is RecipeResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            searchResults = emptyList(),
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Delete a recipe and refresh search results.
     */
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipeId)
                .onSuccess {
                    // Refresh search results after successful deletion
                    if (_uiState.value.hasSearched) {
                        if (_uiState.value.searchQuery.isBlank() &&
                            _uiState.value.selectedIngredients.isEmpty() &&
                            _uiState.value.selectedCategories.isEmpty() &&
                            _uiState.value.selectedTags.isEmpty()
                        ) {
                            loadAllRecipes()
                        } else {
                            performSearch()
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erreur lors de la suppression: ${error.message}"
                    )
                }
        }
    }
}