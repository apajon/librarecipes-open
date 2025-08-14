package com.apajon.librarecipes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apajon.librarecipes.data.local.entities.EtapeEntity

/**
 * Data Access Object for Etape (step) operations.
 */
@Dao
interface EtapeDao {
    
    @Query("SELECT * FROM etapes WHERE recetteId = :recipeId ORDER BY ordre")
    suspend fun getEtapesForRecipe(recipeId: String): List<EtapeEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEtapes(etapes: List<EtapeEntity>)
    
    @Delete
    suspend fun deleteEtapes(etapes: List<EtapeEntity>)
    
    @Query("DELETE FROM etapes WHERE recetteId = :recipeId")
    suspend fun deleteEtapesForRecipe(recipeId: String)
    
    @Query("DELETE FROM etapes WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}