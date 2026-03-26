package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.apajon.librarecipes.R
import com.apajon.librarecipes.data.model.IngredientFormItem
import com.apajon.librarecipes.data.model.MeasurementUnits
import com.apajon.librarecipes.ui.icon.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAddIngredient: (IngredientFormItem) -> Unit,
    initialIngredient: IngredientFormItem? = null,
    isEditing: Boolean = false
) {
    var nom by remember { mutableStateOf(initialIngredient?.nom ?: "") }
    var quantite by remember { mutableStateOf(initialIngredient?.quantite ?: "") }
    var unite by remember { mutableStateOf(initialIngredient?.unite ?: "") }
    var indispensable by remember { mutableStateOf(initialIngredient?.indispensable ?: true) }
    var alternatives by remember { mutableStateOf(initialIngredient?.alternatives ?: "") }
    var showUnitDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) stringResource(R.string.dialog_edit_ingredient_title) else stringResource(R.string.dialog_add_ingredient_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(AppIcons.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                // Ingredient name
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text(stringResource(R.string.dialog_ingredient_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.dialog_ingredient_name_placeholder)) }
                )

                // Quantity and unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantite,
                        onValueChange = { quantite = it },
                        label = { Text(stringResource(R.string.dialog_quantity_label)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.dialog_quantity_placeholder)) }
                    )

                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unite,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(stringResource(R.string.dialog_unit_label)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.dialog_unit_placeholder)) }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dialog_no_unit)) },
                                onClick = {
                                    unite = ""
                                    showUnitDropdown = false
                                }
                            )
                            
                            MeasurementUnits.units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        unite = unit
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Essential checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = indispensable,
                            onClick = { indispensable = !indispensable },
                            role = Role.Checkbox
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = indispensable,
                        onCheckedChange = { indispensable = it }
                    )
                    Text(
                        text = stringResource(R.string.dialog_essential_ingredient),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Alternatives (only show when not essential)
                if (!indispensable) {
                    OutlinedTextField(
                        value = alternatives,
                        onValueChange = { alternatives = it },
                        label = { Text(stringResource(R.string.dialog_alternatives_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.dialog_alternatives_placeholder)) },
                        minLines = 2
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    
                    Button(
                        onClick = {
                            if (nom.isNotBlank()) {
                                onAddIngredient(
                                    IngredientFormItem(
                                        nom = nom.trim(),
                                        quantite = quantite.trim(),
                                        unite = unite.trim(),
                                        indispensable = indispensable,
                                        alternatives = if (indispensable) "" else alternatives.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        enabled = nom.isNotBlank()
                    ) {
                        Text(if (isEditing) stringResource(R.string.action_edit) else stringResource(R.string.action_add))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStepDialog(
    onDismiss: () -> Unit,
    onAddStep: (String) -> Unit,
    initialStep: String = "",
    isEditing: Boolean = false
) {
    var description by remember { mutableStateOf(initialStep) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) stringResource(R.string.dialog_edit_step_title) else stringResource(R.string.dialog_add_step_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(AppIcons.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }

                // Step description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.dialog_step_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.dialog_step_description_placeholder)) },
                    minLines = 3,
                    maxLines = 6
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    
                    Button(
                        onClick = {
                            if (description.isNotBlank()) {
                                onAddStep(description.trim())
                                onDismiss()
                            }
                        },
                        enabled = description.isNotBlank()
                    ) {
                        Text(if (isEditing) stringResource(R.string.action_edit) else stringResource(R.string.action_add))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceDetailsCard(
    sourceType: com.apajon.librarecipes.data.model.SourceType,
    sourceUrl: String,
    onSourceUrlChange: (String) -> Unit,
    sourceBookTitle: String,
    onSourceBookTitleChange: (String) -> Unit,
    sourceBookAuthors: String,
    onSourceBookAuthorsChange: (String) -> Unit,
    sourceBookPage: String,
    onSourceBookPageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (sourceType) {
        com.apajon.librarecipes.data.model.SourceType.URL -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sourceUrl,
                        onValueChange = onSourceUrlChange,
                        label = { Text(stringResource(R.string.source_url_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.source_url_placeholder)) },
                        singleLine = true
                    )
                }
            }
        }
        
        com.apajon.librarecipes.data.model.SourceType.BOOK -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = sourceBookTitle,
                        onValueChange = onSourceBookTitleChange,
                        label = { Text(stringResource(R.string.source_book_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.source_book_title_placeholder)) }
                    )
                    
                    OutlinedTextField(
                        value = sourceBookAuthors,
                        onValueChange = onSourceBookAuthorsChange,
                        label = { Text(stringResource(R.string.source_book_authors_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.source_book_authors_placeholder)) }
                    )
                    
                    OutlinedTextField(
                        value = sourceBookPage,
                        onValueChange = onSourceBookPageChange,
                        label = { Text(stringResource(R.string.source_book_page_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.source_book_page_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }
        
        com.apajon.librarecipes.data.model.SourceType.HOMEMADE -> {
            // No additional fields needed for homemade recipes
        }
    }
}