package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.model.ConviveForm
import com.apajon.librarecipes.data.model.ConviveOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConviveDialog(
    onDismiss: () -> Unit,
    onCreateConvive: (ConviveForm) -> Unit,
    availableConvives: List<ConviveEntity> = emptyList()
) {
    var nom by remember { mutableStateOf("") }
    var groupe by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    
    // Check for duplicate names
    val convivesWithSameName = availableConvives.filter { 
        it.nom.equals(nom.trim(), ignoreCase = true) 
    }
    
    LaunchedEffect(nom) {
        showWarning = nom.isNotBlank() && convivesWithSameName.isNotEmpty()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau convive") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Créer un nouveau convive pour cette réalisation.")
                
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom *") },
                    placeholder = { Text("Ex: David") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showWarning
                )
                
                OutlinedTextField(
                    value = groupe,
                    onValueChange = { groupe = it },
                    label = { Text("Groupe (optionnel)") },
                    placeholder = { Text("Ex: cousin, ami, famille") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (showWarning) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️ Convives existants avec le même nom :",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            
                            convivesWithSameName.forEach { convive ->
                                val displayOption = ConviveOption.fromConvive(convive)
                                Text(
                                    text = "• ${displayOption.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            
                            Text(
                                text = "Suggestion : ajoutez un groupe pour différencier (ex: \"${nom.trim()} (cousin)\").",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val form = ConviveForm(nom = nom, groupe = groupe)
                    onCreateConvive(form)
                },
                enabled = nom.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}