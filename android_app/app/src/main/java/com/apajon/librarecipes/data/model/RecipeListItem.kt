package com.apajon.librarecipes.data.model

/**
 * Data class representing a recipe list item (summary view).
 * Maps to the RecetteListResponse schema from the backend API.
 */
data class RecipeListItem(
    val id: String,
    val nom: String,
    val preparation: Int? = null,
    val cuisson: Int? = null,
    val portions: Int? = null,
    val dateAjout: String, // Using String for easier JSON serialization
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)