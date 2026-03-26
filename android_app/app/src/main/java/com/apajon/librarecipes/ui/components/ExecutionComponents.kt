package com.apajon.librarecipes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.apajon.librarecipes.R
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.model.*
import com.apajon.librarecipes.ui.icon.AppIcons
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExecutionDialog(
    onDismiss: () -> Unit,
    onAddExecution: (ExecutionCreate) -> Unit,
    isLoading: Boolean = false,
    availableConvives: List<ConviveEntity> = emptyList(),
    existingExecution: ExecutionWithDetails? = null
) {
    // Initialize with existing data if editing
    var selectedConvives by remember { 
        mutableStateOf<List<ConviveWithFeedback>>(
            existingExecution?.convivesWithFeedback ?: emptyList()
        ) 
    }
    var showAddConviveDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { 
        mutableStateOf(
            existingExecution?.let { 
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    inputFormat.parse(it.execution.dateExecution) ?: Date()
                } catch (e: Exception) {
                    Date()
                }
            } ?: Date()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingExecution != null) stringResource(R.string.execution_edit_title) else stringResource(R.string.execution_add_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.execution_description))
                
                // Date and Time selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.execution_date_time_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Date picker button
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                Text("📅 ${dateFormat.format(selectedDate)}")
                            }
                            
                            // Time picker button  
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                Text("🕐 ${timeFormat.format(selectedDate)}")
                            }
                        }
                    }
                }
                
                // Convives section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.execution_convives_count, selectedConvives.size),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { showAddConviveDialog = true }
                    ) {
                        Icon(AppIcons.Add, contentDescription = stringResource(R.string.execution_add_convive_cd))
                    }
                }
                
                if (selectedConvives.isEmpty()) {
                    Text(
                        text = stringResource(R.string.execution_no_convive),
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
                        convivesWithFeedback = selectedConvives,
                        executionDate = selectedDate,
                        executionId = existingExecution?.execution?.id // Include existing ID for updates
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
                Text(if (existingExecution != null) stringResource(R.string.action_edit) else stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(R.string.action_cancel))
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
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.time = selectedDate
                            val newCalendar = Calendar.getInstance()
                            newCalendar.timeInMillis = millis
                            // Keep the time part, update only the date part
                            newCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                            newCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                            selectedDate = newCalendar.time
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = Calendar.getInstance().apply { time = selectedDate }.get(Calendar.HOUR_OF_DAY),
            initialMinute = Calendar.getInstance().apply { time = selectedDate }.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.execution_select_time)) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.time = selectedDate
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        selectedDate = calendar.time
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
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
        title = { Text(stringResource(R.string.execution_add_convive_dialog_title)) },
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
                        label = { Text(stringResource(R.string.execution_mode_existing)) }
                    )
                    FilterChip(
                        selected = selectedMode == ConviveSelectionMode.NEW,
                        onClick = { selectedMode = ConviveSelectionMode.NEW },
                        label = { Text(stringResource(R.string.execution_mode_new)) }
                    )
                }
                
                when (selectedMode) {
                    ConviveSelectionMode.EXISTING -> {
                        if (filteredConvives.isNotEmpty()) {
                            Text(stringResource(R.string.execution_select_convive))
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
                                text = stringResource(R.string.execution_no_convive_available),
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
                            Icon(AppIcons.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.execution_create_new_convive))
                        }
                        
                        newConviveForm.takeIf { it.isValid() }?.let { form ->
                            selectedConvive = form.toEntity(UUID.randomUUID().toString())
                        }
                    }
                }
                
                // Feedback selection
                if (selectedConvive != null || newConviveForm.isValid()) {
                    Text(stringResource(R.string.execution_feedback_question))
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
                Text(stringResource(R.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
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
                AppIcons.Person,
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
                        AppIcons.Person,
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
                        AppIcons.Delete,
                        contentDescription = stringResource(R.string.action_delete_cd),
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
fun ExecutionWithDetailsItem(
    executionWithDetails: ExecutionWithDetails,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    text = stringResource(R.string.execution_convive_count_display, executionWithDetails.nombreConvives, if (executionWithDetails.nombreConvives > 1) "s" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (executionWithDetails.convivesWithFeedback.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.execution_convives_label),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionDetailDialog(
    executionWithDetails: ExecutionWithDetails,
    onDismiss: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(stringResource(R.string.execution_detail_title))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date and time
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.execution_detail_date_time),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatExecutionDate(executionWithDetails.execution.dateExecution),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                // Convives
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.execution_convives_count, executionWithDetails.nombreConvives),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (executionWithDetails.convivesWithFeedback.isEmpty()) {
                            Text(
                                text = stringResource(R.string.execution_no_convive_registered),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            executionWithDetails.convivesWithFeedback.forEach { conviveWithFeedback ->
                                val displayOption = ConviveOption.fromConvive(conviveWithFeedback.convive)
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                AppIcons.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = displayOption.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        
                                        FilterChip(
                                            onClick = { /* Read-only */ },
                                            label = { Text(conviveWithFeedback.feedback.displayName) },
                                            selected = true,
                                            enabled = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEdit
                ) {
                    Icon(AppIcons.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_edit))
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(AppIcons.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.action_delete))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}