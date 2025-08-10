package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()
    
    init {
        loadRecipes()
    }
    
    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getRecipes().collect { recipes ->
                _uiState.value = _uiState.value.copy(
                    recipes = recipes,
                    isLoading = false
                )
            }
        }
    }
    
    fun refreshRecipes() {
        loadRecipes()
    }
}

data class RecipeListUiState(
    val recipes: List<RecipeListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)