package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.apajon.librarecipes.data.model.IngredientFormItem
import com.apajon.librarecipes.data.model.RecipeDetail
import com.apajon.librarecipes.ui.components.*
import com.apajon.librarecipes.viewmodel.CreateRecipeViewModel
import com.apajon.librarecipes.viewmodel.RecipeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    navController: NavHostController,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Load recipe details when the screen is first displayed
    LaunchedEffect(recipeId) {
        viewModel.loadRecipeDetails(recipeId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.recipe?.nom ?: "Détail Recette",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                            text = "Erreur",
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
                            Text("Réessayer")
                        }
                    }
                }
                uiState.recipe != null -> {
                    RecipeDetailContent(recipe = uiState.recipe!!)
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
                title = { Text("Nouvelle Recette") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateAndSaveRecipe() },
                        enabled = !formState.isLoading
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
                    enabled = !formState.isLoading
                ) {
                    if (formState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("💾 Sauvegarder la recette")
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
                    Text("Sauvegarde en cours...")
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
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recherche") },
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
                    text = "Rechercher des recettes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "À implémenter : recherche avancée",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun RecipeDetailContent(recipe: RecipeDetail) {
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
        
        // Ingredients section
        if (recipe.ingredients.isNotEmpty()) {
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
                        Text(
                            text = "Ingrédients (${recipe.ingredients.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Étapes de préparation (${recipe.etapes.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (recipe.categories.isNotEmpty()) {
                            Text(
                                text = "Catégories",
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
                                text = "Tags",
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
                            text = "Source",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        when (source.type) {
                            "url" -> {
                                Text(
                                    text = "Site web: ${source.url ?: "URL non spécifiée"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            "book" -> {
                                Text(
                                    text = "Livre: ${source.bookTitle ?: "Titre non spécifié"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                source.bookAuthors?.let { authors ->
                                    Text(
                                        text = "Auteur(s): $authors",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                source.bookPage?.let { page ->
                                    Text(
                                        text = "Page: $page",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Recette maison",
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
                        text = "Informations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Ajoutée le: ${recipe.dateAjout}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        label = "Préparation",
                        value = "${prep} min"
                    )
                }
                
                recipe.cuisson?.let { cuisson ->
                    InfoChip(
                        label = "Cuisson",
                        value = "${cuisson} min"
                    )
                }
                
                recipe.portions?.let { portions ->
                    InfoChip(
                        label = "Portions",
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
                    text = "Alternative: $alternatives",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (!ingredient.indispensable) {
            Text(
                text = "optionnel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
    
    Divider(
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