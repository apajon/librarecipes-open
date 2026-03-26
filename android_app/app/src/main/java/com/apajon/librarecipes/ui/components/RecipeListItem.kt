package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.R
import com.apajon.librarecipes.data.model.RecipeListItem
import com.apajon.librarecipes.ui.icon.AppIcons

@Composable
fun RecipeListItem(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title row with edit/delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = recipe.nom,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Edit and delete actions
                if (onEdit != null || onDelete != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        onEdit?.let { editAction ->
                            IconButton(
                                onClick = { editAction() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    AppIcons.Edit,
                                    contentDescription = stringResource(R.string.action_edit_cd),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        onDelete?.let { deleteAction ->
                            IconButton(
                                onClick = { deleteAction() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    AppIcons.Delete,
                                    contentDescription = stringResource(R.string.action_delete_cd),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time and portions info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recipe.preparation != null || recipe.cuisson != null) {
                    Icon(
                        AppIcons.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val totalTime = (recipe.preparation ?: 0) + (recipe.cuisson ?: 0)
                    Text(
                        text = "${totalTime}min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (recipe.portions != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        AppIcons.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${recipe.portions} pers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Categories and tags
            if (recipe.categories.isNotEmpty() || recipe.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(recipe.categories) { category ->
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    items(recipe.tags) { tag ->
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}