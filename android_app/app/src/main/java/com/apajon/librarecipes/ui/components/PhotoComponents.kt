package com.apajon.librarecipes.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apajon.librarecipes.R
import com.apajon.librarecipes.data.model.PhotoDetail
import com.apajon.librarecipes.ui.icon.AppIcons
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Enhanced photo viewer component with navigation and category indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerCard(
    photos: List<PhotoDetail>,
    selectedPhotoIndex: Int,
    onPhotoSelected: (Int) -> Unit,
    onPreviousPhoto: () -> Unit,
    onNextPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    AppIcons.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.photo_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    val currentPhoto = photos.getOrNull(selectedPhotoIndex) ?: photos.first()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with title and photo counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.photo_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = stringResource(R.string.photo_counter, selectedPhotoIndex + 1, photos.size, getCategoryDisplayName(currentPhoto.categorie)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Main photo with navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
            ) {
                // Main photo
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(File(currentPhoto.chemin))
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.photo_content_description, selectedPhotoIndex + 1),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Category badge
                currentPhoto.categorie?.let { category ->
                    CategoryBadge(
                        category = category,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
                
                // Navigation arrows
                if (photos.size > 1) {
                    // Previous arrow
                    IconButton(
                        onClick = onPreviousPhoto,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            AppIcons.ArrowBack,
                            contentDescription = stringResource(R.string.photo_previous_cd),
                            tint = Color.White
                        )
                    }
                    
                    // Next arrow
                    IconButton(
                        onClick = onNextPhoto,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            AppIcons.ArrowForward,
                            contentDescription = stringResource(R.string.photo_next_cd),
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Thumbnail navigation
            if (photos.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(photos) { index, photo ->
                        ThumbnailPhoto(
                            photo = photo,
                            isSelected = index == selectedPhotoIndex,
                            onClick = { onPhotoSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Category badge with color coding.
 */
@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, emoji) = when (category.lowercase()) {
        "préparation", "preparation" -> Pair(com.apajon.librarecipes.ui.theme.CategoryPreparation, "🟡") // Yellow
        "ingrédient", "ingredient" -> Pair(com.apajon.librarecipes.ui.theme.CategoryIngredient, "🟢") // Green
        "cuisson" -> Pair(com.apajon.librarecipes.ui.theme.CategoryCuisson, "🟠") // Orange
        "final" -> Pair(com.apajon.librarecipes.ui.theme.CategoryFinal, "🔵") // Blue
        else -> Pair(com.apajon.librarecipes.ui.theme.CategoryDefault, "⚪") // Gray
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.9f)
    ) {
        Text(
            text = "$emoji ${category.replaceFirstChar { it.uppercase() }}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Thumbnail photo for navigation.
 */
@Composable
fun ThumbnailPhoto(
    photo: PhotoDetail,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(File(photo.chemin))
            .crossfade(true)
            .build(),
        contentDescription = stringResource(R.string.photo_thumbnail_cd, photo.ordre),
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentScale = ContentScale.Crop
    )
}

/**
 * Photo management card with inline controls.
 */
@Composable
fun PhotoManagementCard(
    photos: List<PhotoDetail>,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onAddPhotoWithPath: (String, String?) -> Unit, // Updated to include path
    onUpdateCategory: (String, String?) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onMovePhotoUp: (String) -> Unit,
    onMovePhotoDown: (String) -> Unit,
    isAddingPhoto: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("préparation") }
    val context = LocalContext.current
    
    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoUri ->
            // Copy the photo to app storage and get the path
            val photoPath = copyPhotoToAppStorage(context, photoUri)
            photoPath?.let { path ->
                onAddPhotoWithPath(path, selectedCategory)
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleVisibility() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.photo_management_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    if (isVisible) AppIcons.ArrowUp else AppIcons.ArrowDown,
                    contentDescription = if (isVisible) stringResource(R.string.photo_management_hide) else stringResource(R.string.photo_management_show)
                )
            }
            
            if (isVisible) {
                HorizontalDivider()
                
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Add photo section
                    PhotoUploadSection(
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        onPickPhoto = { photoPickerLauncher.launch("image/*") },
                        isUploading = isAddingPhoto
                    )
                    
                    if (photos.isNotEmpty()) {
                        HorizontalDivider()
                        
                        // Photo management list
                        photos.forEach { photo ->
                            PhotoManagementItem(
                                photo = photo,
                                onUpdateCategory = onUpdateCategory,
                                onDelete = onDeletePhoto,
                                onMoveUp = onMovePhotoUp,
                                onMoveDown = onMovePhotoDown,
                                canMoveUp = photos.indexOf(photo) > 0,
                                canMoveDown = photos.indexOf(photo) < photos.size - 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Photo upload section with category selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadSection(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onPickPhoto: () -> Unit,
    isUploading: Boolean,
    modifier: Modifier = Modifier
) {
    val categories = listOf("préparation", "ingrédient", "cuisson", "final")
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.photo_add_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        // Category selection
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.photo_category_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onCategoryChange(category)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        // Upload button
        Button(
            onClick = onPickPhoto,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.photo_uploading))
            } else {
                Icon(AppIcons.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.photo_choose))
            }
        }
    }
}

/**
 * Individual photo management item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoManagementItem(
    photo: PhotoDetail,
    onUpdateCategory: (String, String?) -> Unit,
    onDelete: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    modifier: Modifier = Modifier
) {
    val categories = listOf("préparation", "ingrédient", "cuisson", "final")
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(photo.chemin))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.photo_order, photo.ordre),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Photo info and controls
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.photo_order, photo.ordre),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Category selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = photo.categorie ?: stringResource(R.string.photo_no_category),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.photo_category_dropdown_label)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    onUpdateCategory(photo.id, category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Action buttons
            Column {
                // Move up button
                IconButton(
                    onClick = { onMoveUp(photo.id) },
                    enabled = canMoveUp
                ) {
                    Icon(
                        AppIcons.ArrowUp,
                        contentDescription = stringResource(R.string.action_move_up_cd),
                        tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                
                // Move down button
                IconButton(
                    onClick = { onMoveDown(photo.id) },
                    enabled = canMoveDown
                ) {
                    Icon(
                        AppIcons.ArrowDown,
                        contentDescription = stringResource(R.string.action_move_down_cd),
                        tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true }
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
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.photo_delete_title)) },
            text = { Text(stringResource(R.string.photo_delete_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(photo.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

/**
 * Get display name for photo category.
 */
private fun getCategoryDisplayName(category: String?): String {
    return when (category?.lowercase()) {
        "préparation", "preparation" -> "préparation"
        "ingrédient", "ingredient" -> "ingrédient"
        "cuisson" -> "cuisson"
        "final" -> "final"
        else -> "aucune catégorie"
    }
}

/**
 * Copy photo from URI to app storage and return the path.
 */
private fun copyPhotoToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let { stream ->
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            stream.copyTo(outputStream)
            stream.close()
            outputStream.close()
            file.absolutePath
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}