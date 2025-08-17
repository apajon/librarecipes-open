package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for categories table.
 * Mirrors the Python Categorie model.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["recetteId"])],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategorieEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recetteId: String,
    val nom: String
)