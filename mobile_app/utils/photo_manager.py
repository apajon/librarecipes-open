"""
Photo management utilities for LibraRecipes Mobile App
Handles camera capture and gallery access for Android
"""

import os
from pathlib import Path
from kivy.utils import platform
from kivy.logger import Logger

try:
    if platform == 'android':
        from plyer import camera, filechooser
        from android.permissions import request_permissions, Permission
    else:
        # Desktop fallback
        camera = None
        filechooser = None
except ImportError:
    camera = None
    filechooser = None
    Logger.warning("PhotoManager: Plyer not available, photo features disabled")


class PhotoManager:
    """Manages photo capture and selection for recipes"""
    
    def __init__(self):
        self.photos_dir = self.get_photos_directory()
        self.ensure_photos_directory()
        
    def get_photos_directory(self):
        """Get the directory for storing recipe photos"""
        if platform == 'android':
            from android.storage import primary_external_storage_path
            photos_path = os.path.join(
                primary_external_storage_path(), 
                'LibraRecipes', 
                'photos'
            )
        else:
            # Desktop development
            photos_path = Path(__file__).parent.parent.parent / "data" / "photos"
        
        return Path(photos_path)
    
    def ensure_photos_directory(self):
        """Create photos directory if it doesn't exist"""
        try:
            self.photos_dir.mkdir(parents=True, exist_ok=True)
            Logger.info(f"PhotoManager: Photos directory: {self.photos_dir}")
        except Exception as e:
            Logger.error(f"PhotoManager: Could not create photos directory: {e}")
    
    def request_camera_permission(self, callback=None):
        """Request camera permission on Android"""
        if platform == 'android':
            try:
                request_permissions([
                    Permission.CAMERA,
                    Permission.WRITE_EXTERNAL_STORAGE,
                    Permission.READ_EXTERNAL_STORAGE
                ], callback)
                return True
            except Exception as e:
                Logger.error(f"PhotoManager: Permission request failed: {e}")
                return False
        return True
    
    def take_photo(self, filename, callback=None):
        """Take a photo using device camera"""
        if not camera:
            Logger.warning("PhotoManager: Camera not available")
            if callback:
                callback(None, "Camera not available")
            return
        
        photo_path = self.photos_dir / filename
        
        try:
            camera.take_picture(
                filename=str(photo_path),
                on_complete=lambda *args: self._on_photo_complete(photo_path, callback, *args)
            )
        except Exception as e:
            Logger.error(f"PhotoManager: Camera error: {e}")
            if callback:
                callback(None, str(e))
    
    def select_from_gallery(self, callback=None):
        """Select photo from device gallery"""
        if not filechooser:
            Logger.warning("PhotoManager: File chooser not available")
            if callback:
                callback(None, "Gallery not available")
            return
        
        try:
            filechooser.open_file(
                on_selection=lambda selection: self._on_gallery_selection(selection, callback),
                filters=["*.jpg", "*.jpeg", "*.png"]
            )
        except Exception as e:
            Logger.error(f"PhotoManager: Gallery error: {e}")
            if callback:
                callback(None, str(e))
    
    def _on_photo_complete(self, photo_path, callback, *args):
        """Handle camera photo completion"""
        if photo_path.exists():
            Logger.info(f"PhotoManager: Photo saved: {photo_path}")
            if callback:
                callback(str(photo_path), None)
        else:
            Logger.error("PhotoManager: Photo not saved")
            if callback:
                callback(None, "Photo not saved")
    
    def _on_gallery_selection(self, selection, callback):
        """Handle gallery photo selection"""
        if selection and len(selection) > 0:
            selected_file = selection[0]
            Logger.info(f"PhotoManager: Photo selected: {selected_file}")
            
            # Copy to our photos directory
            try:
                import shutil
                filename = Path(selected_file).name
                dest_path = self.photos_dir / filename
                shutil.copy2(selected_file, dest_path)
                
                if callback:
                    callback(str(dest_path), None)
            except Exception as e:
                Logger.error(f"PhotoManager: Copy error: {e}")
                if callback:
                    callback(None, str(e))
        else:
            if callback:
                callback(None, "No photo selected")
    
    def delete_photo(self, photo_path):
        """Delete a photo file"""
        try:
            photo_file = Path(photo_path)
            if photo_file.exists():
                photo_file.unlink()
                Logger.info(f"PhotoManager: Photo deleted: {photo_path}")
                return True
            else:
                Logger.warning(f"PhotoManager: Photo not found: {photo_path}")
                return False
        except Exception as e:
            Logger.error(f"PhotoManager: Delete error: {e}")
            return False
    
    def get_photo_url(self, photo_path):
        """Get displayable URL for photo"""
        if not photo_path:
            return None
        
        photo_file = Path(photo_path)
        if photo_file.exists():
            return str(photo_file)
        return None
    
    def list_recipe_photos(self, recipe_id):
        """List all photos for a specific recipe"""
        try:
            pattern = f"{recipe_id}_*.jpg"
            photos = list(self.photos_dir.glob(pattern))
            photos.extend(list(self.photos_dir.glob(f"{recipe_id}_*.jpeg")))
            photos.extend(list(self.photos_dir.glob(f"{recipe_id}_*.png")))
            return [str(p) for p in photos]
        except Exception as e:
            Logger.error(f"PhotoManager: List photos error: {e}")
            return []
    
    def generate_photo_filename(self, recipe_id, photo_type="general"):
        """Generate unique filename for recipe photo"""
        import uuid
        extension = "jpg"
        filename = f"{recipe_id}_{photo_type}_{uuid.uuid4().hex[:8]}.{extension}"
        return filename