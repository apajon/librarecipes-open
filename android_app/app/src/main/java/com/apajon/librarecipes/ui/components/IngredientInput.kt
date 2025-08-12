package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.data.model.Ingredient

/**
 * Reusable component for managing ingredient input in recipe forms.
 */
@Composable
fun IngredientInput(
    ingredients: List<Ingredient>,
    availableIngredients: List<String>,
    onAddIngredient: (Ingredient) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onUpdateIngredient: (Int, Ingredient) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingrédients",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un ingrédient")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // List of current ingredients
        ingredients.forEachIndexed { index, ingredient ->
            IngredientItem(
                ingredient = ingredient,
                onUpdate = { onUpdateIngredient(index, it) },
                onRemove = { onRemoveIngredient(index) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (ingredients.isEmpty()) {
            Text(
                text = "Aucun ingrédient ajouté",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Add ingredient dialog
    if (showAddDialog) {
        AddIngredientDialog(
            availableIngredients = availableIngredients,
            onDismiss = { showAddDialog = false },
            onAdd = { ingredient ->
                onAddIngredient(ingredient)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun IngredientItem(
    ingredient: Ingredient,
    onUpdate: (Ingredient) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ingredient.nom,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ingredient.quantite?.toString() ?: "",
                    onValueChange = { value ->
                        val quantity = value.toFloatOrNull()
                        onUpdate(ingredient.copy(quantite = quantity))
                    },
                    label = { Text("Quantité") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = ingredient.unite ?: "",
                    onValueChange = { value ->
                        onUpdate(ingredient.copy(unite = value.ifBlank { null }))
                    },
                    label = { Text("Unité") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = ingredient.indispensable,
                    onCheckedChange = { checked ->
                        onUpdate(ingredient.copy(indispensable = checked))
                    }
                )
                Text("Ingrédient indispensable")
            }
            
            if (ingredient.alternatives?.isNotBlank() == true) {
                OutlinedTextField(
                    value = ingredient.alternatives,
                    onValueChange = { value ->
                        onUpdate(ingredient.copy(alternatives = value.ifBlank { null }))
                    },
                    label = { Text("Alternatives (séparées par ;)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AddIngredientDialog(
    availableIngredients: List<String>,
    onDismiss: () -> Unit,
    onAdd: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var isEssential by remember { mutableStateOf(true) }
    var alternatives by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un ingrédient") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ingredient name with suggestions
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'ingrédient *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantité") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unité") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isEssential,
                        onCheckedChange = { isEssential = it }
                    )
                    Text("Ingrédient indispensable")
                }
                
                OutlinedTextField(
                    value = alternatives,
                    onValueChange = { alternatives = it },
                    label = { Text("Alternatives (optionnel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val ingredient = Ingredient(
                            nom = name,
                            quantite = quantity.toFloatOrNull(),
                            unite = unit.ifBlank { null },
                            indispensable = isEssential,
                            alternatives = alternatives.ifBlank { null }
                        )
                        onAdd(ingredient)
                    }
                },
                enabled = name.isNotBlank()
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