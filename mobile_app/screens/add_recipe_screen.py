"""
Add Recipe Screen for LibraRecipes Mobile App
Form to create new recipes with ingredients and steps
"""

from kivy.app import App
from kivy.metrics import dp
from kivy.logger import Logger
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
from kivymd.uix.label import MDLabel
from kivymd.uix.list import MDList
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

# Import FAB Manager
try:
    from components.fab_manager import FABManager
except ImportError:
    FABManager = None


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
        self.editing_ingredient_index = None  # For tracking which ingredient is being edited
        self.editing_step_index = None  # For tracking which step is being edited
        self.editing_recipe_id = None  # For tracking if we're editing an existing recipe
        self.fab_manager = None

        # Initialize photo manager
        self.photo_manager = PhotoManager() if PhotoManager else None

        self.build_screen()
        self.setup_fabs()

    def setup_fabs(self):
        """Initialize floating action buttons"""
        if FABManager:
            self.fab_manager = FABManager(self, context="add_recipe")

    def add_photo_to_recipe(self, photo_path):
        """Add a photo captured from FAB to current recipe"""
        if photo_path and photo_path not in [p.get('chemin') for p in self.photos]:
            # Add photo to internal list
            photo_data = {
                'chemin': str(photo_path),
                'description': 'Photo prise avec l\'appareil photo'
            }
            self.photos.append(photo_data)
            
            # Refresh the photos display
            self.refresh_photos_display()
            
            # Show success message
            show_snackbar(f"Photo ajoutée: {Path(photo_path).name}")

    def refresh_photos_display(self):
        """Refresh the photos grid display"""
        if hasattr(self, 'photos_grid') and self.photos_grid:
            # Clear current photos display
            self.photos_grid.clear_widgets()
            
            # Re-add all photos
            for i, photo in enumerate(self.photos):
                photo_item = self.create_photo_item(photo, i)
                if photo_item:
                    self.photos_grid.add_widget(photo_item)

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

        # Source section
        source_section = self.create_source_section()
        content.add_widget(source_section)

        # Save button
        save_btn = MDButton(
            MDButtonText(text="💾 Sauvegarder la recette"),
            style="filled",
            size_hint_y=None,
            height=dp(56),
            md_bg_color="#4CAF50",  # Green color
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

        title = MDLabel(text="Photos de la recette", font_size=dp(18), bold=True, theme_text_color="Primary")

        camera_btn = MDIconButton(
            icon="camera",
            theme_icon_color="Primary",
            on_release=lambda x: self.show_photo_options(),
        )

        title_layout.add_widget(title)
        title_layout.add_widget(camera_btn)

        # Photos list (changed from grid to vertical list)
        self.photos_grid = MDList(adaptive_height=True, spacing=dp(8))

        layout.add_widget(title_layout)
        layout.add_widget(self.photos_grid)

        card.add_widget(layout)
        return card

    def create_photo_item(self, photo_data, index):
        """Create a photo item widget for the photos list"""
        from kivymd.uix.list import MDListItem, MDListItemHeadlineText, MDListItemSupportingText
        from kivymd.uix.button import MDIconButton
        from pathlib import Path
        
        try:
            photo_path = photo_data.get('chemin', '')
            photo_name = Path(photo_path).name if photo_path else 'Photo sans nom'
            
            # Create list item
            item = MDListItem(
                size_hint_y=None,
                height=dp(72)
            )
            
            # Add photo name and description
            item.add_widget(MDListItemHeadlineText(text=photo_name))
            if photo_data.get('description'):
                item.add_widget(MDListItemSupportingText(text=photo_data['description']))
            
            # Add delete button
            delete_btn = MDIconButton(
                icon="delete",
                theme_icon_color="Error",
                on_release=lambda x: self.remove_photo(index)
            )
            item.add_widget(delete_btn)
            
            return item
            
        except Exception as e:
            Logger.error(f"AddRecipeScreen: Error creating photo item: {e}")
            return None

    def remove_photo(self, index):
        """Remove a photo from the recipe"""
        try:
            if 0 <= index < len(self.photos):
                removed_photo = self.photos.pop(index)
                self.refresh_photos_display()
                show_snackbar(f"Photo supprimée: {Path(removed_photo['chemin']).name}")
        except Exception as e:
            Logger.error(f"AddRecipeScreen: Error removing photo: {e}")

    def create_source_section(self):
        """Create source section for recipe origin"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], adaptive_height=True)

        layout = MDBoxLayout(orientation="vertical", spacing=dp(12), adaptive_height=True)

        # Section title
        title = MDLabel(text="Source de la recette", font_size=dp(18), bold=True, theme_text_color="Primary")
        layout.add_widget(title)

        # Source type selection
        type_label = MDLabel(text="Type de source", theme_text_color="Primary", adaptive_height=True)
        layout.add_widget(type_label)

        # Source type buttons
        type_layout = MDBoxLayout(orientation="horizontal", spacing=dp(8), size_hint_y=None, height=dp(40))

        self.source_type = "homemade"  # Default value

        # Homemade button
        self.homemade_btn = MDButton(
            MDButtonText(text="🏠 Maison"),
            style="filled",
            md_bg_color="#4CAF50",
            size_hint_x=0.33,
            on_release=lambda x: self.set_source_type("homemade"),
        )

        # URL button
        self.url_btn = MDButton(
            MDButtonText(text="🌐 URL"),
            style="outlined",
            size_hint_x=0.33,
            on_release=lambda x: self.set_source_type("url"),
        )

        # Book button
        self.book_btn = MDButton(
            MDButtonText(text="📚 Livre"),
            style="outlined",
            size_hint_x=0.33,
            on_release=lambda x: self.set_source_type("book"),
        )

        type_layout.add_widget(self.homemade_btn)
        type_layout.add_widget(self.url_btn)
        type_layout.add_widget(self.book_btn)
        layout.add_widget(type_layout)

        # Dynamic fields container
        self.source_fields_layout = MDBoxLayout(orientation="vertical", spacing=dp(8), adaptive_height=True)
        layout.add_widget(self.source_fields_layout)

        card.add_widget(layout)
        return card

    def set_source_type(self, source_type):
        """Set source type and update UI"""
        self.source_type = source_type

        # Update button styles
        buttons = [self.homemade_btn, self.url_btn, self.book_btn]
        for btn in buttons:
            btn.style = "outlined"
            btn.md_bg_color = [0, 0, 0, 0]  # Reset background

        # Highlight selected button
        if source_type == "homemade":
            self.homemade_btn.style = "filled"
            self.homemade_btn.md_bg_color = "#4CAF50"
        elif source_type == "url":
            self.url_btn.style = "filled"
            self.url_btn.md_bg_color = "#2196F3"
        elif source_type == "book":
            self.book_btn.style = "filled"
            self.book_btn.md_bg_color = "#FF9800"

        # Update fields
        self.update_source_fields()

    def update_source_fields(self):
        """Update source fields based on selected type"""
        # Clear existing fields
        self.source_fields_layout.clear_widgets()

        if self.source_type == "url":
            # URL field
            url_label = MDLabel(text="URL de la recette", theme_text_color="Primary", adaptive_height=True)
            self.source_url_field = MDTextField(
                hint_text="https://exemple.com/recette", mode="outlined", size_hint_y=None, height=dp(56)
            )
            self.source_fields_layout.add_widget(url_label)
            self.source_fields_layout.add_widget(self.source_url_field)

        elif self.source_type == "book":
            # Book fields
            title_label = MDLabel(text="Titre du livre/magazine", theme_text_color="Primary", adaptive_height=True)
            self.source_book_title_field = MDTextField(
                hint_text="Nom du livre ou magazine", mode="outlined", size_hint_y=None, height=dp(56)
            )

            authors_label = MDLabel(text="Auteur(s)", theme_text_color="Primary", adaptive_height=True)
            self.source_book_authors_field = MDTextField(
                hint_text="Nom des auteurs", mode="outlined", size_hint_y=None, height=dp(56)
            )

            page_label = MDLabel(text="Page (optionnel)", theme_text_color="Primary", adaptive_height=True)
            self.source_book_page_field = MDTextField(
                hint_text="Numéro de page", mode="outlined", size_hint_y=None, height=dp(56)
            )

            self.source_fields_layout.add_widget(title_label)
            self.source_fields_layout.add_widget(self.source_book_title_field)
            self.source_fields_layout.add_widget(authors_label)
            self.source_fields_layout.add_widget(self.source_book_authors_field)
            self.source_fields_layout.add_widget(page_label)
            self.source_fields_layout.add_widget(self.source_book_page_field)

    def on_essential_changed(self, checkbox, value):
        """Show/hide alternatives field based on essential checkbox"""
        if value:  # Essential is checked
            # Hide alternatives
            self.alternatives_label.opacity = 0
            self.alternatives_label.height = 0
            self.alternatives_field.opacity = 0
            self.alternatives_field.height = 0
            self.alternatives_field.disabled = True
            self.alternatives_field.text = ""  # Clear text
        else:  # Essential is unchecked (optional ingredient)
            # Show alternatives
            self.alternatives_label.opacity = 1
            self.alternatives_label.height = dp(18)  # Restore height
            self.alternatives_field.opacity = 1
            self.alternatives_field.height = dp(56)  # Restore height
            self.alternatives_field.disabled = False

    def add_ingredient_dialog(self):
        """Show dialog to add ingredient"""
        # Create dialog content
        dialog_card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=10,
            radius=[dp(15)],
            size_hint=(0.9, None),
            height=dp(580),  # Increased height for alternatives field
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
        self.essential_checkbox = MDCheckbox(
            active=True,
            size_hint_x=None,
            width=dp(30),
            on_active=self.on_essential_changed,  # Add callback for checkbox change
        )
        essential_label = MDLabel(text="Ingrédient essentiel", theme_text_color="Primary", adaptive_height=True)
        essential_layout.add_widget(self.essential_checkbox)
        essential_layout.add_widget(essential_label)

        # Alternatives section (hidden by default)
        self.alternatives_label = MDLabel(
            text="Alternatives (séparées par des points-virgules)",
            theme_text_color="Primary",
            adaptive_height=True,
            opacity=0,  # Hidden by default
            height=0,
        )

        self.alternatives_field = MDTextField(
            hint_text="Ex: sauce soja; tamari; sel de céleri",
            mode="outlined",
            size_hint_y=None,
            height=dp(56),
            opacity=0,  # Hidden by default
            disabled=True,  # Disabled when essential is True
        )

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
        content.add_widget(self.alternatives_label)
        content.add_widget(self.alternatives_field)
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

        # Reset editing mode
        self.editing_ingredient_index = None

    def add_ingredient_from_dialog(self, *args):
        """Add or edit ingredient from dialog form"""
        name = self.name_field.text.strip()
        quantity = self.quantity_field.text.strip()
        unit = self.selected_unit  # Use selected unit instead of text field
        essential = self.essential_checkbox.active
        alternatives = self.alternatives_field.text.strip() if not essential else ""

        if not name:
            show_snackbar("Veuillez entrer le nom de l'ingrédient")
            return

        # Check if we're editing an existing ingredient
        if hasattr(self, "editing_ingredient_index") and self.editing_ingredient_index is not None:
            self.update_ingredient(self.editing_ingredient_index, name, quantity, unit, essential, alternatives)
            self.editing_ingredient_index = None  # Reset editing mode
        else:
            self.confirm_add_ingredient(name, quantity, unit, essential, alternatives)

        self.close_ingredient_dialog()

    def confirm_add_ingredient(self, name, quantity, unit, essential, alternatives=""):
        """Add ingredient to list"""
        if not name.strip():
            show_snackbar("Please enter ingredient name")
            return

        ingredient = {
            "nom": name.strip(),
            "quantite": quantity.strip() if quantity else None,
            "unite": unit.strip() if unit else None,
            "indispensable": essential,
            "alternatives": alternatives.strip() if alternatives else None,
        }

        self.ingredients.append(ingredient)
        self.refresh_ingredients_list()
        if self.dialog:
            self.dialog.dismiss()

    def update_ingredient(self, index, name, quantity, unit, essential, alternatives=""):
        """Update existing ingredient at index"""
        if 0 <= index < len(self.ingredients):
            self.ingredients[index] = {
                "nom": name.strip(),
                "quantite": quantity.strip() if quantity else None,
                "unite": unit.strip() if unit else None,
                "indispensable": essential,
                "alternatives": alternatives.strip() if alternatives else None,
            }
            self.refresh_ingredients_list()

    def add_step_dialog(self):
        """Show dialog to add step"""
        # Create overlay background
        self.step_dialog_overlay = MDFloatLayout(
            md_bg_color=(0, 0, 0, 0.5),  # Semi-transparent background
            size_hint=(1, 1),
        )

        # Create step description field
        self.step_description_field = MDTextField(
            hint_text="Décrivez l'étape de préparation...",
            mode="outlined",
            multiline=True,
            max_height=dp(120),
            size_hint_y=None,
            height=dp(120),
        )

        # Create dialog card
        dialog_card = MDCard(
            MDBoxLayout(
                MDLabel(
                    text="Ajouter une étape",
                    font_style="Headline",
                    theme_text_color="Primary",
                    size_hint_y=None,
                    height=dp(40),
                    halign="center",
                ),
                # Step description field
                self.step_description_field,
                # Buttons
                MDBoxLayout(
                    MDButton(
                        MDButtonText(text="Annuler"),
                        style="text",
                        on_release=self.close_step_dialog,
                        size_hint_x=0.5,
                    ),
                    MDButton(
                        MDButtonText(text="Ajouter"),
                        style="filled",
                        on_release=self.add_step_from_dialog,
                        size_hint_x=0.5,
                    ),
                    orientation="horizontal",
                    spacing=dp(12),
                    size_hint_y=None,
                    height=dp(48),
                ),
                orientation="vertical",
                spacing=dp(16),
                padding=dp(20),
                adaptive_height=True,
            ),
            style="filled",
            size_hint=(0.9, None),
            adaptive_height=True,
            pos_hint={"center_x": 0.5, "center_y": 0.5},
            elevation=8,
            radius=[dp(12)],
        )

        self.step_dialog_overlay.add_widget(dialog_card)
        self.add_widget(self.step_dialog_overlay)

    def close_step_dialog(self, *args):
        """Close the step dialog"""
        if hasattr(self, "step_dialog_overlay"):
            self.remove_widget(self.step_dialog_overlay)
            delattr(self, "step_dialog_overlay")

        # Reset editing mode
        self.editing_step_index = None

    def add_step_from_dialog(self, *args):
        """Add or edit step from dialog form"""
        description = self.step_description_field.text.strip()

        if not description:
            show_snackbar("Veuillez décrire l'étape")
            return

        # Check if we're editing an existing step
        if hasattr(self, "editing_step_index") and self.editing_step_index is not None:
            self.update_step(self.editing_step_index, description)
            self.editing_step_index = None  # Reset editing mode
        else:
            self.confirm_add_step(description)

        self.close_step_dialog()

    def confirm_add_step(self, description):
        """Add step to list"""
        if not description.strip():
            show_snackbar("Veuillez décrire l'étape")
            return

        self.steps.append(description.strip())
        self.refresh_steps_list()

    def update_step(self, index, description):
        """Update existing step at index"""
        if 0 <= index < len(self.steps):
            self.steps[index] = description.strip()
            self.refresh_steps_list()

    def refresh_ingredients_list(self):
        """Refresh ingredients display"""
        self.ingredients_list.clear_widgets()

        for i, ingredient in enumerate(self.ingredients):
            name_text = ingredient["nom"]
            quantity_text = ""
            if ingredient["quantite"]:
                quantity_text = ingredient["quantite"]
                if ingredient["unite"]:
                    quantity_text += f" {ingredient['unite']}"

            # Build secondary text with essential status
            if ingredient["indispensable"]:
                secondary_text = "Essentiel"
                tertiary_text = ""  # Remove numbering
            else:
                secondary_text = "Optionnel"
                if ingredient.get("alternatives"):
                    # Show only first few alternatives to avoid truncation
                    alternatives = ingredient["alternatives"]
                    alt_parts = [alt.strip() for alt in alternatives.split(";") if alt.strip()]

                    if len(alt_parts) <= 2:
                        # Show all if 2 or less
                        tertiary_text = f"Alt: {', '.join(alt_parts)}"
                    else:
                        # Show first 2 and indicate more
                        tertiary_text = f"Alt: {', '.join(alt_parts[:2])} (+{len(alt_parts) - 2} autres)"
                else:
                    tertiary_text = ""  # Remove numbering

            # Create a custom card layout for better control
            card = MDCard(
                MDBoxLayout(
                    MDBoxLayout(
                        MDLabel(
                            text=name_text,
                            theme_text_color="Primary",
                            font_style="Body",
                            bold=True,
                            size_hint_y=None,
                            height=dp(28),
                            adaptive_height=True,
                        ),
                        (
                            MDLabel(
                                text=quantity_text,
                                theme_text_color="Primary",
                                font_style="Body",
                                size_hint_y=None,
                                height=dp(20) if quantity_text else dp(0),
                                adaptive_height=True,
                            )
                            if quantity_text
                            else MDLabel(height=dp(0))
                        ),
                        MDLabel(
                            text=secondary_text,
                            theme_text_color="Secondary",
                            font_style="Body",
                            size_hint_y=None,
                            height=dp(20),
                            adaptive_height=True,
                        ),
                        MDLabel(
                            text=tertiary_text,
                            theme_text_color="Secondary",
                            font_style="Body",
                            size_hint_y=None,
                            height=dp(24),
                            adaptive_height=True,
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.85,
                        adaptive_height=True,
                    ),
                    MDBoxLayout(
                        MDIconButton(
                            icon="chevron-up",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_ingredient_up(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == 0,  # Disable if first item
                        ),
                        MDIconButton(
                            icon="chevron-down",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_ingredient_down(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == len(self.ingredients) - 1,  # Disable if last item
                        ),
                        MDIconButton(
                            icon="delete",
                            theme_icon_color="Custom",
                            icon_color="red",
                            on_release=lambda x, idx=i: self.remove_ingredient(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.15,
                        adaptive_height=True,
                    ),
                    orientation="horizontal",
                    spacing=dp(12),
                    padding=[dp(16), dp(12), dp(16), dp(12)],
                    adaptive_height=True,
                ),
                style="outlined",
                size_hint_y=None,
                adaptive_height=True,
                on_release=lambda x, idx=i: self.edit_ingredient(idx),
                elevation=1,
                radius=[dp(8)],
            )

            self.ingredients_list.add_widget(card)

    def refresh_steps_list(self):
        """Refresh steps display"""
        self.steps_list.clear_widgets()

        for i, step in enumerate(self.steps):
            # Create a custom card layout for steps
            card = MDCard(
                MDBoxLayout(
                    MDBoxLayout(
                        MDLabel(
                            text=f"Étape {i + 1}",
                            theme_text_color="Primary",
                            font_style="Body",
                            bold=True,
                            size_hint_y=None,
                            height=dp(24),
                            adaptive_height=True,
                        ),
                        MDLabel(
                            text=step[:100] + "..." if len(step) > 100 else step,
                            theme_text_color="Secondary",
                            font_style="Body",
                            size_hint_y=None,
                            adaptive_height=True,
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.85,
                        adaptive_height=True,
                    ),
                    MDBoxLayout(
                        MDIconButton(
                            icon="chevron-up",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_step_up(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == 0,  # Disable if first item
                        ),
                        MDIconButton(
                            icon="chevron-down",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_step_down(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == len(self.steps) - 1,  # Disable if last item
                        ),
                        MDIconButton(
                            icon="delete",
                            theme_icon_color="Custom",
                            icon_color="red",
                            on_release=lambda x, idx=i: self.remove_step(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.15,
                        adaptive_height=True,
                    ),
                    orientation="horizontal",
                    spacing=dp(12),
                    padding=[dp(16), dp(12), dp(16), dp(12)],
                    adaptive_height=True,
                ),
                style="outlined",
                size_hint_y=None,
                adaptive_height=True,
                on_release=lambda x, idx=i: self.edit_step(idx),
                elevation=1,
                radius=[dp(8)],
            )

            self.steps_list.add_widget(card)

    def remove_ingredient(self, index):
        """Remove ingredient from list"""
        if 0 <= index < len(self.ingredients):
            self.ingredients.pop(index)
            self.refresh_ingredients_list()

    def move_ingredient_up(self, index):
        """Move ingredient up in the list"""
        if index > 0 and index < len(self.ingredients):
            # Swap with previous ingredient
            self.ingredients[index], self.ingredients[index - 1] = self.ingredients[index - 1], self.ingredients[index]
            self.refresh_ingredients_list()

    def move_ingredient_down(self, index):
        """Move ingredient down in the list"""
        if index >= 0 and index < len(self.ingredients) - 1:
            # Swap with next ingredient
            self.ingredients[index], self.ingredients[index + 1] = self.ingredients[index + 1], self.ingredients[index]
            self.refresh_ingredients_list()

    def edit_ingredient(self, index):
        """Edit existing ingredient"""
        if 0 <= index < len(self.ingredients):
            ingredient = self.ingredients[index]

            # Store the editing index
            self.editing_ingredient_index = index

            # Pre-fill the dialog with existing values
            self.add_ingredient_dialog()

            # Set the values in the dialog fields
            self.name_field.text = ingredient.get("nom") or ""
            self.quantity_field.text = ingredient.get("quantite") or ""

            # Set selected unit and update button display
            unit = ingredient.get("unite") or ""
            self.selected_unit = unit
            if unit:
                # Update the button text to show selected unit
                self.unit_button.children[0].text = unit
            else:
                self.unit_button.children[0].text = "Sélectionner une unité"

            self.essential_checkbox.active = ingredient.get("indispensable", False)

            # Handle alternatives field - ensure we have a string, not None
            alternatives = ingredient.get("alternatives") or ""
            self.alternatives_field.text = alternatives

            # Update dialog title to indicate editing
            if hasattr(self, "ingredient_dialog_title"):
                self.ingredient_dialog_title.text = "Modifier l'ingrédient"

    def remove_step(self, index):
        """Remove step from list"""
        if 0 <= index < len(self.steps):
            self.steps.pop(index)
            self.refresh_steps_list()

    def move_step_up(self, index):
        """Move step up in the list"""
        if index > 0 and index < len(self.steps):
            # Swap with previous step
            self.steps[index], self.steps[index - 1] = self.steps[index - 1], self.steps[index]
            self.refresh_steps_list()

    def move_step_down(self, index):
        """Move step down in the list"""
        if index >= 0 and index < len(self.steps) - 1:
            # Swap with next step
            self.steps[index], self.steps[index + 1] = self.steps[index + 1], self.steps[index]
            self.refresh_steps_list()

    def edit_step(self, index):
        """Edit existing step"""
        if 0 <= index < len(self.steps):
            step = self.steps[index]

            # Store the editing index
            self.editing_step_index = index

            # Pre-fill the dialog with existing values
            self.add_step_dialog()

            # Set the value in the dialog field
            self.step_description_field.text = step

    def show_photo_options(self):
        """Show photo capture/selection options"""
        # Create overlay background
        self.photo_dialog_overlay = MDFloatLayout(
            md_bg_color=(0, 0, 0, 0.5),  # Semi-transparent background
            size_hint=(1, 1),
        )

        # Create dialog card
        dialog_card = MDCard(
            MDBoxLayout(
                MDLabel(
                    text="Ajouter une photo",
                    font_style="Headline",
                    theme_text_color="Primary",
                    size_hint_y=None,
                    height=dp(40),
                    halign="center",
                ),
                # Camera button
                MDButton(
                    MDButtonText(text="📷 Prendre une photo"),
                    style="outlined",
                    on_release=self.take_photo_and_close,
                    size_hint_y=None,
                    height=dp(56),
                ),
                # Gallery button
                MDButton(
                    MDButtonText(text="🖼️ Choisir depuis la galerie"),
                    style="outlined",
                    on_release=self.choose_from_gallery_and_close,
                    size_hint_y=None,
                    height=dp(56),
                ),
                # Cancel button
                MDButton(
                    MDButtonText(text="Annuler"),
                    style="text",
                    on_release=self.close_photo_dialog,
                    size_hint_y=None,
                    height=dp(48),
                ),
                orientation="vertical",
                spacing=dp(16),
                padding=dp(20),
                adaptive_height=True,
            ),
            style="filled",
            size_hint=(0.8, None),
            adaptive_height=True,
            pos_hint={"center_x": 0.5, "center_y": 0.5},
            elevation=8,
            radius=[dp(12)],
        )

        self.photo_dialog_overlay.add_widget(dialog_card)
        self.add_widget(self.photo_dialog_overlay)

    def close_photo_dialog(self, *args):
        """Close the photo dialog"""
        if hasattr(self, "photo_dialog_overlay"):
            self.remove_widget(self.photo_dialog_overlay)
            delattr(self, "photo_dialog_overlay")

    def take_photo_and_close(self, *args):
        """Take photo and close dialog"""
        self.close_photo_dialog()
        self.take_photo()

    def choose_from_gallery_and_close(self, *args):
        """Choose from gallery and close dialog"""
        self.close_photo_dialog()
        self.choose_from_gallery()

    def take_photo(self):
        """Take a photo using camera"""
        if self.photo_manager:
            recipe_name = self.recipe_name.text.strip() or "recipe"
            filename = self.photo_manager.generate_photo_filename(recipe_name.replace(" ", "_"), "recipe")

            self.photo_manager.take_photo(filename, callback=self.on_photo_captured)
        if self.dialog:
            self.dialog.dismiss()

    def choose_from_gallery(self):
        """Choose photo from gallery - alias for select_from_gallery"""
        self.select_from_gallery()

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
            # Create a card for each photo similar to ingredients/steps
            card = MDCard(
                MDBoxLayout(
                    MDBoxLayout(
                        MDLabel(
                            text="📸",
                            font_size=dp(32),
                            theme_text_color="Primary",
                            size_hint_y=None,
                            height=dp(40),
                            halign="center",
                        ),
                        MDLabel(
                            text=f"Photo {i + 1}",
                            theme_text_color="Primary",
                            font_style="Body",
                            bold=True,
                            size_hint_y=None,
                            height=dp(24),
                            halign="center",
                        ),
                        MDLabel(
                            text=photo.get("chemin", "").split("/")[-1] if photo.get("chemin") else "Image",
                            theme_text_color="Secondary",
                            font_style="Body",
                            size_hint_y=None,
                            height=dp(20),
                            halign="center",
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.85,
                        adaptive_height=True,
                    ),
                    MDBoxLayout(
                        MDIconButton(
                            icon="chevron-up",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_photo_up(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == 0,  # Disable if first item
                        ),
                        MDIconButton(
                            icon="chevron-down",
                            theme_icon_color="Primary",
                            on_release=lambda x, idx=i: self.move_photo_down(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                            disabled=i == len(self.photos) - 1,  # Disable if last item
                        ),
                        MDIconButton(
                            icon="delete",
                            theme_icon_color="Custom",
                            icon_color="red",
                            on_release=lambda x, idx=i: self.remove_photo(idx),
                            size_hint_y=None,
                            height=dp(32),
                            width=dp(32),
                        ),
                        orientation="vertical",
                        spacing=dp(4),
                        size_hint_x=0.15,
                        adaptive_height=True,
                    ),
                    orientation="horizontal",
                    spacing=dp(12),
                    padding=[dp(16), dp(12), dp(16), dp(12)],
                    adaptive_height=True,
                ),
                style="outlined",
                size_hint_y=None,
                adaptive_height=True,
                on_release=lambda x, idx=i: self.view_photo(idx),
                elevation=1,
                radius=[dp(8)],
            )

            self.photos_grid.add_widget(card)

    def remove_photo(self, index):
        """Remove photo from list"""
        if 0 <= index < len(self.photos):
            photo = self.photos.pop(index)
            if self.photo_manager:
                self.photo_manager.delete_photo(photo["chemin"])
            self.refresh_photos_grid()
            show_snackbar("Photo removed")

    def move_photo_up(self, index):
        """Move photo up in the list"""
        if index > 0 and index < len(self.photos):
            # Swap with previous photo
            self.photos[index], self.photos[index - 1] = self.photos[index - 1], self.photos[index]
            self.refresh_photos_grid()

    def move_photo_down(self, index):
        """Move photo down in the list"""
        if index >= 0 and index < len(self.photos) - 1:
            # Swap with next photo
            self.photos[index], self.photos[index + 1] = self.photos[index + 1], self.photos[index]
            self.refresh_photos_grid()

    def view_photo(self, index):
        """View photo in full screen - placeholder for future implementation"""
        if 0 <= index < len(self.photos):
            photo_path = self.photos[index].get("chemin", "")
            show_snackbar(f"Visualisation photo: {photo_path.split('/')[-1]}")
            # TODO: Implement photo viewer dialog

    def get_source_data(self):
        """Get source data based on current selection"""
        source_data = {"type": self.source_type}

        if self.source_type == "url":
            if hasattr(self, "source_url_field") and self.source_url_field.text.strip():
                source_data["url"] = self.source_url_field.text.strip()
        elif self.source_type == "book":
            if hasattr(self, "source_book_title_field") and self.source_book_title_field.text.strip():
                source_data["book_title"] = self.source_book_title_field.text.strip()
            if hasattr(self, "source_book_authors_field") and self.source_book_authors_field.text.strip():
                source_data["book_authors"] = self.source_book_authors_field.text.strip()
            if hasattr(self, "source_book_page_field") and self.source_book_page_field.text.strip():
                source_data["book_page"] = self.source_book_page_field.text.strip()

        return source_data

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
                "source": self.get_source_data(),
            }

            # Save to database
            with get_db_session() as session:
                if self.editing_recipe_id:
                    # Update existing recipe
                    from src.crud.recettes import update_recette

                    updated_recipe = update_recette(session, self.editing_recipe_id, recipe_data)
                    if updated_recipe:
                        show_snackbar(f"Recipe '{updated_recipe.nom}' updated successfully!")
                    else:
                        show_snackbar("Error updating recipe")
                        return
                else:
                    # Create new recipe
                    new_recipe = create_recette(session, recipe_data)
                    show_snackbar(f"Recipe '{new_recipe.nom}' saved successfully!")

            # Clear form and go back
            self.clear_form()
            self.editing_recipe_id = None  # Reset editing mode
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
        self.editing_recipe_id = None  # Reset editing mode
        self.refresh_ingredients_list()
        self.refresh_steps_list()
        self.refresh_photos_grid()

    def go_back(self):
        """Go back to previous screen"""
        self.manager.current = "home"

    def on_enter(self):
        """Called when screen is entered"""
        app = App.get_running_app()

        # Check if we're in edit mode
        if app and hasattr(app, "editing_recipe_id") and app.editing_recipe_id:
            self.load_recipe_for_editing(app.editing_recipe_id)
            app.editing_recipe_id = None  # Clear the editing ID
        else:
            # Clear form when entering screen in add mode
            self.clear_form()

    def load_recipe_for_editing(self, recipe_id):
        """Load recipe data for editing"""
        try:
            from src.crud.recettes import get_recette_by_id
            from src.db import get_db_session

            with get_db_session() as session:
                recipe = get_recette_by_id(session, recipe_id)

                if recipe:
                    # Fill form with recipe data
                    self.recipe_name.text = recipe.nom or ""
                    self.prep_time.text = str(recipe.preparation) if recipe.preparation else ""
                    self.cook_time.text = str(recipe.cuisson) if recipe.cuisson else ""
                    self.portions.text = str(recipe.portions) if recipe.portions else "1"

                    # Load categories and tags
                    categories_list = [cat.nom for cat in recipe.categories] if recipe.categories else []
                    self.categories.text = ", ".join(categories_list)

                    tags_list = [tag.nom for tag in recipe.tags] if recipe.tags else []
                    self.tags.text = ", ".join(tags_list)

                    # Load ingredients
                    self.ingredients = []
                    for ingredient in recipe.ingredients:
                        self.ingredients.append(
                            {
                                "nom": ingredient.nom,
                                "quantite": ingredient.quantite,
                                "unite": ingredient.unite,
                                "indispensable": ingredient.indispensable,
                                "alternatives": ingredient.alternatives,
                            }
                        )

                    # Load steps
                    self.steps = []
                    for step in recipe.etapes:
                        self.steps.append(step.description)

                    # Load photos
                    self.photos = []
                    for photo in recipe.photos:
                        self.photos.append({"chemin": photo.chemin, "categorie": photo.categorie})

                    # Load source data
                    if recipe.source:
                        self.source_type = recipe.source.type or "homemade"
                        self.set_source_type(self.source_type)

                        # Fill source-specific fields if they exist
                        if self.source_type == "url" and recipe.source.url:
                            if hasattr(self, "source_url_field"):
                                self.source_url_field.text = recipe.source.url
                        elif self.source_type == "book":
                            if hasattr(self, "source_book_title_field") and recipe.source.book_title:
                                self.source_book_title_field.text = recipe.source.book_title
                            if hasattr(self, "source_book_authors_field") and recipe.source.book_authors:
                                self.source_book_authors_field.text = recipe.source.book_authors
                            if hasattr(self, "source_book_page_field") and recipe.source.book_page:
                                self.source_book_page_field.text = recipe.source.book_page
                    else:
                        # Default to homemade if no source
                        self.source_type = "homemade"
                        self.set_source_type(self.source_type)

                    # Refresh all lists
                    self.refresh_ingredients_list()
                    self.refresh_steps_list()
                    self.refresh_photos_grid()

                    # Store the recipe ID for updating
                    self.editing_recipe_id = recipe_id

                    show_snackbar(f"Loaded recipe: {recipe.nom}")
                else:
                    show_snackbar("Recipe not found")
                    self.clear_form()

        except Exception as e:
            print(f"Error loading recipe for editing: {e}")
            show_snackbar("Error loading recipe")
            self.clear_form()
