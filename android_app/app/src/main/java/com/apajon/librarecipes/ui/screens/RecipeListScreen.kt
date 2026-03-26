package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apajon.librarecipes.R
import com.apajon.librarecipes.ui.components.RecipeListItem
import com.apajon.librarecipes.viewmodel.RecipeListViewModel
import com.apajon.librarecipes.viewmodel.FilterMode
import com.apajon.librarecipes.viewmodel.DatePeriod
import com.apajon.librarecipes.viewmodel.ExecutionPeriod
import com.apajon.librarecipes.viewmodel.ConvivesPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    navController: NavHostController,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var recipeToDelete by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_list_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    // Sort toggle button
                    TextButton(
                        onClick = { viewModel.toggleSort() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        val sortText = when (uiState.filterMode) {
                            FilterMode.ALPHABETICAL -> if (uiState.sortAscending) "A-Z" else "Z-A"
                            FilterMode.DATE, FilterMode.EXECUTION -> if (uiState.sortAscending) stringResource(R.string.sort_recent) else stringResource(R.string.sort_old)
                            FilterMode.CONVIVES -> if (uiState.sortAscending) "1-N" else "N-1"
                            FilterMode.INGREDIENT -> if (uiState.sortAscending) "A-Z" else "Z-A"
                        }
                        Text(sortText)
                    }
                    IconButton(onClick = { viewModel.refreshRecipes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.recipe_list_refresh_cd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // Primary Add Recipe FAB only
            FloatingActionButton(
                onClick = { 
                    navController.navigate("create_new_recipe")
                },
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_fab_add_recipe_cd),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter mode tabs
            ScrollableTabRow(
                selectedTabIndex = when (uiState.filterMode) {
                    FilterMode.ALPHABETICAL -> 0
                    FilterMode.INGREDIENT -> 1
                    FilterMode.DATE -> 2
                    FilterMode.EXECUTION -> 3
                    FilterMode.CONVIVES -> 4
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.filterMode == FilterMode.ALPHABETICAL,
                    onClick = { viewModel.setFilterMode(FilterMode.ALPHABETICAL) },
                    text = { Text(stringResource(R.string.recipe_list_tab_alphabetical)) }
                )
                Tab(
                    selected = uiState.filterMode == FilterMode.INGREDIENT,
                    onClick = { viewModel.setFilterMode(FilterMode.INGREDIENT) },
                    text = { Text(stringResource(R.string.recipe_list_tab_ingredient)) }
                )
                Tab(
                    selected = uiState.filterMode == FilterMode.DATE,
                    onClick = { viewModel.setFilterMode(FilterMode.DATE) },
                    text = { Text(stringResource(R.string.recipe_list_tab_date)) }
                )
                Tab(
                    selected = uiState.filterMode == FilterMode.EXECUTION,
                    onClick = { viewModel.setFilterMode(FilterMode.EXECUTION) },
                    text = { Text(stringResource(R.string.recipe_list_tab_realization)) }
                )
                Tab(
                    selected = uiState.filterMode == FilterMode.CONVIVES,
                    onClick = { viewModel.setFilterMode(FilterMode.CONVIVES) },
                    text = { Text(stringResource(R.string.recipe_list_tab_convives)) }
                )
            }
            
            // Filter buttons based on mode
            when (uiState.filterMode) {
                FilterMode.ALPHABETICAL -> {
                    // Letter filter buttons
                    if (uiState.availableLetters.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // "All" button
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByLetter(null) },
                                    label = { Text(stringResource(R.string.recipe_list_filter_all)) },
                                    selected = uiState.selectedLetter == null,
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                            
                            // Letter buttons
                            items(uiState.availableLetters) { letter ->
                                FilterChip(
                                    onClick = { viewModel.filterByLetter(letter) },
                                    label = { Text(letter) },
                                    selected = uiState.selectedLetter == letter,
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                    }
                }
                FilterMode.DATE -> {
                    // Date period filter buttons
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(DatePeriod.values()) { period ->
                            FilterChip(
                                onClick = { viewModel.setDatePeriod(period) },
                                label = { Text(period.displayName) },
                                selected = uiState.selectedDatePeriod == period,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
                FilterMode.EXECUTION -> {
                    // Execution period filter buttons
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ExecutionPeriod.values()) { period ->
                            FilterChip(
                                onClick = { viewModel.setExecutionPeriod(period) },
                                label = { Text(period.displayName) },
                                selected = uiState.selectedExecutionPeriod == period,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
                FilterMode.CONVIVES -> {
                    // Show convive letter filter when no specific convive is selected
                    if (uiState.selectedConvive == null && uiState.availableConvivesFromExecutions.isNotEmpty()) {
                        // Get available convive letters
                        val availableConviveLetters = uiState.availableConvivesFromExecutions
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .distinct()
                            .sorted()
                        
                        if (availableConviveLetters.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "All" button
                                item {
                                    FilterChip(
                                        onClick = { viewModel.filterByConviveLetter(null) },
                                        label = { Text(stringResource(R.string.recipe_list_filter_all)) },
                                        selected = uiState.selectedConviveLetter == null,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                                
                                // Letter buttons
                                items(availableConviveLetters) { letter ->
                                    FilterChip(
                                        onClick = { viewModel.filterByConviveLetter(letter) },
                                        label = { Text(letter) },
                                        selected = uiState.selectedConviveLetter == letter,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                            }
                        }
                    } else if (uiState.selectedConvive != null) {
                        // Show "Back to convives" button when convive is selected
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByConvive(null) },
                                    label = { Text(stringResource(R.string.recipe_list_back_convives)) },
                                    selected = false,
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                    }
                }
                FilterMode.INGREDIENT -> {
                    // Show ingredient letter filter when no specific ingredient is selected
                    if (uiState.selectedIngredient == null && uiState.availableIngredients.isNotEmpty()) {
                        // Get available ingredient letters
                        val availableIngredientLetters = uiState.availableIngredients
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .distinct()
                            .sorted()
                        
                        if (availableIngredientLetters.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "All" button
                                item {
                                    FilterChip(
                                        onClick = { viewModel.filterByIngredientLetter(null) },
                                        label = { Text(stringResource(R.string.recipe_list_filter_all)) },
                                        selected = uiState.selectedIngredientLetter == null,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                                
                                // Letter buttons
                                items(availableIngredientLetters) { letter ->
                                    FilterChip(
                                        onClick = { viewModel.filterByIngredientLetter(letter) },
                                        label = { Text(letter) },
                                        selected = uiState.selectedIngredientLetter == letter,
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                            }
                        }
                    } else if (uiState.selectedIngredient != null) {
                        // Show "Back to ingredients" button when ingredient is selected
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByIngredient(null) },
                                    label = { Text(stringResource(R.string.recipe_list_back_ingredients)) },
                                    selected = false,
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.recipe_list_error_connection),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refreshRecipes() }) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                    uiState.recipeSections.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (uiState.filterMode) {
                                FilterMode.ALPHABETICAL -> {
                                    if (uiState.selectedLetter != null) {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe_for_letter, uiState.selectedLetter ?: ""),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.filterByLetter(null) }) {
                                            Text(stringResource(R.string.recipe_list_see_all))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.recipe_list_add_first),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilterMode.DATE -> {
                                    if (uiState.selectedDatePeriod != DatePeriod.ALL) {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe_period),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.setDatePeriod(DatePeriod.ALL) }) {
                                            Text(stringResource(R.string.recipe_list_see_all))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.recipe_list_add_first),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilterMode.EXECUTION -> {
                                    if (uiState.selectedExecutionPeriod != ExecutionPeriod.ALL) {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe_period),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.setExecutionPeriod(ExecutionPeriod.ALL) }) {
                                            Text(stringResource(R.string.recipe_list_see_all))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.recipe_list_add_first),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilterMode.CONVIVES -> {
                                    if (uiState.selectedConvive != null) {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_convive_for, uiState.selectedConvive ?: ""),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.filterByConvive(null) }) {
                                            Text(stringResource(R.string.recipe_list_back_to_convives))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_convive),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_realization_convives),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                FilterMode.INGREDIENT -> {
                                    if (uiState.selectedIngredient != null) {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_recipe_ingredient, uiState.selectedIngredient ?: ""),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(onClick = { viewModel.filterByIngredient(null) }) {
                                            Text(stringResource(R.string.recipe_list_back_to_ingredients))
                                        }
                                    } else {
                                        Text(
                                            text = stringResource(R.string.recipe_list_no_ingredient),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.recipe_list_add_with_ingredients),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.recipeSections.forEach { section ->
                                // Section header (Letter for alphabetical, Date range for date mode)
                                item(key = "header_${section.letter}") {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (uiState.filterMode) {
                                                FilterMode.ALPHABETICAL -> MaterialTheme.colorScheme.primaryContainer
                                                FilterMode.INGREDIENT -> MaterialTheme.colorScheme.secondaryContainer
                                                FilterMode.DATE -> MaterialTheme.colorScheme.tertiaryContainer
                                                FilterMode.EXECUTION -> MaterialTheme.colorScheme.surfaceVariant
                                                FilterMode.CONVIVES -> MaterialTheme.colorScheme.errorContainer
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = section.letter,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = when (uiState.filterMode) {
                                                    FilterMode.ALPHABETICAL -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    FilterMode.INGREDIENT -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    FilterMode.DATE -> MaterialTheme.colorScheme.onTertiaryContainer
                                                    FilterMode.EXECUTION -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    FilterMode.CONVIVES -> MaterialTheme.colorScheme.onErrorContainer
                                                }
                                            )
                                            Text(
                                                text = if (section.isIngredientSection) {
                                                    when (uiState.filterMode) {
                                                        FilterMode.INGREDIENT -> "${section.ingredients.size} ingrédient${if (section.ingredients.size > 1) "s" else ""}"
                                                        FilterMode.CONVIVES -> "${section.ingredients.size} convive${if (section.ingredients.size > 1) "s" else ""}"
                                                        else -> "${section.ingredients.size} élément${if (section.ingredients.size > 1) "s" else ""}"
                                                    }
                                                } else {
                                                    "${section.recipes.size} recette${if (section.recipes.size > 1) "s" else ""}"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = when (uiState.filterMode) {
                                                    FilterMode.ALPHABETICAL -> MaterialTheme.colorScheme.onPrimaryContainer
                                                    FilterMode.INGREDIENT -> MaterialTheme.colorScheme.onSecondaryContainer
                                                    FilterMode.DATE -> MaterialTheme.colorScheme.onTertiaryContainer
                                                    FilterMode.EXECUTION -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    FilterMode.CONVIVES -> MaterialTheme.colorScheme.onErrorContainer
                                                }
                                            )
                                        }
                                    }
                                }
                                
                                // Show ingredients/convives for ingredient/convive sections
                                if (section.isIngredientSection) {
                                    items(
                                        items = section.ingredients,
                                        key = { item -> when (uiState.filterMode) {
                                            FilterMode.INGREDIENT -> "ingredient_$item"
                                            FilterMode.CONVIVES -> "convive_$item"
                                            else -> "item_$item"
                                        }}
                                    ) { item ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { 
                                                    when (uiState.filterMode) {
                                                        FilterMode.INGREDIENT -> viewModel.filterByIngredient(item)
                                                        FilterMode.CONVIVES -> viewModel.filterByConvive(item)
                                                        else -> {}
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = item,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Show recipes
                                items(
                                    items = section.recipes,
                                    key = { recipe -> recipe.id }
                                ) { recipe ->
                                    RecipeListItem(
                                        recipe = recipe,
                                        onClick = { 
                                            navController.navigate("recipe/${recipe.id}")
                                        },
                                        onEdit = {
                                            navController.navigate("edit_recipe/${recipe.id}")
                                        },
                                        onDelete = {
                                            recipeToDelete = recipe.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    recipeToDelete?.let { recipeId ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text(stringResource(R.string.recipe_detail_delete_title)) },
            text = { 
                Text(stringResource(R.string.recipe_detail_delete_message))
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.deleteRecipe(recipeId)
                        recipeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}