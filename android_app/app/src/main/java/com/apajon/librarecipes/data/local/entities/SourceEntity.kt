package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for sources table.
 * Mirrors the Python Source model.
 */
@Entity(
    tableName = "sources",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SourceEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recetteId: String,
    val type: String, // 'homemade', 'url', 'book'
    val url: String? = null,
    val bookTitle: String? = null,
    val bookAuthors: String? = null,
    val bookPage: String? = null
)