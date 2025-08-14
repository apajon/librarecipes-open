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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)
    
    @Delete
    suspend fun deleteIngredients(ingredients: List<IngredientEntity>)
    
    @Query("DELETE FROM ingredients WHERE recetteId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: String)
    
    @Query("DELETE FROM ingredients WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}