"""
LibraRecipes Mobile App
The recipe notebook you will never lose.
"""

import sys
from pathlib import Path

from kivy.core.text import LabelBase
from kivy.core.window import Window
from kivy.utils import platform
from kivymd.app import MDApp
from kivymd.uix.screenmanager import MDScreenManager

# Add the app directory to Python path for imports
sys.path.append(str(Path(__file__).parent.parent))

from screens.add_recipe_screen import AddRecipeScreen  # noqa: E402

# Now import our local modules after path is set
from screens.home_screen import HomeScreen  # noqa: E402
from screens.recipe_detail_screen import RecipeDetailScreen  # noqa: E402
from screens.recipe_list_screen import RecipeListScreen  # noqa: E402
from screens.search_screen import SearchScreen  # noqa: E402

# Import database layer based on platform
if platform == "android":
    import src.db_android as db_android  # noqa: E402
else:
    from src.db import engine  # noqa: E402
    from src.model import Base  # noqa: E402

# Set window size for desktop testing
if platform not in ("android", "ios"):
    Window.size = (360, 640)


class LibraRecipesApp(MDApp):
    """Main application class for LibraRecipes mobile app"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.title = "LibraRecipes"
        self.theme_cls.primary_palette = "Teal"
        self.theme_cls.primary_hue = "600"  # #006D77
        self.theme_cls.accent_palette = "Orange"
        self.theme_cls.accent_hue = "300"
        self.theme_cls.theme_style = "Light"

        # Custom colors for our branding
        self.colors = {
            "primary": "#006D77",
            "secondary_peach": "#FFE5CC",
            "dark_curry": "#91372C",
            "navy": "#090C6B",
            "yellow": "#FFD447",
            "brown": "#4E342E",
            "purple": "#8A4699",
            "green": "#A5E079",
        }

        # Navigation tracking
        self.selected_recipe_id = None
        self.previous_screen = "home"
        self.returning_from_detail = False  # Flag to preserve search state

        # Initialize database
        self.init_database()

    def init_database(self):
        """Initialize the database and create tables"""
        try:
            if platform == "android":
                # Use Android-compatible database layer
                db_android.init_database()
                print("Android database initialized successfully")
            else:
                # Create data directory if it doesn't exist
                data_dir = Path(__file__).parent.parent / "data"
                data_dir.mkdir(exist_ok=True)

                # Create all tables
                Base.metadata.create_all(engine)
                print("Desktop database initialized successfully")
        except Exception as e:
            print(f"Database initialization error: {e}")

    def build(self):
        """Build the app interface"""
        # Register custom fonts
        self.register_fonts()

        # Create screen manager
        sm = MDScreenManager()

        # Add screens
        sm.add_widget(HomeScreen(name="home"))
        sm.add_widget(RecipeListScreen(name="recipe_list"))
        sm.add_widget(AddRecipeScreen(name="add_recipe"))
        sm.add_widget(RecipeDetailScreen(name="recipe_detail"))
        sm.add_widget(SearchScreen(name="search"))

        return sm

    def register_fonts(self):
        """Register custom fonts for the app"""
        # Note: In a real app, you'd include font files in the assets
        # For now, we'll use system defaults that are similar
        try:
            # These would be actual font files in assets/fonts/
            LabelBase.register(name="Merienda", fn_regular="assets/fonts/Merienda-Regular.ttf")
            LabelBase.register(name="Nunito", fn_regular="assets/fonts/Nunito-Regular.ttf")
        except Exception as e:
            # Fallback to system fonts
            print(f"Custom fonts not found, using system defaults: {e}")


if __name__ == "__main__":
    LibraRecipesApp().run()
