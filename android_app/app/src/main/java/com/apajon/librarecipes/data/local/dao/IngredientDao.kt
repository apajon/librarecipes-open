package com.apajon.librarecipes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apajon.librarecipes.data.local.entities.IngredientEntity

/**
 * Data Access Object for Ingredient operations.
 */
@Dao
interface IngredientDao {
    
    @Query("SELECT * FROM ingredients WHERE recetteId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: String): List<IngredientEntity>
    
    @Query("SELECT DISTINCT nom FROM ingredients ORDER BY nom ASC")
    suspend fun getAllUniqueIngredientNames(): List<String>
    
    @Query("SELECT DISTINCT recetteId FROM ingredients WHERE nom = :ingredientName")
    suspend fun getRecipeIdsWithIngredient(ingredientName: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)
    
    @Delete
    suspend fun deleteIngredients(ingredients: List<IngredientEntity>)
    
    @Query("DELETE FROM ingredients WHERE recetteId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)
    
    @Query("DELETE FROM ingredients WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}