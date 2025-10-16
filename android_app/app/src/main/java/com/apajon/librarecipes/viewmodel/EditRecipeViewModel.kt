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

    // Photo management state
    private val _photos = MutableStateFlow<List<PhotoDetail>>(emptyList())
    val photos: StateFlow<List<PhotoDetail>> = _photos.asStateFlow()
    
    private val _isPhotoManagementVisible = MutableStateFlow(false)
    val isPhotoManagementVisible: StateFlow<Boolean> = _isPhotoManagementVisible.asStateFlow()
    
    private val _isAddingPhoto = MutableStateFlow(false)
    val isAddingPhoto: StateFlow<Boolean> = _isAddingPhoto.asStateFlow()

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
                                unite = ingredient.unite ?: "",
                                indispensable = ingredient.indispensable,
                                alternatives = ingredient.alternatives ?: ""
                            )
                        },
                        steps = recipeCreate.etapes,
                        sourceType = when (recipeCreate.source?.type) {
                            "url" -> SourceType.URL
                            "book" -> SourceType.BOOK
                            else -> SourceType.HOMEMADE
                        },
                        sourceUrl = if (recipeCreate.source?.type == "url") recipeCreate.source.valeur ?: "" else "",
                        sourceBookTitle = if (recipeCreate.source?.type == "book") recipeCreate.source.valeur ?: "" else "",
                        sourceBookAuthors = "",
                        sourceBookPage = ""
                    )
                    // Load photos
                    _photos.value = recipeDetail.photos
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
                                SourceType.URL -> "url"
                                SourceType.BOOK -> "book"
                                else -> "homemade"
                            },
                            valeur = when (state.sourceType) {
                                SourceType.URL -> state.sourceUrl
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
                
                result.onSuccess { response ->
                    // If creating a new recipe and there are photos to add
                    if (!_isEditMode && _photos.value.isNotEmpty()) {
                        val newRecipeId = response.id
                        // Save all temporary photos to the database
                        _photos.value.forEach { photo ->
                            repository.addPhoto(newRecipeId, photo.chemin, photo.categorie)
                        }
                    }
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
    
    /**
     * Toggle photo management visibility.
     */
    fun togglePhotoManagement() {
        _isPhotoManagementVisible.value = !_isPhotoManagementVisible.value
    }
    
    /**
     * Add a photo to the recipe.
     */
    fun addPhoto(photoPath: String, category: String?) {
        viewModelScope.launch {
            _isAddingPhoto.value = true
            
            if (_recipeId != null) {
                // Edit mode - save directly to database
                repository.addPhoto(_recipeId!!, photoPath, category)
                    .onSuccess { photoId ->
                        // Reload recipe details to get updated photos
                        repository.getRecipeDetails(_recipeId!!).onSuccess { recipeDetail ->
                            _photos.value = recipeDetail.photos
                        }
                        _isAddingPhoto.value = false
                    }
                    .onFailure { error ->
                        _isAddingPhoto.value = false
                        _errorMessage.value = "Erreur lors de l'ajout de la photo: ${error.message}"
                    }
            } else {
                // Create mode - store in memory temporarily
                val nextOrder = (_photos.value.maxOfOrNull { it.ordre } ?: 0) + 1
                val tempPhoto = PhotoDetail(
                    id = "temp_${System.currentTimeMillis()}", // Temporary ID
                    chemin = photoPath,
                    categorie = category,
                    ordre = nextOrder
                )
                _photos.value = _photos.value + tempPhoto
                _isAddingPhoto.value = false
            }
        }
    }
    
    /**
     * Update photo category.
     */
    fun updatePhotoCategory(photoId: String, category: String?) {
        viewModelScope.launch {
            if (_recipeId != null) {
                // Edit mode - update in database
                repository.updatePhotoCategory(photoId, category)
                    .onSuccess {
                        _photos.value = _photos.value.map { photo ->
                            if (photo.id == photoId) photo.copy(categorie = category) else photo
                        }
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Erreur lors de la mise à jour: ${error.message}"
                    }
            } else {
                // Create mode - update in memory
                _photos.value = _photos.value.map { photo ->
                    if (photo.id == photoId) photo.copy(categorie = category) else photo
                }
            }
        }
    }
    
    /**
     * Delete a photo.
     */
    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            if (_recipeId != null) {
                // Edit mode - delete from database
                repository.deletePhoto(photoId)
                    .onSuccess {
                        _photos.value = _photos.value.filter { it.id != photoId }
                    }
                    .onFailure { error ->
                        _errorMessage.value = "Erreur lors de la suppression: ${error.message}"
                    }
            } else {
                // Create mode - remove from memory
                _photos.value = _photos.value.filter { it.id != photoId }
            }
        }
    }
    
    /**
     * Move photo up in order.
     */
    fun movePhotoUp(photoId: String) {
        val currentPhotos = _photos.value
        val index = currentPhotos.indexOfFirst { it.id == photoId }
        if (index > 0) {
            val reordered = currentPhotos.toMutableList()
            val temp = reordered[index]
            reordered[index] = reordered[index - 1]
            reordered[index - 1] = temp
            reorderPhotos(reordered)
        }
    }
    
    /**
     * Move photo down in order.
     */
    fun movePhotoDown(photoId: String) {
        val currentPhotos = _photos.value
        val index = currentPhotos.indexOfFirst { it.id == photoId }
        if (index < currentPhotos.size - 1) {
            val reordered = currentPhotos.toMutableList()
            val temp = reordered[index]
            reordered[index] = reordered[index + 1]
            reordered[index + 1] = temp
            reorderPhotos(reordered)
        }
    }
    
    /**
     * Reorder photos and update in repository.
     */
    private fun reorderPhotos(reorderedPhotos: List<PhotoDetail>) {
        viewModelScope.launch {
            val updatedPhotos = reorderedPhotos.mapIndexed { index, photo ->
                photo.copy(ordre = index + 1)
            }
            _photos.value = updatedPhotos
            
            // Only update in repository if in edit mode
            if (_recipeId != null) {
                // Create map of photoId to order
                val photoOrders = updatedPhotos.associate { it.id to it.ordre }
                
                // Update in repository
                repository.reorderPhotos(_recipeId!!, photoOrders)
            }
            // In create mode, just keep in memory
        }
    }
}
}