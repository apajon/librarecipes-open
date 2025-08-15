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
    version = 2,
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
    abstract fun executionDao(): ExecutionDao
    abstract fun conviveDao(): ConviveDao
    abstract fun feedbackExecutionDao(): FeedbackExecutionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create executions table
                database.execSQL("""
                    CREATE TABLE executions (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        dateExecution TEXT NOT NULL,
                        nombreConvives INTEGER,
                        FOREIGN KEY(recetteId) REFERENCES recipes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create convives table
                database.execSQL("""
                    CREATE TABLE convives (
                        id TEXT NOT NULL PRIMARY KEY,
                        nom TEXT NOT NULL,
                        groupe TEXT
                    )
                """)
                
                // Create feedback_execution table
                database.execSQL("""
                    CREATE TABLE feedback_execution (
                        id TEXT NOT NULL PRIMARY KEY,
                        executionId TEXT NOT NULL,
                        conviveId TEXT NOT NULL,
                        statut TEXT NOT NULL,
                        FOREIGN KEY(executionId) REFERENCES executions(id) ON DELETE CASCADE,
                        FOREIGN KEY(conviveId) REFERENCES convives(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indices
                database.execSQL("CREATE UNIQUE INDEX index_executions_recetteId_dateExecution ON executions(recetteId, dateExecution)")
                database.execSQL("CREATE UNIQUE INDEX index_convives_nom ON convives(nom)")
                database.execSQL("CREATE INDEX index_feedback_execution_executionId ON feedback_execution(executionId)")
                database.execSQL("CREATE INDEX index_feedback_execution_conviveId ON feedback_execution(conviveId)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "librarecipes_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}