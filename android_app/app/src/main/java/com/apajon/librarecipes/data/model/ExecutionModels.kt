package com.apajon.librarecipes.data.model

import com.apajon.librarecipes.data.local.entities.ConviveEntity
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
import com.apajon.librarecipes.data.local.entities.FeedbackExecutionEntity
import java.util.Date

/**
 * Enum for feedback status
 */
enum class FeedbackStatus(val displayName: String) {
    AIME("Aimé"),
    PARTIELLEMENT("Partiellement"),
    RIEN_MANGE("Rien mangé")
}

/**
 * Data class representing a convive with feedback for a specific execution
 */
data class ConviveWithFeedback(
    val convive: ConviveEntity,
    val feedback: FeedbackStatus
)

/**
 * Data class representing an execution with its associated convives and their feedback
 */
data class ExecutionWithDetails(
    val execution: ExecutionEntity,
    val convivesWithFeedback: List<ConviveWithFeedback>
) {
    val nombreConvives: Int
        get() = convivesWithFeedback.size
}

/**
 * Data class for creating a new execution with convives
 */
data class ExecutionCreate(
    val recipeId: String,
    val convivesWithFeedback: List<ConviveWithFeedback>,
    val executionDate: Date = Date(),
    val executionId: String? = null // For updates, null for new executions
)

/**
 * Data class for displaying convive options with formatted name
 */
data class ConviveOption(
    val convive: ConviveEntity,
    val displayName: String
) {
    companion object {
        fun fromConvive(convive: ConviveEntity): ConviveOption {
            val displayName = if (convive.groupe.isNullOrBlank()) {
                convive.nom
            } else {
                "${convive.nom} (${convive.groupe})"
            }
            return ConviveOption(convive, displayName)
        }
    }
}

/**
 * Data class for adding/editing a convive
 */
data class ConviveForm(
    val nom: String = "",
    val groupe: String = ""
) {
    fun isValid(): Boolean = nom.isNotBlank()
    
    fun toEntity(id: String): ConviveEntity {
        return ConviveEntity(
            id = id,
            nom = nom.trim(),
            groupe = if (groupe.isBlank()) null else groupe.trim()
        )
    }
}