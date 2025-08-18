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
    abstract fun executionDao(): ExecutionDao
    abstract fun conviveDao(): ConviveDao
    abstract fun feedbackExecutionDao(): FeedbackExecutionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create all required tables for version 2
                
                // Create recettes table
                db.execSQL("""
                    CREATE TABLE recettes (
                        id TEXT NOT NULL PRIMARY KEY,
                        nom TEXT NOT NULL,
                        preparation INTEGER,
                        cuisson INTEGER,
                        portions INTEGER,
                        dateAjout INTEGER NOT NULL
                    )
                """)
                
                // Create ingredients table
                db.execSQL("""
                    CREATE TABLE ingredients (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        nom TEXT NOT NULL,
                        quantite TEXT,
                        unite TEXT,
                        indispensable INTEGER NOT NULL,
                        alternatives TEXT,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create etapes table
                db.execSQL("""
                    CREATE TABLE etapes (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        ordre INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create categories table
                db.execSQL("""
                    CREATE TABLE categories (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        nom TEXT NOT NULL,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create tags table
                db.execSQL("""
                    CREATE TABLE tags (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        nom TEXT NOT NULL,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create sources table
                db.execSQL("""
                    CREATE TABLE sources (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        url TEXT,
                        bookTitle TEXT,
                        bookAuthors TEXT,
                        bookPage TEXT,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create photos table
                db.execSQL("""
                    CREATE TABLE photos (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        chemin TEXT NOT NULL,
                        categorie TEXT,
                        ordre INTEGER NOT NULL,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indices for foreign keys
                db.execSQL("CREATE INDEX index_ingredients_recetteId ON ingredients(recetteId)")
                db.execSQL("CREATE INDEX index_etapes_recetteId ON etapes(recetteId)")
                db.execSQL("CREATE INDEX index_categories_recetteId ON categories(recetteId)")
                db.execSQL("CREATE INDEX index_tags_recetteId ON tags(recetteId)")
                db.execSQL("CREATE INDEX index_sources_recetteId ON sources(recetteId)")
                db.execSQL("CREATE INDEX index_photos_recetteId ON photos(recetteId)")
                
                // Create executions table
                db.execSQL("""
                    CREATE TABLE executions (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        dateExecution TEXT NOT NULL,
                        nombreConvives INTEGER,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Create convives table
                db.execSQL("""
                    CREATE TABLE convives (
                        id TEXT NOT NULL PRIMARY KEY,
                        nom TEXT NOT NULL,
                        groupe TEXT
                    )
                """)
                
                // Create feedback_execution table
                db.execSQL("""
                    CREATE TABLE feedback_execution (
                        id TEXT NOT NULL PRIMARY KEY,
                        executionId TEXT NOT NULL,
                        conviveId TEXT NOT NULL,
                        statut TEXT NOT NULL,
                        FOREIGN KEY(executionId) REFERENCES executions(id) ON DELETE CASCADE,
                        FOREIGN KEY(conviveId) REFERENCES convives(id) ON DELETE CASCADE
                    )
                """)
                
                // Create indices for execution tables
                db.execSQL("CREATE UNIQUE INDEX index_executions_recetteId_dateExecution ON executions(recetteId, dateExecution)")
                db.execSQL("CREATE UNIQUE INDEX index_convives_nom ON convives(nom)")
                db.execSQL("CREATE INDEX index_feedback_execution_executionId ON feedback_execution(executionId)")
                db.execSQL("CREATE INDEX index_feedback_execution_conviveId ON feedback_execution(conviveId)")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove nombreConvives column from executions table
                // SQLite doesn't support dropping columns directly, so we need to recreate the table
                
                // Create new executions table without nombreConvives
                db.execSQL("""
                    CREATE TABLE executions_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        recetteId TEXT NOT NULL,
                        dateExecution TEXT NOT NULL,
                        FOREIGN KEY(recetteId) REFERENCES recettes(id) ON DELETE CASCADE
                    )
                """)
                
                // Copy data from old table to new table
                db.execSQL("""
                    INSERT INTO executions_new (id, recetteId, dateExecution)
                    SELECT id, recetteId, dateExecution FROM executions
                """)
                
                // Drop old table
                db.execSQL("DROP TABLE executions")
                
                // Rename new table
                db.execSQL("ALTER TABLE executions_new RENAME TO executions")
                
                // Recreate index
                db.execSQL("CREATE UNIQUE INDEX index_executions_recetteId_dateExecution ON executions(recetteId, dateExecution)")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "librarecipes_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration() // This will recreate the database if migration fails
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}