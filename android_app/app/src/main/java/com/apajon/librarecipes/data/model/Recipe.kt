package com.apajon.librarecipes.data.model

/**
 * Data classes for recipe creation and API responses.
 */

/**
 * Recipe creation request model for API.
 */
data class RecipeCreate(
    val nom: String,
    val preparation: Int? = null,
    val cuisson: Int? = null,
    val portions: Int? = null,
    val ingredients: List<IngredientCreate>,
    val etapes: List<String>,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val source: SourceCreate? = null
)

/**
 * Recipe response model from API.
 */
data class RecipeResponse(
    val id: String,
    val nom: String,
    val preparation: Int? = null,
    val cuisson: Int? = null,
    val portions: Int? = null,
    val dateAjout: String,
    val derniereExecution: String? = null,
    val source: SourceResponse? = null
)

/**
 * Ingredient creation model for API.
 */
data class IngredientCreate(
    val nom: String,
    val quantite: Float? = null,
    val unite: String? = null,
    val indispensable: Boolean = true,
    val alternatives: String? = null
)

/**
 * Source creation model for API.
 */
data class SourceCreate(
    val type: String, // "homemade", "url", "book"
    val valeur: String? = null
)

/**
 * Source response model from API.
 */
data class SourceResponse(
    val type: String,
    val valeur: String? = null
)

/**
 * Form state for recipe creation UI.
 */
data class RecipeFormState(
    val nom: String = "",
    val preparation: String = "",
    val cuisson: String = "",
    val portions: String = "",
    val categories: String = "",
    val tags: String = "",
    val ingredients: List<IngredientFormItem> = emptyList(),
    val steps: List<String> = emptyList(),
    val sourceType: SourceType = SourceType.HOMEMADE,
    val sourceUrl: String = "",
    val sourceBookTitle: String = "",
    val sourceBookAuthors: String = "",
    val sourceBookPage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Ingredient form item for UI.
 */
data class IngredientFormItem(
    val nom: String,
    val quantite: String = "",
    val unite: String = "",
    val indispensable: Boolean = true,
    val alternatives: String = ""
)

/**
 * Source type enum for UI.
 */
enum class SourceType(val displayName: String) {
    HOMEMADE("Maison"),
    URL("Site web"),
    BOOK("Livre")
}

/**
 * Measurement units for ingredients.
 */
object MeasurementUnits {
    val units = listOf(
        "",
        "g",
        "kg",
        "ml",
        "cl",
        "l",
        "c. à c.",
        "c. à s.",
        "tasse",
        "pièce",
        "pincée",
        "gousse",
        "botte",
        "tranche"
    )
}

/**
 * Search filters for recipe search API.
 */
data class SearchFilters(
    val nom: String? = null,
    val ingredients: List<String>? = null,
    val ingredientsMode: String = "ANY",
    val tags: List<String>? = null,
    val categories: List<String>? = null
)