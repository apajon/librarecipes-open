package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apajon.librarecipes.R
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
import com.apajon.librarecipes.data.model.ExecutionCreate
import com.apajon.librarecipes.data.model.ExecutionWithDetails
import com.apajon.librarecipes.data.model.IngredientFormItem
import com.apajon.librarecipes.data.model.RecipeDetail
import com.apajon.librarecipes.ui.components.*
import com.apajon.librarecipes.viewmodel.CreateRecipeViewModel
import com.apajon.librarecipes.viewmodel.RecipeDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavHostController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddExecutionDialog by remember { mutableStateOf(false) }
    var selectedExecution by remember { mutableStateOf<ExecutionWithDetails?>(null) }
    var executionToEdit by remember { mutableStateOf<ExecutionWithDetails?>(null) }
    
    // Load recipe details when the screen is first displayed
    LaunchedEffect(recipeId) {
        viewModel.loadRecipeDetails(recipeId)
    }
    
    // Handle delete success
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.clearDeleteSuccess()
            navController.navigateUp()
        }
    }
    
    // Handle add/edit execution success
    LaunchedEffect(uiState.addExecutionSuccess) {
        if (uiState.addExecutionSuccess) {
            viewModel.clearAddExecutionSuccess()
            showAddExecutionDialog = false
            executionToEdit = null
        }
    }
    
    // Handle copy success - navigate to the copied recipe
    LaunchedEffect(uiState.copySuccess) {
        if (uiState.copySuccess && uiState.copiedRecipeId != null) {
            viewModel.clearCopySuccess()
            navController.navigate("recipe/${uiState.copiedRecipeId}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.recipe?.nom ?: stringResource(R.string.recipe_detail_default_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    // Edit button
                    IconButton(
                        onClick = { 
                            navController.navigate("edit_recipe/$recipeId")
                        },
                        enabled = uiState.recipe != null
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.action_edit_cd),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = uiState.recipe != null && !uiState.isDeleting
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete_cd),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
            // Copy recipe FAB
            FloatingActionButton(
                onClick = { 
                    viewModel.copyRecipe(recipeId)
                },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                if (uiState.isCopying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.recipe_detail_copy_cd)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                            text = stringResource(R.string.error_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry(recipeId) }) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                }
                uiState.recipe != null -> {
                    RecipeDetailContent(
                        recipe = uiState.recipe!!,
                        executions = uiState.executions,
                        onEdit = { navController.navigate("edit_recipe/$recipeId") },
                        onDelete = { showDeleteDialog = true },
                        onEditSection = { section -> navController.navigate("edit_recipe/$recipeId/section/$section") },
                        onAddExecution = { showAddExecutionDialog = true },
                        onExecutionClick = { execution ->
                            selectedExecution = execution
                        },
                        // Photo viewer callbacks
                        selectedPhotoIndex = uiState.selectedPhotoIndex,
                        onPhotoSelected = viewModel::selectPhoto,
                        onPreviousPhoto = viewModel::previousPhoto,
                        onNextPhoto = viewModel::nextPhoto
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.recipe_detail_delete_title)) },
            text = { 
                Text(stringResource(R.string.recipe_detail_delete_message))
            },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.deleteRecipe(recipeId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    
    // Add/Edit execution dialog
    if (showAddExecutionDialog) {
        com.apajon.librarecipes.ui.components.AddExecutionDialog(
            onDismiss = { 
                showAddExecutionDialog = false
                executionToEdit = null
            },
            onAddExecution = { executionCreate ->
                if (executionCreate.executionId != null) {
                    // Update existing execution
                    viewModel.updateExecution(executionCreate.copy(recipeId = recipeId))
                } else {
                    // Add new execution
                    viewModel.addExecution(executionCreate.copy(recipeId = recipeId))
                }
            },
            isLoading = uiState.isAddingExecution,
            availableConvives = uiState.availableConvives,
            existingExecution = executionToEdit
        )
    }
    
    // Execution detail dialog
    selectedExecution?.let { execution ->
        com.apajon.librarecipes.ui.components.ExecutionDetailDialog(
            executionWithDetails = execution,
            onDismiss = { selectedExecution = null },
            onEdit = {
                selectedExecution = null
                executionToEdit = execution
                showAddExecutionDialog = true
            },
            onDelete = {
                viewModel.deleteExecution(execution.execution.id)
                selectedExecution = null
            }
        )
    }
    
    // Add execution error display
    uiState.addExecutionError?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearAddExecutionError()
        }
    }
    
    // Delete error display
    uiState.deleteError?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar - you might want to implement a proper snackbar here
            viewModel.clearDeleteError()
        }
    }
    
    // Copy error display
    uiState.copyError?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar - you might want to implement a proper snackbar here
            viewModel.clearCopyError()
        }
    }
    
    // Loading overlay for delete operation
    if (uiState.isDeleting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.recipe_detail_deleting))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavHostController,
    viewModel: CreateRecipeViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var showAddStepDialog by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableIntStateOf(-1) }
    var editingStepIndex by remember { mutableIntStateOf(-1) }

    // Handle save success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            navController.navigateUp()
        }
    }

    // Show error snackbar
    formState.error?.let { error ->
        LaunchedEffect(error) {
            // You might want to implement a snackbar host state here
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_title_new_recipe)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateAndSaveRecipe() },
                        enabled = !formState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Basic information
            item {
                RecipeBasicInfoCard(
                    nom = formState.nom,
                    onNomChange = viewModel::updateRecipeName,
                    preparation = formState.preparation,
                    onPreparationChange = viewModel::updatePreparationTime,
                    cuisson = formState.cuisson,
                    onCuissonChange = viewModel::updateCookingTime,
                    portions = formState.portions,
                    onPortionsChange = viewModel::updatePortions,
                    categories = formState.categories,
                    onCategoriesChange = viewModel::updateCategories,
                    tags = formState.tags,
                    onTagsChange = viewModel::updateTags
                )
            }

            // Ingredients section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.edit_section_ingredients_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            FilledTonalButton(
                                onClick = {
                                    editingIngredientIndex = -1
                                    showAddIngredientDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_add))
                            }
                        }

                        if (formState.ingredients.isEmpty()) {
                            Text(
                                text = stringResource(R.string.edit_empty_ingredients),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Ingredients list
            itemsIndexed(formState.ingredients) { index, ingredient ->
                IngredientCard(
                    ingredient = ingredient,
                    index = index,
                    onEdit = { idx ->
                        editingIngredientIndex = idx
                        showAddIngredientDialog = true
                    },
                    onDelete = viewModel::removeIngredient,
                    onMoveUp = viewModel::moveIngredientUp,
                    onMoveDown = viewModel::moveIngredientDown,
                    canMoveUp = index > 0,
                    canMoveDown = index < formState.ingredients.size - 1
                )
            }

            // Steps section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.edit_section_steps_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            FilledTonalButton(
                                onClick = {
                                    editingStepIndex = -1
                                    showAddStepDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_add))
                            }
                        }

                        if (formState.steps.isEmpty()) {
                            Text(
                                text = stringResource(R.string.edit_empty_steps),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Steps list
            itemsIndexed(formState.steps) { index, step ->
                StepCard(
                    step = step,
                    index = index,
                    onEdit = { idx ->
                        editingStepIndex = idx
                        showAddStepDialog = true
                    },
                    onDelete = viewModel::removeStep,
                    onMoveUp = viewModel::moveStepUp,
                    onMoveDown = viewModel::moveStepDown,
                    canMoveUp = index > 0,
                    canMoveDown = index < formState.steps.size - 1
                )
            }

            // Source section
            item {
                SourceTypeSelector(
                    selectedType = formState.sourceType,
                    onTypeSelected = viewModel::updateSourceType
                )
            }

            // Source details
            item {
                SourceDetailsCard(
                    sourceType = formState.sourceType,
                    sourceUrl = formState.sourceUrl,
                    onSourceUrlChange = viewModel::updateSourceUrl,
                    sourceBookTitle = formState.sourceBookTitle,
                    onSourceBookTitleChange = viewModel::updateSourceBookTitle,
                    sourceBookAuthors = formState.sourceBookAuthors,
                    onSourceBookAuthorsChange = viewModel::updateSourceBookAuthors,
                    sourceBookPage = formState.sourceBookPage,
                    onSourceBookPageChange = viewModel::updateSourceBookPage
                )
            }

            // Save button
            item {
                Button(
                    onClick = { viewModel.validateAndSaveRecipe() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !formState.isLoading
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.edit_save_create))
                }
            }
        }
    }

    // Add ingredient dialog
    if (showAddIngredientDialog) {
        val initialIngredient = if (editingIngredientIndex >= 0) {
            formState.ingredients.getOrNull(editingIngredientIndex)
        } else null

        AddIngredientDialog(
            onDismiss = { showAddIngredientDialog = false },
            onAddIngredient = { ingredient ->
                if (editingIngredientIndex >= 0) {
                    viewModel.updateIngredient(editingIngredientIndex, ingredient)
                } else {
                    viewModel.addIngredient(ingredient)
                }
            },
            initialIngredient = initialIngredient,
            isEditing = editingIngredientIndex >= 0
        )
    }

    // Add step dialog
    if (showAddStepDialog) {
        val initialStep = if (editingStepIndex >= 0) {
            formState.steps.getOrNull(editingStepIndex) ?: ""
        } else ""

        AddStepDialog(
            onDismiss = { showAddStepDialog = false },
            onAddStep = { step ->
                if (editingStepIndex >= 0) {
                    viewModel.updateStep(editingStepIndex, step)
                } else {
                    viewModel.addStep(step)
                }
            },
            initialStep = initialStep,
            isEditing = editingStepIndex >= 0
        )
    }

    // Loading overlay
    if (formState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.edit_saving_create))
                }
            }
        }
    }

    // Error display
    formState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error - you might want to implement a snackbar here
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: com.apajon.librarecipes.viewmodel.SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var ingredientsText by remember { mutableStateOf("") }
    var categoriesText by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }
    var recipeToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    // Clear search button
                    if (uiState.hasSearched) {
                        IconButton(onClick = { 
                            viewModel.clearSearch()
                            ingredientsText = ""
                            categoriesText = ""
                            tagsText = ""
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.search_clear_cd))
                        }
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
                    contentDescription = stringResource(R.string.search_fab_add_recipe_cd),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Search filters card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.search_filters_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Recipe name search
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            label = { Text(stringResource(R.string.search_recipe_name_label)) },
                            placeholder = { Text(stringResource(R.string.search_recipe_name_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Ingredients search
                        OutlinedTextField(
                            value = ingredientsText,
                            onValueChange = { newValue ->
                                ingredientsText = newValue
                                viewModel.updateIngredients(newValue)
                            },
                            label = { Text(stringResource(R.string.search_ingredients_label)) },
                            placeholder = { Text(stringResource(R.string.search_ingredients_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                Text(stringResource(R.string.search_ingredients_supporting))
                            }
                        )

                        // Categories search
                        OutlinedTextField(
                            value = categoriesText,
                            onValueChange = { newValue ->
                                categoriesText = newValue
                                viewModel.updateCategories(newValue)
                            },
                            label = { Text(stringResource(R.string.search_categories_label)) },
                            placeholder = { Text(stringResource(R.string.search_categories_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Face, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                Text(stringResource(R.string.search_categories_supporting))
                            }
                        )

                        // Tags search
                        OutlinedTextField(
                            value = tagsText,
                            onValueChange = { newValue ->
                                tagsText = newValue
                                viewModel.updateTags(newValue)
                            },
                            label = { Text(stringResource(R.string.search_tags_label)) },
                            placeholder = { Text(stringResource(R.string.search_tags_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = {
                                Text(stringResource(R.string.search_tags_supporting))
                            }
                        )

                        // Search buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.performSearch() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(stringResource(R.string.search_button))
                            }
                            
                            OutlinedButton(
                                onClick = { viewModel.loadAllRecipes() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading
                            ) {
                                Text(stringResource(R.string.search_all_recipes_button))
                            }
                        }
                    }
                }
            }

            // Search results section
            if (uiState.hasSearched) {
                item {
                    Text(
                        text = if (uiState.searchResults.isNotEmpty()) {
                            stringResource(R.string.search_results_count, uiState.searchResults.size)
                        } else if (uiState.isLoading) {
                            stringResource(R.string.search_in_progress)
                        } else if (uiState.errorMessage != null) {
                            stringResource(R.string.search_error)
                        } else {
                            stringResource(R.string.search_no_results)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.clearError() }) {
                                    Text(stringResource(R.string.action_ok))
                                }
                            }
                        }
                    }
                }

                // Loading indicator
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Empty state
                if (!uiState.isLoading && uiState.errorMessage == null && uiState.searchResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.search_no_recipe_found),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.search_no_recipe_suggestion),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Search results list
            items(uiState.searchResults.size) { index ->
                val recipe = uiState.searchResults[index]
                com.apajon.librarecipes.ui.components.RecipeListItem(
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

@Composable
fun RecipeDetailContent(
    recipe: RecipeDetail,
    executions: List<ExecutionWithDetails> = emptyList(),
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEditSection: (String) -> Unit = {},
    onAddExecution: () -> Unit = {},
    onExecutionClick: (ExecutionWithDetails) -> Unit = {},
    // Photo viewer parameters
    selectedPhotoIndex: Int = 0,
    onPhotoSelected: (Int) -> Unit = {},
    onPreviousPhoto: () -> Unit = {},
    onNextPhoto: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Recipe basic information
        item {
            RecipeBasicInfoDisplay(recipe = recipe)
        }
        
        // Photos section - positioned as second card
        item {
            PhotoViewerCard(
                photos = recipe.photos,
                selectedPhotoIndex = selectedPhotoIndex,
                onPhotoSelected = onPhotoSelected,
                onPreviousPhoto = onPreviousPhoto,
                onNextPhoto = onNextPhoto
            )
        }
        
        // Ingredients section
        if (recipe.ingredients.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditSection("ingredients") },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recipe_ingredients_count, recipe.ingredients.size),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.recipe_ingredients_edit_cd),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        recipe.ingredients.forEach { ingredient ->
                            IngredientDisplayItem(ingredient = ingredient)
                        }
                    }
                }
            }
        }
        
        // Steps section
        if (recipe.etapes.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditSection("steps") },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recipe_steps_count, recipe.etapes.size),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.recipe_steps_edit_cd),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        recipe.etapes.forEach { etape ->
                            EtapeDisplayItem(etape = etape)
                        }
                    }
                }
            }
        }
        
        // Categories and tags section
        if (recipe.categories.isNotEmpty() || recipe.tags.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditSection("categories") },
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                            text = stringResource(R.string.recipe_section_categories_tags),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(R.string.recipe_categories_tags_edit_cd),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (recipe.categories.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.recipe_categories_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recipe.categories.size) { index ->
                                    FilterChip(
                                        onClick = { },
                                        label = { Text(recipe.categories[index]) },
                                        selected = false
                                    )
                                }
                            }
                        }
                        
                        if (recipe.tags.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.recipe_tags_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recipe.tags.size) { index ->
                                    FilterChip(
                                        onClick = { },
                                        label = { Text(recipe.tags[index]) },
                                        selected = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Source section
        recipe.source?.let { source ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.recipe_source_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        when (source.type) {
                            "url" -> {
                                Text(
                                    text = stringResource(R.string.recipe_source_web, source.url ?: stringResource(R.string.recipe_source_web_unknown)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            "book" -> {
                                Text(
                                    text = stringResource(R.string.recipe_source_book, source.bookTitle ?: stringResource(R.string.recipe_source_book_unknown)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                source.bookAuthors?.let { authors ->
                                    Text(
                                        text = stringResource(R.string.recipe_source_authors, authors),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                source.bookPage?.let { page ->
                                    Text(
                                        text = stringResource(R.string.recipe_source_page, page),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = stringResource(R.string.recipe_source_homemade),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Additional info section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recipe_info_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = stringResource(R.string.recipe_date_added, recipe.dateAjout),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Execution section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.recipe_realizations_count, executions.size),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = onAddExecution
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.recipe_add_realization_cd),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (executions.isEmpty()) {
                        Text(
                            text = stringResource(R.string.recipe_no_realizations),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        executions.forEach { execution ->
                            com.apajon.librarecipes.ui.components.ExecutionWithDetailsItem(
                                executionWithDetails = execution,
                                onClick = { onExecutionClick(execution) }
                            )
                        }
                    }
                }
            }
        }
        
        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_edit))
                }
                
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_delete))
                }
            }
        }
    }
}

@Composable
fun RecipeBasicInfoDisplay(recipe: RecipeDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = recipe.nom,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                recipe.preparation?.let { prep ->
                    InfoChip(
                        label = stringResource(R.string.recipe_info_preparation),
                        value = "${prep} min"
                    )
                }
                
                recipe.cuisson?.let { cuisson ->
                    InfoChip(
                        label = stringResource(R.string.recipe_info_cooking),
                        value = "${cuisson} min"
                    )
                }
                
                recipe.portions?.let { portions ->
                    InfoChip(
                        label = stringResource(R.string.recipe_info_portions),
                        value = "$portions"
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun IngredientDisplayItem(ingredient: com.apajon.librarecipes.data.model.IngredientDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ingredient.nom,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (ingredient.indispensable) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                
                val quantityText = buildString {
                    ingredient.quantite?.let { quantite ->
                        append(quantite.toString())
                        ingredient.unite?.takeIf { it.isNotBlank() }?.let { unite ->
                            append(" $unite")
                        }
                    }
                }
                
                if (quantityText.isNotBlank()) {
                    Text(
                        text = quantityText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            ingredient.alternatives?.takeIf { it.isNotBlank() }?.let { alternatives ->
                Text(
                    text = stringResource(R.string.recipe_alternative, alternatives),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (!ingredient.indispensable) {
            Text(
                text = stringResource(R.string.ingredient_optional_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}

@Composable
fun EtapeDisplayItem(etape: com.apajon.librarecipes.data.model.EtapeDetail) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.size(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = etape.numero.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = etape.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueChoisirScreen(
    navController: NavHostController,
    viewModel: com.apajon.librarecipes.viewmodel.SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by remember { mutableStateOf("suggestions") }
    
    // Load random recipes when screen first loads
    LaunchedEffect(Unit) {
        viewModel.loadRandomRecipes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.que_choisir_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
            // Primary Add Recipe FAB
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Welcome card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.que_choisir_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.que_choisir_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Filter tabs
            item {
                val filterLabels = listOf(
                    "suggestions" to stringResource(R.string.que_choisir_filter_suggestions),
                    "popular" to stringResource(R.string.que_choisir_filter_popular), 
                    "random" to stringResource(R.string.que_choisir_filter_random)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                        filterLabels
                    ) { (key, label) ->
                        FilterChip(
                            onClick = { 
                                selectedFilter = key
                                when (key) {
                                    "suggestions" -> viewModel.loadRandomRecipes()
                                    "popular" -> viewModel.loadAllRecipes()
                                    "random" -> viewModel.loadRandomRecipes()
                                }
                            },
                            label = { Text(label) },
                            selected = selectedFilter == key
                        )
                    }
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.loadRandomRecipes() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.que_choisir_renew))
                    }
                    
                    OutlinedButton(
                        onClick = { navController.navigate("search") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.que_choisir_search))
                    }
                }
            }

            // Content section
            when (selectedFilter) {
                "suggestions" -> {
                    item {
                        Text(
                            text = stringResource(R.string.que_choisir_suggestions_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                "popular" -> {
                    item {
                        Text(
                            text = stringResource(R.string.que_choisir_popular_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                "random" -> {
                    item {
                        Text(
                            text = stringResource(R.string.que_choisir_random_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Error message
            uiState.errorMessage?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Empty state
            if (!uiState.isLoading && uiState.errorMessage == null && uiState.searchResults.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.que_choisir_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.que_choisir_empty_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("create_recipe") }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.que_choisir_add_recipe))
                            }
                        }
                    }
                }
            }

            // Recipe results list
            items(uiState.searchResults.size) { index ->
                val recipe = uiState.searchResults[index]
                com.apajon.librarecipes.ui.components.RecipeListItem(
                    recipe = recipe,
                    onClick = {
                        navController.navigate("recipe/${recipe.id}")
                    },
                    onEdit = {
                        navController.navigate("edit_recipe/${recipe.id}")
                    },
                    onDelete = {
                        // For this screen, we don't allow deleting
                        // User can go to recipe detail to delete if needed
                    }
                )
            }

            // Tips section (if no recipes to show)
            if (!uiState.isLoading && uiState.errorMessage == null && uiState.searchResults.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.que_choisir_tip_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedFilter) {
                                    "suggestions" -> stringResource(R.string.que_choisir_tip_suggestions)
                                    "popular" -> stringResource(R.string.que_choisir_tip_popular)
                                    "random" -> stringResource(R.string.que_choisir_tip_random)
                                    else -> stringResource(R.string.que_choisir_tip_default)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}