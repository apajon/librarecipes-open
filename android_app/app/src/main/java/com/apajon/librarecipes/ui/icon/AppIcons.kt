package com.apajon.librarecipes.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AppIcons provides a centralized access point for all icons used in the LibraRecipes app.
 * 
 * This follows the same design principle as the Material3 theme system:
 * - Single point of access for all icons
 * - Clear, semantic naming
 * - No hardcoded icon references in screens/components
 * 
 * Benefits:
 * - Icons can be swapped globally without touching screen code
 * - Consistent icon usage across the app
 * - Easy to add custom vector drawables when needed
 * - Supports future icon theme variants
 * 
 * Usage:
 * ```kotlin
 * Icon(
 *     imageVector = AppIcons.Add,
 *     contentDescription = "Add item"
 * )
 * ```
 */
object AppIcons {
    // Navigation & Actions
    val Add: ImageVector = Icons.Default.Add
    val Close: ImageVector = Icons.Default.Close
    val ArrowBack: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val ArrowForward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val ArrowUp: ImageVector = Icons.Default.KeyboardArrowUp
    val ArrowDown: ImageVector = Icons.Default.KeyboardArrowDown
    val List: ImageVector = Icons.AutoMirrored.Filled.List
    
    // CRUD Operations
    val Edit: ImageVector = Icons.Default.Edit
    val Delete: ImageVector = Icons.Default.Delete
    
    // Information & User
    val Info: ImageVector = Icons.Default.Info
    val Person: ImageVector = Icons.Default.Person
    
    /**
     * Expand/Collapse icon - returns ArrowUp or ArrowDown based on state
     * 
     * @param isExpanded Whether the content is currently expanded
     * @return ArrowUp when expanded, ArrowDown when collapsed
     */
    fun expandCollapse(isExpanded: Boolean): ImageVector {
        return if (isExpanded) ArrowUp else ArrowDown
    }
}
