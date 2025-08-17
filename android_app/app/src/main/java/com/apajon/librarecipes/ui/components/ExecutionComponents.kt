package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExecutionDialog(
    onDismiss: () -> Unit,
    onAddExecution: (ExecutionCreate) -> Unit,
    isLoading: Boolean = false,
    availableConvives: List<ConviveEntity> = emptyList()
) {
    var selectedConvives by remember { mutableStateOf<List<ConviveWithFeedback>>(emptyList()) }
    var showAddConviveDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une exécution") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Enregistrer que vous avez préparé cette recette avec les convives.")
                
                // Convives section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Convives (${selectedConvives.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { showAddConviveDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Ajouter un convive")
                    }
                }
                
                if (selectedConvives.isEmpty()) {
                    Text(
                        text = "Aucun convive ajouté. Ajoutez au moins un convive pour continuer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    selectedConvives.forEachIndexed { index, conviveWithFeedback ->
                        ConviveWithFeedbackItem(
                            conviveWithFeedback = conviveWithFeedback,
                            onRemove = { 
                                selectedConvives = selectedConvives.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            onFeedbackChange = { newFeedback ->
                                selectedConvives = selectedConvives.toMutableList().apply {
                                    set(index, conviveWithFeedback.copy(feedback = newFeedback))
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddExecution(ExecutionCreate(
                        recipeId = "", // Will be set by the caller
                        convivesWithFeedback = selectedConvives
                    ))
                },
                enabled = !isLoading && selectedConvives.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Annuler")
            }
        }
    )
    
    if (showAddConviveDialog) {
        AddConviveToExecutionDialog(
            onDismiss = { showAddConviveDialog = false },
            onAddConvive = { conviveWithFeedback ->
                selectedConvives = selectedConvives + conviveWithFeedback
                showAddConviveDialog = false
            },
            availableConvives = availableConvives,
            alreadySelectedConvives = selectedConvives.map { it.convive.id }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConviveToExecutionDialog(
    onDismiss: () -> Unit,
    onAddConvive: (ConviveWithFeedback) -> Unit,
    availableConvives: List<ConviveEntity>,
    alreadySelectedConvives: List<String> = emptyList()
) {
    var selectedMode by remember { mutableStateOf(ConviveSelectionMode.EXISTING) }
    var selectedConvive by remember { mutableStateOf<ConviveEntity?>(null) }
    var selectedFeedback by remember { mutableStateOf(FeedbackStatus.AIME) }
    var newConviveForm by remember { mutableStateOf(ConviveForm()) }
    var showNewConviveDialog by remember { mutableStateOf(false) }
    
    val filteredConvives = availableConvives.filter { it.id !in alreadySelectedConvives }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un convive") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selection mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == ConviveSelectionMode.EXISTING,
                        onClick = { selectedMode = ConviveSelectionMode.EXISTING },
                        label = { Text("Existant") }
                    )
                    FilterChip(
                        selected = selectedMode == ConviveSelectionMode.NEW,
                        onClick = { selectedMode = ConviveSelectionMode.NEW },
                        label = { Text("Nouveau") }
                    )
                }
                
                when (selectedMode) {
                    ConviveSelectionMode.EXISTING -> {
                        if (filteredConvives.isNotEmpty()) {
                            Text("Sélectionner un convive :")
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredConvives) { convive ->
                                    ConviveSelectionItem(
                                        convive = convive,
                                        isSelected = selectedConvive?.id == convive.id,
                                        onClick = { selectedConvive = convive }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Aucun convive disponible. Créez-en un nouveau.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            selectedMode = ConviveSelectionMode.NEW
                        }
                    }
                    ConviveSelectionMode.NEW -> {
                        Button(
                            onClick = { showNewConviveDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Créer un nouveau convive")
                        }
                        
                        newConviveForm.takeIf { it.isValid() }?.let { form ->
                            selectedConvive = form.toEntity(UUID.randomUUID().toString())
                        }
                    }
                }
                
                // Feedback selection
                if (selectedConvive != null || newConviveForm.isValid()) {
                    Text("Comment cette personne a-t-elle apprécié ?")
                    FeedbackStatusSelector(
                        selectedFeedback = selectedFeedback,
                        onFeedbackChange = { selectedFeedback = it }
                    )
                }
            }
        },
        confirmButton = {
            val canAdd = selectedConvive != null || 
                        (selectedMode == ConviveSelectionMode.NEW && newConviveForm.isValid())
            
            Button(
                onClick = {
                    val convive = selectedConvive ?: newConviveForm.toEntity(UUID.randomUUID().toString())
                    onAddConvive(ConviveWithFeedback(convive, selectedFeedback))
                },
                enabled = canAdd
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
    
    if (showNewConviveDialog) {
        NewConviveDialog(
            onDismiss = { showNewConviveDialog = false },
            onCreateConvive = { form ->
                newConviveForm = form
                showNewConviveDialog = false
            },
            availableConvives = availableConvives
        )
    }
}

@Composable
fun ConviveSelectionItem(
    convive: ConviveEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayOption = ConviveOption.fromConvive(convive)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayOption.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FeedbackStatusSelector(
    selectedFeedback: FeedbackStatus,
    onFeedbackChange: (FeedbackStatus) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FeedbackStatus.values().forEach { status ->
            FilterChip(
                selected = selectedFeedback == status,
                onClick = { onFeedbackChange(status) },
                label = { Text(status.displayName) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ConviveWithFeedbackItem(
    conviveWithFeedback: ConviveWithFeedback,
    onRemove: () -> Unit,
    onFeedbackChange: (FeedbackStatus) -> Unit
) {
    val displayOption = ConviveOption.fromConvive(conviveWithFeedback.convive)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayOption.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onRemove
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            FeedbackStatusSelector(
                selectedFeedback = conviveWithFeedback.feedback,
                onFeedbackChange = onFeedbackChange
            )
        }
    }
}

@Composable
fun ExecutionWithDetailsItem(executionWithDetails: ExecutionWithDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatExecutionDate(executionWithDetails.execution.dateExecution),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${executionWithDetails.nombreConvives} convive${if (executionWithDetails.nombreConvives > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (executionWithDetails.convivesWithFeedback.isNotEmpty()) {
                Text(
                    text = "Convives :",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                
                executionWithDetails.convivesWithFeedback.forEach { conviveWithFeedback ->
                    val displayOption = ConviveOption.fromConvive(conviveWithFeedback.convive)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayOption.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = conviveWithFeedback.feedback.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (conviveWithFeedback.feedback) {
                                FeedbackStatus.AIME -> MaterialTheme.colorScheme.primary
                                FeedbackStatus.PARTIELLEMENT -> MaterialTheme.colorScheme.tertiary
                                FeedbackStatus.RIEN_MANGE -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format execution date string for display
 */
private fun formatExecutionDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

enum class ConviveSelectionMode {
    EXISTING, NEW
}