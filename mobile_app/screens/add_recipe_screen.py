"""
Add Recipe Screen for LibraRecipes Mobile App
Form to create new recipes with ingredients and steps
"""

from kivymd.uix.screen import MDScreen
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.textfield import MDTextField
from kivymd.uix.button import MDRaisedButton, MDIconButton, MDFlatButton
from kivymd.uix.toolbar import MDTopAppBar
from kivymd.uix.scrollview import MDScrollView
from kivymd.uix.card import MDCard
from kivymd.uix.label import MDLabel
from kivymd.uix.selectioncontrol import MDCheckbox
from kivymd.uix.list import MDList, ThreeLineListItem
from kivymd.uix.dialog import MDDialog
from kivymd.uix.snackbar import Snackbar
from kivymd.uix.gridlayout import MDGridLayout
from kivy.metrics import dp
from kivy.app import App

from src.db import get_db_session
from src.crud.recettes import create_recette

# Import photo manager
try:
    from utils.photo_manager import PhotoManager
except ImportError:
    PhotoManager = None


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
    
    def build_screen(self):
        """Build the add recipe screen layout"""
        # Main layout
        main_layout = MDBoxLayout(
            orientation='vertical'
        )
        
        # App bar
        app_bar = MDTopAppBar(
            title="Add New Recipe",
            md_bg_color=App.get_running_app().colors['primary'],
            specific_text_color="white",
            left_action_items=[["arrow-left", lambda x: self.go_back()]],
            right_action_items=[["content-save", lambda x: self.save_recipe()]]
        )
        main_layout.add_widget(app_bar)
        
        # Scrollable content
        scroll = MDScrollView()
        content = MDBoxLayout(
            orientation='vertical',
            spacing=dp(16),
            padding=dp(16),
            adaptive_height=True
        )
        
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
        save_btn = MDRaisedButton(
            text="💾 Save Recipe",
            md_bg_color=App.get_running_app().colors['primary'],
            theme_text_color="Custom",
            text_color="white",
            size_hint_y=None,
            height=dp(50),
            on_release=lambda x: self.save_recipe()
        )
        content.add_widget(save_btn)
        
        scroll.add_widget(content)
        main_layout.add_widget(scroll)
        self.add_widget(main_layout)
    
    def create_basic_info_section(self):
        """Create basic recipe information form"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(280)
        )
        
        layout = MDBoxLayout(
            orientation='vertical',
            spacing=dp(12)
        )
        
        # Section title
        title = MDLabel(
            text="📝 Recipe Information",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30)
        )
        
        # Recipe name
        self.recipe_name = MDTextField(
            hint_text="Recipe name*",
            required=True,
            size_hint_y=None,
            height=dp(40)
        )
        
        # Time and portions in grid
        time_layout = MDBoxLayout(
            orientation='horizontal',
            spacing=dp(10),
            size_hint_y=None,
            height=dp(40)
        )
        
        self.prep_time = MDTextField(
            hint_text="Prep time (min)",
            input_filter='int',
            size_hint_x=0.33
        )
        
        self.cook_time = MDTextField(
            hint_text="Cook time (min)",
            input_filter='int',
            size_hint_x=0.33
        )
        
        self.portions = MDTextField(
            hint_text="Portions",
            input_filter='int',
            size_hint_x=0.33
        )
        
        time_layout.add_widget(self.prep_time)
        time_layout.add_widget(self.cook_time)
        time_layout.add_widget(self.portions)
        
        # Categories and tags
        self.categories = MDTextField(
            hint_text="Categories (comma separated)",
            size_hint_y=None,
            height=dp(40)
        )
        
        self.tags = MDTextField(
            hint_text="Tags (comma separated)",
            size_hint_y=None,
            height=dp(40)
        )
        
        layout.add_widget(title)
        layout.add_widget(self.recipe_name)
        layout.add_widget(time_layout)
        layout.add_widget(self.categories)
        layout.add_widget(self.tags)
        
        card.add_widget(layout)
        return card
    
    def create_ingredients_section(self):
        """Create ingredients management section"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=2,
            radius=[dp(10)],
            adaptive_height=True
        )
        
        layout = MDBoxLayout(
            orientation='vertical',
            spacing=dp(12),
            adaptive_height=True
        )
        
        # Section title with add button
        title_layout = MDBoxLayout(
            orientation='horizontal',
            size_hint_y=None,
            height=dp(40)
        )
        
        title = MDLabel(
            text="🧂 Ingredients",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary"
        )
        
        add_ingredient_btn = MDIconButton(
            icon="plus",
            theme_icon_color="Custom",
            icon_color=App.get_running_app().colors['primary'],
            on_release=lambda x: self.add_ingredient_dialog()
        )
        
        title_layout.add_widget(title)
        title_layout.add_widget(add_ingredient_btn)
        
        # Ingredients list
        self.ingredients_list = MDList(
            adaptive_height=True
        )
        
        layout.add_widget(title_layout)
        layout.add_widget(self.ingredients_list)
        
        card.add_widget(layout)
        return card
    
    def create_steps_section(self):
        """Create recipe steps management section"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(15),
            elevation=2,
            radius=[dp(10)],
            adaptive_height=True
        )
        
        layout = MDBoxLayout(
            orientation='vertical',
            spacing=dp(12),
            adaptive_height=True
        )
        
        # Section title with add button
        title_layout = MDBoxLayout(
            orientation='horizontal',
            size_hint_y=None,
            height=dp(40)
        )
        
        title = MDLabel(
            text="📋 Preparation Steps",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary"
        )
        
        add_step_btn = MDIconButton(
            icon="plus",
            theme_icon_color="Custom",
            icon_color=App.get_running_app().colors['primary'],
            on_release=lambda x: self.add_step_dialog()
        )
        
        title_layout.add_widget(title)
        title_layout.add_widget(add_step_btn)
        
        # Steps list
        self.steps_list = MDList(
            adaptive_height=True
        )
        
        layout.add_widget(title_layout)
        layout.add_widget(self.steps_list)
        
        card.add_widget(layout)
        return card
    
    def add_ingredient_dialog(self):
        """Show dialog to add ingredient"""
        content = MDBoxLayout(
            orientation='vertical',
            spacing=dp(10),
            adaptive_height=True
        )
        
        name_field = MDTextField(
            hint_text="Ingredient name*",
            required=True
        )
        
        quantity_field = MDTextField(
            hint_text="Quantity"
        )
        
        unit_field = MDTextField(
            hint_text="Unit (g, ml, cups, etc.)"
        )
        
        essential_layout = MDBoxLayout(
            orientation='horizontal',
            spacing=dp(10),
            adaptive_height=True
        )
        
        essential_checkbox = MDCheckbox(
            active=True,
            size_hint_x=None,
            width=dp(30)
        )
        
        essential_label = MDLabel(
            text="Essential ingredient",
            theme_text_color="Primary"
        )
        
        essential_layout.add_widget(essential_checkbox)
        essential_layout.add_widget(essential_label)
        
        content.add_widget(name_field)
        content.add_widget(quantity_field)
        content.add_widget(unit_field)
        content.add_widget(essential_layout)
        
        self.dialog = MDDialog(
            title="Add Ingredient",
            type="custom",
            content_cls=content,
            buttons=[
                MDRaisedButton(
                    text="Cancel",
                    on_release=lambda x: self.dialog.dismiss()
                ),
                MDRaisedButton(
                    text="Add",
                    md_bg_color=App.get_running_app().colors['primary'],
                    theme_text_color="Custom",
                    text_color="white",
                    on_release=lambda x: self.confirm_add_ingredient(
                        name_field.text, quantity_field.text, 
                        unit_field.text, essential_checkbox.active
                    )
                )
            ]
        )
        self.dialog.open()
    
    def confirm_add_ingredient(self, name, quantity, unit, essential):
        """Add ingredient to list"""
        if not name.strip():
            Snackbar(text="Please enter ingredient name").open()
            return
        
        ingredient = {
            'nom': name.strip(),
            'quantite': quantity.strip() if quantity else None,
            'unite': unit.strip() if unit else None,
            'indispensable': essential
        }
        
        self.ingredients.append(ingredient)
        self.refresh_ingredients_list()
        self.dialog.dismiss()
    
    def add_step_dialog(self):
        """Show dialog to add preparation step"""
        content = MDBoxLayout(
            orientation='vertical',
            spacing=dp(10),
            adaptive_height=True
        )
        
        step_field = MDTextField(
            hint_text="Describe this preparation step...",
            multiline=True,
            max_text_length=500
        )
        
        content.add_widget(step_field)
        
        self.dialog = MDDialog(
            title=f"Add Step {len(self.steps) + 1}",
            type="custom",
            content_cls=content,
            buttons=[
                MDRaisedButton(
                    text="Cancel",
                    on_release=lambda x: self.dialog.dismiss()
                ),
                MDRaisedButton(
                    text="Add",
                    md_bg_color=App.get_running_app().colors['primary'],
                    theme_text_color="Custom",
                    text_color="white",
                    on_release=lambda x: self.confirm_add_step(step_field.text)
                )
            ]
        )
        self.dialog.open()
    
    def confirm_add_step(self, description):
        """Add step to list"""
        if not description.strip():
            Snackbar(text="Please enter step description").open()
            return
        
        self.steps.append(description.strip())
        self.refresh_steps_list()
        self.dialog.dismiss()
    
    def refresh_ingredients_list(self):
        """Refresh ingredients display"""
        self.ingredients_list.clear_widgets()
        
        for i, ingredient in enumerate(self.ingredients):
            text = ingredient['nom']
            if ingredient['quantite']:
                text += f" - {ingredient['quantite']}"
            if ingredient['unite']:
                text += f" {ingredient['unite']}"
            
            secondary_text = "Essential" if ingredient['indispensable'] else "Optional"
            
            item = ThreeLineListItem(
                text=text,
                secondary_text=secondary_text,
                tertiary_text=f"Ingredient {i+1}",
                on_release=lambda x, idx=i: self.remove_ingredient(idx)
            )
            
            self.ingredients_list.add_widget(item)
    
    def refresh_steps_list(self):
        """Refresh steps display"""
        self.steps_list.clear_widgets()
        
        for i, step in enumerate(self.steps):
            item = ThreeLineListItem(
                text=f"Step {i+1}",
                secondary_text=step[:50] + "..." if len(step) > 50 else step,
                tertiary_text="Tap to remove",
                on_release=lambda x, idx=i: self.remove_step(idx)
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
    
    def save_recipe(self):
        """Save the recipe to database"""
        # Validate required fields
        if not self.recipe_name.text.strip():
            Snackbar(text="Please enter recipe name").open()
            return
        
        if not self.ingredients:
            Snackbar(text="Please add at least one ingredient").open()
            return
        
        if not self.steps:
            Snackbar(text="Please add at least one preparation step").open()
            return
        
        try:
            # Prepare recipe data
            recipe_data = {
                'nom': self.recipe_name.text.strip(),
                'preparation': int(self.prep_time.text) if self.prep_time.text.strip() else None,
                'cuisson': int(self.cook_time.text) if self.cook_time.text.strip() else None,
                'portions': int(self.portions.text) if self.portions.text.strip() else None,
                'ingredients': self.ingredients,
                'etapes': self.steps,
                'categories': [cat.strip() for cat in self.categories.text.split(',') if cat.strip()],
                'tags': [tag.strip() for tag in self.tags.text.split(',') if tag.strip()],
                'source': {'type': 'homemade'}
            }
            
            # Save to database
            with get_db_session() as session:
                new_recipe = create_recette(session, recipe_data)
                
            Snackbar(text=f"Recipe '{new_recipe.nom}' saved successfully!").open()
            
            # Clear form and go back
            self.clear_form()
            self.manager.current = 'recipe_list'
            
        except Exception as e:
            print(f"Error saving recipe: {e}")
            Snackbar(text="Error saving recipe. Please try again.").open()
    
    def clear_form(self):
        """Clear all form fields"""
        self.recipe_name.text = ""
        self.prep_time.text = ""
        self.cook_time.text = ""
        self.portions.text = ""
        self.categories.text = ""
        self.tags.text = ""
        self.ingredients = []
        self.steps = []
        self.refresh_ingredients_list()
        self.refresh_steps_list()
    
    def go_back(self):
        """Go back to previous screen"""
        self.manager.current = 'home'
    
    def on_enter(self):
        """Called when screen is entered"""
        # Clear form when entering screen
        self.clear_form()