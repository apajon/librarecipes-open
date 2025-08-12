package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.model.SearchFilters
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Search Recipe screen.
 * Manages search form state and search results.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    // Search state
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Metadata for filters
    private val _availableIngredients = MutableStateFlow<List<String>>(emptyList())
    val availableIngredients: StateFlow<List<String>> = _availableIngredients.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    init {
        loadMetadata()
    }

    /**
     * Load metadata for search filters.
     */
    private fun loadMetadata() {
        viewModelScope.launch {
            repository.getIngredients().collect { ingredients ->
                _availableIngredients.value = ingredients
            }
        }
        
        viewModelScope.launch {
            repository.getCategories().collect { categories ->
                _availableCategories.value = categories
            }
        }
        
        viewModelScope.launch {
            repository.getTags().collect { tags ->
                _availableTags.value = tags
            }
        }
    }

    /**
     * Update recipe name filter.
     */
    fun updateNameFilter(name: String) {
        _uiState.value = _uiState.value.copy(nameFilter = name.ifBlank { null })
    }

    /**
     * Update selected ingredients filter.
     */
    fun updateIngredientsFilter(ingredients: List<String>) {
        _uiState.value = _uiState.value.copy(
            ingredientsFilter = ingredients.ifEmpty { null }
        )
    }

    /**
     * Update ingredients search mode.
     */
    fun updateIngredientsMode(mode: IngredientsMode) {
        _uiState.value = _uiState.value.copy(ingredientsMode = mode)
    }

    /**
     * Update selected tags filter.
     */
    fun updateTagsFilter(tags: List<String>) {
        _uiState.value = _uiState.value.copy(
            tagsFilter = tags.ifEmpty { null }
        )
    }

    /**
     * Update selected categories filter.
     */
    fun updateCategoriesFilter(categories: List<String>) {
        _uiState.value = _uiState.value.copy(
            categoriesFilter = categories.ifEmpty { null }
        )
    }

    /**
     * Perform search with current filters.
     */
    fun performSearch() {
        val current = _uiState.value
        
        // Don't search if no filters are set
        if (current.nameFilter == null && 
            current.ingredientsFilter == null && 
            current.tagsFilter == null && 
            current.categoriesFilter == null) {
            return
        }

        _uiState.value = current.copy(isLoading = true, searchError = null)

        val searchFilters = SearchFilters(
            nom = current.nameFilter,
            ingredients = current.ingredientsFilter,
            ingredientsMode = current.ingredientsMode,
            tags = current.tagsFilter,
            categories = current.categoriesFilter
        )

        viewModelScope.launch {
            repository.searchRecipes(searchFilters).collect { results ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    searchResults = results,
                    hasSearched = true
                )
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
     * Clear search results while keeping filters.
     */
    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            hasSearched = false,
            searchError = null
        )
    }

    /**
     * Add a quick filter for a specific ingredient.
     */
    fun addIngredientQuickFilter(ingredient: String) {
        val current = _uiState.value.ingredientsFilter ?: emptyList()
        if (!current.contains(ingredient)) {
            updateIngredientsFilter(current + ingredient)
        }
    }

    /**
     * Add a quick filter for a specific tag.
     */
    fun addTagQuickFilter(tag: String) {
        val current = _uiState.value.tagsFilter ?: emptyList()
        if (!current.contains(tag)) {
            updateTagsFilter(current + tag)
        }
    }

    /**
     * Add a quick filter for a specific category.
     */
    fun addCategoryQuickFilter(category: String) {
        val current = _uiState.value.categoriesFilter ?: emptyList()
        if (!current.contains(category)) {
            updateCategoriesFilter(current + category)
        }
    }
}

/**
 * UI state for the Search screen.
 */
data class SearchUiState(
    // Search filters
    val nameFilter: String? = null,
    val ingredientsFilter: List<String>? = null,
    val ingredientsMode: IngredientsMode = IngredientsMode.ANY,
    val tagsFilter: List<String>? = null,
    val categoriesFilter: List<String>? = null,
    
    // Search state
    val isLoading: Boolean = false,
    val hasSearched: Boolean = false,
    val searchResults: List<RecipeListItem> = emptyList(),
    val searchError: String? = null
) {
    /**
     * Check if any filters are active.
     */
    fun hasActiveFilters(): Boolean {
        return nameFilter != null || 
               ingredientsFilter != null || 
               tagsFilter != null || 
               categoriesFilter != null
    }
    
    /**
     * Get a summary of active filters for display.
     */
    fun getFilterSummary(): String {
        val filters = mutableListOf<String>()
        
        nameFilter?.let { filters.add("Nom: $it") }
        ingredientsFilter?.let { 
            if (it.isNotEmpty()) {
                val mode = if (ingredientsMode == IngredientsMode.ALL) "tous" else "au moins un"
                filters.add("Ingrédients ($mode): ${it.joinToString(", ")}")
            }
        }
        tagsFilter?.let { 
            if (it.isNotEmpty()) filters.add("Tags: ${it.joinToString(", ")}") 
        }
        categoriesFilter?.let { 
            if (it.isNotEmpty()) filters.add("Catégories: ${it.joinToString(", ")}") 
        }
        
        return filters.joinToString(" • ")
    }
}