package com.apajon.librarecipes.data.local

import com.apajon.librarecipes.data.local.entities.*
import com.apajon.librarecipes.data.model.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mapper functions to convert between database entities and API models.
 */
object EntityMapper {
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    
    /**
     * Convert RecipeCreate (from UI) to database entities.
     */
    fun recipeCreateToEntities(recipeCreate: RecipeCreate): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        val recipe = RecipeEntity(
            id = recipeId,
            nom = recipeCreate.nom,
            preparation = recipeCreate.preparation,
            cuisson = recipeCreate.cuisson,
            portions = recipeCreate.portions
        )
        
        val ingredients = recipeCreate.ingredients.map { ingredient ->
            IngredientEntity(
                recetteId = recipeId,
                nom = ingredient.nom,
                quantite = ingredient.quantite?.toString(),
                unite = ingredient.unite,
                indispensable = ingredient.indispensable,
                alternatives = ingredient.alternatives
            )
        }
        
        val etapes = recipeCreate.etapes.mapIndexed { index, description ->
            EtapeEntity(
                recetteId = recipeId,
                ordre = index + 1,
                description = description
            )
        }
        
        val categories = recipeCreate.categories.map { categoryName ->
            CategorieEntity(
                recetteId = recipeId,
                nom = categoryName
            )
        }
        
        val tags = recipeCreate.tags.map { tagName ->
            TagEntity(
                recetteId = recipeId,
                nom = tagName
            )
        }
        
        val source = recipeCreate.source?.let { sourceCreate ->
            SourceEntity(
                recetteId = recipeId,
                type = sourceCreate.type,
                url = if (sourceCreate.type == "url") sourceCreate.valeur else null,
                bookTitle = if (sourceCreate.type == "book") sourceCreate.valeur else null,
                bookAuthors = null, // Not captured in current UI
                bookPage = null // Not captured in current UI
            )
        }
        
        return RecipeWithEntities(
            recipe = recipe,
            ingredients = ingredients,
            etapes = etapes,
            categories = categories,
            tags = tags,
            source = source
        )
    }
    
    /**
     * Convert RecipeEntity to RecipeListItem (for list display).
     */
    fun recipeEntityToListItem(recipe: RecipeEntity): RecipeListItem {
        return RecipeListItem(
            id = recipe.id,
            nom = recipe.nom,
            preparation = recipe.preparation,
            cuisson = recipe.cuisson,
            portions = recipe.portions,
            dateAjout = dateFormat.format(recipe.dateAjout),
            categories = emptyList(), // Will be populated separately if needed
            tags = emptyList() // Will be populated separately if needed
        )
    }
    
    /**
     * Convert RecipeWithDetails to RecipeDetail (for detailed display).
     */
    fun recipeWithDetailsToDetail(recipeWithDetails: RecipeWithDetails): RecipeDetail {
        return RecipeDetail(
            id = recipeWithDetails.recipe.id,
            nom = recipeWithDetails.recipe.nom,
            preparation = recipeWithDetails.recipe.preparation,
            cuisson = recipeWithDetails.recipe.cuisson,
            portions = recipeWithDetails.recipe.portions,
            dateAjout = dateFormat.format(recipeWithDetails.recipe.dateAjout),
            ingredients = recipeWithDetails.ingredients.map { ingredient ->
                IngredientDetail(
                    nom = ingredient.nom,
                    quantite = ingredient.quantite?.toFloatOrNull(),
                    unite = ingredient.unite,
                    indispensable = ingredient.indispensable,
                    alternatives = ingredient.alternatives
                )
            },
            etapes = recipeWithDetails.etapes.sortedBy { it.ordre }.map { etape ->
                EtapeDetail(
                    numero = etape.ordre,
                    description = etape.description
                )
            },
            categories = recipeWithDetails.categories.map { it.nom },
            tags = recipeWithDetails.tags.map { it.nom },
            source = recipeWithDetails.source?.let { source ->
                SourceDetail(
                    type = source.type,
                    url = source.url,
                    bookTitle = source.bookTitle,
                    bookAuthors = source.bookAuthors,
                    bookPage = source.bookPage
                )
            }
        )
    }

    /**
     * Convert RecipeWithDetails to RecipeResponse.
     */
    fun recipeWithDetailsToResponse(recipeWithDetails: RecipeWithDetails): RecipeResponse {
        return RecipeResponse(
            id = recipeWithDetails.recipe.id,
            nom = recipeWithDetails.recipe.nom,
            preparation = recipeWithDetails.recipe.preparation,
            cuisson = recipeWithDetails.recipe.cuisson,
            portions = recipeWithDetails.recipe.portions,
            dateAjout = dateFormat.format(recipeWithDetails.recipe.dateAjout),
            derniereExecution = null, // Not implemented yet
            source = recipeWithDetails.source?.let { source ->
                SourceResponse(
                    type = source.type,
                    valeur = when (source.type) {
                        "url" -> source.url
                        "book" -> source.bookTitle
                        else -> null
                    }
                )
            }
        )
    }
}

/**
 * Data class to hold all entities for a recipe.
 */
data class RecipeWithEntities(
    val recipe: RecipeEntity,
    val ingredients: List<IngredientEntity>,
    val etapes: List<EtapeEntity>,
    val categories: List<CategorieEntity>,
    val tags: List<TagEntity>,
    val source: SourceEntity?
)