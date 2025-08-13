package com.apajon.librarecipes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.apajon.librarecipes.data.local.entities.RecipeEntity
import com.apajon.librarecipes.data.local.entities.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Recipe operations.
 */
@Dao
interface RecipeDao {
    
    @Query("SELECT * FROM recettes ORDER BY dateAjout DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>
    
    @Query("SELECT * FROM recettes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?
    
    @Transaction
    @Query("SELECT * FROM recettes WHERE id = :recipeId")
    suspend fun getRecipeWithDetails(recipeId: String): RecipeWithDetails?
    
    @Transaction
    @Query("SELECT * FROM recettes ORDER BY dateAjout DESC")
    fun getAllRecipesWithDetails(): Flow<List<RecipeWithDetails>>
    
    @Query("""
        SELECT DISTINCT r.* FROM recettes r 
        LEFT JOIN ingredients i ON r.id = i.recetteId 
        LEFT JOIN categories c ON r.id = c.recetteId 
        LEFT JOIN tags t ON r.id = t.recetteId 
        WHERE (:nom IS NULL OR r.nom LIKE '%' || :nom || '%')
        AND (:ingredients IS NULL OR i.nom IN (:ingredients))
        AND (:categories IS NULL OR c.nom IN (:categories))
        AND (:tags IS NULL OR t.nom IN (:tags))
        ORDER BY r.dateAjout DESC
    """)
    suspend fun searchRecipes(
        nom: String? = null,
        ingredients: List<String>? = null,
        categories: List<String>? = null,
        tags: List<String>? = null
    ): List<RecipeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long
    
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)
    
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)
    
    @Query("DELETE FROM recettes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)
}