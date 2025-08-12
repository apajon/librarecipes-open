package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsState
import androidx.navigation.NavHostController
import com.apajon.librarecipes.data.model.Source
import com.apajon.librarecipes.ui.components.IngredientInput
import com.apajon.librarecipes.ui.components.StepsInput
import com.apajon.librarecipes.ui.components.TagSelector
import com.apajon.librarecipes.viewmodel.AddRecipeViewModel
import com.apajon.librarecipes.viewmodel.SearchViewModel
import com.apajon.librarecipes.viewmodel.ValidationErrors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail Recette") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Détail de la recette",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: $recipeId",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "À implémenter : affichage complet de la recette",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavHostController,
    viewModel: AddRecipeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableIngredients by viewModel.availableIngredients.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    
    // Handle navigation after successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            navController.navigateUp()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouvelle Recette") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    // Save button in top bar
                    TextButton(
                        onClick = { viewModel.saveRecipe() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                "Sauvegarder",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipe basic info
            item {
                RecipeBasicInfoSection(
                    uiState = uiState,
                    onUpdateName = viewModel::updateName,
                    onUpdatePreparationTime = viewModel::updatePreparationTime,
                    onUpdateCookingTime = viewModel::updateCookingTime,
                    onUpdatePortions = viewModel::updatePortions
                )
            }
            
            // Ingredients section
            item {
                IngredientInput(
                    ingredients = uiState.ingredients,
                    availableIngredients = availableIngredients,
                    onAddIngredient = viewModel::addIngredient,
                    onRemoveIngredient = viewModel::removeIngredient,
                    onUpdateIngredient = viewModel::updateIngredient
                )
            }
            
            // Steps section
            item {
                StepsInput(
                    steps = uiState.etapes,
                    onAddStep = viewModel::addStep,
                    onRemoveStep = viewModel::removeStep,
                    onUpdateStep = viewModel::updateStep
                )
            }
            
            // Categories section
            item {
                TagSelector(
                    label = "Catégories",
                    selectedItems = uiState.categories,
                    availableItems = availableCategories,
                    onSelectionChange = viewModel::updateCategories
                )
            }
            
            // Tags section
            item {
                TagSelector(
                    label = "Tags",
                    selectedItems = uiState.tags,
                    availableItems = availableTags,
                    onSelectionChange = viewModel::updateTags
                )
            }
            
            // Source section
            item {
                SourceInput(
                    source = uiState.source,
                    onUpdateSource = viewModel::updateSource
                )
            }
            
            // Error messages
            if (uiState.saveError != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.saveError,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Validation errors
            if (!uiState.validationErrors.hasNoErrors()) {
                item {
                    ValidationErrorsSection(uiState.validationErrors)
                }
            }
            
            // Bottom padding for save button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        // Floating save button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.BottomCenter
        ) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.saveRecipe() },
                modifier = Modifier.padding(16.dp),
                expanded = true,
                icon = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "Sauvegarder")
                    }
                },
                text = { Text("Sauvegarder la recette") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableIngredients by viewModel.availableIngredients.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recherche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    // Clear filters button
                    if (uiState.hasActiveFilters()) {
                        TextButton(
                            onClick = { viewModel.clearSearch() }
                        ) {
                            Text(
                                "Effacer",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // Search FAB
            if (uiState.hasActiveFilters()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.performSearch() },
                    expanded = true,
                    icon = {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Add, contentDescription = "Rechercher")
                        }
                    },
                    text = { Text("Rechercher") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search filters section
            item {
                SearchFiltersSection(
                    uiState = uiState,
                    availableIngredients = availableIngredients,
                    availableCategories = availableCategories,
                    availableTags = availableTags,
                    onUpdateNameFilter = viewModel::updateNameFilter,
                    onUpdateIngredientsFilter = viewModel::updateIngredientsFilter,
                    onUpdateIngredientsMode = viewModel::updateIngredientsMode,
                    onUpdateTagsFilter = viewModel::updateTagsFilter,
                    onUpdateCategoriesFilter = viewModel::updateCategoriesFilter
                )
            }
            
            // Active filters summary
            if (uiState.hasActiveFilters()) {
                item {
                    ActiveFiltersCard(
                        filterSummary = uiState.getFilterSummary(),
                        onClearFilters = { viewModel.clearSearch() }
                    )
                }
            }
            
            // Search results section
            if (uiState.hasSearched) {
                item {
                    SearchResultsSection(
                        searchResults = uiState.searchResults,
                        isLoading = uiState.isLoading,
                        searchError = uiState.searchError,
                        onRecipeClick = { recipeId ->
                            navController.navigate("recipe/$recipeId")
                        }
                    )
                }
            }
            
            // Help section when no filters are active
            if (!uiState.hasActiveFilters() && !uiState.hasSearched) {
                item {
                    SearchHelpSection()
                }
            }
            
            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
}

// Helper components for SearchScreen

@Composable
private fun SearchFiltersSection(
    uiState: com.apajon.librarecipes.viewmodel.SearchUiState,
    availableIngredients: List<String>,
    availableCategories: List<String>,
    availableTags: List<String>,
    onUpdateNameFilter: (String) -> Unit,
    onUpdateIngredientsFilter: (List<String>) -> Unit,
    onUpdateIngredientsMode: (com.apajon.librarecipes.data.model.IngredientsMode) -> Unit,
    onUpdateTagsFilter: (List<String>) -> Unit,
    onUpdateCategoriesFilter: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtres de recherche",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Recipe name filter
            OutlinedTextField(
                value = uiState.nameFilter ?: "",
                onValueChange = onUpdateNameFilter,
                label = { Text("Nom de la recette") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Ingredients filter
            TagSelector(
                label = "Ingrédients",
                selectedItems = uiState.ingredientsFilter ?: emptyList(),
                availableItems = availableIngredients,
                onSelectionChange = onUpdateIngredientsFilter
            )
            
            // Ingredients mode selector
            if (!uiState.ingredientsFilter.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Mode de recherche des ingrédients :",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { onUpdateIngredientsMode(com.apajon.librarecipes.data.model.IngredientsMode.ANY) },
                            label = { Text("Au moins un") },
                            selected = uiState.ingredientsMode == com.apajon.librarecipes.data.model.IngredientsMode.ANY
                        )
                        FilterChip(
                            onClick = { onUpdateIngredientsMode(com.apajon.librarecipes.data.model.IngredientsMode.ALL) },
                            label = { Text("Tous requis") },
                            selected = uiState.ingredientsMode == com.apajon.librarecipes.data.model.IngredientsMode.ALL
                        )
                    }
                }
            }
            
            // Tags filter
            TagSelector(
                label = "Tags",
                selectedItems = uiState.tagsFilter ?: emptyList(),
                availableItems = availableTags,
                onSelectionChange = onUpdateTagsFilter
            )
            
            // Categories filter
            TagSelector(
                label = "Catégories",
                selectedItems = uiState.categoriesFilter ?: emptyList(),
                availableItems = availableCategories,
                onSelectionChange = onUpdateCategoriesFilter
            )
        }
    }
}

@Composable
private fun ActiveFiltersCard(
    filterSummary: String,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtres actifs",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                TextButton(onClick = onClearFilters) {
                    Text("Effacer tout")
                }
            }
            
            Text(
                text = filterSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchResults: List<com.apajon.librarecipes.data.model.RecipeListItem>,
    isLoading: Boolean,
    searchError: String?,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Résultats de recherche",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                searchError != null -> {
                    Text(
                        text = "Erreur : $searchError",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                searchResults.isEmpty() -> {
                    Text(
                        text = "Aucune recette trouvée avec ces critères",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                else -> {
                    Text(
                        text = "${searchResults.size} recette(s) trouvée(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    searchResults.forEach { recipe ->
                        SearchResultItem(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    recipe: com.apajon.librarecipes.data.model.RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = recipe.nom,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Recipe info
            val totalTime = (recipe.preparation ?: 0) + (recipe.cuisson ?: 0)
            val infoText = buildString {
                if (totalTime > 0) append("⏱️ ${totalTime}min")
                if (recipe.portions != null) {
                    if (isNotEmpty()) append(" • ")
                    append("👥 ${recipe.portions} portions")
                }
            }
            
            if (infoText.isNotEmpty()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Categories and tags
            if (recipe.categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏷️ ${recipe.categories.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchHelpSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Comment rechercher ?",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val helpItems = listOf(
                "Nom : Recherchez par nom de recette",
                "Ingrédients : Trouvez des recettes contenant certains ingrédients",
                "Tags : Filtrez par tags (ex: facile, rapide, végétarien)",
                "Catégories : Filtrez par type de plat (ex: entrée, plat, dessert)"
            )
            
            helpItems.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Ajoutez des filtres ci-dessus pour commencer votre recherche.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper components for CreateRecipeScreen

@Composable
private fun RecipeBasicInfoSection(
    uiState: com.apajon.librarecipes.viewmodel.AddRecipeUiState,
    onUpdateName: (String) -> Unit,
    onUpdatePreparationTime: (Int?) -> Unit,
    onUpdateCookingTime: (Int?) -> Unit,
    onUpdatePortions: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Informations générales",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = uiState.nom,
                onValueChange = onUpdateName,
                label = { Text("Nom de la recette *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.validationErrors.nom != null,
                supportingText = {
                    uiState.validationErrors.nom?.let { 
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.preparation?.toString() ?: "",
                    onValueChange = { value ->
                        onUpdatePreparationTime(value.toIntOrNull())
                    },
                    label = { Text("Préparation (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = uiState.cuisson?.toString() ?: "",
                    onValueChange = { value ->
                        onUpdateCookingTime(value.toIntOrNull())
                    },
                    label = { Text("Cuisson (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = uiState.portions?.toString() ?: "",
                    onValueChange = { value ->
                        onUpdatePortions(value.toIntOrNull())
                    },
                    label = { Text("Portions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SourceInput(
    source: Source?,
    onUpdateSource: (Source?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSourceDialog by remember { mutableStateOf(false) }
    
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source (optionnel)",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (source == null) {
                    OutlinedButton(
                        onClick = { showSourceDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter une source")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter")
                    }
                }
            }
            
            if (source != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (source.type) {
                                    "homemade" -> "Fait maison"
                                    "url" -> "Site web"
                                    "book" -> "Livre de cuisine"
                                    else -> source.type
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            
                            TextButton(
                                onClick = { onUpdateSource(null) }
                            ) {
                                Text("Supprimer")
                            }
                        }
                        
                        if (source.valeur?.isNotBlank() == true) {
                            Text(
                                text = source.valeur,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showSourceDialog) {
        SourceSelectionDialog(
            onDismiss = { showSourceDialog = false },
            onSourceSelected = { selectedSource ->
                onUpdateSource(selectedSource)
                showSourceDialog = false
            }
        )
    }
}

@Composable
private fun SourceSelectionDialog(
    onDismiss: () -> Unit,
    onSourceSelected: (Source) -> Unit
) {
    var selectedType by remember { mutableStateOf("homemade") }
    var sourceValue by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une source") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Source type selection
                Text("Type de source :")
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedType = "homemade" },
                        label = { Text("Fait maison") },
                        selected = selectedType == "homemade"
                    )
                    FilterChip(
                        onClick = { selectedType = "url" },
                        label = { Text("Site web") },
                        selected = selectedType == "url"
                    )
                    FilterChip(
                        onClick = { selectedType = "book" },
                        label = { Text("Livre") },
                        selected = selectedType == "book"
                    )
                }
                
                // Source value input (only for url and book)
                if (selectedType != "homemade") {
                    OutlinedTextField(
                        value = sourceValue,
                        onValueChange = { sourceValue = it },
                        label = { 
                            Text(
                                if (selectedType == "url") "URL du site" 
                                else "Titre du livre"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val source = Source(
                        type = selectedType,
                        valeur = if (selectedType == "homemade") null else sourceValue.ifBlank { null }
                    )
                    onSourceSelected(source)
                },
                enabled = selectedType == "homemade" || sourceValue.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ValidationErrorsSection(
    validationErrors: ValidationErrors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Erreurs de validation :",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            validationErrors.nom?.let { error ->
                Text(
                    text = "• $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            validationErrors.ingredients?.let { error ->
                Text(
                    text = "• $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            validationErrors.etapes?.let { error ->
                Text(
                    text = "• $error",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}