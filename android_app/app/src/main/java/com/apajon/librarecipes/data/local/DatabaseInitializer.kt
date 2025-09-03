package com.apajon.librarecipes.data.local

import com.apajon.librarecipes.data.local.entities.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class responsible for initializing the database with sample data.
 */
@Singleton
class DatabaseInitializer @Inject constructor(
    private val database: AppDatabase
) {
    
    suspend fun initializeWithSampleData() {
        // Check if database is already initialized
        val existingRecipes = database.recipeDao().getAllRecipes()
        // Since this is a Flow, we need to check differently
        // For simplicity, let's just add sample data every time for now
        
        addSampleRecipes()
    }
    
    private suspend fun addSampleRecipes() {
        val sampleRecipes = createSampleRecipes()
        
        sampleRecipes.forEach { recipeData ->
            // Insert recipe
            database.recipeDao().insertRecipe(recipeData.recipe)
            
            // Insert related entities
            database.ingredientDao().insertIngredients(recipeData.ingredients)
            database.etapeDao().insertEtapes(recipeData.etapes)
            database.categorieDao().insertCategories(recipeData.categories)
            database.tagDao().insertTags(recipeData.tags)
            recipeData.source?.let { source ->
                database.sourceDao().insertSource(source)
            }
        }
    }
    
    private fun createSampleRecipes(): List<RecipeWithEntities> {
        return listOf(
            createPatesCarbonara(),
            createSaladeCesar(),
            createTiramisuClassique(),
            createSoupeTomates(),
            createCroqueMonsieur()
        )
    }
    
    private fun createPatesCarbonara(): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        return RecipeWithEntities(
            recipe = RecipeEntity(
                id = recipeId,
                nom = "Pâtes à la Carbonara",
                preparation = 15,
                cuisson = 10,
                portions = 4
            ),
            ingredients = listOf(
                IngredientEntity(recetteId = recipeId, nom = "Spaghettis", quantite = "400", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Lardons", quantite = "200", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Œufs", quantite = "3", unite = "pièce"),
                IngredientEntity(recetteId = recipeId, nom = "Parmesan râpé", quantite = "100", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Crème fraîche", quantite = "200", unite = "ml"),
                IngredientEntity(recetteId = recipeId, nom = "Poivre", quantite = "1", unite = "pincée"),
                IngredientEntity(recetteId = recipeId, nom = "Sel", quantite = "1", unite = "pincée")
            ),
            etapes = listOf(
                EtapeEntity(recetteId = recipeId, ordre = 1, description = "Faire cuire les pâtes dans l'eau bouillante salée selon les indications du paquet."),
                EtapeEntity(recetteId = recipeId, ordre = 2, description = "Pendant ce temps, faire dorer les lardons dans une poêle sans matière grasse."),
                EtapeEntity(recetteId = recipeId, ordre = 3, description = "Dans un bol, battre les œufs avec la crème fraîche et le parmesan."),
                EtapeEntity(recetteId = recipeId, ordre = 4, description = "Égoutter les pâtes et les mélanger immédiatement avec les lardons."),
                EtapeEntity(recetteId = recipeId, ordre = 5, description = "Hors du feu, ajouter le mélange œufs-crème en remuant rapidement."),
                EtapeEntity(recetteId = recipeId, ordre = 6, description = "Poivrer généreusement et servir immédiatement.")
            ),
            categories = listOf(
                CategorieEntity(recetteId = recipeId, nom = "Plats principaux"),
                CategorieEntity(recetteId = recipeId, nom = "Pâtes")
            ),
            tags = listOf(
                TagEntity(recetteId = recipeId, nom = "Italien"),
                TagEntity(recetteId = recipeId, nom = "Rapide"),
                TagEntity(recetteId = recipeId, nom = "Famille")
            ),
            source = SourceEntity(recetteId = recipeId, type = "homemade")
        )
    }
    
    private fun createSaladeCesar(): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        return RecipeWithEntities(
            recipe = RecipeEntity(
                id = recipeId,
                nom = "Salade César",
                preparation = 20,
                cuisson = null,
                portions = 2
            ),
            ingredients = listOf(
                IngredientEntity(recetteId = recipeId, nom = "Salade romaine", quantite = "1", unite = "pièce"),
                IngredientEntity(recetteId = recipeId, nom = "Escalope de poulet", quantite = "200", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Parmesan", quantite = "50", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Croûtons", quantite = "100", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Mayonnaise", quantite = "3", unite = "c. à s."),
                IngredientEntity(recetteId = recipeId, nom = "Citron", quantite = "1", unite = "pièce"),
                IngredientEntity(recetteId = recipeId, nom = "Ail", quantite = "1", unite = "gousse")
            ),
            etapes = listOf(
                EtapeEntity(recetteId = recipeId, ordre = 1, description = "Laver et couper la salade romaine en lanières."),
                EtapeEntity(recetteId = recipeId, ordre = 2, description = "Cuire le poulet à la poêle, le laisser refroidir et le couper en lamelles."),
                EtapeEntity(recetteId = recipeId, ordre = 3, description = "Préparer la sauce : mélanger mayonnaise, jus de citron et ail pressé."),
                EtapeEntity(recetteId = recipeId, ordre = 4, description = "Dans un saladier, mélanger la salade avec la sauce."),
                EtapeEntity(recetteId = recipeId, ordre = 5, description = "Ajouter le poulet, les croûtons et le parmesan râpé."),
                EtapeEntity(recetteId = recipeId, ordre = 6, description = "Mélanger délicatement et servir immédiatement.")
            ),
            categories = listOf(
                CategorieEntity(recetteId = recipeId, nom = "Salades"),
                CategorieEntity(recetteId = recipeId, nom = "Entrées")
            ),
            tags = listOf(
                TagEntity(recetteId = recipeId, nom = "Froid"),
                TagEntity(recetteId = recipeId, nom = "Léger"),
                TagEntity(recetteId = recipeId, nom = "Été")
            ),
            source = SourceEntity(recetteId = recipeId, type = "homemade")
        )
    }
    
    private fun createTiramisuClassique(): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        return RecipeWithEntities(
            recipe = RecipeEntity(
                id = recipeId,
                nom = "Tiramisu classique",
                preparation = 30,
                cuisson = null,
                portions = 6
            ),
            ingredients = listOf(
                IngredientEntity(recetteId = recipeId, nom = "Mascarpone", quantite = "500", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Œufs", quantite = "3", unite = "pièce"),
                IngredientEntity(recetteId = recipeId, nom = "Sucre", quantite = "75", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Biscuits à la cuillère", quantite = "200", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Café fort", quantite = "250", unite = "ml"),
                IngredientEntity(recetteId = recipeId, nom = "Cacao en poudre", quantite = "2", unite = "c. à s."),
                IngredientEntity(recetteId = recipeId, nom = "Amaretto", quantite = "2", unite = "c. à s.", indispensable = false)
            ),
            etapes = listOf(
                EtapeEntity(recetteId = recipeId, ordre = 1, description = "Séparer les blancs des jaunes d'œufs."),
                EtapeEntity(recetteId = recipeId, ordre = 2, description = "Fouetter les jaunes avec le sucre jusqu'à ce que le mélange blanchisse."),
                EtapeEntity(recetteId = recipeId, ordre = 3, description = "Incorporer le mascarpone au mélange."),
                EtapeEntity(recetteId = recipeId, ordre = 4, description = "Monter les blancs en neige et les incorporer délicatement."),
                EtapeEntity(recetteId = recipeId, ordre = 5, description = "Tremper rapidement les biscuits dans le café et disposer une couche dans le plat."),
                EtapeEntity(recetteId = recipeId, ordre = 6, description = "Recouvrir de crème, répéter l'opération."),
                EtapeEntity(recetteId = recipeId, ordre = 7, description = "Réserver au frais 4h minimum, saupoudrer de cacao avant de servir.")
            ),
            categories = listOf(
                CategorieEntity(recetteId = recipeId, nom = "Desserts"),
                CategorieEntity(recetteId = recipeId, nom = "Desserts froids")
            ),
            tags = listOf(
                TagEntity(recetteId = recipeId, nom = "Italien"),
                TagEntity(recetteId = recipeId, nom = "Fête"),
                TagEntity(recetteId = recipeId, nom = "Sans cuisson")
            ),
            source = SourceEntity(recetteId = recipeId, type = "homemade")
        )
    }
    
    private fun createSoupeTomates(): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        return RecipeWithEntities(
            recipe = RecipeEntity(
                id = recipeId,
                nom = "Soupe de tomates maison",
                preparation = 15,
                cuisson = 30,
                portions = 4
            ),
            ingredients = listOf(
                IngredientEntity(recetteId = recipeId, nom = "Tomates", quantite = "1", unite = "kg"),
                IngredientEntity(recetteId = recipeId, nom = "Oignon", quantite = "1", unite = "pièce"),
                IngredientEntity(recetteId = recipeId, nom = "Ail", quantite = "2", unite = "gousse"),
                IngredientEntity(recetteId = recipeId, nom = "Bouillon de légumes", quantite = "500", unite = "ml"),
                IngredientEntity(recetteId = recipeId, nom = "Huile d'olive", quantite = "2", unite = "c. à s."),
                IngredientEntity(recetteId = recipeId, nom = "Basilic frais", quantite = "10", unite = "feuilles"),
                IngredientEntity(recetteId = recipeId, nom = "Sel et poivre", quantite = "", unite = "")
            ),
            etapes = listOf(
                EtapeEntity(recetteId = recipeId, ordre = 1, description = "Laver et couper les tomates en quartiers."),
                EtapeEntity(recetteId = recipeId, ordre = 2, description = "Éplucher et émincer l'oignon et l'ail."),
                EtapeEntity(recetteId = recipeId, ordre = 3, description = "Faire revenir l'oignon dans l'huile d'olive."),
                EtapeEntity(recetteId = recipeId, ordre = 4, description = "Ajouter l'ail et les tomates, cuire 10 minutes."),
                EtapeEntity(recetteId = recipeId, ordre = 5, description = "Verser le bouillon, laisser mijoter 20 minutes."),
                EtapeEntity(recetteId = recipeId, ordre = 6, description = "Mixer la soupe, ajouter le basilic, saler et poivrer.")
            ),
            categories = listOf(
                CategorieEntity(recetteId = recipeId, nom = "Soupes"),
                CategorieEntity(recetteId = recipeId, nom = "Entrées")
            ),
            tags = listOf(
                TagEntity(recetteId = recipeId, nom = "Végétarien"),
                TagEntity(recetteId = recipeId, nom = "Réconfortant"),
                TagEntity(recetteId = recipeId, nom = "Hiver")
            ),
            source = SourceEntity(recetteId = recipeId, type = "homemade")
        )
    }
    
    private fun createCroqueMonsieur(): RecipeWithEntities {
        val recipeId = UUID.randomUUID().toString()
        
        return RecipeWithEntities(
            recipe = RecipeEntity(
                id = recipeId,
                nom = "Croque-monsieur traditionnel",
                preparation = 10,
                cuisson = 15,
                portions = 2
            ),
            ingredients = listOf(
                IngredientEntity(recetteId = recipeId, nom = "Pain de mie", quantite = "4", unite = "tranche"),
                IngredientEntity(recetteId = recipeId, nom = "Jambon blanc", quantite = "4", unite = "tranche"),
                IngredientEntity(recetteId = recipeId, nom = "Gruyère râpé", quantite = "100", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Beurre", quantite = "30", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Farine", quantite = "20", unite = "g"),
                IngredientEntity(recetteId = recipeId, nom = "Lait", quantite = "200", unite = "ml"),
                IngredientEntity(recetteId = recipeId, nom = "Muscade", quantite = "1", unite = "pincée")
            ),
            etapes = listOf(
                EtapeEntity(recetteId = recipeId, ordre = 1, description = "Préparer une béchamel : faire fondre le beurre, ajouter la farine."),
                EtapeEntity(recetteId = recipeId, ordre = 2, description = "Verser le lait progressivement en remuant, assaisonner."),
                EtapeEntity(recetteId = recipeId, ordre = 3, description = "Beurrer les tranches de pain de mie."),
                EtapeEntity(recetteId = recipeId, ordre = 4, description = "Garnir de jambon et d'un peu de gruyère."),
                EtapeEntity(recetteId = recipeId, ordre = 5, description = "Recouvrir de béchamel et de gruyère."),
                EtapeEntity(recetteId = recipeId, ordre = 6, description = "Passer au four à 200°C pendant 15 minutes jusqu'à dorure.")
            ),
            categories = listOf(
                CategorieEntity(recetteId = recipeId, nom = "Plats principaux"),
                CategorieEntity(recetteId = recipeId, nom = "Sandwichs")
            ),
            tags = listOf(
                TagEntity(recetteId = recipeId, nom = "Français"),
                TagEntity(recetteId = recipeId, nom = "Rapide"),
                TagEntity(recetteId = recipeId, nom = "Enfants")
            ),
            source = SourceEntity(recetteId = recipeId, type = "homemade")
        )
    }
}