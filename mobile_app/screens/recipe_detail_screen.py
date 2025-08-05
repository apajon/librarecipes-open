"""
Recipe Detail Screen for LibraRecipes Mobile App
Shows full recipe details with ingredients, steps, and photos
"""

from typing import Optional

from kivy.app import App
from kivy.metrics import dp
from kivymd.uix.appbar import (
    MDActionTopAppBarButton,
    MDTopAppBar,
    MDTopAppBarLeadingButtonContainer,
    MDTopAppBarTitle,
    MDTopAppBarTrailingButtonContainer,
)
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton
from kivymd.uix.button.button import MDButtonText
from kivymd.uix.card import MDCard
from kivymd.uix.chip import MDChip
from kivymd.uix.gridlayout import MDGridLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.list import MDList
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.snackbar import MDSnackbar, MDSnackbarText

from src.crud.recettes import delete_recette, get_recette_by_id
from src.db import get_db_session


def show_snackbar(text: str):
    """Helper function to show snackbar with KivyMD 2.0 syntax"""
    MDSnackbar(MDSnackbarText(text=text)).open()


class RecipeDetailScreen(MDScreen):
    """Screen showing detailed recipe information"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.recipe: Optional[any] = None  # Type hint for recipe object
        self.build_screen()

    def build_screen(self):
        """Build the recipe detail screen layout"""
        # Main layout
        self.main_layout = MDBoxLayout(orientation="vertical")

        # App bar
        self.app_bar = MDTopAppBar(
            MDTopAppBarLeadingButtonContainer(
                MDActionTopAppBarButton(icon="arrow-left", on_release=lambda x: self.go_back())
            ),
            MDTopAppBarTitle(text="Recipe Details"),
            MDTopAppBarTrailingButtonContainer(
                MDActionTopAppBarButton(icon="delete", on_release=lambda x: self.delete_recipe())
            ),
            md_bg_color=App.get_running_app().colors["primary"],
        )
        self.main_layout.add_widget(self.app_bar)

        # Content placeholder
        self.content_area = MDBoxLayout(orientation="vertical")
        self.main_layout.add_widget(self.content_area)

        self.add_widget(self.main_layout)

    def load_recipe_content(self):
        """Load and display recipe content"""
        if not hasattr(App.get_running_app(), "selected_recipe_id"):
            self.show_error("No recipe selected")
            return

        recipe_id = App.get_running_app().selected_recipe_id

        try:
            with get_db_session() as session:
                self.recipe = get_recette_by_id(session, recipe_id)

            if not self.recipe:
                self.show_error("Recipe not found")
                return

            # Update app bar title
            self.app_bar.title = self.recipe.nom

            # Clear and rebuild content
            self.content_area.clear_widgets()

            # Create scrollable content
            scroll = MDScrollView()
            content = MDBoxLayout(orientation="vertical", spacing=dp(16), padding=dp(16), adaptive_height=True)

            # Recipe header
            header = self.create_recipe_header()
            content.add_widget(header)

            # Categories and tags
            if self.recipe.categories or self.recipe.tags:
                categories_section = self.create_categories_section()
                content.add_widget(categories_section)

            # Ingredients
            ingredients_section = self.create_ingredients_section()
            content.add_widget(ingredients_section)

            # Steps
            steps_section = self.create_steps_section()
            content.add_widget(steps_section)

            # Recipe info
            info_section = self.create_info_section()
            content.add_widget(info_section)

            scroll.add_widget(content)
            self.content_area.add_widget(scroll)

        except Exception as e:
            print(f"Error loading recipe: {e}")
            self.show_error("Error loading recipe")

    def create_recipe_header(self):
        """Create recipe header with basic info"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], size_hint_y=None, height=dp(120))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

        # Recipe name
        name_label = MDLabel(
            text=self.recipe.nom if self.recipe else "Unknown Recipe",
            font_size=dp(24),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(40),
        )

        # Time and portions info
        info_layout = MDBoxLayout(orientation="horizontal", spacing=dp(20), size_hint_y=None, height=dp(30))

        if self.recipe and self.recipe.preparation:
            prep_label = MDLabel(
                text=f"⏱️ Prep: {self.recipe.preparation}min",
                font_size=dp(14),
                theme_text_color="Secondary",
                size_hint_x=None,
                width=dp(100),
            )
            info_layout.add_widget(prep_label)

        if self.recipe and self.recipe.cuisson:
            cook_label = MDLabel(
                text=f"🔥 Cook: {self.recipe.cuisson}min",
                font_size=dp(14),
                theme_text_color="Secondary",
                size_hint_x=None,
                width=dp(100),
            )
            info_layout.add_widget(cook_label)

        if self.recipe and self.recipe.portions:
            portions_label = MDLabel(
                text=f"👥 Serves: {self.recipe.portions}",
                font_size=dp(14),
                theme_text_color="Secondary",
                size_hint_x=None,
                width=dp(100),
            )
            info_layout.add_widget(portions_label)

        # Total time
        total_time = 0
        if self.recipe:
            total_time = (self.recipe.preparation or 0) + (self.recipe.cuisson or 0)
        if total_time > 0:
            total_label = MDLabel(
                text=f"⏰ Total: {total_time}min",
                font_size=dp(14),
                bold=True,
                theme_text_color="Custom",
                text_color=App.get_running_app().colors["primary"],
            )
            info_layout.add_widget(total_label)

        layout.add_widget(name_label)
        layout.add_widget(info_layout)
        card.add_widget(layout)

        return card

    def create_categories_section(self):
        """Create categories and tags section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Title
        title = MDLabel(
            text="🏷️ Categories & Tags",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )
        layout.add_widget(title)

        # Categories chips
        if self.recipe and self.recipe.categories:
            categories_layout = MDGridLayout(cols=3, spacing=dp(8), adaptive_height=True, size_hint_y=None)

            for category in self.recipe.categories:
                chip = MDChip(
                    text=category.nom,
                    md_bg_color=App.get_running_app().colors["secondary_peach"],
                    text_color=App.get_running_app().colors["dark_curry"],
                    size_hint_x=None,
                    height=dp(32),
                )
                categories_layout.add_widget(chip)

            layout.add_widget(categories_layout)

        # Tags chips
        if self.recipe and self.recipe.tags:
            tags_layout = MDGridLayout(cols=3, spacing=dp(8), adaptive_height=True, size_hint_y=None)

            for tag in self.recipe.tags:
                chip = MDChip(
                    text=tag.nom,
                    md_bg_color=App.get_running_app().colors["navy"],
                    text_color="white",
                    size_hint_x=None,
                    height=dp(32),
                )
                tags_layout.add_widget(chip)

            layout.add_widget(tags_layout)

        card.add_widget(layout)
        return card

    def create_ingredients_section(self):
        """Create ingredients section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Title
        title = MDLabel(
            text="🧂 Ingredients",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )
        layout.add_widget(title)

        # Ingredients list
        ingredients_list = MDList(adaptive_height=True)

        if self.recipe and self.recipe.ingredients:
            for ingredient in self.recipe.ingredients:
                # Build ingredient text
                text = f"• {ingredient.nom}"
                if ingredient.quantite:
                    text += f" - {ingredient.quantite}"
                if ingredient.unite:
                    text += f" {ingredient.unite}"

                # Add essential/optional indicator
                if not ingredient.indispensable:
                    text += " (optional)"

                ingredient_label = MDLabel(
                    text=text,
                    font_size=dp(14),
                    theme_text_color="Primary" if ingredient.indispensable else "Secondary",
                    text_size=(None, None),
                    adaptive_height=True,
                )
                ingredients_list.add_widget(ingredient_label)

        layout.add_widget(ingredients_list)
        card.add_widget(layout)

        return card

    def create_steps_section(self):
        """Create preparation steps section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Title
        title = MDLabel(
            text="📋 Preparation Steps",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )
        layout.add_widget(title)

        # Steps list
        steps_list = MDList(adaptive_height=True)

        if self.recipe and self.recipe.etapes:
            for step in sorted(self.recipe.etapes, key=lambda x: x.ordre):
                step_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), adaptive_height=True)

                # Step number
                step_number = MDLabel(
                    text=str(step.ordre),
                    font_size=dp(16),
                    bold=True,
                    theme_text_color="Custom",
                    text_color=(
                        App.get_running_app().colors["primary"]
                        if App.get_running_app() and hasattr(App.get_running_app(), "colors")
                        else "#000000"
                    ),
                    size_hint_x=None,
                    width=dp(30),
                    halign="center",
                )

                # Step description
                step_desc = MDLabel(
                    text=step.description,
                    font_size=dp(14),
                    theme_text_color="Primary",
                    text_size=(None, None),
                    adaptive_height=True,
                )

                step_layout.add_widget(step_number)
                step_layout.add_widget(step_desc)
                steps_list.add_widget(step_layout)

        layout.add_widget(steps_list)
        card.add_widget(layout)

        return card

    def create_info_section(self):
        """Create additional recipe info section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], size_hint_y=None, height=dp(100))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

        # Title
        title = MDLabel(
            text="ℹ️ Recipe Info",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Date added
        date_added = "Unknown"
        if self.recipe and self.recipe.date_ajout:
            date_added = self.recipe.date_ajout.strftime("%B %d, %Y")
        date_label = MDLabel(
            text=f"📅 Added: {date_added}",
            font_size=dp(14),
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(25),
        )

        # Source info
        source_text = "🏠 Homemade recipe"
        if self.recipe and self.recipe.source:
            if self.recipe.source.type == "url" and self.recipe.source.url:
                source_text = f"🌐 From: {self.recipe.source.url}"
            elif self.recipe.source.type == "book" and self.recipe.source.book_title:
                source_text = f"📚 From: {self.recipe.source.book_title}"

        source_label = MDLabel(
            text=source_text, font_size=dp(14), theme_text_color="Secondary", size_hint_y=None, height=dp(25)
        )

        layout.add_widget(title)
        layout.add_widget(date_label)
        layout.add_widget(source_label)
        card.add_widget(layout)

        return card

    def show_error(self, message):
        """Show error message"""
        self.content_area.clear_widgets()

        error_card = MDCard(
            padding=dp(20), spacing=dp(10), elevation=1, radius=[dp(10)], size_hint_y=None, height=dp(100)
        )

        error_layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

        error_label = MDLabel(text=f"❌ {message}", font_size=dp(18), halign="center", theme_text_color="Error")

        back_btn = MDButton(
            size_hint_x=None,
            width=dp(120),
            pos_hint={"center_x": 0.5},
            on_release=lambda x: self.go_back(),
            children=[MDButtonText(text="Go Back")],
        )

        error_layout.add_widget(error_label)
        error_layout.add_widget(back_btn)
        error_card.add_widget(error_layout)

        self.content_area.add_widget(error_card)

    def delete_recipe(self):
        """Delete the current recipe"""
        if not self.recipe:
            return

        try:
            with get_db_session() as session:
                success = delete_recette(session, self.recipe.id)

            if success:
                show_snackbar(f"Recipe '{self.recipe.nom if self.recipe else 'Unknown'}' deleted")
                self.manager.current = "recipe_list"
            else:
                show_snackbar("Error deleting recipe")
        except Exception as e:
            print(f"Error deleting recipe: {e}")
            show_snackbar("Error deleting recipe")

    def go_back(self):
        """Go back to recipe list"""
        self.manager.current = "recipe_list"

    def on_enter(self):
        """Called when screen is entered"""
        self.load_recipe_content()
