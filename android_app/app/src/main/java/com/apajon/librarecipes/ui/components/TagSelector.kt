package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable component for selecting tags and categories with autocomplete.
 */
@Composable
fun TagSelector(
    label: String,
    selectedItems: List<String>,
    availableItems: List<String>,
    onSelectionChange: (List<String>) -> Unit,
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
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Selected items as chips
        if (selectedItems.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedItems) { item ->
                    FilterChip(
                        onClick = {
                            onSelectionChange(selectedItems - item)
                        },
                        label = { Text(item) },
                        selected = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Supprimer $item",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        } else {
            Text(
                text = "Aucun élément sélectionné",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Add item dialog
    if (showAddDialog) {
        AddItemDialog(
            title = "Ajouter $label",
            availableItems = availableItems.filter { it !in selectedItems },
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                if (item !in selectedItems) {
                    onSelectionChange(selectedItems + item)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddItemDialog(
    title: String,
    availableItems: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }
    
    // Filter available items based on text input
    val filteredItems = remember(text, availableItems) {
        if (text.isBlank()) availableItems
        else availableItems.filter { it.contains(text, ignoreCase = true) }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { 
                        text = it
                        isCustom = it.isNotBlank() && it !in availableItems
                    },
                    label = { Text("Rechercher ou créer") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isCustom && text.isNotBlank()) {
                    Text(
                        text = "Créer nouveau : \"$text\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Show filtered suggestions
                if (filteredItems.isNotEmpty() && !isCustom) {
                    Text(
                        text = "Suggestions :",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems.take(5)) { item ->
                            FilterChip(
                                onClick = {
                                    text = item
                                    isCustom = false
                                },
                                label = { Text(item) },
                                selected = text == item
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onAdd(text.trim())
                    }
                },
                enabled = text.isNotBlank()
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