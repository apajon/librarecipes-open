package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Reusable component for managing recipe steps input.
 */
@Composable
fun StepsInput(
    steps: List<String>,
    onAddStep: (String) -> Unit,
    onRemoveStep: (Int) -> Unit,
    onUpdateStep: (Int, String) -> Unit,
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
                text = "Étapes de préparation",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une étape")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // List of current steps
        steps.forEachIndexed { index, step ->
            StepItem(
                stepNumber = index + 1,
                stepDescription = step,
                onUpdate = { onUpdateStep(index, it) },
                onRemove = { onRemoveStep(index) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (steps.isEmpty()) {
            Text(
                text = "Aucune étape ajoutée",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Add step dialog
    if (showAddDialog) {
        AddStepDialog(
            stepNumber = steps.size + 1,
            onDismiss = { showAddDialog = false },
            onAdd = { step ->
                onAddStep(step)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    stepDescription: String,
    onUpdate: (String) -> Unit,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Étape $stepNumber",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = stepDescription,
                onValueChange = onUpdate,
                label = { Text("Description de l'étape") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )
        }
    }
}

@Composable
private fun AddStepDialog(
    stepNumber: Int,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter l'étape $stepNumber") },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description de l'étape") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onAdd(description.trim())
                    }
                },
                enabled = description.isNotBlank()
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