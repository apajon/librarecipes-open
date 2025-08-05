"""
Search Screen for LibraRecipes Mobile App
Advanced search and filtering for recipes
"""

from kivy.app import App
from kivy.metrics import dp
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton
from kivymd.uix.button.button import MDButtonText
from kivymd.uix.card import MDCard
from kivymd.uix.label import MDLabel
from kivymd.uix.list import MDList, ThreeLineListItem
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.selectioncontrol import MDCheckbox
from kivymd.uix.textfield import MDTextField
from kivymd.uix.toolbar import MDTopAppBar

from src.crud.recherche import IngredientsMode, rechercher_recettes
from src.db import get_db_session


class SearchScreen(MDScreen):
    """Screen for searching and filtering recipes"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.search_results = []
        self.build_screen()

    def build_screen(self):
        """Build the search screen layout"""
        # Main layout
        main_layout = MDBoxLayout(orientation="vertical")

        # App bar
        app_bar = MDTopAppBar(
            title="Search Recipes",
            md_bg_color=App.get_running_app().colors["primary"],
            specific_text_color="white",
            left_action_items=[["arrow-left", lambda x: self.go_back()]],
        )
        main_layout.add_widget(app_bar)

        # Scrollable content
        scroll = MDScrollView()
        content = MDBoxLayout(orientation="vertical", spacing=dp(16), padding=dp(16), adaptive_height=True)

        # Search form
        search_form = self.create_search_form()
        content.add_widget(search_form)

        # Results section
        self.results_section = self.create_results_section()
        content.add_widget(self.results_section)

        scroll.add_widget(content)
        main_layout.add_widget(scroll)
        self.add_widget(main_layout)

    def create_search_form(self):
        """Create search form with filters"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], size_hint_y=None, height=dp(300))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12))

        # Title
        title = MDLabel(
            text="🔍 Search Filters",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Recipe name search
        self.name_field = MDTextField(hint_text="Recipe name...", size_hint_y=None, height=dp(40))

        # Ingredients search
        self.ingredients_field = MDTextField(
            hint_text="Ingredients (comma separated)...", size_hint_y=None, height=dp(40)
        )

        # Ingredients mode
        ingredients_mode_layout = MDBoxLayout(
            orientation="horizontal", spacing=dp(10), size_hint_y=None, height=dp(40)
        )

        self.any_ingredients_check = MDCheckbox(active=True, group="ingredients_mode", size_hint_x=None, width=dp(30))

        any_label = MDLabel(text="Any ingredient", theme_text_color="Primary", font_size=dp(14))

        self.all_ingredients_check = MDCheckbox(active=False, group="ingredients_mode", size_hint_x=None, width=dp(30))

        all_label = MDLabel(text="All ingredients", theme_text_color="Primary", font_size=dp(14))

        ingredients_mode_layout.add_widget(self.any_ingredients_check)
        ingredients_mode_layout.add_widget(any_label)
        ingredients_mode_layout.add_widget(self.all_ingredients_check)
        ingredients_mode_layout.add_widget(all_label)

        # Categories and tags
        self.categories_field = MDTextField(
            hint_text="Categories (comma separated)...", size_hint_y=None, height=dp(40)
        )

        self.tags_field = MDTextField(hint_text="Tags (comma separated)...", size_hint_y=None, height=dp(40))

        # Search button
        search_btn = MDButton(
            md_bg_color=App.get_running_app().colors["primary"],
            theme_text_color="Custom",
            text_color="white",
            size_hint_y=None,
            height=dp(40),
            on_release=lambda x: self.perform_search(),
            children=[MDButtonText(text="🔍 Search")],
        )

        layout.add_widget(title)
        layout.add_widget(self.name_field)
        layout.add_widget(self.ingredients_field)
        layout.add_widget(ingredients_mode_layout)
        layout.add_widget(self.categories_field)
        layout.add_widget(self.tags_field)
        layout.add_widget(search_btn)

        card.add_widget(layout)
        return card

    def create_results_section(self):
        """Create results display section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Results title
        self.results_title = MDLabel(
            text="🍽️ Search Results",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Results list
        self.results_list = MDList(adaptive_height=True)

        # Initial empty state
        self.show_empty_results()

        layout.add_widget(self.results_title)
        layout.add_widget(self.results_list)

        card.add_widget(layout)
        return card

    def perform_search(self):
        """Perform recipe search with current filters"""
        try:
            # Get search parameters
            search_params = {
                "nom": self.name_field.text.strip() if self.name_field.text.strip() else None,
                "ingredients": [ing.strip() for ing in self.ingredients_field.text.split(",") if ing.strip()],
                "ingredients_mode": IngredientsMode.ANY if self.any_ingredients_check.active else IngredientsMode.ALL,
                "categories": [cat.strip() for cat in self.categories_field.text.split(",") if cat.strip()],
                "tags": [tag.strip() for tag in self.tags_field.text.split(",") if tag.strip()],
            }

            # Remove empty parameters
            search_params = {k: v for k, v in search_params.items() if v}

            # Perform search
            with get_db_session() as session:
                self.search_results = rechercher_recettes(session, **search_params)

            # Update results display
            self.update_results_display()

        except Exception as e:
            print(f"Search error: {e}")
            from kivymd.uix.snackbar import Snackbar

            Snackbar(text="Search error. Please try again.").open()

    def update_results_display(self):
        """Update the results display with search results"""
        self.results_list.clear_widgets()

        # Update title with count
        count = len(self.search_results)
        self.results_title.text = f"🍽️ Search Results ({count})"

        if not self.search_results:
            self.show_no_results()
            return

        # Add result items
        for recipe in self.search_results:
            item = self.create_result_item(recipe)
            self.results_list.add_widget(item)

    def create_result_item(self, recipe):
        """Create a single search result item"""
        # Build recipe summary
        primary_text = recipe.nom

        # Secondary text with timing info
        secondary_parts = []
        if recipe.preparation:
            secondary_parts.append(f"⏱️ {recipe.preparation}min prep")
        if recipe.cuisson:
            secondary_parts.append(f"🔥 {recipe.cuisson}min cook")
        if recipe.portions:
            secondary_parts.append(f"👥 {recipe.portions} portions")

        secondary_text = " | ".join(secondary_parts) if secondary_parts else "No timing info"

        # Tertiary text with categories
        tertiary_text = ""
        if recipe.categories:
            categories = [c.nom for c in recipe.categories[:2]]
            tertiary_text = f"🏷️ {', '.join(categories)}"
            if len(recipe.categories) > 2:
                tertiary_text += "..."

        item = ThreeLineListItem(
            text=primary_text,
            secondary_text=secondary_text,
            tertiary_text=tertiary_text,
            on_release=lambda x: self.view_recipe(recipe.id),
        )

        return item

    def show_empty_results(self):
        """Show empty search state"""
        empty_label = MDLabel(
            text="🔍 Enter search criteria above to find recipes",
            font_size=dp(14),
            halign="center",
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(40),
        )
        self.results_list.add_widget(empty_label)

    def show_no_results(self):
        """Show no results found state"""
        no_results_label = MDLabel(
            text="❌ No recipes found matching your criteria",
            font_size=dp(14),
            halign="center",
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(40),
        )
        self.results_list.add_widget(no_results_label)

        suggestion_label = MDLabel(
            text="Try adjusting your search filters",
            font_size=dp(12),
            halign="center",
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(30),
        )
        self.results_list.add_widget(suggestion_label)

    def view_recipe(self, recipe_id):
        """Navigate to recipe detail view"""
        App.get_running_app().selected_recipe_id = recipe_id
        self.manager.current = "recipe_detail"

    def clear_search(self):
        """Clear all search fields"""
        self.name_field.text = ""
        self.ingredients_field.text = ""
        self.categories_field.text = ""
        self.tags_field.text = ""
        self.any_ingredients_check.active = True
        self.all_ingredients_check.active = False
        self.search_results = []
        self.results_list.clear_widgets()
        self.show_empty_results()
        self.results_title.text = "🍽️ Search Results"

    def go_back(self):
        """Go back to home screen"""
        self.manager.current = "home"

    def on_enter(self):
        """Called when screen is entered"""
        # Clear search when entering screen
        self.clear_search()
