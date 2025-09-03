package com.apajon.librarecipes.data.local.dao

import androidx.room.*
import com.apajon.librarecipes.data.local.entities.ConviveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConviveDao {

    @Query("SELECT * FROM convives ORDER BY nom")
    fun getAllConvives(): Flow<List<ConviveEntity>>

    @Query("SELECT * FROM convives WHERE id = :id")
    suspend fun getConvive(id: String): ConviveEntity?

    @Query("SELECT * FROM convives WHERE nom = :nom")
    suspend fun getConviveByName(nom: String): ConviveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConvive(convive: ConviveEntity)

    @Update
    suspend fun updateConvive(convive: ConviveEntity)

    @Delete
    suspend fun deleteConvive(convive: ConviveEntity)
}