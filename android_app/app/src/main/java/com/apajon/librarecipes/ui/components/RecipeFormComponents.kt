package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.data.model.IngredientFormItem
import com.apajon.librarecipes.data.model.MeasurementUnits
import com.apajon.librarecipes.data.model.SourceType
import com.apajon.librarecipes.ui.icon.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBasicInfoCard(
    nom: String,
    onNomChange: (String) -> Unit,
    preparation: String,
    onPreparationChange: (String) -> Unit,
    cuisson: String,
    onCuissonChange: (String) -> Unit,
    portions: String,
    onPortionsChange: (String) -> Unit,
    categories: String,
    onCategoriesChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informations de base",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nom,
                onValueChange = onNomChange,
                label = { Text("Nom de la recette *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = preparation,
                    onValueChange = onPreparationChange,
                    label = { Text("Préparation (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cuisson,
                    onValueChange = onCuissonChange,
                    label = { Text("Cuisson (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Portions with +/- buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Portions:", style = MaterialTheme.typography.bodyLarge)
                
                IconButton(
                    onClick = {
                        val current = portions.toIntOrNull() ?: 1
                        if (current > 1) onPortionsChange((current - 1).toString())
                    }
                ) {
                    Icon(AppIcons.ArrowDown, contentDescription = "Diminuer")
                }
                
                OutlinedTextField(
                    value = portions,
                    onValueChange = onPortionsChange,
                    modifier = Modifier.width(80.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                IconButton(
                    onClick = {
                        val current = portions.toIntOrNull() ?: 1
                        if (current < 99) onPortionsChange((current + 1).toString())
                    }
                ) {
                    Icon(AppIcons.Add, contentDescription = "Augmenter")
                }
            }

            OutlinedTextField(
                value = categories,
                onValueChange = onCategoriesChange,
                label = { Text("Catégories (séparées par des virgules)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: plat principal, végétarien") }
            )

            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text("Tags (séparés par des virgules)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: facile, rapide, économique") }
            )
        }
    }
}

@Composable
fun IngredientCard(
    ingredient: IngredientFormItem,
    index: Int,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onEdit(index) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (ingredient.quantite.isNotBlank() || ingredient.unite.isNotBlank()) {
                    Text(
                        text = "${ingredient.quantite} ${ingredient.unite}".trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = if (ingredient.indispensable) "Essentiel" else "Optionnel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!ingredient.indispensable && ingredient.alternatives.isNotBlank()) {
                    Text(
                        text = "Alt: ${ingredient.alternatives}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column {
                IconButton(
                    onClick = { onMoveUp(index) },
                    enabled = canMoveUp
                ) {
                    Icon(
                        AppIcons.ArrowUp,
                        contentDescription = "Déplacer vers le haut",
                        tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onMoveDown(index) },
                    enabled = canMoveDown
                ) {
                    Icon(
                        AppIcons.ArrowDown,
                        contentDescription = "Déplacer vers le bas",
                        tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onDelete(index) }
                ) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun StepCard(
    step: String,
    index: Int,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { onEdit(index) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Étape ${index + 1}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = if (step.length > 100) "${step.take(100)}..." else step,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column {
                IconButton(
                    onClick = { onMoveUp(index) },
                    enabled = canMoveUp
                ) {
                    Icon(
                        AppIcons.ArrowUp,
                        contentDescription = "Déplacer vers le haut",
                        tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onMoveDown(index) },
                    enabled = canMoveDown
                ) {
                    Icon(
                        AppIcons.ArrowDown,
                        contentDescription = "Déplacer vers le bas",
                        tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onDelete(index) }
                ) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SourceTypeSelector(
    selectedType: SourceType,
    onTypeSelected: (SourceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Source de la recette",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SourceType.values().forEach { type ->
                    FilterChip(
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) },
                        selected = selectedType == type,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}