package com.apajon.librarecipes.viewmodel

import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeCreate
import com.apajon.librarecipes.data.model.RecipeTextFormat
import com.apajon.librarecipes.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the import recipe screen.
 */
data class ImportRecipeUiState(
    val clipboardText: String? = null,
    val parsedRecipe: RecipeCreate? = null,
    val parseError: Boolean = false,
    val emptyClipboard: Boolean = false,
    val isSaving: Boolean = false,
    val savedRecipeId: String? = null,
    val saveError: String? = null
)

/**
 * ViewModel for importing recipes from clipboard text.
 */
@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportRecipeUiState())
    val uiState: StateFlow<ImportRecipeUiState> = _uiState.asStateFlow()

    /**
     * Read clipboard content and try to parse it as a recipe.
     */
    fun pasteFromClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip == null || clip.itemCount == 0) {
            _uiState.value = ImportRecipeUiState(emptyClipboard = true)
            return
        }

        val text = clip.getItemAt(0).text?.toString()
        if (text.isNullOrBlank()) {
            _uiState.value = ImportRecipeUiState(emptyClipboard = true)
            return
        }

        val parsed = RecipeTextFormat.parseRecipe(text)
        _uiState.value = if (parsed != null) {
            ImportRecipeUiState(clipboardText = text, parsedRecipe = parsed)
        } else {
            ImportRecipeUiState(clipboardText = text, parseError = true)
        }
    }

    /**
     * Import the parsed recipe into the local database.
     */
    fun importRecipe() {
        val recipe = _uiState.value.parsedRecipe ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)

            recipeRepository.createRecipe(recipe)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedRecipeId = response.id
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveError = error.message ?: "Erreur lors de l'importation"
                    )
                }
        }
    }

    /**
     * Reset the state to allow a new paste.
     */
    fun reset() {
        _uiState.value = ImportRecipeUiState()
    }
}
