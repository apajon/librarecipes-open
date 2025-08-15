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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class Quad<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)

enum class FilterMode {
    ALPHABETICAL,
    DATE
}

enum class DatePeriod(val displayName: String, val days: Int?) {
    ALL("Tout", null),
    ONE_WEEK("1 semaine", 7),
    TWO_WEEKS("2 semaines", 14),
    ONE_MONTH("1 mois", 30),
    THREE_MONTHS("3 mois", 90),
    SIX_MONTHS("6 mois", 180),
    ONE_YEAR("1 an", 365),
    TWO_YEARS("2 ans", 730),
    THREE_YEARS("3 ans", 1095)
}

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val repository: RecipeRepository
) : ViewModel() {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    private val _recipes = MutableStateFlow<List<RecipeListItem>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _sortAscending = MutableStateFlow(true)
    private val _selectedLetter = MutableStateFlow<String?>(null)
    private val _filterMode = MutableStateFlow(FilterMode.ALPHABETICAL)
    private val _selectedDatePeriod = MutableStateFlow(DatePeriod.ALL)
    
    val uiState: StateFlow<RecipeListUiState> = combine(
        combine(_recipes, _isLoading, _errorMessage, _sortAscending) { recipes, isLoading, errorMessage, sortAscending ->
            Quad(recipes, isLoading, errorMessage, sortAscending)
        },
        combine(_selectedLetter, _filterMode, _selectedDatePeriod) { selectedLetter, filterMode, selectedDatePeriod ->
            Triple(selectedLetter, filterMode, selectedDatePeriod)
        }
    ) { firstGroup, secondGroup ->
        val (recipes, isLoading, errorMessage, sortAscending) = firstGroup
        val (selectedLetter, filterMode, selectedDatePeriod) = secondGroup
        
        val processedRecipes = when (filterMode) {
            FilterMode.ALPHABETICAL -> processRecipesAlphabetically(recipes, sortAscending, selectedLetter)
            FilterMode.DATE -> processRecipesByDate(recipes, sortAscending, selectedDatePeriod)
        }
        RecipeListUiState(
            recipeSections = processedRecipes,
            isLoading = isLoading,
            errorMessage = errorMessage,
            sortAscending = sortAscending,
            selectedLetter = selectedLetter,
            availableLetters = getAvailableLetters(recipes),
            filterMode = filterMode,
            selectedDatePeriod = selectedDatePeriod
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
    
    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
        // Reset filters when switching modes
        if (mode == FilterMode.ALPHABETICAL) {
            _selectedDatePeriod.value = DatePeriod.ALL
        } else {
            _selectedLetter.value = null
        }
    }
    
    fun setDatePeriod(period: DatePeriod) {
        _selectedDatePeriod.value = period
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
    
    private fun processRecipesAlphabetically(
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
    
    private fun processRecipesByDate(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedDatePeriod: DatePeriod
    ): List<RecipeSection> {
        // Filter by date period
        val filteredRecipes = if (selectedDatePeriod == DatePeriod.ALL) {
            recipes
        } else {
            val cutoffDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -(selectedDatePeriod.days ?: 0))
            }.time
            
            recipes.filter { recipe ->
                try {
                    val recipeDate = dateFormat.parse(recipe.dateAjout)
                    recipeDate?.after(cutoffDate) == true
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        // Sort by date
        val sortedRecipes = try {
            if (sortAscending) {
                // Recent first for ascending when in date mode (recent = higher priority)
                filteredRecipes.sortedByDescending { recipe ->
                    dateFormat.parse(recipe.dateAjout)?.time ?: 0L
                }
            } else {
                // Oldest first for descending 
                filteredRecipes.sortedBy { recipe ->
                    dateFormat.parse(recipe.dateAjout)?.time ?: 0L
                }
            }
        } catch (e: Exception) {
            filteredRecipes
        }
        
        // Group by date ranges
        return groupRecipesByDateRanges(sortedRecipes)
    }
    
    private fun groupRecipesByDateRanges(recipes: List<RecipeListItem>): List<RecipeSection> {
        val now = Calendar.getInstance()
        val sections = mutableMapOf<String, MutableList<RecipeListItem>>()
        
        for (recipe in recipes) {
            try {
                val recipeDate = dateFormat.parse(recipe.dateAjout)
                if (recipeDate != null) {
                    val recipeCal = Calendar.getInstance().apply { time = recipeDate }
                    val daysDiff = ((now.timeInMillis - recipeCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    
                    val section = when {
                        daysDiff == 0 -> "Aujourd'hui"
                        daysDiff == 1 -> "Hier" 
                        daysDiff <= 7 -> "Cette semaine"
                        daysDiff <= 14 -> "Il y a 2 semaines"
                        daysDiff <= 30 -> "Ce mois-ci"
                        daysDiff <= 90 -> "Il y a 3 mois"
                        daysDiff <= 180 -> "Il y a 6 mois"
                        daysDiff <= 365 -> "Cette année"
                        daysDiff <= 730 -> "Il y a 2 ans"
                        else -> "Il y a plus de 2 ans"
                    }
                    
                    sections.getOrPut(section) { mutableListOf() }.add(recipe)
                }
            } catch (e: Exception) {
                // Add to "Date inconnue" section for recipes with invalid dates
                sections.getOrPut("Date inconnue") { mutableListOf() }.add(recipe)
            }
        }
        
        // Define the order of sections
        val sectionOrder = listOf(
            "Aujourd'hui", "Hier", "Cette semaine", "Il y a 2 semaines", 
            "Ce mois-ci", "Il y a 3 mois", "Il y a 6 mois", "Cette année", 
            "Il y a 2 ans", "Il y a plus de 2 ans", "Date inconnue"
        )
        
        return sectionOrder.mapNotNull { sectionName ->
            sections[sectionName]?.let { recipesList ->
                RecipeSection(
                    letter = sectionName,
                    recipes = recipesList
                )
            }
        }
    }
    
    private fun processRecipes(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedLetter: String?
    ): List<RecipeSection> {
        return processRecipesAlphabetically(recipes, sortAscending, selectedLetter)
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
    val availableLetters: List<String> = emptyList(),
    val filterMode: FilterMode = FilterMode.ALPHABETICAL,
    val selectedDatePeriod: DatePeriod = DatePeriod.ALL
)