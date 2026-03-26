package com.apajon.librarecipes.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apajon.librarecipes.ui.components.ActionCard
import com.apajon.librarecipes.ui.icon.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    useSystemDarkTheme: Boolean = true,
    forceDarkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    onUseSystemDarkThemeChange: (Boolean) -> Unit = {},
    onForceDarkThemeChange: (Boolean) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LibraRecipes") },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres du thème"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
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
                    imageVector = AppIcons.Add,
                    contentDescription = "Ajouter une recette",
                    modifier = Modifier.size(32.dp)
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Bienvenue dans LibraRecipes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Le carnet de cuisine que vous ne perdrez jamais.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Actions principales",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                ActionCard(
                    title = "Voir mes recettes",
                    description = "Parcourir toutes vos recettes enregistrées",
                    icon = AppIcons.List,
                    onClick = { navController.navigate("recipes") }
                )
            }

            item {
                ActionCard(
                    title = "Ajouter une recette",
                    description = "Créer une nouvelle recette",
                    icon = AppIcons.Add,
                    onClick = { navController.navigate("create_recipe") }
                )
            }

            item {
                ActionCard(
                    title = "Rechercher",
                    description = "Rechercher des recettes par nom, ingrédients ou tags",
                    icon = Icons.Default.Search,
                    onClick = { navController.navigate("search") }
                )
            }

            item {
                ActionCard(
                    title = "Que choisir ?",
                    description = "Suggestions de recettes et inspiration culinaire",
                    icon = Icons.Default.Star,
                    onClick = { navController.navigate("que_choisir") }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSettingsDialog(
            useSystemDarkTheme = useSystemDarkTheme,
            forceDarkTheme = forceDarkTheme,
            dynamicColor = dynamicColor,
            onUseSystemDarkThemeChange = onUseSystemDarkThemeChange,
            onForceDarkThemeChange = onForceDarkThemeChange,
            onDynamicColorChange = onDynamicColorChange,
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun ThemeSettingsDialog(
    useSystemDarkTheme: Boolean,
    forceDarkTheme: Boolean,
    dynamicColor: Boolean,
    onUseSystemDarkThemeChange: (Boolean) -> Unit,
    onForceDarkThemeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
        },
        title = {
            Text("Thème")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Dark theme section
                Text(
                    text = "Mode sombre",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Suivre le système",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = useSystemDarkTheme,
                        onCheckedChange = onUseSystemDarkThemeChange
                    )
                }
                if (!useSystemDarkTheme) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mode sombre",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = forceDarkTheme,
                            onCheckedChange = onForceDarkThemeChange
                        )
                    }
                }

                HorizontalDivider()

                // Dynamic colors section
                Text(
                    text = "Couleurs",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Couleurs dynamiques",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                "Adapter les couleurs au fond d'écran"
                            else
                                "Nécessite Android 12+",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
