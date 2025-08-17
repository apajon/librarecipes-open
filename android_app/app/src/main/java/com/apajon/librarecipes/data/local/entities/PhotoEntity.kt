package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for photos table.
 * Mirrors the Python Photo model.
 */
@Entity(
    tableName = "photos",
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
data class PhotoEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recetteId: String,
    val chemin: String, // chemin local
    val categorie: String? = null, // final, cuisson, ingrédient...
    val ordre: Int = 0
)