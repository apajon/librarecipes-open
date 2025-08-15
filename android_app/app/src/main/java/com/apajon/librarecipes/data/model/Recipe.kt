package com.apajon.librarecipes.data.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName("ingredients_mode")
    val ingredientsMode: String = "ANY",
    val tags: List<String>? = null,
    val categories: List<String>? = null
)

/**
 * Detailed recipe model for UI display.
 */
data class RecipeDetail(
    val id: String,
    val nom: String,
    val preparation: Int? = null,
    val cuisson: Int? = null,
    val portions: Int? = null,
    val dateAjout: String,
    val ingredients: List<IngredientDetail>,
    val etapes: List<EtapeDetail>,
    val categories: List<String>,
    val tags: List<String>,
    val source: SourceDetail? = null
)

/**
 * Ingredient detail model for UI display.
 */
data class IngredientDetail(
    val nom: String,
    val quantite: Float? = null,
    val unite: String? = null,
    val indispensable: Boolean = true,
    val alternatives: String? = null
)

/**
 * Step detail model for UI display.
 */
data class EtapeDetail(
    val numero: Int,
    val description: String
)

/**
 * Source detail model for UI display.
 */
data class SourceDetail(
    val type: String,
    val url: String? = null,
    val bookTitle: String? = null,
    val bookAuthors: String? = null,
    val bookPage: String? = null
)

/**
 * Execution detail model for UI display.
 */
data class ExecutionDetail(
    val id: String,
    val dateExecution: String,
    val nombreConvives: Int? = null,
    val feedbacks: List<FeedbackDetail> = emptyList()
)

/**
 * Convive detail model for UI display.
 */
data class ConviveDetail(
    val id: String,
    val nom: String,
    val groupe: String? = null
)

/**
 * Feedback detail model for UI display.
 */
data class FeedbackDetail(
    val id: String,
    val convive: ConviveDetail,
    val statut: String
)

/**
 * Execution form state for UI.
 */
data class ExecutionFormState(
    val nombreConvives: String = "",
    val dateExecution: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)