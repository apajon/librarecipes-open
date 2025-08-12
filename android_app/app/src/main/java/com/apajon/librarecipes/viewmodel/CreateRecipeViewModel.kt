package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.*
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(RecipeFormState())
    val formState: StateFlow<RecipeFormState> = _formState.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

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

    fun updateIngredient(index: Int, ingredient: IngredientFormItem) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index >= 0 && index < currentIngredients.size) {
            currentIngredients[index] = ingredient
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun removeIngredient(index: Int) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index >= 0 && index < currentIngredients.size) {
            currentIngredients.removeAt(index)
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun moveIngredientUp(index: Int) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index > 0 && index < currentIngredients.size) {
            val temp = currentIngredients[index]
            currentIngredients[index] = currentIngredients[index - 1]
            currentIngredients[index - 1] = temp
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun moveIngredientDown(index: Int) {
        val currentIngredients = _formState.value.ingredients.toMutableList()
        if (index >= 0 && index < currentIngredients.size - 1) {
            val temp = currentIngredients[index]
            currentIngredients[index] = currentIngredients[index + 1]
            currentIngredients[index + 1] = temp
            _formState.value = _formState.value.copy(ingredients = currentIngredients)
        }
    }

    fun addStep(step: String) {
        val currentSteps = _formState.value.steps.toMutableList()
        currentSteps.add(step)
        _formState.value = _formState.value.copy(steps = currentSteps)
    }

    fun updateStep(index: Int, step: String) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index >= 0 && index < currentSteps.size) {
            currentSteps[index] = step
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun removeStep(index: Int) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index >= 0 && index < currentSteps.size) {
            currentSteps.removeAt(index)
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun moveStepUp(index: Int) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index > 0 && index < currentSteps.size) {
            val temp = currentSteps[index]
            currentSteps[index] = currentSteps[index - 1]
            currentSteps[index - 1] = temp
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun moveStepDown(index: Int) {
        val currentSteps = _formState.value.steps.toMutableList()
        if (index >= 0 && index < currentSteps.size - 1) {
            val temp = currentSteps[index]
            currentSteps[index] = currentSteps[index + 1]
            currentSteps[index + 1] = temp
            _formState.value = _formState.value.copy(steps = currentSteps)
        }
    }

    fun validateAndSaveRecipe() {
        val currentState = _formState.value

        // Basic validation
        if (currentState.nom.isBlank()) {
            _formState.value = currentState.copy(error = "Le nom de la recette est obligatoire")
            return
        }

        if (currentState.ingredients.isEmpty()) {
            _formState.value = currentState.copy(error = "Au moins un ingrédient est requis")
            return
        }

        if (currentState.steps.isEmpty()) {
            _formState.value = currentState.copy(error = "Au moins une étape est requise")
            return
        }

        // Convert form state to API model
        val recipeCreate = RecipeCreate(
            nom = currentState.nom,
            preparation = currentState.preparation.toIntOrNull(),
            cuisson = currentState.cuisson.toIntOrNull(),
            portions = currentState.portions.toIntOrNull(),
            ingredients = currentState.ingredients.mapIndexed { _, ingredient ->
                IngredientCreate(
                    nom = ingredient.nom,
                    quantite = ingredient.quantite.toFloatOrNull(),
                    unite = ingredient.unite.ifBlank { null },
                    indispensable = ingredient.indispensable,
                    alternatives = ingredient.alternatives.ifBlank { null }
                )
            },
            etapes = currentState.steps,
            categories = currentState.categories.split(",").map { it.trim() }.filter { it.isNotBlank() },
            tags = currentState.tags.split(",").map { it.trim() }.filter { it.isNotBlank() },
            photos = emptyList(), // TODO: Add photo support
            source = createSourceFromState(currentState)
        )

        // Save recipe
        _formState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            repository.createRecipe(recipeCreate)
                .onSuccess {
                    _formState.value = _formState.value.copy(isLoading = false)
                    _saveSuccess.value = true
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        error = "Erreur lors de la sauvegarde: ${error.message}"
                    )
                }
        }
    }

    private fun createSourceFromState(state: RecipeFormState): SourceCreate? {
        return when (state.sourceType) {
            SourceType.HOMEMADE -> SourceCreate(type = "homemade")
            SourceType.URL -> {
                if (state.sourceUrl.isNotBlank()) {
                    SourceCreate(type = "url", valeur = state.sourceUrl)
                } else {
                    SourceCreate(type = "homemade")
                }
            }
            SourceType.BOOK -> {
                val bookValue = buildString {
                    if (state.sourceBookTitle.isNotBlank()) {
                        append(state.sourceBookTitle)
                        if (state.sourceBookAuthors.isNotBlank()) {
                            append(" - ${state.sourceBookAuthors}")
                        }
                        if (state.sourceBookPage.isNotBlank()) {
                            append(" (p. ${state.sourceBookPage})")
                        }
                    }
                }
                if (bookValue.isNotBlank()) {
                    SourceCreate(type = "book", valeur = bookValue)
                } else {
                    SourceCreate(type = "homemade")
                }
            }
        }
    }

    fun clearError() {
        _formState.value = _formState.value.copy(error = null)
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearForm() {
        _formState.value = RecipeFormState()
        _saveSuccess.value = false
    }
}