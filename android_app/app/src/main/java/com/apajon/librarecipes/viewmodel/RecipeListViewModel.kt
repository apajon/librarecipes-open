package com.apajon.librarecipes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.data.repository.RecipeRepository
import com.apajon.librarecipes.data.repository.RecipeResult
import com.apajon.librarecipes.data.repository.ExecutionStatus
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

enum class ExecutionPeriod(val displayName: String, val days: Int?, val isNever: Boolean = false) {
    ALL("Tout", null),
    NEVER("Jamais", null, true),
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
    private val _executionStatus = MutableStateFlow<Map<String, ExecutionStatus>>(emptyMap())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _sortAscending = MutableStateFlow(true)
    private val _selectedLetter = MutableStateFlow<String?>(null)
    private val _filterMode = MutableStateFlow(FilterMode.ALPHABETICAL)
    private val _selectedDatePeriod = MutableStateFlow(DatePeriod.ALL)
    private val _selectedExecutionPeriod = MutableStateFlow(ExecutionPeriod.ALL)
    private val _selectedConvivesPeriod = MutableStateFlow(ConvivesPeriod.ALL)
    private val _selectedConvives = MutableStateFlow<Int?>(null)
    private val _selectedIngredient = MutableStateFlow<String?>(null)
    private val _availableIngredients = MutableStateFlow<List<String>>(emptyList())
    
    val uiState: StateFlow<RecipeListUiState> = combine(
        combine(_recipes, _executionStatus, _isLoading, _errorMessage) { recipes, executionStatus, isLoading, errorMessage ->
            Quad(recipes, executionStatus, isLoading, errorMessage)
        },
        combine(_sortAscending, _selectedLetter, _filterMode, _selectedDatePeriod) { sortAscending, selectedLetter, filterMode, selectedDatePeriod ->
            Quad(sortAscending, selectedLetter, filterMode, selectedDatePeriod)
        },
        combine(_selectedExecutionPeriod, _selectedConvives, _selectedIngredient, _availableIngredients) { selectedExecutionPeriod, selectedConvives, selectedIngredient, availableIngredients ->
            Quad(selectedExecutionPeriod, selectedConvives, selectedIngredient, availableIngredients)
        },
        _selectedConvivesPeriod
    ) { firstGroup, secondGroup, thirdGroup, selectedConvivesPeriod ->
        val (recipes, executionStatus, isLoading, errorMessage) = firstGroup
        val (sortAscending, selectedLetter, filterMode, selectedDatePeriod) = secondGroup
        val (selectedExecutionPeriod, selectedConvives, selectedIngredient, availableIngredients) = thirdGroup
        
        val processedRecipes = when (filterMode) {
            FilterMode.ALPHABETICAL -> processRecipesAlphabetically(recipes, sortAscending, selectedLetter)
            FilterMode.DATE -> processRecipesByDate(recipes, sortAscending, selectedDatePeriod)
            FilterMode.EXECUTION -> processRecipesByExecution(recipes, executionStatus, sortAscending, selectedExecutionPeriod)
            FilterMode.CONVIVES -> processRecipesByConvivesAlphabetical(recipes, sortAscending, selectedConvives)
            FilterMode.INGREDIENT -> processRecipesByIngredientAlphabetical(recipes, sortAscending, selectedIngredient)
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
            selectedExecutionPeriod = selectedExecutionPeriod,
            selectedConvivesPeriod = selectedConvivesPeriod,
            selectedConvives = selectedConvives,
            availableConvives = getAvailableConvives(recipes),
            selectedIngredient = selectedIngredient,
            availableIngredients = availableIngredients
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeListUiState()
    )
    
    init {
        loadRecipes()
        loadAvailableIngredients()
        loadExecutionStatus()
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
    
    private fun loadAvailableIngredients() {
        viewModelScope.launch {
            repository.getAllUniqueIngredients().collect { ingredients ->
                _availableIngredients.value = ingredients
            }
        }
    }
    
    private fun loadExecutionStatus() {
        viewModelScope.launch {
            repository.getRecipesWithExecutionStatus().collect { executionStatus ->
                _executionStatus.value = executionStatus
            }
        }
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
                _selectedExecutionPeriod.value = ExecutionPeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.DATE -> {
                _selectedLetter.value = null
                _selectedExecutionPeriod.value = ExecutionPeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.EXECUTION -> {
                _selectedLetter.value = null
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedIngredient.value = null
            }
            FilterMode.CONVIVES -> {
                _selectedLetter.value = null
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedExecutionPeriod.value = ExecutionPeriod.ALL
                _selectedConvives.value = null
                _selectedIngredient.value = null
            }
            FilterMode.INGREDIENT -> {
                _selectedDatePeriod.value = DatePeriod.ALL
                _selectedExecutionPeriod.value = ExecutionPeriod.ALL
                _selectedConvivesPeriod.value = ConvivesPeriod.ALL
                _selectedConvives.value = null
                _selectedIngredient.value = null
                // Keep selectedLetter for ingredient mode (it now works like alphabetical)
            }
        }
    }
    
    fun setDatePeriod(period: DatePeriod) {
        _selectedDatePeriod.value = period
    }
    
    fun setExecutionPeriod(period: ExecutionPeriod) {
        _selectedExecutionPeriod.value = period
    }
    
    fun setConvivesPeriod(period: ConvivesPeriod) {
        _selectedConvivesPeriod.value = period
    }
    
    fun filterByConvives(convives: Int?) {
        _selectedConvives.value = convives
    }
    
    fun filterByIngredient(ingredient: String?) {
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
                    recipes = recipesList,
                    isIngredientSection = false,
                    ingredients = emptyList()
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
                    recipes = recipesList,
                    isIngredientSection = false,
                    ingredients = emptyList()
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
    
    private fun getAvailableConvives(recipes: List<RecipeListItem>): List<Int> {
        return recipes
            .mapNotNull { recipe -> recipe.portions }
            .distinct()
            .sorted()
    }
    
    private fun getAvailableIngredients(recipes: List<RecipeListItem>): List<String> {
        // For now, we'll extract ingredients from recipe names as a simple implementation
        // In a real app, this would query the ingredients database
        val commonIngredients = listOf(
            "tomate", "oignon", "ail", "carotte", "pomme de terre", "courgette",
            "aubergine", "poivron", "champignon", "épinard", "bœuf", "porc", 
            "agneau", "poulet", "poisson", "saumon", "crevette", "œuf", "fromage",
            "pâtes", "riz", "quinoa", "lentille", "haricot", "pois chiche"
        )
        
        return commonIngredients.filter { ingredient ->
            recipes.any { recipe -> 
                recipe.nom.contains(ingredient, ignoreCase = true)
            }
        }.sorted()
    }
    
    private fun processRecipesByExecution(
        recipes: List<RecipeListItem>,
        executionStatus: Map<String, ExecutionStatus>,
        sortAscending: Boolean,
        selectedExecutionPeriod: ExecutionPeriod
    ): List<RecipeSection> {
        // Filter recipes based on execution period
        val filteredRecipes = when {
            selectedExecutionPeriod == ExecutionPeriod.ALL -> recipes
            selectedExecutionPeriod.isNever -> {
                // Filter recipes that have never been executed
                recipes.filter { recipe ->
                    val status = executionStatus[recipe.id]
                    status?.hasExecutions != true
                }
            }
            else -> {
                // Filter by execution date within period
                val cutoffDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(selectedExecutionPeriod.days ?: 0))
                }.time
                
                recipes.filter { recipe ->
                    val status = executionStatus[recipe.id]
                    if (status?.hasExecutions != true || status.lastExecutionDate == null) {
                        false
                    } else {
                        try {
                            val executionDate = dateFormat.parse(status.lastExecutionDate)
                            executionDate?.after(cutoffDate) == true
                        } catch (e: Exception) {
                            false
                        }
                    }
                }
            }
        }
        
        // Sort by execution date (using execution date when available, fallback to recipe date)
        val sortedRecipes = try {
            if (sortAscending) {
                filteredRecipes.sortedByDescending { recipe ->
                    val status = executionStatus[recipe.id]
                    if (status?.hasExecutions == true && status.lastExecutionDate != null) {
                        try {
                            dateFormat.parse(status.lastExecutionDate)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    } else {
                        // Fallback to recipe creation date
                        try {
                            dateFormat.parse(recipe.dateAjout)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    }
                }
            } else {
                filteredRecipes.sortedBy { recipe ->
                    val status = executionStatus[recipe.id]
                    if (status?.hasExecutions == true && status.lastExecutionDate != null) {
                        try {
                            dateFormat.parse(status.lastExecutionDate)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    } else {
                        try {
                            dateFormat.parse(recipe.dateAjout)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    }
                }
            }
        } catch (e: Exception) {
            filteredRecipes
        }
        
        // Group by execution periods
        return groupRecipesByExecutionRanges(sortedRecipes, executionStatus)
    }
    
    private fun groupRecipesByExecutionRanges(
        recipes: List<RecipeListItem>,
        executionStatus: Map<String, ExecutionStatus>
    ): List<RecipeSection> {
        val now = Calendar.getInstance()
        val sections = mutableMapOf<String, MutableList<RecipeListItem>>()
        
        for (recipe in recipes) {
            val status = executionStatus[recipe.id]
            
            val section = if (status?.hasExecutions != true || status.lastExecutionDate == null) {
                "Jamais exécutée"
            } else {
                try {
                    val executionDate = dateFormat.parse(status.lastExecutionDate)
                    if (executionDate != null) {
                        val executionCal = Calendar.getInstance().apply { time = executionDate }
                        val daysDiff = ((now.timeInMillis - executionCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                        
                        when {
                            daysDiff == 0 -> "Exécutée aujourd'hui"
                            daysDiff == 1 -> "Exécutée hier" 
                            daysDiff <= 7 -> "Exécutée cette semaine"
                            daysDiff <= 14 -> "Exécutée il y a 2 semaines"
                            daysDiff <= 30 -> "Exécutée ce mois-ci"
                            daysDiff <= 90 -> "Exécutée il y a 3 mois"
                            daysDiff <= 180 -> "Exécutée il y a 6 mois"
                            daysDiff <= 365 -> "Exécutée cette année"
                            daysDiff <= 730 -> "Exécutée il y a 2 ans"
                            else -> "Exécutée il y a plus de 2 ans"
                        }
                    } else {
                        "Jamais exécutée"
                    }
                } catch (e: Exception) {
                    "Jamais exécutée"
                }
            }
            
            sections.getOrPut(section) { mutableListOf() }.add(recipe)
        }
        
        // Define the order of sections
        val sectionOrder = listOf(
            "Exécutée aujourd'hui", "Exécutée hier", "Exécutée cette semaine", "Exécutée il y a 2 semaines", 
            "Exécutée ce mois-ci", "Exécutée il y a 3 mois", "Exécutée il y a 6 mois", "Exécutée cette année", 
            "Exécutée il y a 2 ans", "Exécutée il y a plus de 2 ans", "Jamais exécutée"
        )
        
        return sectionOrder.mapNotNull { sectionName ->
            sections[sectionName]?.let { recipesList ->
                RecipeSection(
                    letter = sectionName,
                    recipes = recipesList,
                    isIngredientSection = false,
                    ingredients = emptyList()
                )
            }
        }
    }
    
    private fun processRecipesByConvivesAlphabetical(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedConvives: Int?
    ): List<RecipeSection> {
        val filteredRecipes = if (selectedConvives != null) {
            recipes.filter { recipe ->
                recipe.portions == selectedConvives
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
                    recipes = recipesList,
                    isIngredientSection = false,
                    ingredients = emptyList()
                )
            }
    }
    
    private fun processRecipesByIngredientAlphabetical(
        recipes: List<RecipeListItem>,
        sortAscending: Boolean,
        selectedIngredient: String?
    ): List<RecipeSection> {
        // If an ingredient is selected, filter recipes containing that ingredient
        if (selectedIngredient != null) {
            // TODO: Replace with actual database search by ingredient
            val filteredRecipes = recipes.filter { recipe ->
                recipe.nom.contains(selectedIngredient, ignoreCase = true)
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
                        recipes = recipesList,
                        isIngredientSection = false,
                        ingredients = emptyList()
                    )
                }
        }
        
        // If no ingredient is selected, show recipes grouped alphabetically by first letter
        // This mirrors the alphabetical mode behavior exactly
        return processRecipesAlphabetically(recipes, sortAscending, _selectedLetter.value)
    }
}

data class RecipeSection(
    val letter: String,
    val recipes: List<RecipeListItem>,
    val isIngredientSection: Boolean = false,
    val ingredients: List<String> = emptyList()
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
    val selectedExecutionPeriod: ExecutionPeriod = ExecutionPeriod.ALL,
    val selectedConvivesPeriod: ConvivesPeriod = ConvivesPeriod.ALL,
    val selectedConvives: Int? = null,
    val availableConvives: List<Int> = emptyList(),
    val selectedIngredient: String? = null,
    val availableIngredients: List<String> = emptyList()
)