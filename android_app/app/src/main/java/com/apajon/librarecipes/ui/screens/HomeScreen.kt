package com.apajon.librarecipes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LibraRecipes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Camera FAB - Secondary FAB for photo capture
                FloatingActionButton(
                    onClick = { 
                        // TODO: Implement camera functionality
                        // For now, just show a placeholder action
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Prendre une photo"
                    )
                }
                
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
                        contentDescription = "Ajouter une recette",
                        modifier = Modifier.size(32.dp)
                    )
                }
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
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = { navController.navigate("recipes") }
                )
            }

            item {
                ActionCard(
                    title = "Ajouter une recette",
                    description = "Créer une nouvelle recette",
                    icon = Icons.Default.Add,
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
}
