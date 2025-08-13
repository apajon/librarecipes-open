package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for ingredients table.
 * Mirrors the Python Ingredient model.
 */
@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IngredientEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recetteId: String,
    val nom: String,
    val quantite: String? = null,
    val unite: String? = null,
    val indispensable: Boolean = true,
    val alternatives: String? = null // Liste séparée par ;
)