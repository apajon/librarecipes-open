package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.local.EntityMapper
import com.apajon.librarecipes.data.model.*
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RecipeFormState())
    val formState: StateFlow<RecipeFormState> = _formState.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var _recipeId: String? = null
    private var _isEditMode: Boolean = false

    /**
     * Initialize form with existing recipe data for editing.
     */
    fun loadRecipeForEdit(recipeId: String) {
        _recipeId = recipeId
        _isEditMode = true
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = repository.getRecipeDetails(recipeId)
                result.onSuccess { recipeDetail ->
                    val recipeCreate = EntityMapper.recipeDetailToCreate(recipeDetail)
                    _formState.value = RecipeFormState(
                        nom = recipeCreate.nom,
                        preparation = recipeCreate.preparation?.toString() ?: "",
                        cuisson = recipeCreate.cuisson?.toString() ?: "",
                        portions = recipeCreate.portions?.toString() ?: "",
                        categories = recipeCreate.categories.joinToString(", "),
                        tags = recipeCreate.tags.joinToString(", "),
                        ingredients = recipeCreate.ingredients.map { ingredient ->
                            IngredientFormItem(
                                nom = ingredient.nom,
                                quantite = ingredient.quantite?.toString() ?: "",
                                unite = ingredient.unite,
                                indispensable = ingredient.indispensable,
                                alternatives = ingredient.alternatives
                            )
                        },
                        steps = recipeCreate.etapes,
                        sourceType = when (recipeCreate.source?.type) {
                            "url" -> SourceType.WEB
                            "book" -> SourceType.BOOK
                            else -> SourceType.HOMEMADE
                        },
                        sourceUrl = if (recipeCreate.source?.type == "url") recipeCreate.source.valeur else "",
                        sourceBookTitle = if (recipeCreate.source?.type == "book") recipeCreate.source.valeur else "",
                        sourceBookAuthors = "",
                        sourceBookPage = ""
                    )
                }.onFailure { exception ->
                    _errorMessage.value = "Erreur lors du chargement de la recette: ${exception.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Initialize form for creating a new recipe.
     */
    fun initializeForCreate() {
        _recipeId = null
        _isEditMode = false
        _formState.value = RecipeFormState()
    }

    fun updateRecipeName(name: String) {
        _formState.value = _formState.value.copy(nom = name)
    }

    fun updatePreparationTime(time: String) {
        _formState.value = _formState.value.copy(preparation = time)
    }

    fun updateCookingTime(time: String) {
        _formState.value = _formState.value.copy(cuisson = time)
    }

    fun updatePortions(portions: String) {
        _formState.value = _formState.value.copy(portions = portions)
    }

    fun updateCategories(categories: String) {
        _formState.value = _formState.value.copy(categories = categories)
    }

    fun updateTags(tags: String) {
        _formState.value = _formState.value.copy(tags = tags)
    }

    fun updateSourceType(sourceType: SourceType) {
        _formState.value = _formState.value.copy(sourceType = sourceType)
    }

    fun updateSourceUrl(url: String) {
        _formState.value = _formState.value.copy(sourceUrl = url)
    }

    fun updateSourceBookTitle(title: String) {
        _formState.value = _formState.value.copy(sourceBookTitle = title)
    }

    fun updateSourceBookAuthors(authors: String) {
        _formState.value = _formState.value.copy(sourceBookAuthors = authors)
    }

    fun updateSourceBookPage(page: String) {
        _formState.value = _formState.value.copy(sourceBookPage = page)
    }

    fun addIngredient(ingredient: IngredientFormItem) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        currentIngredients.add(ingredient)
        _formState.value = _formState.value.copy(ingredients = currentIngredients)
    }

    fun removeIngredient(index: Int) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index >= 0 && index < currentIngredients.size) {
            currentIngredients.removeAt(index)
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun updateIngredient(index: Int, ingredient: IngredientFormItem) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index >= 0 && index < currentIngredients.size) {
            currentIngredients[index] = ingredient
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun moveIngredientUp(index: Int) {
        if (index > 0) {
            val currentIngredients = _formState.value.ingredients.toMutableList()
            val ingredient = currentIngredients.removeAt(index)
            currentIngredients.add(index - 1, ingredient)
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun moveIngredientDown(index: Int) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index < currentIngredients.size - 1) {
            val ingredient = currentIngredients.removeAt(index)
            currentIngredients.add(index + 1, ingredient)
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun addStep(step: String) {
        val currentSteps = _formState.value.steps.toMutableList()
        currentSteps.add(step)
        _formState.value = _formState.value.copy(steps = currentSteps)
    }

    fun removeStep(index: Int) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index >= 0 && index < currentSteps.size) {
            currentSteps.removeAt(index)
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun updateStep(index: Int, step: String) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index >= 0 && index < currentSteps.size) {
            currentSteps[index] = step
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun moveStepUp(index: Int) {
        if (index > 0) {
            val currentSteps = _formState.value.steps.toMutableList()
            val step = currentSteps.removeAt(index)
            currentSteps.add(index - 1, step)
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun moveStepDown(index: Int) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index < currentSteps.size - 1) {
            val step = currentSteps.removeAt(index)
            currentSteps.add(index + 1, step)
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun validateAndSaveRecipe() {
        val state = _formState.value
        
        // Basic validation
        if (state.nom.isBlank()) {
            _formState.value = state.copy(error = "Le nom de la recette est obligatoire")
            return
        }
        
        if (state.ingredients.isEmpty()) {
            _formState.value = state.copy(error = "Au moins un ingrédient est requis")
            return
        }
        
        if (state.steps.isEmpty()) {
            _formState.value = state.copy(error = "Au moins une étape est requise")
            return
        }

        _formState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val recipe = RecipeCreate(
                    nom = state.nom,
                    preparation = state.preparation.toIntOrNull(),
                    cuisson = state.cuisson.toIntOrNull(),
                    portions = state.portions.toIntOrNull(),
                    ingredients = state.ingredients.map { ingredient ->
                        IngredientCreate(
                            nom = ingredient.nom,
                            quantite = ingredient.quantite.toFloatOrNull(),
                            unite = ingredient.unite,
                            indispensable = ingredient.indispensable,
                            alternatives = ingredient.alternatives
                        )
                    },
                    etapes = state.steps,
                    categories = state.categories.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    tags = state.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    source = if (state.sourceType != SourceType.HOMEMADE) {
                        SourceCreate(
                            type = when (state.sourceType) {
                                SourceType.WEB -> "url"
                                SourceType.BOOK -> "book"
                                else -> "homemade"
                            },
                            valeur = when (state.sourceType) {
                                SourceType.WEB -> state.sourceUrl
                                SourceType.BOOK -> state.sourceBookTitle
                                else -> ""
                            }
                        )
                    } else null
                )

                val result = if (_isEditMode && _recipeId != null) {
                    repository.updateRecipe(_recipeId!!, recipe)
                } else {
                    repository.createRecipe(recipe)
                }
                
                result.onSuccess {
                    _saveSuccess.value = true
                }.onFailure { exception ->
                    _formState.value = state.copy(
                        isLoading = false,
                        error = "Erreur lors de la sauvegarde: ${exception.message}"
                    )
                }
            } catch (e: Exception) {
                _formState.value = state.copy(
                    isLoading = false,
                    error = "Erreur inattendue: ${e.message}"
                )
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearError() {
        _formState.value = _formState.value.copy(error = null)
        _errorMessage.value = null
    }
    
    fun isEditMode(): Boolean = _isEditMode
    fun getRecipeId(): String? = _recipeId
}