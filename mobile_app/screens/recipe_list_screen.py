"""
Recipe List Screen for LibraRecipes Mobile App
Shows all recipes in a scrollable list with sorting and categorization options
"""

from kivy.app import App
from kivy.metrics import dp
from kivymd.uix.appbar import MDActionTopAppBarButton, MDTopAppBar, MDTopAppBarLeadingButtonContainer, MDTopAppBarTitle
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton, MDButtonText
from kivymd.uix.card import MDCard
from kivymd.uix.label import MDLabel
from kivymd.uix.list import MDList
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from sqlalchemy import func
from sqlalchemy.orm import joinedload

from src.crud.recettes import list_recettes
from src.db import get_db_session
from src.model import Execution, Recette


class RecipeListScreen(MDScreen):
    """Screen showing list of all recipes with sorting and categorization"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Sorting options
        self.sort_type = "alphabetical"  # "alphabetical", "date_added", "last_execution"
        self.sort_order = "asc"  # "asc", "desc"
        self.build_screen()

    def build_screen(self):
        """Build the recipe list screen layout"""
        # Main layout
        main_layout = MDBoxLayout(orientation="vertical")

        # App bar with back button
        app_bar = MDTopAppBar(
            MDTopAppBarLeadingButtonContainer(
                MDActionTopAppBarButton(icon="arrow-left", on_release=lambda x: self.go_back())
            ),
            MDTopAppBarTitle(text="All Recipes"),
            md_bg_color=App.get_running_app().colors["primary"],
        )
        main_layout.add_widget(app_bar)

        # Sorting controls
        sorting_section = self.create_sorting_controls()
        main_layout.add_widget(sorting_section)

        # Recipe list
        self.recipe_list = self.create_recipe_list()
        main_layout.add_widget(self.recipe_list)

        self.add_widget(main_layout)

    def create_sorting_controls(self):
        """Create sorting and filtering controls"""
        card = MDCard(padding=dp(15), spacing=dp(10), elevation=1, radius=[dp(8)], size_hint_y=None, height=dp(120))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(8))

        # Sort type row
        type_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(35))

        type_label = MDLabel(
            text="Trier par:", font_size=dp(14), theme_text_color="Primary", size_hint_x=None, width=dp(70)
        )

        # Sort type buttons
        self.alpha_btn = MDButton(
            MDButtonText(text="ABC", font_size=dp(12)),
            style="filled" if self.sort_type == "alphabetical" else "outlined",
            md_bg_color="#4CAF50" if self.sort_type == "alphabetical" else [0, 0, 0, 0],
            size_hint_x=0.33,
            on_release=lambda x: self.set_sort_type("alphabetical"),
        )

        self.date_btn = MDButton(
            MDButtonText(text="Date+", font_size=dp(12)),
            style="filled" if self.sort_type == "date_added" else "outlined",
            md_bg_color="#2196F3" if self.sort_type == "date_added" else [0, 0, 0, 0],
            size_hint_x=0.33,
            on_release=lambda x: self.set_sort_type("date_added"),
        )

        self.exec_btn = MDButton(
            MDButtonText(text="Exec", font_size=dp(12)),
            style="filled" if self.sort_type == "last_execution" else "outlined",
            md_bg_color="#FF9800" if self.sort_type == "last_execution" else [0, 0, 0, 0],
            size_hint_x=0.33,
            on_release=lambda x: self.set_sort_type("last_execution"),
        )

        type_layout.add_widget(type_label)
        type_layout.add_widget(self.alpha_btn)
        type_layout.add_widget(self.date_btn)
        type_layout.add_widget(self.exec_btn)

        # Sort order row
        order_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(35))

        order_label = MDLabel(
            text="Ordre:", font_size=dp(14), theme_text_color="Primary", size_hint_x=None, width=dp(70)
        )

        self.asc_btn = MDButton(
            MDButtonText(text="↑ Croissant", font_size=dp(12)),
            style="filled" if self.sort_order == "asc" else "outlined",
            md_bg_color="#4CAF50" if self.sort_order == "asc" else [0, 0, 0, 0],
            size_hint_x=0.5,
            on_release=lambda x: self.set_sort_order("asc"),
        )

        self.desc_btn = MDButton(
            MDButtonText(text="↓ Décroissant", font_size=dp(12)),
            style="filled" if self.sort_order == "desc" else "outlined",
            md_bg_color="#F44336" if self.sort_order == "desc" else [0, 0, 0, 0],
            size_hint_x=0.5,
            on_release=lambda x: self.set_sort_order("desc"),
        )

        order_layout.add_widget(order_label)
        order_layout.add_widget(self.asc_btn)
        order_layout.add_widget(self.desc_btn)

        layout.add_widget(type_layout)
        layout.add_widget(order_layout)
        card.add_widget(layout)

        # Apply correct colors after creation
        self.update_sort_buttons()

        return card

    def set_sort_type(self, sort_type):
        """Set sorting type and refresh list"""
        self.sort_type = sort_type
        self.update_sort_buttons()
        self.refresh_recipe_list()

    def set_sort_order(self, sort_order):
        """Set sorting order and refresh list"""
        self.sort_order = sort_order
        self.update_sort_buttons()
        self.refresh_recipe_list()

    def update_sort_buttons(self):
        """Update button styles based on current selection"""
        # Update sort type buttons
        type_buttons = [
            (self.alpha_btn, "alphabetical", "#4CAF50"),
            (self.date_btn, "date_added", "#2196F3"),
            (self.exec_btn, "last_execution", "#FF9800"),
        ]

        for btn, btn_type, color in type_buttons:
            if self.sort_type == btn_type:
                btn.style = "filled"
                btn.md_bg_color = color
                # Set text color to white for filled buttons
                for child in btn.children:
                    if hasattr(child, "theme_text_color"):
                        child.theme_text_color = "Custom"
                        child.text_color = [1, 1, 1, 1]  # White
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]
                # Set text color to green for outlined buttons
                for child in btn.children:
                    if hasattr(child, "theme_text_color"):
                        child.theme_text_color = "Custom"
                        child.text_color = "#4CAF50"  # Green

        # Update sort order buttons
        order_buttons = [(self.asc_btn, "asc", "#4CAF50"), (self.desc_btn, "desc", "#F44336")]

        for btn, btn_order, color in order_buttons:
            if self.sort_order == btn_order:
                btn.style = "filled"
                btn.md_bg_color = color
                # Set text color to white for filled buttons
                for child in btn.children:
                    if hasattr(child, "theme_text_color"):
                        child.theme_text_color = "Custom"
                        child.text_color = [1, 1, 1, 1]  # White
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]
                # Set text color to green for outlined buttons
                for child in btn.children:
                    if hasattr(child, "theme_text_color"):
                        child.theme_text_color = "Custom"
                        child.text_color = "#4CAF50"  # Green

    def refresh_recipe_list(self):
        """Refresh the recipe list with current sorting"""
        # Remove old list
        main_layout = self.children[0]
        main_layout.remove_widget(self.recipe_list)

        # Create new sorted list
        self.recipe_list = self.create_recipe_list()
        main_layout.add_widget(self.recipe_list)

    def create_recipe_list(self):
        """Create scrollable list of recipes with sorting and categorization"""
        scroll = MDScrollView()
        list_widget = MDList(spacing=dp(5))

        # Load and sort recipes from database
        recipes = self.load_and_sort_recipes()

        if not recipes:
            # Empty state
            empty_card = MDCard(
                padding=dp(20), spacing=dp(10), elevation=1, radius=[dp(10)], size_hint_y=None, height=dp(100)
            )

            empty_layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

            empty_label = MDLabel(
                text="📝 No recipes yet!", font_size=dp(18), halign="center", theme_text_color="Secondary"
            )

            add_label = MDLabel(
                text="Add your first recipe to get started.",
                font_size=dp(14),
                halign="center",
                theme_text_color="Secondary",
            )

            empty_layout.add_widget(empty_label)
            empty_layout.add_widget(add_label)
            empty_card.add_widget(empty_layout)

            list_widget.add_widget(empty_card)
        else:
            # Group recipes by category (letter or date)
            grouped_recipes = self.group_recipes(recipes)

            # Add categorized sections
            for category, category_recipes in grouped_recipes.items():
                # Add category header
                header = self.create_category_header(category)
                list_widget.add_widget(header)

                # Add recipe items in this category
                for recipe in category_recipes:
                    item = self.create_recipe_item(recipe)
                    list_widget.add_widget(item)

        scroll.add_widget(list_widget)
        return scroll

    def load_and_sort_recipes(self):
        """Load recipes from database with sorting applied"""
        try:
            with get_db_session() as session:
                # Base query with all necessary joins to avoid lazy loading issues
                base_query = session.query(Recette).options(
                    joinedload(Recette.categories), joinedload(Recette.tags), joinedload(Recette.executions)
                )

                if self.sort_type == "alphabetical":
                    # Sort by name
                    recipes = base_query.order_by(
                        Recette.nom.asc() if self.sort_order == "asc" else Recette.nom.desc()
                    ).all()
                elif self.sort_type == "date_added":
                    # Sort by date added
                    recipes = base_query.order_by(
                        Recette.date_ajout.asc() if self.sort_order == "asc" else Recette.date_ajout.desc()
                    ).all()
                elif self.sort_type == "last_execution":
                    # Sort by last execution date
                    recipes = (
                        base_query.outerjoin(Execution)
                        .group_by(Recette.id)
                        .order_by(
                            func.max(Execution.date_execution).asc().nullslast()
                            if self.sort_order == "asc"
                            else func.max(Execution.date_execution).desc().nullslast()
                        )
                        .all()
                    )
                else:
                    # Default to alphabetical
                    recipes = base_query.order_by(Recette.nom.asc()).all()

                return recipes
        except Exception as e:
            print(f"Error loading recipes: {e}")
            return []

    def group_recipes(self, recipes):
        """Group recipes by category based on sort type"""
        grouped = {}

        if self.sort_type == "alphabetical":
            # Group by first letter
            for recipe in recipes:
                first_letter = recipe.nom[0].upper() if recipe.nom else "#"
                if first_letter not in grouped:
                    grouped[first_letter] = []
                grouped[first_letter].append(recipe)

        elif self.sort_type == "date_added":
            # Group by date (month/year)
            for recipe in recipes:
                if recipe.date_ajout:
                    date_key = recipe.date_ajout.strftime("%B %Y")
                else:
                    date_key = "Date inconnue"

                if date_key not in grouped:
                    grouped[date_key] = []
                grouped[date_key].append(recipe)

        elif self.sort_type == "last_execution":
            # Group by execution period
            for recipe in recipes:
                # Get last execution date
                last_exec = None
                if recipe.executions:
                    last_exec = max(exec.date_execution for exec in recipe.executions)

                if last_exec:
                    date_key = last_exec.strftime("%B %Y")
                else:
                    date_key = "Jamais exécutée"

                if date_key not in grouped:
                    grouped[date_key] = []
                grouped[date_key].append(recipe)

        return grouped

    def create_category_header(self, category):
        """Create a category header"""
        header_card = MDCard(
            padding=dp(10), elevation=0, md_bg_color="#E3F2FD", radius=[dp(5)], size_hint_y=None, height=dp(40)
        )

        header_label = MDLabel(text=category, font_size=dp(16), bold=True, theme_text_color="Primary", halign="left")

        header_card.add_widget(header_label)
        return header_card

    def create_recipe_item(self, recipe):
        """Create a single recipe list item"""
        # Calculate height based on content
        base_height = dp(80)  # Base height for title + details
        if recipe.categories:
            base_height += dp(25)  # Extra height for categories line

        card = MDCard(
            padding=dp(15),
            spacing=dp(10),
            elevation=2,
            radius=[dp(8)],
            size_hint_y=None,
            height=base_height,
            on_release=lambda x: self.view_recipe(recipe.id),
        )

        layout = MDBoxLayout(orientation="horizontal", spacing=dp(15))

        # Recipe info
        info_layout = MDBoxLayout(orientation="vertical", spacing=dp(5))

        # Recipe name
        name_label = MDLabel(
            text=recipe.nom, font_size=dp(16), bold=True, theme_text_color="Primary", size_hint_y=None, height=dp(25)
        )

        # Recipe details
        details = []
        if recipe.preparation:
            details.append(f"{recipe.preparation}min")
        if recipe.portions:
            details.append(f"{recipe.portions} portions")

        details_text = " | ".join(details) if details else "No timing info"

        details_label = MDLabel(
            text=details_text, font_size=dp(12), theme_text_color="Secondary", size_hint_y=None, height=dp(20)
        )

        # Categories
        if recipe.categories:
            categories_text = ", ".join([c.nom for c in recipe.categories[:2]])
            if len(recipe.categories) > 2:
                categories_text += "..."

            categories_label = MDLabel(
                text=f"Tags: {categories_text}",
                font_size=dp(11),
                theme_text_color="Secondary",
                size_hint_y=None,
                height=dp(18),
            )

        # Add widgets in correct order: name, details, then categories
        info_layout.add_widget(name_label)
        info_layout.add_widget(details_label)

        # Add categories last if they exist
        if recipe.categories:
            info_layout.add_widget(categories_label)

        layout.add_widget(info_layout)
        card.add_widget(layout)

        return card

    def load_recipes(self):
        """Load recipes from database (legacy method - kept for compatibility)"""
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
        App.get_running_app().previous_screen = "recipe_list"  # Remember we came from recipe list
        self.manager.current = "recipe_detail"

    def go_back(self):
        """Go back to home screen"""
        self.manager.current = "home"

    def on_enter(self):
        """Called when screen is entered - refresh recipe list"""
        # Refresh the recipe list to show any new recipes with current sorting
        self.refresh_recipe_list()
