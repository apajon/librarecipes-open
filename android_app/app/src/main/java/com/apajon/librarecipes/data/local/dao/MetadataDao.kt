package com.apajon.librarecipes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apajon.librarecipes.data.local.entities.CategorieEntity
import com.apajon.librarecipes.data.local.entities.TagEntity
import com.apajon.librarecipes.data.local.entities.SourceEntity

/**
 * Data Access Objects for smaller entities.
 */
@Dao
interface CategorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategorieEntity>)
    
    @Query("DELETE FROM categories WHERE recetteId = :recipeId")
    suspend fun deleteCategoriesForRecipe(recipeId: String)
    
    @Query("DELETE FROM categories WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)
    
    @Query("DELETE FROM tags WHERE recetteId = :recipeId")
    suspend fun deleteTagsForRecipe(recipeId: String)
    
    @Query("DELETE FROM tags WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}

@Dao
interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)
    
    @Query("DELETE FROM sources WHERE recetteId = :recipeId")
    suspend fun deleteSourceForRecipe(recipeId: String)
    
    @Query("DELETE FROM sources WHERE recetteId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}