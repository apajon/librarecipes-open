package com.apajon.librarecipes.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Room entity for recipes table.
 * Mirrors the Python Recette model.
 */
@Entity(tableName = "recettes")
data class RecipeEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val preparation: Int? = null, // en minutes
    val cuisson: Int? = null, // en minutes
    val portions: Int? = null,
    val dateAjout: Date = Date()
)