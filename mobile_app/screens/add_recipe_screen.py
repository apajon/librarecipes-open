"""
Add Recipe Screen for LibraRecipes Mobile App
Form to create new recipes with ingredients and steps
"""

from kivy.metrics import dp
from kivymd.uix.appbar import (
    MDActionTopAppBarButton,
    MDTopAppBar,
    MDTopAppBarLeadingButtonContainer,
    MDTopAppBarTitle,
    MDTopAppBarTrailingButtonContainer,
)
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton, MDIconButton
from kivymd.uix.button.button import MDButtonText  # pour gérer le texte dans le bouton
from kivymd.uix.card import MDCard
from kivymd.uix.floatlayout import MDFloatLayout
from kivymd.uix.gridlayout import MDGridLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.list import (
    MDList,
    MDListItem,
    MDListItemHeadlineText,
    MDListItemSupportingText,
    MDListItemTertiaryText,
)
from kivymd.uix.screen import MDScreen
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.selectioncontrol import MDCheckbox
from kivymd.uix.snackbar import MDSnackbar, MDSnackbarText
from kivymd.uix.textfield import MDTextField

from src.crud.recettes import create_recette
from src.db import get_db_session

# Import photo manager
try:
    from utils.photo_manager import PhotoManager
except ImportError:
    PhotoManager = None


def show_snackbar(text: str):
    """Helper function to show snackbar with KivyMD 2.0 syntax"""
    MDSnackbar(MDSnackbarText(text=text)).open()


class AddRecipeScreen(MDScreen):
    """Screen for adding new recipes"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.ingredients = []
        self.steps = []
        self.photos = []
        self.dialog = None

        # Initialize photo manager
        self.photo_manager = PhotoManager() if PhotoManager else None

        self.build_screen()

    def quantity_filter(self, string, from_undo):
        """Custom filter for quantity field to allow numbers, decimals and fractions"""
        # Allow digits, decimal point, and slash for fractions
        allowed_chars = "0123456789./"
        return "".join([c for c in string if c in allowed_chars])

    def show_unit_menu(self, *args):
        """Show unit selection menu"""
        # List of available units (same as Streamlit app)
        units = [
            "g",
            "kg",
            "ml",
            "cl",
            "l",
            "c. à c.",
            "c. à s.",
            "pièce(s)",
            "gousse(s)",
            "pincée(s)",
            "tasse(s)",
            "cup(s)",
            "oz",
            "lb",
        ]

        # Create unit selection card with fixed height
        unit_card = MDCard(
            padding=dp(15),  # Reduced padding
            spacing=dp(8),  # Reduced spacing
            elevation=10,
            radius=[dp(15)],
            size_hint=(0.8, None),
            height=dp(450),  # Reduced height
            pos_hint={"center_x": 0.5, "center_y": 0.5},
        )

        content = MDBoxLayout(orientation="vertical", spacing=dp(8), adaptive_height=True)  # Reduced spacing

        # Title
        title = MDLabel(
            text="Choisir une unité", font_size=dp(18), bold=True, theme_text_color="Primary", adaptive_height=True
        )
        content.add_widget(title)

        # Simple scrollable list area
        from kivy.uix.scrollview import ScrollView

        scroll = ScrollView(
            size_hint_y=None,
            height=dp(320),  # Increased scroll area since card is smaller
            do_scroll_x=False,
            do_scroll_y=True,
        )

        units_layout = MDBoxLayout(orientation="vertical", spacing=dp(5), adaptive_height=True)

        for unit in units:
            unit_btn = MDButton(
                style="text", size_hint_y=None, height=dp(48), on_release=lambda x, u=unit: self.select_unit(u)
            )
            unit_btn.add_widget(MDButtonText(text=unit))
            units_layout.add_widget(unit_btn)

        # Add "No unit" option
        no_unit_btn = MDButton(
            style="text", size_hint_y=None, height=dp(48), on_release=lambda x: self.select_unit("")
        )
        no_unit_btn.add_widget(MDButtonText(text="Aucune unité"))
        units_layout.add_widget(no_unit_btn)

        scroll.add_widget(units_layout)
        content.add_widget(scroll)

        # Cancel button
        cancel_btn = MDButton(style="outlined", size_hint_y=None, height=dp(40), on_release=self.close_unit_menu)
        cancel_btn.add_widget(MDButtonText(text="Annuler"))
        content.add_widget(cancel_btn)

        unit_card.add_widget(content)

        # Add overlay
        self.unit_overlay = MDFloatLayout()
        self.unit_overlay.md_bg_color = (0, 0, 0, 0.5)
        self.unit_overlay.add_widget(unit_card)
        self.add_widget(self.unit_overlay)

    def select_unit(self, unit):
        """Select a unit and update button text"""
        self.selected_unit = unit
        if unit:
            self.unit_button.children[0].text = unit
        else:
            self.unit_button.children[0].text = "Aucune unité"
        self.close_unit_menu()

    def close_unit_menu(self, *args):
        """Close unit selection menu"""
        if hasattr(self, "unit_overlay"):
            self.remove_widget(self.unit_overlay)
            delattr(self, "unit_overlay")

    def build_screen(self):
        """Build the add recipe screen layout"""
        # Main layout
        main_layout = MDBoxLayout(orientation="vertical")

        # App bar
        app_bar = MDTopAppBar(
            MDTopAppBarLeadingButtonContainer(
                MDActionTopAppBarButton(icon="arrow-left", on_release=lambda x: self.go_back())
            ),
            MDTopAppBarTitle(text="Add New Recipe"),
            MDTopAppBarTrailingButtonContainer(
                MDActionTopAppBarButton(icon="content-save", on_release=lambda x: self.save_recipe())
            ),
        )
        main_layout.add_widget(app_bar)

        # Scrollable content
        scroll = MDScrollView()
        content = MDBoxLayout(orientation="vertical", spacing=dp(16), padding=dp(16), adaptive_height=True)

        # Basic info section
        basic_info = self.create_basic_info_section()
        content.add_widget(basic_info)

        # Ingredients section
        ingredients_section = self.create_ingredients_section()
        content.add_widget(ingredients_section)

        # Steps section
        steps_section = self.create_steps_section()
        content.add_widget(steps_section)

        # Photos section
        photos_section = self.create_photos_section()
        content.add_widget(photos_section)

        # Save button
        save_btn = MDButton(
            children=[MDButtonText(text="Save Recipe")],
            style="filled",
            size_hint_y=None,
            height=dp(50),
            on_release=lambda x: self.save_recipe(),
        )
        content.add_widget(save_btn)

        scroll.add_widget(content)
        main_layout.add_widget(scroll)
        self.add_widget(main_layout)

    def create_basic_info_section(self):
        """Create basic recipe information form"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(8), adaptive_height=True)

        # Section title
        title = MDLabel(
            text="Recipe Information",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            adaptive_height=True,
            markup=True,
        )

        # Recipe name
        name_label = MDLabel(
            text="Nom de la recette*", theme_text_color="Primary", adaptive_height=True, text_size=(None, None)
        )

        self.recipe_name = MDTextField(hint_text="Recipe name*", mode="outlined", size_hint_y=None, height=dp(56))

        # Time and portions in grid with individual labels
        time_labels_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), adaptive_height=True)

        prep_label = MDLabel(
            text="Prep. (min)",
            theme_text_color="Secondary",
            font_size=dp(12),
            size_hint_x=0.5,
            halign="center",
            adaptive_height=True,
            text_size=(None, None),
        )

        cook_label = MDLabel(
            text="Cuisson (min)",
            theme_text_color="Secondary",
            font_size=dp(12),
            size_hint_x=0.5,
            halign="center",
            adaptive_height=True,
            text_size=(None, None),
        )

        time_labels_layout.add_widget(prep_label)
        time_labels_layout.add_widget(cook_label)

        time_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), size_hint_y=None, height=dp(56))

        self.prep_time = MDTextField(
            hint_text="Ex: 15", input_filter="int", mode="outlined", size_hint_x=0.5, size_hint_y=None, height=dp(56)
        )

        self.cook_time = MDTextField(
            hint_text="Ex: 30", input_filter="int", mode="outlined", size_hint_x=0.5, size_hint_y=None, height=dp(56)
        )

        time_layout.add_widget(self.prep_time)
        time_layout.add_widget(self.cook_time)

        # Portions section separate
        portions_label = MDLabel(
            text="Portions",
            theme_text_color="Secondary",
            font_size=dp(12),
            halign="center",
            adaptive_height=True,
            text_size=(None, None),
        )

        self.portions = MDTextField(
            text="1", input_filter="int", mode="outlined", size_hint_x=0.5, size_hint_y=None, height=dp(56)
        )

        # Create portions layout with +/- buttons
        portions_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), adaptive_height=True)

        minus_btn = MDIconButton(
            icon="minus", theme_icon_color="Primary", size_hint_x=0.25, on_release=self.decrease_portions
        )

        plus_btn = MDIconButton(
            icon="plus", theme_icon_color="Primary", size_hint_x=0.25, on_release=self.increase_portions
        )

        portions_layout.add_widget(minus_btn)
        portions_layout.add_widget(self.portions)
        portions_layout.add_widget(plus_btn)

        # Categories and tags
        categories_label = MDLabel(
            text="Catégories (séparées par des virgules)",
            theme_text_color="Primary",
            adaptive_height=True,
            text_size=(None, None),
        )

        self.categories = MDTextField(
            hint_text="Ex: plat principal, végétarien", mode="outlined", size_hint_y=None, height=dp(56)
        )

        tags_label = MDLabel(
            text="Tags (séparés par des virgules)",
            theme_text_color="Primary",
            adaptive_height=True,
            text_size=(None, None),
        )

        self.tags = MDTextField(
            hint_text="Ex: facile, rapide, économique", mode="outlined", size_hint_y=None, height=dp(56)
        )

        layout.add_widget(title)
        layout.add_widget(name_label)
        layout.add_widget(self.recipe_name)
        layout.add_widget(time_labels_layout)
        layout.add_widget(time_layout)
        layout.add_widget(portions_label)
        layout.add_widget(portions_layout)
        layout.add_widget(categories_label)
        layout.add_widget(self.categories)
        layout.add_widget(tags_label)
        layout.add_widget(self.tags)

        card.add_widget(layout)
        return card

    def create_ingredients_section(self):
        """Create ingredients management section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Section title with add button
        title_layout = MDBoxLayout(orientation="horizontal", size_hint_y=None, height=dp(40))

        title = MDLabel(text="Ingredients", font_size=dp(18), bold=True, theme_text_color="Primary")

        add_ingredient_btn = MDIconButton(
            icon="plus",
            theme_icon_color="Primary",
            on_release=lambda x: self.add_ingredient_dialog(),
        )

        title_layout.add_widget(title)
        title_layout.add_widget(add_ingredient_btn)

        # Ingredients list
        self.ingredients_list = MDList(adaptive_height=True)

        layout.add_widget(title_layout)
        layout.add_widget(self.ingredients_list)

        card.add_widget(layout)
        return card

    def create_steps_section(self):
        """Create recipe steps management section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Section title with add button
        title_layout = MDBoxLayout(orientation="horizontal", size_hint_y=None, height=dp(40))

        title = MDLabel(text="Preparation Steps", font_size=dp(18), bold=True, theme_text_color="Primary")

        add_step_btn = MDIconButton(
            icon="plus",
            theme_icon_color="Primary",
            on_release=lambda x: self.add_step_dialog(),
        )

        title_layout.add_widget(title)
        title_layout.add_widget(add_step_btn)

        # Steps list
        self.steps_list = MDList(adaptive_height=True)

        layout.add_widget(title_layout)
        layout.add_widget(self.steps_list)

        card.add_widget(layout)
        return card

    def create_photos_section(self):
        """Create photos management section"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Section title with camera button
        title_layout = MDBoxLayout(orientation="horizontal", size_hint_y=None, height=dp(40))

        title = MDLabel(text="Recipe Photos", font_size=dp(18), bold=True, theme_text_color="Primary")

        camera_btn = MDIconButton(
            icon="camera",
            theme_icon_color="Primary",
            on_release=lambda x: self.show_photo_options(),
        )

        title_layout.add_widget(title)
        title_layout.add_widget(camera_btn)

        # Photos grid
        self.photos_grid = MDGridLayout(cols=3, spacing=dp(10), adaptive_height=True)

        layout.add_widget(title_layout)
        layout.add_widget(self.photos_grid)

        card.add_widget(layout)
        return card

    def add_ingredient_dialog(self):
        """Show dialog to add ingredient"""
        # Create dialog content
        dialog_card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=10,
            radius=[dp(15)],
            size_hint=(0.9, None),
            height=dp(500),
            pos_hint={"center_x": 0.5, "center_y": 0.5},
        )

        content = MDBoxLayout(orientation="vertical", spacing=dp(15), adaptive_height=True)

        # Title
        title = MDLabel(
            text="Ajouter un ingrédient", font_size=dp(20), bold=True, theme_text_color="Primary", adaptive_height=True
        )

        # Form fields
        name_label = MDLabel(text="Nom de l'ingrédient*", theme_text_color="Primary", adaptive_height=True)

        self.name_field = MDTextField(hint_text="Ex: Farine", mode="outlined", size_hint_y=None, height=dp(56))

        quantity_label = MDLabel(text="Quantité", theme_text_color="Primary", adaptive_height=True)

        self.quantity_field = MDTextField(
            hint_text="Ex: 250, 3/4, 1.5",
            input_filter=self.quantity_filter,
            mode="outlined",
            size_hint_y=None,
            height=dp(56),
        )

        unit_label = MDLabel(text="Unité", theme_text_color="Primary", adaptive_height=True)

        # Unit selection button instead of text field
        self.unit_button = MDButton(style="outlined", size_hint_y=None, height=dp(56), on_release=self.show_unit_menu)
        self.unit_button.add_widget(MDButtonText(text="Sélectionner une unité"))
        self.selected_unit = ""  # Store selected unit

        # Essential checkbox
        essential_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), adaptive_height=True)
        self.essential_checkbox = MDCheckbox(active=True, size_hint_x=None, width=dp(30))
        essential_label = MDLabel(text="Ingrédient essentiel", theme_text_color="Primary", adaptive_height=True)
        essential_layout.add_widget(self.essential_checkbox)
        essential_layout.add_widget(essential_label)

        # Buttons
        button_layout = MDBoxLayout(orientation="horizontal", spacing=dp(10), adaptive_height=True)

        cancel_btn = MDButton(style="outlined", on_release=self.close_ingredient_dialog)
        cancel_btn.add_widget(MDButtonText(text="Annuler"))

        add_btn = MDButton(style="filled", on_release=self.add_ingredient_from_dialog)
        add_btn.add_widget(MDButtonText(text="Ajouter"))

        button_layout.add_widget(cancel_btn)
        button_layout.add_widget(add_btn)

        # Add all to content
        content.add_widget(title)
        content.add_widget(name_label)
        content.add_widget(self.name_field)
        content.add_widget(quantity_label)
        content.add_widget(self.quantity_field)
        content.add_widget(unit_label)
        content.add_widget(self.unit_button)
        content.add_widget(essential_layout)
        content.add_widget(button_layout)

        dialog_card.add_widget(content)

        # Add to screen with overlay
        self.dialog_overlay = MDFloatLayout()
        self.dialog_overlay.md_bg_color = (0, 0, 0, 0.5)  # Semi-transparent background
        self.dialog_overlay.add_widget(dialog_card)

        self.add_widget(self.dialog_overlay)

    def close_ingredient_dialog(self, *args):
        """Close the ingredient dialog"""
        if hasattr(self, "dialog_overlay"):
            self.remove_widget(self.dialog_overlay)
            delattr(self, "dialog_overlay")

    def add_ingredient_from_dialog(self, *args):
        """Add ingredient from dialog form"""
        name = self.name_field.text.strip()
        quantity = self.quantity_field.text.strip()
        unit = self.selected_unit  # Use selected unit instead of text field
        essential = self.essential_checkbox.active

        if not name:
            show_snackbar("Veuillez entrer le nom de l'ingrédient")
            return

        self.confirm_add_ingredient(name, quantity, unit, essential)
        self.close_ingredient_dialog()

    def confirm_add_ingredient(self, name, quantity, unit, essential):
        """Add ingredient to list"""
        if not name.strip():
            show_snackbar("Please enter ingredient name")
            return

        ingredient = {
            "nom": name.strip(),
            "quantite": quantity.strip() if quantity else None,
            "unite": unit.strip() if unit else None,
            "indispensable": essential,
        }

        self.ingredients.append(ingredient)
        self.refresh_ingredients_list()
        if self.dialog:
            self.dialog.dismiss()

    def add_step_dialog(self):
        """Show dialog to add step - TODO: Update to KivyMD 2.0"""
        show_snackbar("Dialogue étapes en cours de migration vers KivyMD 2.0")
        # Temporary placeholder
        pass

    def confirm_add_step(self, description):
        """Add step to list"""
        if not description.strip():
            show_snackbar("Please enter step description")
            return

        self.steps.append(description.strip())
        self.refresh_steps_list()
        if self.dialog:
            self.dialog.dismiss()

    def refresh_ingredients_list(self):
        """Refresh ingredients display"""
        self.ingredients_list.clear_widgets()

        for i, ingredient in enumerate(self.ingredients):
            text = ingredient["nom"]
            if ingredient["quantite"]:
                text += f" - {ingredient['quantite']}"
            if ingredient["unite"]:
                text += f" {ingredient['unite']}"

            secondary_text = "Essential" if ingredient["indispensable"] else "Optional"

            item = MDListItem(
                MDListItemHeadlineText(text=text),
                MDListItemSupportingText(text=secondary_text),
                MDListItemTertiaryText(text=f"Ingredient {i+1}"),  # noqa E226
                on_release=lambda x, idx=i: self.remove_ingredient(idx),
            )

            self.ingredients_list.add_widget(item)

    def refresh_steps_list(self):
        """Refresh steps display"""
        self.steps_list.clear_widgets()

        for i, step in enumerate(self.steps):
            item = MDListItem(
                MDListItemHeadlineText(text=f"Step {i+1}"),  # noqa E226
                MDListItemSupportingText(text=step[:50] + "..." if len(step) > 50 else step),
                MDListItemTertiaryText(text="Tap to remove"),
                on_release=lambda x, idx=i: self.remove_step(idx),
            )

            self.steps_list.add_widget(item)

    def remove_ingredient(self, index):
        """Remove ingredient from list"""
        if 0 <= index < len(self.ingredients):
            self.ingredients.pop(index)
            self.refresh_ingredients_list()

    def remove_step(self, index):
        """Remove step from list"""
        if 0 <= index < len(self.steps):
            self.steps.pop(index)
            self.refresh_steps_list()

    def show_photo_options(self):
        """Show photo capture/selection options - TODO: Update to KivyMD 2.0"""
        show_snackbar("Dialogue photos en cours de migration vers KivyMD 2.0")
        # Temporary placeholder
        pass

    def take_photo(self):
        """Take a photo using camera"""
        if self.photo_manager:
            recipe_name = self.recipe_name.text.strip() or "recipe"
            filename = self.photo_manager.generate_photo_filename(recipe_name.replace(" ", "_"), "recipe")

            self.photo_manager.take_photo(filename, callback=self.on_photo_captured)
        if self.dialog:
            self.dialog.dismiss()

    def select_from_gallery(self):
        """Select photo from gallery"""
        if self.photo_manager:
            self.photo_manager.select_from_gallery(callback=self.on_photo_captured)
        if self.dialog:
            self.dialog.dismiss()

    def on_photo_captured(self, photo_path, error):
        """Handle photo capture result"""
        if error:
            show_snackbar(f"Photo error: {error}")
            return

        if photo_path:
            self.photos.append({"chemin": photo_path, "categorie": "recipe"})
            self.refresh_photos_grid()
            show_snackbar("Photo added successfully!")

    def refresh_photos_grid(self):
        """Refresh photos display"""
        self.photos_grid.clear_widgets()

        for i, photo in enumerate(self.photos):
            photo_card = MDCard(
                size_hint_y=None,
                height=dp(80),
                elevation=2,
                radius=[dp(5)],
                on_release=lambda x, idx=i: self.remove_photo(idx),
            )

            photo_layout = MDBoxLayout(orientation="vertical", spacing=dp(5), padding=dp(10))

            photo_icon = MDLabel(text="📸", font_size=dp(24), halign="center")

            photo_label = MDLabel(
                text=f"Photo {i+1}",  # noqa E226
                font_size=dp(12),
                halign="center",
                theme_text_color="Secondary",
            )

            photo_layout.add_widget(photo_icon)
            photo_layout.add_widget(photo_label)
            photo_card.add_widget(photo_layout)

            self.photos_grid.add_widget(photo_card)

    def remove_photo(self, index):
        """Remove photo from list"""
        if 0 <= index < len(self.photos):
            photo = self.photos.pop(index)
            if self.photo_manager:
                self.photo_manager.delete_photo(photo["chemin"])
            self.refresh_photos_grid()
            show_snackbar("Photo removed")

    def save_recipe(self):
        """Save the recipe to database"""
        # Validate required fields
        if not self.recipe_name.text.strip():
            show_snackbar("Please enter recipe name")
            return

        if not self.ingredients:
            show_snackbar("Please add at least one ingredient")
            return

        if not self.steps:
            show_snackbar("Please add at least one preparation step")
            return

        try:
            # Prepare recipe data
            recipe_data = {
                "nom": self.recipe_name.text.strip(),
                "preparation": int(self.prep_time.text) if self.prep_time.text.strip() else None,
                "cuisson": int(self.cook_time.text) if self.cook_time.text.strip() else None,
                "portions": int(self.portions.text) if self.portions.text.strip() else None,
                "ingredients": self.ingredients,
                "etapes": self.steps,
                "categories": [cat.strip() for cat in self.categories.text.split(",") if cat.strip()],
                "tags": [tag.strip() for tag in self.tags.text.split(",") if tag.strip()],
                "photos": self.photos,
                "source": {"type": "homemade"},
            }

            # Save to database
            with get_db_session() as session:
                new_recipe = create_recette(session, recipe_data)

            show_snackbar(f"Recipe '{new_recipe.nom}' saved successfully!")

            # Clear form and go back
            self.clear_form()
            self.manager.current = "recipe_list"

        except Exception as e:
            print(f"Error saving recipe: {e}")
            show_snackbar("Error saving recipe. Please try again.")

    def increase_portions(self, *args):
        """Increase portions count"""
        try:
            current = int(self.portions.text) if self.portions.text else 1
            if current < 99:  # Limit to 99 portions max
                self.portions.text = str(current + 1)
        except ValueError:
            self.portions.text = "1"

    def decrease_portions(self, *args):
        """Decrease portions count"""
        try:
            current = int(self.portions.text) if self.portions.text else 1
            if current > 1:  # Minimum 1 portion
                self.portions.text = str(current - 1)
        except ValueError:
            self.portions.text = "1"

    def clear_form(self):
        """Clear all form fields"""
        self.recipe_name.text = ""
        self.prep_time.text = ""
        self.cook_time.text = ""
        self.portions.text = "1"
        self.categories.text = ""
        self.tags.text = ""
        self.ingredients = []
        self.steps = []
        self.photos = []
        self.refresh_ingredients_list()
        self.refresh_steps_list()
        self.refresh_photos_grid()

    def go_back(self):
        """Go back to previous screen"""
        self.manager.current = "home"

    def on_enter(self):
        """Called when screen is entered"""
        # Clear form when entering screen
        self.clear_form()
