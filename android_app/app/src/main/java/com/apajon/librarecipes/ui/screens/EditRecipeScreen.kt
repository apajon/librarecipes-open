package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apajon.librarecipes.data.model.IngredientFormItem
import com.apajon.librarecipes.ui.components.*
import com.apajon.librarecipes.viewmodel.EditRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: String?,
    editSection: String? = null,
    navController: NavHostController,
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    var showAddStepDialog by remember { mutableStateOf(false) }
    var editingIngredientIndex by remember { mutableIntStateOf(-1) }
    var editingStepIndex by remember { mutableIntStateOf(-1) }
    
    val listState = rememberLazyListState()

    // Initialize the form based on mode
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipeForEdit(recipeId)
        } else {
            viewModel.initializeForCreate()
        }
    }
    
    // Handle section-specific editing - scroll to the appropriate section
    LaunchedEffect(editSection) {
        if (editSection != null && !isLoading) {
            val sectionIndex = when (editSection) {
                "ingredients" -> 1 // Basic info (0) + Ingredients section (1)
                "steps" -> formState.ingredients.size + 2 // Basic info + Ingredients section + ingredients list + Steps section
                "categories" -> 0 // Basic info includes categories and tags
                else -> 0
            }
            if (sectionIndex >= 0) {
                listState.animateScrollToItem(sectionIndex)
            }
        }
    }

    // Handle save success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            navController.navigateUp()
        }
    }

    // Show error snackbar
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            // You might want to implement a snackbar host state here
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when {
                            editSection != null -> {
                                val sectionName = when (editSection) {
                                    "ingredients" -> "Ingrédients"
                                    "steps" -> "Étapes"
                                    "categories" -> "Catégories & Tags"
                                    else -> "Modifier Recette"
                                }
                                "Modifier: $sectionName"
                            }
                            viewModel.isEditMode() -> "Modifier Recette"
                            else -> "Nouvelle Recette"
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateAndSaveRecipe() },
                        enabled = !formState.isLoading && !isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Sauvegarder")
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
            // Camera FAB for adding photos to the recipe
            FloatingActionButton(
                onClick = { 
                    // TODO: Implement camera functionality to add photos to current recipe
                    // This would integrate with the existing photo management system
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Ajouter une photo à la recette"
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Chargement de la recette...")
                }
            }
        } else {
            LazyColumn(
                state = listState,
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
                                    text = "Ingrédients",
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
                                    Text("Ajouter")
                                }
                            }

                            if (formState.ingredients.isEmpty()) {
                                Text(
                                    text = "Aucun ingrédient ajouté",
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
                                    text = "Étapes de préparation",
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
                                    Text("Ajouter")
                                }
                            }

                            if (formState.steps.isEmpty()) {
                                Text(
                                    text = "Aucune étape ajoutée",
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
                        enabled = !formState.isLoading && !isLoading
                    ) {
                        if (formState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (viewModel.isEditMode()) "💾 Mettre à jour la recette" else "💾 Sauvegarder la recette"
                        )
                    }
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
                    Text(
                        if (viewModel.isEditMode()) "Mise à jour en cours..." else "Sauvegarde en cours..."
                    )
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