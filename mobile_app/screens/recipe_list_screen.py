"""
Recipe List Screen for LibraRecipes Mobile App
Shows all recipes in a scrollable list
"""

from kivymd.uix.screen import MDScreen
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.card import MDCard
from kivymd.uix.toolbar import MDTopAppBar
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.list import MDList, OneLineListItem
from kivymd.uix.button import MDIconButton
from kivy.metrics import dp
from kivy.app import App

from src.db import get_db_session
from src.crud.recettes import list_recettes


class RecipeListScreen(MDScreen):
    """Screen showing list of all recipes"""
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.build_screen()
    
    def build_screen(self):
        """Build the recipe list screen layout"""
        # Main layout
        main_layout = MDBoxLayout(
            orientation='vertical'
        )
        
        # App bar with back button
        app_bar = MDTopAppBar(
            title="All Recipes",
            md_bg_color=App.get_running_app().colors['primary'],
            specific_text_color="white",
            left_action_items=[["arrow-left", lambda x: self.go_back()]]
        )
        main_layout.add_widget(app_bar)
        
        # Recipe list
        self.recipe_list = self.create_recipe_list()
        main_layout.add_widget(self.recipe_list)
        
        self.add_widget(main_layout)
    
    def create_recipe_list(self):
        """Create scrollable list of recipes"""
        scroll = MDScrollView()
        
        list_widget = MDList()
        
        # Load recipes from database
        recipes = self.load_recipes()
        
        if not recipes:
            # Empty state
            empty_card = MDCard(
                padding=dp(20),
                spacing=dp(10),
                elevation=1,
                radius=[dp(10)],
                size_hint_y=None,
                height=dp(100)
            )
            
            empty_layout = MDBoxLayout(
                orientation='vertical',
                spacing=dp(10)
            )
            
            empty_label = MDLabel(
                text="📝 No recipes yet!",
                font_size=dp(18),
                halign="center",
                theme_text_color="Secondary"
            )
            
            add_label = MDLabel(
                text="Add your first recipe to get started.",
                font_size=dp(14),
                halign="center", 
                theme_text_color="Secondary"
            )
            
            empty_layout.add_widget(empty_label)
            empty_layout.add_widget(add_label)
            empty_card.add_widget(empty_layout)
            
            list_widget.add_widget(empty_card)
        else:
            # Add recipe items
            for recipe in recipes:
                item = self.create_recipe_item(recipe)
                list_widget.add_widget(item)
        
        scroll.add_widget(list_widget)
        return scroll
    
    def create_recipe_item(self, recipe):
        """Create a single recipe list item"""
        card = MDCard(
            padding=dp(15),
            spacing=dp(10),
            elevation=2,
            radius=[dp(8)],
            size_hint_y=None,
            height=dp(80),
            on_release=lambda x: self.view_recipe(recipe.id)
        )
        
        layout = MDBoxLayout(
            orientation='horizontal',
            spacing=dp(15)
        )
        
        # Recipe info
        info_layout = MDBoxLayout(
            orientation='vertical',
            spacing=dp(5)
        )
        
        # Recipe name
        name_label = MDLabel(
            text=recipe.nom,
            font_size=dp(16),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(25)
        )
        
        # Recipe details
        details = []
        if recipe.preparation:
            details.append(f"⏱️ {recipe.preparation}min")
        if recipe.portions:
            details.append(f"👥 {recipe.portions} portions")
        
        details_text = " | ".join(details) if details else "No timing info"
        
        details_label = MDLabel(
            text=details_text,
            font_size=dp(12),
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(20)
        )
        
        # Categories
        if recipe.categories:
            categories_text = ", ".join([c.nom for c in recipe.categories[:2]])
            if len(recipe.categories) > 2:
                categories_text += "..."
                
            categories_label = MDLabel(
                text=f"🏷️ {categories_text}",
                font_size=dp(11),
                theme_text_color="Secondary",
                size_hint_y=None,
                height=dp(18)
            )
            info_layout.add_widget(categories_label)
        
        info_layout.add_widget(name_label)
        info_layout.add_widget(details_label)
        
        # Arrow icon
        arrow_icon = MDIconButton(
            icon="chevron-right",
            theme_icon_color="Custom",
            icon_color=App.get_running_app().colors['primary'],
            size_hint_x=None,
            width=dp(40)
        )
        
        layout.add_widget(info_layout)
        layout.add_widget(arrow_icon)
        card.add_widget(layout)
        
        return card
    
    def load_recipes(self):
        """Load recipes from database"""
        try:
            with get_db_session() as session:
                return list_recettes(session)
        except Exception as e:
            print(f"Error loading recipes: {e}")
            return []
    
    def view_recipe(self, recipe_id):
        """Navigate to recipe detail view"""
        # Store recipe ID in app for detail screen
        App.get_running_app().selected_recipe_id = recipe_id
        self.manager.current = 'recipe_detail'
    
    def go_back(self):
        """Go back to home screen"""
        self.manager.current = 'home'
    
    def on_enter(self):
        """Called when screen is entered - refresh recipe list"""
        # Rebuild the recipe list to show any new recipes
        main_layout = self.children[0]
        main_layout.remove_widget(self.recipe_list)
        self.recipe_list = self.create_recipe_list()
        main_layout.add_widget(self.recipe_list)