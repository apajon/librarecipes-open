package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.repository.RecipeRepository
import com.apajon.librarecipes.data.repository.RecipeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _recipes = MutableStateFlow<List<RecipeListItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _sortAscending = MutableStateFlow(true)
    private val _selectedLetter = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<RecipeListUiState> = combine(
        _recipes,
        _isLoading,
        _errorMessage,
        _sortAscending,
        _selectedLetter
    ) { recipes, isLoading, errorMessage, sortAscending, selectedLetter ->
        val processedRecipes = processRecipes(recipes, sortAscending, selectedLetter)
        RecipeListUiState(
            recipeSections = processedRecipes,
            isLoading = isLoading,
            errorMessage = errorMessage,
            sortAscending = sortAscending,
            selectedLetter = selectedLetter,
            availableLetters = getAvailableLetters(recipes)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeListUiState()
    )
    
    init {
        loadRecipes()
    }
    
    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repository.getRecipes().collect { result ->
                when (result) {
                    is RecipeResult.Success -> {
                        _recipes.value = result.recipes
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
                    is RecipeResult.Error -> {
                        _recipes.value = emptyList()
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }
                }
            }
        }
    }
    
    fun refreshRecipes() {
        loadRecipes()
    }
    
    fun toggleSort() {
        _sortAscending.value = !_sortAscending.value
    }
    
    fun filterByLetter(letter: String?) {
        _selectedLetter.value = letter
    }
    
    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            repository.deleteRecipe(recipeId)
                .onSuccess {
                    // Refresh the list after successful deletion
                    loadRecipes()
                }
                .onFailure { error ->
                    _errorMessage.value = "Erreur lors de la suppression: ${error.message}"
                }
        }
    }
    
    private fun processRecipes(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedLetter: String?
    ): List<RecipeSection> {
        val filteredRecipes = if (selectedLetter != null) {
            recipes.filter { recipe ->
                recipe.nom.firstOrNull()?.uppercase() == selectedLetter.uppercase()
            }
        } else {
            recipes
        }
        
        val sortedRecipes = if (sortAscending) {
            filteredRecipes.sortedBy { it.nom }
        } else {
            filteredRecipes.sortedByDescending { it.nom }
        }
        
        return sortedRecipes
            .groupBy { recipe ->
                recipe.nom.firstOrNull()?.uppercase() ?: "#"
            }
            .toSortedMap(if (sortAscending) compareBy { it } else compareByDescending { it })
            .map { (letter, recipesList) ->
                RecipeSection(
                    letter = letter,
                    recipes = recipesList
                )
            }
    }
    
    private fun getAvailableLetters(recipes: List<RecipeListItem>): List<String> {
        return recipes
            .mapNotNull { recipe -> recipe.nom.firstOrNull()?.uppercase() }
            .distinct()
            .sorted()
    }
}

data class RecipeSection(
    val letter: String,
    val recipes: List<RecipeListItem>
)

data class RecipeListUiState(
    val recipeSections: List<RecipeSection> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sortAscending: Boolean = true,
    val selectedLetter: String? = null,
    val availableLetters: List<String> = emptyList()
)