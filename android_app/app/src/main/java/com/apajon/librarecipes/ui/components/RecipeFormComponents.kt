package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.R
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
                text = stringResource(R.string.form_basic_info_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nom,
                onValueChange = onNomChange,
                label = { Text(stringResource(R.string.form_recipe_name_label)) },
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
                    label = { Text(stringResource(R.string.form_preparation_label)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cuisson,
                    onValueChange = onCuissonChange,
                    label = { Text(stringResource(R.string.form_cooking_label)) },
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
                Text(stringResource(R.string.form_portions_label), style = MaterialTheme.typography.bodyLarge)
                
                IconButton(
                    onClick = {
                        val current = portions.toIntOrNull() ?: 1
                        if (current > 1) onPortionsChange((current - 1).toString())
                    }
                ) {
                    Icon(AppIcons.ArrowDown, contentDescription = stringResource(R.string.form_portions_decrease_cd))
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
                    Icon(AppIcons.Add, contentDescription = stringResource(R.string.form_portions_increase_cd))
                }
            }

            OutlinedTextField(
                value = categories,
                onValueChange = onCategoriesChange,
                label = { Text(stringResource(R.string.form_categories_label)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.form_categories_placeholder)) }
            )

            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text(stringResource(R.string.form_tags_label)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.form_tags_placeholder)) }
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
                    text = if (ingredient.indispensable) stringResource(R.string.ingredient_essential) else stringResource(R.string.ingredient_optional),
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
                        contentDescription = stringResource(R.string.action_move_up_cd),
                        tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onMoveDown(index) },
                    enabled = canMoveDown
                ) {
                    Icon(
                        AppIcons.ArrowDown,
                        contentDescription = stringResource(R.string.action_move_down_cd),
                        tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onDelete(index) }
                ) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = stringResource(R.string.action_delete_cd),
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
                    text = stringResource(R.string.form_step_number, index + 1),
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
                        contentDescription = stringResource(R.string.action_move_up_cd),
                        tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onMoveDown(index) },
                    enabled = canMoveDown
                ) {
                    Icon(
                        AppIcons.ArrowDown,
                        contentDescription = stringResource(R.string.action_move_down_cd),
                        tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = { onDelete(index) }
                ) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = stringResource(R.string.action_delete_cd),
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
                text = stringResource(R.string.form_source_title),
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