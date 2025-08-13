package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for tags table.
 * Mirrors the Python Tag model.
 */
@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TagEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recetteId: String,
    val nom: String
)