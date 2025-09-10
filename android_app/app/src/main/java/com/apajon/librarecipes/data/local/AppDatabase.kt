package com.apajon.librarecipes.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.apajon.librarecipes.data.local.dao.*
import com.apajon.librarecipes.data.local.entities.*

/**
 * Room database for LibraRecipes app.
 * Contains all entities and provides access to DAOs.
 */
@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        EtapeEntity::class,
        CategorieEntity::class,
        TagEntity::class,
        SourceEntity::class,
        PhotoEntity::class,
        ExecutionEntity::class,
        ConviveEntity::class,
        FeedbackExecutionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun recipeDao(): RecipeDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun etapeDao(): EtapeDao
    abstract fun categorieDao(): CategorieDao
    abstract fun tagDao(): TagDao
    abstract fun sourceDao(): SourceDao
    abstract fun photoDao(): PhotoDao
    abstract fun executionDao(): ExecutionDao
    abstract fun conviveDao(): ConviveDao
    abstract fun feedbackExecutionDao(): FeedbackExecutionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "librarecipes_database"
                )
                .fallbackToDestructiveMigration() // Start fresh - recreate database if any schema issues
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}