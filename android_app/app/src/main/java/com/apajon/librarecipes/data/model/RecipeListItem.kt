package com.apajon.librarecipes.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a recipe list item (summary view).
 * Maps to the RecetteListResponse schema from the backend API.
 */
data class RecipeListItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("nom")
    val nom: String,
    @SerializedName("preparation")
    val preparation: Int? = null,
    @SerializedName("cuisson") 
    val cuisson: Int? = null,
    @SerializedName("portions")
    val portions: Int? = null,
    @SerializedName("date_ajout")
    val dateAjout: String, // Using String for easier JSON serialization
    @SerializedName("categories")
    val categories: List<String> = emptyList(),
    @SerializedName("tags")
    val tags: List<String> = emptyList()
)