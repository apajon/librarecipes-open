package com.apajon.librarecipes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.apajon.librarecipes.data.local.entities.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Photo operations.
 */
@Dao
interface PhotoDao {
    
    @Query("SELECT * FROM photos WHERE recetteId = :recipeId ORDER BY ordre ASC")
    suspend fun getPhotosForRecipe(recipeId: String): List<PhotoEntity>
    
    @Query("SELECT * FROM photos WHERE recetteId = :recipeId ORDER BY ordre ASC")
    fun getPhotosForRecipeFlow(recipeId: String): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE id = :photoId")
    suspend fun getPhotoById(photoId: String): PhotoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)
    
    @Query("DELETE FROM photos WHERE recetteId = :recipeId")
    suspend fun deletePhotosForRecipe(recipeId: String)
    
    @Query("UPDATE photos SET ordre = :newOrder WHERE id = :photoId")
    suspend fun updatePhotoOrder(photoId: String, newOrder: Int)
    
    @Query("UPDATE photos SET categorie = :category WHERE id = :photoId")
    suspend fun updatePhotoCategory(photoId: String, category: String?)
}