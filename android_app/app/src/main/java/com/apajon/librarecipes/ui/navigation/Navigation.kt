package com.apajon.librarecipes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apajon.librarecipes.ui.screens.HomeScreen
import com.apajon.librarecipes.ui.screens.RecipeListScreen
import com.apajon.librarecipes.ui.screens.RecipeDetailScreen
import com.apajon.librarecipes.ui.screens.CreateRecipeScreen
import com.apajon.librarecipes.ui.screens.EditRecipeScreen
import com.apajon.librarecipes.ui.screens.SearchScreen
import com.apajon.librarecipes.ui.screens.QueChoisirScreen

@Composable
fun LibraRecipesNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("recipes") {
            RecipeListScreen(navController = navController)
        }
        composable("recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeDetailScreen(
                recipeId = recipeId,
                navController = navController
            )
        }
        composable("create_recipe") {
            CreateRecipeScreen(navController = navController)
        }
        composable("edit_recipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            EditRecipeScreen(
                recipeId = recipeId,
                navController = navController
            )
        }
        composable("edit_recipe/{recipeId}/section/{section}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val section = backStackEntry.arguments?.getString("section") ?: ""
            EditRecipeScreen(
                recipeId = recipeId,
                editSection = section,
                navController = navController
            )
        }
        composable("create_new_recipe") {
            EditRecipeScreen(
                recipeId = null,
                navController = navController
            )
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("que_choisir") {
            QueChoisirScreen(navController = navController)
        }
    }
}