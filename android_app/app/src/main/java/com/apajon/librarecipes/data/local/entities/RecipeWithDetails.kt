package com.apajon.librarecipes.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a complete recipe with all its related data.
 * Used for complex queries that fetch recipe with ingredients, steps, etc.
 */
data class RecipeWithDetails(
    @Embedded
    val recipe: RecipeEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val ingredients: List<IngredientEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val etapes: List<EtapeEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val categories: List<CategorieEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val tags: List<TagEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val photos: List<PhotoEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val source: SourceEntity?
)