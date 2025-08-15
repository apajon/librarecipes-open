package com.apajon.librarecipes.data.local.dao

import androidx.room.*
import com.apajon.librarecipes.data.local.entities.ExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionDao {

    @Query("SELECT * FROM executions WHERE recetteId = :recetteId ORDER BY dateExecution DESC")
    fun getExecutionsForRecipe(recetteId: String): Flow<List<ExecutionEntity>>

    @Query("SELECT * FROM executions WHERE id = :id")
    suspend fun getExecution(id: String): ExecutionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: ExecutionEntity)

    @Update
    suspend fun updateExecution(execution: ExecutionEntity)

    @Delete
    suspend fun deleteExecution(execution: ExecutionEntity)

    @Query("DELETE FROM executions WHERE recetteId = :recetteId")
    suspend fun deleteExecutionsForRecipe(recetteId: String)
}