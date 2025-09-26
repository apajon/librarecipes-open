"""
Floating Action Button Manager for LibraRecipes
Handles the primary '+' FAB and secondary camera FAB with Material Design 3 styling
"""

from kivy.app import App
from kivy.clock import Clock
from kivy.metrics import dp
from kivy.utils import platform
from kivymd.uix.button import MDIconButton
from kivymd.uix.floatlayout import MDFloatLayout
from kivymd.uix.tooltip import MDTooltip
from kivy.logger import Logger

try:
    from utils.photo_manager import PhotoManager
except ImportError:
    PhotoManager = None
    Logger.warning("FABManager: PhotoManager not available")


class TooltipIconButton(MDIconButton, MDTooltip):
    """Icon button with tooltip support for accessibility"""
    pass


class FABManager:
    """Manages floating action buttons for recipe management"""
    
    def __init__(self, screen, context="general"):
        """
        Initialize FAB Manager
        
        Args:
            screen: The screen widget to attach FABs to
            context: The context ('general', 'add_recipe', 'recipe_detail') to determine behavior
        """
        self.screen = screen
        self.context = context
        self.app = App.get_running_app()
        self.photo_manager = PhotoManager() if PhotoManager else None
        
        # FAB references
        self.fab_container = None
        self.add_fab = None
        self.photo_fab = None
        
        # Camera permission status
        self.camera_available = False
        self.camera_permission_granted = False
        
        self.init_fabs()
        self.check_camera_availability()
    
    def init_fabs(self):
        """Initialize the floating action buttons"""
        # Create container for FABs
        self.fab_container = MDFloatLayout(
            size_hint=(None, None),
            size=(dp(56), dp(120)),  # Space for both FABs
            pos_hint={'right': 0.95, 'bottom': 0.05}
        )
        
        # Primary '+' FAB (hidden in add_recipe context since it's redundant)
        add_fab_visible = self.context != "add_recipe"
        
        self.add_fab = TooltipIconButton(
            icon="plus",
            theme_icon_color="Custom",
            icon_color=(1, 1, 1, 1),  # White icon
            md_bg_color=self.app.colors["primary"],
            size_hint=(None, None),
            size=(dp(56), dp(56)),
            pos_hint={'center_x': 0.5, 'bottom': 0},
            elevation=6,
            tooltip_text="Ajouter une recette",
            opacity=1 if add_fab_visible else 0,
            on_release=self.on_add_fab_pressed
        )
        
        # Secondary photo FAB (conditional visibility) - position differently if add FAB is hidden
        photo_fab_bottom = 0 if not add_fab_visible else 0.55
        
        self.photo_fab = TooltipIconButton(
            icon="camera",
            theme_icon_color="Custom", 
            icon_color=(1, 1, 1, 1),  # White icon
            md_bg_color=self.app.colors.get("secondary_peach", "#FFE5CC"),
            size_hint=(None, None),
            size=(dp(48), dp(48)),  # Slightly smaller
            pos_hint={'center_x': 0.5, 'bottom': photo_fab_bottom},  # Above the + FAB or at bottom if + is hidden
            elevation=4,
            tooltip_text="Prendre une photo",
            opacity=0,  # Start hidden, show after permission check
            on_release=self.on_photo_fab_pressed
        )
        
        # Add FABs to container
        self.fab_container.add_widget(self.add_fab)
        self.fab_container.add_widget(self.photo_fab)
        
        # Add container to screen
        if hasattr(self.screen, 'add_widget'):
            self.screen.add_widget(self.fab_container)
    
    def check_camera_availability(self):
        """Check camera availability and permissions"""
        if not self.photo_manager:
            Logger.info("FABManager: PhotoManager not available")
            return
            
        # Check if camera is available
        if platform == 'android':
            try:
                from plyer import camera
                self.camera_available = camera is not None
            except ImportError:
                self.camera_available = False
        else:
            # On desktop, assume camera is available for development
            self.camera_available = True
        
        if self.camera_available:
            # Request camera permissions
            self.photo_manager.request_camera_permission(
                callback=self.on_permission_result
            )
        else:
            Logger.info("FABManager: Camera not available")
    
    def on_permission_result(self, permissions_result):
        """Handle camera permission result"""
        if permissions_result:
            self.camera_permission_granted = True
            self.show_photo_fab()
            Logger.info("FABManager: Camera permission granted")
        else:
            self.camera_permission_granted = False
            Logger.warning("FABManager: Camera permission denied")
    
    def show_photo_fab(self):
        """Animate the photo FAB into view"""
        if self.photo_fab and self.camera_available and self.camera_permission_granted:
            # Animate opacity from 0 to 1
            from kivy.animation import Animation
            anim = Animation(opacity=1, duration=0.3)
            anim.start(self.photo_fab)
    
    def hide_photo_fab(self):
        """Animate the photo FAB out of view"""
        if self.photo_fab:
            from kivy.animation import Animation
            anim = Animation(opacity=0, duration=0.3)
            anim.start(self.photo_fab)
    
    def on_add_fab_pressed(self, *args):
        """Handle add recipe FAB press"""
        Logger.info(f"FABManager: Add FAB pressed in context: {self.context}")
        
        # Navigate to add recipe screen
        try:
            self.screen.manager.current = "add_recipe"
        except AttributeError:
            Logger.error("FABManager: Could not navigate - no screen manager")
    
    def on_photo_fab_pressed(self, *args):
        """Handle photo FAB press"""
        Logger.info(f"FABManager: Photo FAB pressed in context: {self.context}")
        
        if not self.photo_manager:
            Logger.warning("FABManager: PhotoManager not available")
            return
        
        if not self.camera_permission_granted:
            Logger.warning("FABManager: Camera permission not granted")
            return
        
        # Generate filename based on context
        if self.context == "add_recipe":
            # If in add_recipe screen, associate with current recipe
            filename = self.photo_manager.generate_photo_filename(
                recipe_id="new_recipe", 
                photo_type="recipe"
            )
        else:
            # General photo capture
            filename = self.photo_manager.generate_photo_filename(
                recipe_id="general", 
                photo_type="general"
            )
        
        # Take photo
        self.photo_manager.take_photo(
            filename=filename,
            callback=self.on_photo_taken
        )
    
    def on_photo_taken(self, photo_path, error=None):
        """Handle photo capture result"""
        if error:
            Logger.error(f"FABManager: Photo capture error: {error}")
            # Show error message to user
            from kivymd.uix.snackbar import MDSnackbar, MDSnackbarText
            MDSnackbar(
                MDSnackbarText(text=f"Erreur lors de la capture: {error}")
            ).open()
        else:
            Logger.info(f"FABManager: Photo captured successfully: {photo_path}")
            # Show success message
            from kivymd.uix.snackbar import MDSnackbar, MDSnackbarText
            MDSnackbar(
                MDSnackbarText(text="Photo capturée avec succès!")
            ).open()
            
            # Handle photo based on context
            if self.context == "add_recipe":
                # If in add_recipe screen, add photo to recipe
                self.add_photo_to_current_recipe(photo_path)
    
    def add_photo_to_current_recipe(self, photo_path):
        """Add captured photo to current recipe being edited"""
        # This would integrate with the AddRecipeScreen to add the photo
        # to the current recipe being created/edited
        try:
            if hasattr(self.screen, 'add_photo_to_recipe'):
                self.screen.add_photo_to_recipe(photo_path)
        except Exception as e:
            Logger.error(f"FABManager: Could not add photo to recipe: {e}")
    
    def update_context(self, new_context):
        """Update the FAB context (useful when screen context changes)"""
        self.context = new_context
        Logger.info(f"FABManager: Context updated to {new_context}")
    
    def set_visibility(self, visible=True):
        """Show or hide the entire FAB container"""
        if self.fab_container:
            self.fab_container.opacity = 1 if visible else 0
    
    def destroy(self):
        """Clean up FAB manager resources"""
        if self.fab_container and hasattr(self.screen, 'remove_widget'):
            self.screen.remove_widget(self.fab_container)
        self.fab_container = None
        self.add_fab = None
        self.photo_fab = None