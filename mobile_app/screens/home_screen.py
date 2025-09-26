"""
Home Screen for LibraRecipes Mobile App
Main dashboard with quick stats and navigation
"""

from kivy.metrics import dp
from kivymd.uix.appbar import MDTopAppBar, MDTopAppBarTitle
from kivymd.uix.boxlayout import MDBoxLayout
from kivymd.uix.button import MDButton
from kivymd.uix.button.button import MDButtonIcon, MDButtonText
from kivymd.uix.card import MDCard
from kivymd.uix.gridlayout import MDGridLayout
from kivymd.uix.label import MDLabel
from kivymd.uix.screen import MDScreen

from src.crud.metadata import get_all_categories
from src.crud.recettes import list_recettes
from src.db import get_db_session

# Import FAB Manager
try:
    from components.fab_manager import FABManager
except ImportError:
    FABManager = None


class HomeScreen(MDScreen):
    """Home screen with app branding and quick navigation"""

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.fab_manager = None
        self.build_screen()
        self.setup_fabs()

    def setup_fabs(self):
        """Initialize floating action buttons"""
        if FABManager:
            self.fab_manager = FABManager(self, context="general")

    def build_screen(self):
        """Build the home screen layout"""
        # Main layout
        main_layout = MDBoxLayout(orientation="vertical", spacing=dp(16), padding=dp(16))

        # App bar
        app_bar = MDTopAppBar(MDTopAppBarTitle(text="LibraRecipes"))
        main_layout.add_widget(app_bar)

        # Content area with padding for FABs
        content = MDBoxLayout(orientation="vertical", spacing=dp(20), adaptive_height=True, padding=(0, 0, 0, dp(140)))  # Bottom padding for FABs

        # Welcome section
        welcome_card = self.create_welcome_card()
        content.add_widget(welcome_card)

        # Statistics section
        stats_card = self.create_stats_card()
        content.add_widget(stats_card)

        # Quick actions
        actions_card = self.create_actions_card()
        content.add_widget(actions_card)

        main_layout.add_widget(content)
        self.add_widget(main_layout)

    def create_welcome_card(self):
        """Create welcome card with app branding"""
        card = MDCard(
            padding=dp(20),
            spacing=dp(10),
            elevation=2,
            radius=[dp(10)],
            size_hint_y=None,
            height=dp(120),
        )

        layout = MDBoxLayout(orientation="vertical", spacing=dp(8))

        # App title
        title = MDLabel(
            text="LibraRecipes",
            font_size=dp(28),
            bold=True,
            theme_text_color="Primary",
            halign="center",
            size_hint_y=None,
            height=dp(40),
        )

        # Slogan
        slogan = MDLabel(
            text="The recipe notebook you will never lose.",
            font_size=dp(16),
            theme_text_color="Secondary",
            halign="center",
            italic=True,
            size_hint_y=None,
            height=dp(30),
        )

        layout.add_widget(title)
        layout.add_widget(slogan)
        card.add_widget(layout)

        return card

    def create_stats_card(self):
        """Create statistics card showing recipe counts"""
        card = MDCard(padding=dp(20), spacing=dp(10), elevation=2, radius=[dp(10)], size_hint_y=None, height=dp(140))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(10))

        # Title
        title = MDLabel(
            text="Your Recipe Collection",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Stats grid
        stats_grid = MDGridLayout(cols=2, spacing=dp(10), adaptive_height=True)

        # Get stats from database
        stats = self.get_recipe_stats()

        # Recipe count
        recipe_stat = self.create_stat_item("", "Recipes", str(stats["recipes"]))
        category_stat = self.create_stat_item("", "Categories", str(stats["categories"]))

        stats_grid.add_widget(recipe_stat)
        stats_grid.add_widget(category_stat)

        layout.add_widget(title)
        layout.add_widget(stats_grid)
        card.add_widget(layout)

        return card

    def create_stat_item(self, icon, label, value):
        """Create a single statistic item"""
        layout = MDBoxLayout(orientation="vertical", spacing=dp(5), size_hint_y=None, height=dp(60))

        # Icon and value
        top_layout = MDBoxLayout(orientation="horizontal", spacing=dp(5), adaptive_height=True)

        icon_label = MDLabel(text=icon, font_size=dp(20), size_hint_x=None, width=dp(30), halign="center")

        value_label = MDLabel(
            text=value,
            font_size=dp(24),
            bold=True,
            theme_text_color="Primary",
        )

        top_layout.add_widget(icon_label)
        top_layout.add_widget(value_label)

        # Label
        label_widget = MDLabel(
            text=label, font_size=dp(12), theme_text_color="Secondary", size_hint_y=None, height=dp(20)
        )

        layout.add_widget(top_layout)
        layout.add_widget(label_widget)

        return layout

    def create_actions_card(self):
        """Create quick actions card"""
        card = MDCard(padding=dp(20), spacing=dp(15), elevation=2, radius=[dp(10)], size_hint_y=None, height=dp(200))

        layout = MDBoxLayout(orientation="vertical", spacing=dp(15))

        # Title
        title = MDLabel(
            text="Quick Actions",
            font_size=dp(18),
            bold=True,
            theme_text_color="Primary",
            size_hint_y=None,
            height=dp(30),
        )

        # Action buttons
        actions_layout = MDBoxLayout(orientation="vertical", spacing=dp(10), adaptive_height=True)

        # Add recipe button
        add_btn = MDButton(
            MDButtonIcon(icon="plus"),
            MDButtonText(text="Add New Recipe"),
            style="filled",
            size_hint_y=None,
            height=dp(40),
            on_release=lambda x: self.navigate_to_screen("add_recipe"),
        )

        # Browse recipes button
        browse_btn = MDButton(
            MDButtonIcon(icon="book-open-variant"),
            MDButtonText(text="Browse Recipes"),
            style="filled",
            size_hint_y=None,
            height=dp(40),
            on_release=lambda x: self.navigate_to_screen("recipe_list"),
        )

        # Search button
        search_btn = MDButton(
            MDButtonIcon(icon="magnify"),
            MDButtonText(text="Search Recipes"),
            style="filled",
            size_hint_y=None,
            height=dp(40),
            on_release=lambda x: self.navigate_to_screen("search"),
        )

        actions_layout.add_widget(add_btn)
        actions_layout.add_widget(browse_btn)
        actions_layout.add_widget(search_btn)

        layout.add_widget(title)
        layout.add_widget(actions_layout)
        card.add_widget(layout)

        return card

    def get_recipe_stats(self):
        """Get recipe statistics from database"""
        try:
            with get_db_session() as session:
                recipes = list_recettes(session)
                categories = get_all_categories(session)

                return {"recipes": len(recipes), "categories": len(categories)}
        except Exception as e:
            print(f"Error getting stats: {e}")
            return {"recipes": 0, "categories": 0}

    def navigate_to_screen(self, screen_name):
        """Navigate to specified screen"""
        self.manager.current = screen_name
