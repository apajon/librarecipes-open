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
    DATE,
    EXECUTION,
    CONVIVES,
    INGREDIENT
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

enum class ConvivesPeriod(val displayName: String, val minConvives: Int?, val maxConvives: Int?) {
    ALL("Tous", null, null),
    SOLO("Solo (1)", 1, 1),
    COUPLE("Couple (2)", 2, 2),
    FAMILLE("Famille (3-4)", 3, 4),
    GROUPE("Groupe (5+)", 5, null)
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
    private val _selectedConvivesPeriod = MutableStateFlow(ConvivesPeriod.ALL)
    private val _selectedIngredient = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<RecipeListUiState> = combine(
        combine(_recipes, _isLoading, _errorMessage, _sortAscending) { recipes, isLoading, errorMessage, sortAscending ->
            Quad(recipes, isLoading, errorMessage, sortAscending)
        },
        combine(_selectedLetter, _filterMode, _selectedDatePeriod, _selectedConvivesPeriod) { selectedLetter, filterMode, selectedDatePeriod, selectedConvivesPeriod ->
            Quad(selectedLetter, filterMode, selectedDatePeriod, selectedConvivesPeriod)
        },
        _selectedIngredient
    ) { firstGroup, secondGroup, selectedIngredient ->
        val (recipes, isLoading, errorMessage, sortAscending) = firstGroup
        val (selectedLetter, filterMode, selectedDatePeriod, selectedConvivesPeriod) = secondGroup
        
        val processedRecipes = when (filterMode) {
            FilterMode.ALPHABETICAL -> processRecipesAlphabetically(recipes, sortAscending, selectedLetter)
            FilterMode.DATE -> processRecipesByDate(recipes, sortAscending, selectedDatePeriod)
            FilterMode.EXECUTION -> processRecipesByExecution(recipes, sortAscending, selectedDatePeriod)
            FilterMode.CONVIVES -> processRecipesByConvives(recipes, sortAscending, selectedConvivesPeriod)
            FilterMode.INGREDIENT -> processRecipesByIngredient(recipes, sortAscending, selectedIngredient)
        }
        RecipeListUiState(
            recipeSections = processedRecipes,
            isLoading = isLoading,
            errorMessage = errorMessage,
            sortAscending = sortAscending,
            selectedLetter = selectedLetter,
            availableLetters = getAvailableLetters(recipes),
            filterMode = filterMode,
            selectedDatePeriod = selectedDatePeriod,
            selectedConvivesPeriod = selectedConvivesPeriod,
            selectedIngredient = selectedIngredient,
            availableIngredients = getAvailableIngredients(recipes)
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
        when (mode) {
            FilterMode.ALPHABETICAL -> {
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.DATE, FilterMode.EXECUTION -> {
                _selectedLetter.value = null
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.CONVIVES -> {
                _selectedLetter.value = null
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.INGREDIENT -> {
                _selectedLetter.value = null
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
            }
        }
    }
    
    fun setDatePeriod(period: DatePeriod) {
        _selectedDatePeriod.value = period
    }
    
    fun setConvivesPeriod(period: ConvivesPeriod) {
        _selectedConvivesPeriod.value = period
    }
    
    fun setSelectedIngredient(ingredient: String?) {
        _selectedIngredient.value = ingredient
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
    
    private fun getAvailableIngredients(recipes: List<RecipeListItem>): List<String> {
        // This is a simplified implementation - in reality we'd need to query the database
        // for ingredients. For now, return an empty list.
        return emptyList()
    }
    
    private fun processRecipesByExecution(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedDatePeriod: DatePeriod
    ): List<RecipeSection> {
        // This is similar to processRecipesByDate but would be based on execution dates
        // For now, use the same implementation as date-based filtering
        // TODO: Implement actual execution-based filtering when execution data is available
        return processRecipesByDate(recipes, sortAscending, selectedDatePeriod)
    }
    
    private fun processRecipesByConvives(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedConvivesPeriod: ConvivesPeriod
    ): List<RecipeSection> {
        // Filter by convives range
        val filteredRecipes = if (selectedConvivesPeriod == ConvivesPeriod.ALL) {
            recipes
        } else {
            recipes.filter { recipe ->
                val portions = recipe.portions ?: 1
                when {
                    selectedConvivesPeriod.minConvives != null && selectedConvivesPeriod.maxConvives != null -> {
                        portions >= selectedConvivesPeriod.minConvives && portions <= selectedConvivesPeriod.maxConvives
                    }
                    selectedConvivesPeriod.minConvives != null && selectedConvivesPeriod.maxConvives == null -> {
                        portions >= selectedConvivesPeriod.minConvives
                    }
                    else -> true
                }
            }
        }
        
        // Sort by portions
        val sortedRecipes = if (sortAscending) {
            filteredRecipes.sortedBy { it.portions ?: 1 }
        } else {
            filteredRecipes.sortedByDescending { it.portions ?: 1 }
        }
        
        // Group by convives ranges
        return groupRecipesByConvivesRanges(sortedRecipes)
    }
    
    private fun processRecipesByIngredient(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedIngredient: String?
    ): List<RecipeSection> {
        // For now, filter by recipe name containing the ingredient
        // TODO: Implement actual ingredient-based filtering when ingredient data is available
        val filteredRecipes = if (selectedIngredient.isNullOrBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.nom.contains(selectedIngredient, ignoreCase = true)
            }
        }
        
        val sortedRecipes = if (sortAscending) {
            filteredRecipes.sortedBy { it.nom }
        } else {
            filteredRecipes.sortedByDescending { it.nom }
        }
        
        return if (selectedIngredient.isNullOrBlank()) {
            // Group alphabetically when no ingredient is selected
            sortedRecipes
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
        } else {
            // Single section when filtering by ingredient
            listOf(
                RecipeSection(
                    letter = "Contient: $selectedIngredient",
                    recipes = sortedRecipes
                )
            )
        }
    }
    
    private fun groupRecipesByConvivesRanges(recipes: List<RecipeListItem>): List<RecipeSection> {
        val sections = mutableMapOf<String, MutableList<RecipeListItem>>()
        
        for (recipe in recipes) {
            val portions = recipe.portions ?: 1
            val section = when {
                portions == 1 -> "Solo (1 personne)"
                portions == 2 -> "Couple (2 personnes)"
                portions in 3..4 -> "Famille (3-4 personnes)"
                portions >= 5 -> "Groupe (5+ personnes)"
                else -> "Non spécifié"
            }
            
            sections.getOrPut(section) { mutableListOf() }.add(recipe)
        }
        
        // Define the order of sections
        val sectionOrder = listOf(
            "Solo (1 personne)", "Couple (2 personnes)", "Famille (3-4 personnes)",
            "Groupe (5+ personnes)", "Non spécifié"
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
    val selectedDatePeriod: DatePeriod = DatePeriod.ALL,
    val selectedConvivesPeriod: ConvivesPeriod = ConvivesPeriod.ALL,
    val selectedIngredient: String? = null,
    val availableIngredients: List<String> = emptyList()
)