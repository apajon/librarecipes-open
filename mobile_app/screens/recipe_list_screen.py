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
from kivymd.uix.floatlayout import MDFloatLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.list import MDList
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from sqlalchemy import func
from sqlalchemy.orm import joinedload

from src.crud.recettes import list_recettes
from src.db import get_db_session
from src.model import Execution, Recette

# Import FAB Manager
try:
    from components.fab_manager import FABManager
except ImportError:
    FABManager = None


class RecipeListScreen(MDScreen):
    """Screen showing list of all recipes with sorting and categorization"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Sorting options
        self.sort_type = "alphabetical"  # "alphabetical", "date_added", "last_execution"
        self.sort_order = "asc"  # "asc", "desc"
        # Filtering options
        self.current_filter = None  # Current active filter
        self.filter_buttons = []  # Store filter buttons for updates
        self.fab_manager = None
        self.build_screen()
        self.setup_fabs()

    def setup_fabs(self):
        """Initialize floating action buttons"""
        if FABManager:
            self.fab_manager = FABManager(self, context="recipe_list")

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
        self.sorting_section = self.create_sorting_controls()
        main_layout.add_widget(self.sorting_section)

        # Filtering controls
        self.filtering_section = self.create_filtering_controls()
        main_layout.add_widget(self.filtering_section)

        # Recipe list
        self.recipe_list = self.create_recipe_list()
        main_layout.add_widget(self.recipe_list)

        self.add_widget(main_layout)

    def create_sorting_controls(self):
        """Create sorting controls"""
        card = MDCard(elevation=1, radius=[dp(8)], size_hint_y=None, height=dp(110))

        # Use FloatLayout to center content vertically
        float_layout = MDFloatLayout()

        layout = MDBoxLayout(
            orientation="vertical",
            spacing=dp(5),
            pos_hint={"center_x": 0.5, "center_y": 0.5},
            size_hint=(None, None),
            size=(dp(350), dp(75)),
            padding=[dp(5), dp(8), dp(5), dp(8)],  # left, top, right, bottom
        )

        # Sort type row
        type_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(32))

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
        order_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(32))

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

        # Add layout to float_layout, then float_layout to card
        float_layout.add_widget(layout)
        card.add_widget(float_layout)

        # Apply correct colors after creation
        self.update_sort_buttons()

        return card

    def set_sort_type(self, sort_type):
        """Set sorting type and refresh list"""
        self.sort_type = sort_type
        self.current_filter = None  # Reset filter when changing sort type
        self.update_sort_buttons()
        self.refresh_filtering_controls()
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
                self._set_button_text_color(btn, [1, 1, 1, 1])  # White
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]
                # Set text color to teal (vert canard) for outlined buttons
                self._set_button_text_color(btn, [0, 0.5, 0.5, 1])  # Teal #008080

        # Update sort order buttons
        order_buttons = [(self.asc_btn, "asc", "#4CAF50"), (self.desc_btn, "desc", "#F44336")]

        for btn, btn_order, color in order_buttons:
            if self.sort_order == btn_order:
                btn.style = "filled"
                btn.md_bg_color = color
                # Set text color to white for filled buttons
                self._set_button_text_color(btn, [1, 1, 1, 1])  # White
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]
                # Set text color to teal (vert canard) for outlined buttons
                self._set_button_text_color(btn, [0, 0.5, 0.5, 1])  # Teal #008080

    def _set_button_text_color(self, button, color):
        """Helper method to set button text color"""
        # For KivyMD 2.0, we need to find the MDButtonText widget
        for child in button.children:
            # MDButtonText is usually the direct child
            if hasattr(child, "text") and hasattr(child, "theme_text_color"):
                child.theme_text_color = "Custom"
                child.text_color = color
                break
            # If not found in direct children, search recursively
            for grandchild in getattr(child, "children", []):
                if hasattr(grandchild, "text") and hasattr(grandchild, "theme_text_color"):
                    grandchild.theme_text_color = "Custom"
                    grandchild.text_color = color
                    break

    def create_filtering_controls(self):
        """Create dynamic filtering controls based on sort type"""
        card = MDCard(padding=dp(0), elevation=1, radius=[dp(8)], size_hint_y=None, height=dp(65))

        # Use FloatLayout to center content
        float_layout = MDFloatLayout()

        # Filter label and buttons container - centered in the card
        filter_layout = MDBoxLayout(
            orientation="horizontal",
            spacing=dp(5),
            size_hint=(1, None),
            height=dp(35),
            pos_hint={"center_x": 0.5, "center_y": 0.5},
            padding=[dp(8), 0, dp(8), 0],  # Left and right padding
        )

        filter_label = MDLabel(
            text="Filtre:", font_size=dp(12), theme_text_color="Primary", size_hint_x=None, width=dp(60)
        )
        filter_layout.add_widget(filter_label)

        # Scrollable filter buttons container
        filter_scroll = MDScrollView(size_hint=(1, 1), bar_width=dp(0))
        filter_buttons_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), adaptive_width=True)

        # Clear filter buttons list
        self.filter_buttons = []

        # Generate filter buttons based on sort type
        if self.sort_type == "alphabetical":
            self._create_letter_filters(filter_buttons_layout)
        elif self.sort_type == "date_added":
            self._create_date_filters(filter_buttons_layout)
        elif self.sort_type == "last_execution":
            self._create_execution_filters(filter_buttons_layout)

        filter_scroll.add_widget(filter_buttons_layout)
        filter_layout.add_widget(filter_scroll)

        float_layout.add_widget(filter_layout)
        card.add_widget(float_layout)

        return card

    def _create_letter_filters(self, layout):
        """Create letter filter buttons A-Z"""
        # Get all first letters from recipes
        letters = set()
        with get_db_session() as session:
            recipes = session.query(Recette).all()
            for recipe in recipes:
                if recipe.nom:
                    first_letter = recipe.nom[0].upper()
                    if first_letter.isalpha():
                        letters.add(first_letter)

        # Add "Tout" button
        all_btn = MDButton(
            MDButtonText(text="Tout", font_size=dp(10)),
            style="filled" if self.current_filter is None else "outlined",
            md_bg_color="#4CAF50" if self.current_filter is None else [0, 0, 0, 0],
            size_hint_x=None,
            width=dp(50),
            height=dp(30),
            on_release=lambda x: self.set_filter(None),
        )
        all_btn._filter_value = None  # Store filter value for updates
        self.filter_buttons.append(all_btn)
        layout.add_widget(all_btn)

        # Add letter buttons
        for letter in sorted(letters):
            btn = MDButton(
                MDButtonText(text=letter, font_size=dp(10)),
                style="filled" if self.current_filter == letter else "outlined",
                md_bg_color="#4CAF50" if self.current_filter == letter else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(35),
                height=dp(30),
                on_release=lambda x, letter=letter: self.set_filter(letter),
            )
            btn._filter_value = letter  # Store filter value for updates
            self.filter_buttons.append(btn)
            layout.add_widget(btn)

    def _create_date_filters(self, layout):
        """Create date-based filter buttons"""
        # Add "Tout" button
        all_btn = MDButton(
            MDButtonText(text="Tout", font_size=dp(10)),
            style="filled" if self.current_filter is None else "outlined",
            md_bg_color="#2196F3" if self.current_filter is None else [0, 0, 0, 0],
            size_hint_x=None,
            width=dp(50),
            height=dp(30),
            on_release=lambda x: self.set_filter(None),
        )
        all_btn._filter_value = None  # Store filter value for updates
        self.filter_buttons.append(all_btn)
        layout.add_widget(all_btn)

        # Date filters
        date_filters = [
            ("Aujourd'hui", "today"),
            ("Cette semaine", "week"),
            ("Ce mois", "month"),
            ("Cette année", "year"),
            ("Plus ancien", "older"),
        ]

        for text, filter_value in date_filters:
            btn = MDButton(
                MDButtonText(text=text, font_size=dp(10)),
                style="filled" if self.current_filter == filter_value else "outlined",
                md_bg_color="#2196F3" if self.current_filter == filter_value else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(85),
                height=dp(30),
                on_release=lambda x, f=filter_value: self.set_filter(f),
            )
            btn._filter_value = filter_value  # Store filter value for updates
            self.filter_buttons.append(btn)
            layout.add_widget(btn)

    def _create_execution_filters(self, layout):
        """Create execution-based filter buttons"""
        # Add "Tout" button
        all_btn = MDButton(
            MDButtonText(text="Tout", font_size=dp(10)),
            style="filled" if self.current_filter is None else "outlined",
            md_bg_color="#FF9800" if self.current_filter is None else [0, 0, 0, 0],
            size_hint_x=None,
            width=dp(50),
            height=dp(30),
            on_release=lambda x: self.set_filter(None),
        )
        all_btn._filter_value = None  # Store filter value for updates
        self.filter_buttons.append(all_btn)
        layout.add_widget(all_btn)

        # Execution filters
        exec_filters = [
            ("Jamais", "never"),
            ("Récemment", "recent"),
            ("Ce mois", "month"),
            ("Cette année", "year"),
            ("Ancien", "old"),
        ]

        for text, filter_value in exec_filters:
            btn = MDButton(
                MDButtonText(text=text, font_size=dp(10)),
                style="filled" if self.current_filter == filter_value else "outlined",
                md_bg_color="#FF9800" if self.current_filter == filter_value else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(70),
                height=dp(30),
                on_release=lambda x, f=filter_value: self.set_filter(f),
            )
            btn._filter_value = filter_value  # Store filter value for updates
            self.filter_buttons.append(btn)
            layout.add_widget(btn)

    def set_filter(self, filter_value):
        """Set current filter and refresh list"""
        self.current_filter = filter_value
        self.update_filter_buttons()
        self.refresh_recipe_list()

    def update_filter_buttons(self):
        """Update filter button styles based on current selection"""
        for btn in self.filter_buttons:
            # Check if this button should be active
            filter_value = getattr(btn, "_filter_value", None)
            is_active = filter_value == self.current_filter

            # Set button style and colors
            if is_active:
                btn.style = "filled"
                if self.sort_type == "alphabetical":
                    btn.md_bg_color = "#4CAF50"
                elif self.sort_type == "date_added":
                    btn.md_bg_color = "#2196F3"
                elif self.sort_type == "last_execution":
                    btn.md_bg_color = "#FF9800"
                self._set_button_text_color(btn, [1, 1, 1, 1])  # White
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]
                self._set_button_text_color(btn, [0, 0.5, 0.5, 1])  # Teal

    def refresh_filtering_controls(self):
        """Refresh the filtering controls when sort type changes"""
        # Remove old filtering section
        main_layout = self.children[0]
        main_layout.remove_widget(self.filtering_section)

        # Create new filtering section
        self.filtering_section = self.create_filtering_controls()

        # Find the index of the sorting section and insert right after it
        sorting_index = main_layout.children.index(self.sorting_section)
        main_layout.add_widget(self.filtering_section, index=sorting_index)

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
        # Add padding at bottom to prevent FAB overlap
        list_widget = MDList(spacing=dp(5), padding=(0, 0, 0, dp(140)))

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
        """Load recipes from database with sorting and filtering applied"""
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

                # Apply filtering if active
                if self.current_filter is not None:
                    recipes = self.apply_filter(recipes)

                return recipes
        except Exception as e:
            print(f"Error loading recipes: {e}")
            return []

    def apply_filter(self, recipes):
        """Apply current filter to recipes list"""
        if self.current_filter is None:
            return recipes

        filtered_recipes = []

        if self.sort_type == "alphabetical":
            # Filter by first letter
            for recipe in recipes:
                if recipe.nom and recipe.nom[0].upper() == self.current_filter:
                    filtered_recipes.append(recipe)

        elif self.sort_type == "date_added":
            # Filter by date ranges
            from datetime import datetime, timedelta

            now = datetime.now()

            for recipe in recipes:
                if not recipe.date_ajout:
                    continue

                if self.current_filter == "today":
                    if recipe.date_ajout.date() == now.date():
                        filtered_recipes.append(recipe)
                elif self.current_filter == "week":
                    week_ago = now - timedelta(days=7)
                    if recipe.date_ajout >= week_ago:
                        filtered_recipes.append(recipe)
                elif self.current_filter == "month":
                    month_ago = now - timedelta(days=30)
                    if recipe.date_ajout >= month_ago:
                        filtered_recipes.append(recipe)
                elif self.current_filter == "year":
                    if recipe.date_ajout.year == now.year:
                        filtered_recipes.append(recipe)
                elif self.current_filter == "older":
                    if recipe.date_ajout.year < now.year:
                        filtered_recipes.append(recipe)

        elif self.sort_type == "last_execution":
            # Filter by execution status
            from datetime import datetime, timedelta

            now = datetime.now()

            for recipe in recipes:
                executions = recipe.executions

                if self.current_filter == "never":
                    if not executions:
                        filtered_recipes.append(recipe)
                elif self.current_filter == "recent":
                    if executions:
                        last_execution = max(executions, key=lambda e: e.date_execution)
                        if (now - last_execution.date_execution).days <= 7:
                            filtered_recipes.append(recipe)
                elif self.current_filter == "month":
                    if executions:
                        last_execution = max(executions, key=lambda e: e.date_execution)
                        if (now - last_execution.date_execution).days <= 30:
                            filtered_recipes.append(recipe)
                elif self.current_filter == "year":
                    if executions:
                        last_execution = max(executions, key=lambda e: e.date_execution)
                        if last_execution.date_execution.year == now.year:
                            filtered_recipes.append(recipe)
                elif self.current_filter == "old":
                    if executions:
                        last_execution = max(executions, key=lambda e: e.date_execution)
                        if last_execution.date_execution.year < now.year:
                            filtered_recipes.append(recipe)

        return filtered_recipes

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
