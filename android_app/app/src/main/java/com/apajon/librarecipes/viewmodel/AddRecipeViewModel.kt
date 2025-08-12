package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeCreateRequest
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Add Recipe screen.
 * Manages recipe creation form state and API interactions.
 */
@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    // Form state
    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    // Metadata for dropdowns
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
     * Load metadata for form autocomplete.
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
     * Update recipe name.
     */
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            nom = name,
            validationErrors = _uiState.value.validationErrors.copy(nom = null)
        )
    }

    /**
     * Update preparation time.
     */
    fun updatePreparationTime(minutes: Int?) {
        _uiState.value = _uiState.value.copy(preparation = minutes)
    }

    /**
     * Update cooking time.
     */
    fun updateCookingTime(minutes: Int?) {
        _uiState.value = _uiState.value.copy(cuisson = minutes)
    }

    /**
     * Update number of portions.
     */
    fun updatePortions(portions: Int?) {
        _uiState.value = _uiState.value.copy(portions = portions)
    }

    /**
     * Add an ingredient to the recipe.
     */
    fun addIngredient(ingredient: Ingredient) {
        val current = _uiState.value
        _uiState.value = current.copy(
            ingredients = current.ingredients + ingredient,
            validationErrors = current.validationErrors.copy(ingredients = null)
        )
    }

    /**
     * Remove an ingredient from the recipe.
     */
    fun removeIngredient(index: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            ingredients = current.ingredients.filterIndexed { i, _ -> i != index }
        )
    }

    /**
     * Update an ingredient in the recipe.
     */
    fun updateIngredient(index: Int, ingredient: Ingredient) {
        val current = _uiState.value
        _uiState.value = current.copy(
            ingredients = current.ingredients.mapIndexed { i, ing ->
                if (i == index) ingredient else ing
            }
        )
    }

    /**
     * Add a step to the recipe.
     */
    fun addStep(description: String) {
        if (description.isNotBlank()) {
            val current = _uiState.value
            _uiState.value = current.copy(
                etapes = current.etapes + description,
                validationErrors = current.validationErrors.copy(etapes = null)
            )
        }
    }

    /**
     * Remove a step from the recipe.
     */
    fun removeStep(index: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(
            etapes = current.etapes.filterIndexed { i, _ -> i != index }
        )
    }

    /**
     * Update a step in the recipe.
     */
    fun updateStep(index: Int, description: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            etapes = current.etapes.mapIndexed { i, step ->
                if (i == index) description else step
            }
        )
    }

    /**
     * Update selected categories.
     */
    fun updateCategories(categories: List<String>) {
        _uiState.value = _uiState.value.copy(categories = categories)
    }

    /**
     * Update selected tags.
     */
    fun updateTags(tags: List<String>) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    /**
     * Update source information.
     */
    fun updateSource(source: Source?) {
        _uiState.value = _uiState.value.copy(source = source)
    }

    /**
     * Validate the recipe form.
     */
    private fun validateRecipe(): Boolean {
        val current = _uiState.value
        val errors = ValidationErrors()

        if (current.nom.isBlank()) {
            errors.nom = "Le nom de la recette est requis"
        }

        if (current.ingredients.isEmpty()) {
            errors.ingredients = "Au moins un ingrédient est requis"
        }

        if (current.etapes.isEmpty()) {
            errors.etapes = "Au moins une étape est requise"
        }

        _uiState.value = current.copy(validationErrors = errors)
        return errors.hasNoErrors()
    }

    /**
     * Save the recipe.
     */
    fun saveRecipe() {
        if (!validateRecipe()) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, saveError = null)

        val request = RecipeCreateRequest(
            nom = _uiState.value.nom,
            preparation = _uiState.value.preparation,
            cuisson = _uiState.value.cuisson,
            portions = _uiState.value.portions,
            ingredients = _uiState.value.ingredients,
            etapes = _uiState.value.etapes,
            categories = _uiState.value.categories,
            tags = _uiState.value.tags,
            source = _uiState.value.source
        )

        viewModelScope.launch {
            repository.createRecipe(request).collect { result ->
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveSuccess = true,
                        createdRecipeId = result.id
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        saveError = "Erreur lors de la création de la recette"
                    )
                }
            }
        }
    }

    /**
     * Reset the form after successful save.
     */
    fun resetForm() {
        _uiState.value = AddRecipeUiState()
    }

    /**
     * Clear save success state.
     */
    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false, createdRecipeId = null)
    }
}

/**
 * UI state for the Add Recipe screen.
 */
data class AddRecipeUiState(
    val nom: String = "",
    val preparation: Int? = null,
    val cuisson: Int? = null,
    val portions: Int? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val etapes: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val source: Source? = null,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null,
    val createdRecipeId: String? = null,
    val validationErrors: ValidationErrors = ValidationErrors()
)

/**
 * Validation errors for form fields.
 */
data class ValidationErrors(
    val nom: String? = null,
    val ingredients: String? = null,
    val etapes: String? = null
) {
    fun hasNoErrors(): Boolean = nom == null && ingredients == null && etapes == null
}