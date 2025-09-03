"""
Que Cuisiner Screen for LibraRecipes Mobile App
Suggestions and inspiration for what to cook today
"""

import random
from kivy.app import App
from kivy.metrics import dp
from kivymd.uix.appbar import MDActionTopAppBarButton, MDTopAppBar, MDTopAppBarLeadingButtonContainer, MDTopAppBarTitle
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton
from kivymd.uix.button.button import MDButtonIcon, MDButtonText
from kivymd.uix.card import MDCard
from kivymd.uix.gridlayout import MDGridLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.selection import MDSelectionControlRadio
from kivymd.uix.selectioncontrol import MDCheckbox

from src.crud.recettes import list_recettes
from src.db import get_db_session


class QueCuisinerScreen(MDScreen):
    """Screen for recipe suggestions and inspiration"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.suggested_recipe = None
        self.popular_recipes = []
        self.daily_tip = None
        
        # Filter values
        self.selected_time = 60  # Default: 60 minutes
        self.selected_dish_type = "Tous"  # Default: All
        self.selected_difficulty = "Toutes"  # Default: All
        
        self.build_screen()

    def build_screen(self):
        """Build the que cuisiner screen layout"""
        # Main layout
        main_layout = MDBoxLayout(orientation="vertical")

        # App bar
        app_bar = MDTopAppBar(
            MDTopAppBarLeadingButtonContainer(
                MDActionTopAppBarButton(icon="arrow-left", on_release=lambda x: self.go_back())
            ),
            MDTopAppBarTitle(text="🎲 Que cuisiner ?"),
            md_bg_color=App.get_running_app().colors["primary"],
        )
        main_layout.add_widget(app_bar)

        # Scrollable content
        scroll = MDScrollView()
        content = MDBoxLayout(orientation="vertical", spacing=dp(15), padding=dp(15), adaptive_height=True)

        # Welcome section
        welcome_card = self.create_welcome_section()
        content.add_widget(welcome_card)

        # Filters section
        filters_card = self.create_filters_section()
        content.add_widget(filters_card)

        # Suggestion button
        suggest_button = self.create_suggest_button()
        content.add_widget(suggest_button)

        # Suggested recipe section (will be shown after suggestion)
        self.suggestion_result_card = MDCard(
            size_hint_y=None,
            height=dp(0),
            adaptive_height=True,
            padding=dp(0),
            elevation=0,
        )
        content.add_widget(self.suggestion_result_card)

        # Popular recipes section
        popular_card = self.create_popular_recipes_section()
        content.add_widget(popular_card)

        # Daily tip section
        tip_card = self.create_daily_tip_section()
        content.add_widget(tip_card)

        scroll.add_widget(content)
        main_layout.add_widget(scroll)
        self.add_widget(main_layout)

    def create_welcome_section(self):
        """Create welcome section"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(10),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(100),
        )

        layout = MDBoxLayout(orientation="vertical", spacing=dp(8))

        title = MDLabel(
            text="🎲 Que cuisiner aujourd'hui ?",
            font_size=dp(22),
            bold=True,
            theme_text_color="Primary",
            halign="center",
            size_hint_y=None,
            height=dp(35),
        )

        subtitle = MDLabel(
            text="Laissez-nous vous inspirer avec des suggestions de recettes !",
            font_size=dp(14),
            theme_text_color="Secondary",
            halign="center",
            italic=True,
            size_hint_y=None,
            height=dp(25),
        )

        layout.add_widget(title)
        layout.add_widget(subtitle)
        card.add_widget(layout)

        return card

    def create_filters_section(self):
        """Create filters section for personalized suggestions"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(300),
        )

        layout = MDBoxLayout(orientation="vertical", spacing=dp(15))

        # Title
        title = MDLabel(
            text="🎯 Suggestions personnalisées",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )
        layout.add_widget(title)

        # Time filter
        time_section = MDBoxLayout(orientation="vertical", spacing=dp(8), size_hint_y=None, height=dp(70))
        time_label = MDLabel(
            text="Temps de préparation maximum :",
            font_size=dp(14),
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(25),
        )
        time_section.add_widget(time_label)
        
        time_options = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(40))
        self.time_buttons = []
        
        time_values = [15, 30, 45, 60, 90, 120, 999]  # 999 = "Peu importe"
        time_labels = ["15min", "30min", "45min", "60min", "90min", "120min", "Peu importe"]
        
        for i, (value, label) in enumerate(zip(time_values, time_labels)):
            btn = MDButton(
                MDButtonText(text=label, font_size=dp(10)),
                style="filled" if value == self.selected_time else "outlined",
                md_bg_color="#4CAF50" if value == self.selected_time else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(65),
                height=dp(30),
                on_release=lambda x, val=value: self.set_time_filter(val),
            )
            btn._time_value = value
            self.time_buttons.append(btn)
            time_options.add_widget(btn)
        
        time_section.add_widget(time_options)
        layout.add_widget(time_section)

        # Dish type filter
        dish_section = MDBoxLayout(orientation="vertical", spacing=dp(8), size_hint_y=None, height=dp(70))
        dish_label = MDLabel(
            text="Type de plat :",
            font_size=dp(14),
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(25),
        )
        dish_section.add_widget(dish_label)
        
        dish_options = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(40))
        self.dish_buttons = []
        
        dish_types = ["Tous", "Plat principal", "Entrée", "Dessert", "Apéritif"]
        
        for dish_type in dish_types:
            btn = MDButton(
                MDButtonText(text=dish_type, font_size=dp(9)),
                style="filled" if dish_type == self.selected_dish_type else "outlined",
                md_bg_color="#4CAF50" if dish_type == self.selected_dish_type else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(65),
                height=dp(30),
                on_release=lambda x, dt=dish_type: self.set_dish_type_filter(dt),
            )
            btn._dish_type = dish_type
            self.dish_buttons.append(btn)
            dish_options.add_widget(btn)
        
        dish_section.add_widget(dish_options)
        layout.add_widget(dish_section)

        # Difficulty filter
        diff_section = MDBoxLayout(orientation="vertical", spacing=dp(8), size_hint_y=None, height=dp(70))
        diff_label = MDLabel(
            text="Difficulté :",
            font_size=dp(14),
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(25),
        )
        diff_section.add_widget(diff_label)
        
        diff_options = MDBoxLayout(orientation="horizontal", spacing=dp(5), size_hint_y=None, height=dp(40))
        self.diff_buttons = []
        
        difficulties = ["Toutes", "Facile", "Moyen", "Difficile"]
        
        for difficulty in difficulties:
            btn = MDButton(
                MDButtonText(text=difficulty, font_size=dp(10)),
                style="filled" if difficulty == self.selected_difficulty else "outlined",
                md_bg_color="#4CAF50" if difficulty == self.selected_difficulty else [0, 0, 0, 0],
                size_hint_x=None,
                width=dp(65),
                height=dp(30),
                on_release=lambda x, diff=difficulty: self.set_difficulty_filter(diff),
            )
            btn._difficulty = difficulty
            self.diff_buttons.append(btn)
            diff_options.add_widget(btn)
        
        diff_section.add_widget(diff_options)
        layout.add_widget(diff_section)

        card.add_widget(layout)
        return card

    def create_suggest_button(self):
        """Create the main suggestion button"""
        button = MDButton(
            MDButtonIcon(icon="dice-6"),
            MDButtonText(text="Suggérer une recette"),
            style="filled",
            md_bg_color=App.get_running_app().colors["primary"],
            size_hint_y=None,
            height=dp(50),
            on_release=lambda x: self.suggest_recipe(),
        )
        return button

    def create_popular_recipes_section(self):
        """Create popular recipes section"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(300),
        )

        layout = MDBoxLayout(orientation="vertical", spacing=dp(15))

        # Title and refresh button
        header = MDBoxLayout(orientation="horizontal", size_hint_y=None, height=dp(40))
        
        title = MDLabel(
            text="⭐ Recettes populaires",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
        )
        
        refresh_btn = MDButton(
            MDButtonIcon(icon="refresh"),
            MDButtonText(text="Nouvelles"),
            style="outlined",
            size_hint_x=None,
            width=dp(100),
            height=dp(35),
            on_release=lambda x: self.refresh_popular_recipes(),
        )
        
        header.add_widget(title)
        header.add_widget(refresh_btn)
        layout.add_widget(header)

        # Popular recipes container
        self.popular_container = MDBoxLayout(orientation="vertical", spacing=dp(10), adaptive_height=True)
        layout.add_widget(self.popular_container)

        # Load initial popular recipes
        self.load_popular_recipes()

        card.add_widget(layout)
        return card

    def create_daily_tip_section(self):
        """Create daily tip section"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(10),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(120),
        )

        layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

        title = MDLabel(
            text="💡 Conseil du chef",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Get random tip
        tips = [
            "🧂 Goûtez vos plats en cours de cuisson pour ajuster l'assaisonnement.",
            "🔥 Préchauffez toujours votre four avant d'y mettre vos plats.",
            "🥗 Préparez vos légumes à l'avance pour gagner du temps en cuisine.",
            "🍖 Laissez reposer la viande quelques minutes après cuisson.",
            "🧄 Émincez l'ail finement pour libérer plus de saveurs.",
            "🧅 Pour éviter de pleurer en coupant les oignons, mettez-les au frigo avant.",
            "🥄 Une pincée de sucre peut équilibrer l'acidité d'une sauce tomate.",
            "🧈 Sortez le beurre du frigo 30 minutes avant de l'utiliser en pâtisserie.",
        ]

        self.daily_tip = random.choice(tips)
        
        tip_label = MDLabel(
            text=self.daily_tip,
            font_size=dp(14),
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(60),
        )

        layout.add_widget(title)
        layout.add_widget(tip_label)
        card.add_widget(layout)

        return card

    def set_time_filter(self, time_value):
        """Set the time filter"""
        self.selected_time = time_value
        self.update_time_buttons()

    def set_dish_type_filter(self, dish_type):
        """Set the dish type filter"""
        self.selected_dish_type = dish_type
        self.update_dish_type_buttons()

    def set_difficulty_filter(self, difficulty):
        """Set the difficulty filter"""
        self.selected_difficulty = difficulty
        self.update_difficulty_buttons()

    def update_time_buttons(self):
        """Update time button styles"""
        for btn in self.time_buttons:
            if btn._time_value == self.selected_time:
                btn.style = "filled"
                btn.md_bg_color = "#4CAF50"
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]

    def update_dish_type_buttons(self):
        """Update dish type button styles"""
        for btn in self.dish_buttons:
            if btn._dish_type == self.selected_dish_type:
                btn.style = "filled"
                btn.md_bg_color = "#4CAF50"
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]

    def update_difficulty_buttons(self):
        """Update difficulty button styles"""
        for btn in self.diff_buttons:
            if btn._difficulty == self.selected_difficulty:
                btn.style = "filled"
                btn.md_bg_color = "#4CAF50"
            else:
                btn.style = "outlined"
                btn.md_bg_color = [0, 0, 0, 0]

    def suggest_recipe(self):
        """Suggest a recipe based on current filters"""
        try:
            with get_db_session() as session:
                all_recipes = list_recettes(session)
                
                if not all_recipes:
                    self.show_no_recipes_message()
                    return

                # Apply filters
                filtered_recipes = []
                
                for recipe in all_recipes:
                    # Time filter
                    total_time = (recipe.preparation or 0) + (recipe.cuisson or 0)
                    if self.selected_time != 999 and total_time > self.selected_time:
                        continue
                    
                    # Dish type filter (based on categories)
                    if self.selected_dish_type != "Tous":
                        category_names = [c.nom.lower() for c in recipe.categories]
                        if self.selected_dish_type.lower() not in category_names:
                            continue
                    
                    # Difficulty filter would go here (not implemented in the database model yet)
                    # For now, we'll accept all difficulties
                    
                    filtered_recipes.append(recipe)

                if filtered_recipes:
                    self.suggested_recipe = random.choice(filtered_recipes)
                    self.show_suggested_recipe()
                else:
                    self.show_no_match_message()

        except Exception as e:
            print(f"Error suggesting recipe: {e}")
            from kivymd.uix.snackbar import MDSnackbar, MDSnackbarText
            MDSnackbar(MDSnackbarText(text="Erreur lors de la suggestion. Veuillez réessayer.")).open()

    def show_suggested_recipe(self):
        """Show the suggested recipe"""
        if not self.suggested_recipe:
            return

        # Clear previous suggestion
        self.suggestion_result_card.clear_widgets()
        
        # Create suggestion display
        layout = MDBoxLayout(orientation="vertical", spacing=dp(15), padding=dp(20), adaptive_height=True)

        # Success message
        success_label = MDLabel(
            text="🎉 Voici notre suggestion !",
            font_size=dp(16),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )
        layout.add_widget(success_label)

        # Recipe name
        name_label = MDLabel(
            text=self.suggested_recipe.nom,
            font_size=dp(20),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(35),
        )
        layout.add_widget(name_label)

        # Recipe metrics
        metrics_layout = MDGridLayout(cols=3, spacing=dp(10), size_hint_y=None, height=dp(60))
        
        # Time
        total_time = (self.suggested_recipe.preparation or 0) + (self.suggested_recipe.cuisson or 0)
        time_card = self.create_metric_card("Temps total", f"{total_time} min")
        metrics_layout.add_widget(time_card)
        
        # Portions
        portions_card = self.create_metric_card("Portions", str(self.suggested_recipe.portions or 0))
        metrics_layout.add_widget(portions_card)
        
        # Categories
        if self.suggested_recipe.categories:
            categories_str = ", ".join([c.nom for c in self.suggested_recipe.categories[:2]])
            category_card = self.create_metric_card("Catégorie", categories_str)
        else:
            category_card = self.create_metric_card("Catégorie", "Aucune")
        metrics_layout.add_widget(category_card)
        
        layout.add_widget(metrics_layout)

        # Ingredients preview
        if self.suggested_recipe.ingredients:
            ing_label = MDLabel(
                text="Ingrédients principaux :",
                font_size=dp(14),
                bold=True,
                theme_text_color="Primary",
                size_hint_y=None,
                height=dp(25),
            )
            layout.add_widget(ing_label)
            
            ingredients_preview = self.suggested_recipe.ingredients[:5]
            for ing in ingredients_preview:
                ing_item = MDLabel(
                    text=f"• {ing.nom}",
                    font_size=dp(12),
                    theme_text_color="Secondary",
                    size_hint_y=None,
                    height=dp(20),
                )
                layout.add_widget(ing_item)
            
            if len(self.suggested_recipe.ingredients) > 5:
                more_label = MDLabel(
                    text=f"• ... et {len(self.suggested_recipe.ingredients) - 5} autres ingrédients",
                    font_size=dp(12),
                    theme_text_color="Secondary",
                    size_hint_y=None,
                    height=dp(20),
                )
                layout.add_widget(more_label)

        # Action buttons
        actions_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), size_hint_y=None, height=dp(50))
        
        view_btn = MDButton(
            MDButtonIcon(icon="eye"),
            MDButtonText(text="Voir la recette"),
            style="filled",
            md_bg_color=App.get_running_app().colors["primary"],
            on_release=lambda x: self.view_recipe(self.suggested_recipe.id),
        )
        
        another_btn = MDButton(
            MDButtonIcon(icon="dice-6"),
            MDButtonText(text="Autre suggestion"),
            style="outlined",
            on_release=lambda x: self.suggest_recipe(),
        )
        
        actions_layout.add_widget(view_btn)
        actions_layout.add_widget(another_btn)
        layout.add_widget(actions_layout)

        self.suggestion_result_card.add_widget(layout)
        self.suggestion_result_card.elevation = 2
        self.suggestion_result_card.height = dp(400)  # Set appropriate height

    def create_metric_card(self, title, value):
        """Create a small metric card"""
        card = MDCard(
            padding=dp(8),
            elevation=1,
            radius=[dp(5)],
            size_hint_y=None,
            height=dp(60),
        )
        
        layout = MDBoxLayout(orientation="vertical", spacing=dp(2))
        
        value_label = MDLabel(
            text=value,
            font_size=dp(14),
            bold=True,
            theme_text_color="Primary",
            halign="center",
            size_hint_y=None,
            height=dp(25),
        )
        
        title_label = MDLabel(
            text=title,
            font_size=dp(10),
            theme_text_color="Secondary",
            halign="center",
            size_hint_y=None,
            height=dp(20),
        )
        
        layout.add_widget(value_label)
        layout.add_widget(title_label)
        card.add_widget(layout)
        
        return card

    def show_no_recipes_message(self):
        """Show message when no recipes are found"""
        # Clear previous suggestion
        self.suggestion_result_card.clear_widgets()
        
        layout = MDBoxLayout(orientation="vertical", spacing=dp(10), padding=dp(20), adaptive_height=True)
        
        message = MDLabel(
            text="Aucune recette trouvée. Ajoutez d'abord quelques recettes !",
            font_size=dp(14),
            theme_text_color="Secondary",
            halign="center",
            size_hint_y=None,
            height=dp(40),
        )
        layout.add_widget(message)
        
        self.suggestion_result_card.add_widget(layout)
        self.suggestion_result_card.elevation = 2
        self.suggestion_result_card.height = dp(80)

    def show_no_match_message(self):
        """Show message when no recipes match the filters"""
        # Clear previous suggestion
        self.suggestion_result_card.clear_widgets()
        
        layout = MDBoxLayout(orientation="vertical", spacing=dp(10), padding=dp(20), adaptive_height=True)
        
        message = MDLabel(
            text="Aucune recette ne correspond à vos critères. Essayez avec des filtres moins stricts !",
            font_size=dp(14),
            theme_text_color="Secondary",
            halign="center",
            size_hint_y=None,
            height=dp(40),
        )
        layout.add_widget(message)
        
        self.suggestion_result_card.add_widget(layout)
        self.suggestion_result_card.elevation = 2
        self.suggestion_result_card.height = dp(80)

    def load_popular_recipes(self):
        """Load popular recipes"""
        try:
            with get_db_session() as session:
                all_recipes = list_recettes(session)
                
                if len(all_recipes) >= 3:
                    self.popular_recipes = random.sample(all_recipes, min(3, len(all_recipes)))
                else:
                    self.popular_recipes = all_recipes
                
                self.update_popular_recipes_display()

        except Exception as e:
            print(f"Error loading popular recipes: {e}")

    def refresh_popular_recipes(self):
        """Refresh popular recipes"""
        self.load_popular_recipes()

    def update_popular_recipes_display(self):
        """Update the popular recipes display"""
        self.popular_container.clear_widgets()
        
        if not self.popular_recipes:
            no_recipes_label = MDLabel(
                text="Aucune recette disponible.",
                font_size=dp(14),
                theme_text_color="Secondary",
                halign="center",
                size_hint_y=None,
                height=dp(40),
            )
            self.popular_container.add_widget(no_recipes_label)
            return

        for recipe in self.popular_recipes:
            recipe_card = self.create_popular_recipe_card(recipe)
            self.popular_container.add_widget(recipe_card)

    def create_popular_recipe_card(self, recipe):
        """Create a card for a popular recipe"""
        card = MDCard(
            padding=dp(15),
            spacing=dp(8),
            elevation=1,
            radius=[dp(8)],
            size_hint_y=None,
            height=dp(80),
        )
        
        layout = MDBoxLayout(orientation="horizontal", spacing=dp(10))
        
        # Recipe info
        info_layout = MDBoxLayout(orientation="vertical", spacing=dp(5))
        
        name_label = MDLabel(
            text=recipe.nom,
            font_size=dp(16),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(25),
        )
        
        # Time and portions
        total_time = (recipe.preparation or 0) + (recipe.cuisson or 0)
        details = f"⏱️ {total_time}min | 👥 {recipe.portions or 0} portions"
        details_label = MDLabel(
            text=details,
            font_size=dp(12),
            theme_text_color="Secondary",
            size_hint_y=None,
            height=dp(20),
        )
        
        # Categories
        if recipe.categories:
            categories_str = ", ".join([c.nom for c in recipe.categories[:2]])
            category_label = MDLabel(
                text=f"🏷️ {categories_str}",
                font_size=dp(12),
                theme_text_color="Secondary",
                size_hint_y=None,
                height=dp(20),
            )
        else:
            category_label = MDLabel(
                text="",
                size_hint_y=None,
                height=dp(20),
            )
        
        info_layout.add_widget(name_label)
        info_layout.add_widget(details_label)
        info_layout.add_widget(category_label)
        
        # View button
        view_btn = MDButton(
            MDButtonIcon(icon="eye"),
            style="outlined",
            size_hint_x=None,
            width=dp(50),
            height=dp(40),
            on_release=lambda x, recipe_id=recipe.id: self.view_recipe(recipe_id),
        )
        
        layout.add_widget(info_layout)
        layout.add_widget(view_btn)
        card.add_widget(layout)
        
        return card

    def view_recipe(self, recipe_id):
        """Navigate to recipe detail view"""
        app = App.get_running_app()
        app.selected_recipe_id = str(recipe_id)
        self.manager.current = "recipe_detail"

    def go_back(self):
        """Go back to home screen"""
        self.manager.current = "home"

    def on_enter(self):
        """Called when screen is entered"""
        # Refresh data when entering the screen
        self.load_popular_recipes()