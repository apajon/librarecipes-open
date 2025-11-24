"""
IO utilities for loading and saving theme configuration files.
"""

import json
from pathlib import Path
from typing import Union

from .models import ThemeConfig


def load_theme(path: Union[str, Path]) -> ThemeConfig:
    """
    Load theme configuration from a JSON file.
    
    Args:
        path: Path to the theme JSON file
        
    Returns:
        Parsed ThemeConfig object
        
    Raises:
        FileNotFoundError: If the file doesn't exist
        ValidationError: If the JSON content is invalid
        JSONDecodeError: If the file is not valid JSON
    """
    path = Path(path)
    
    if not path.exists():
        raise FileNotFoundError(f"Theme file not found: {path}")
    
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    # Use Pydantic v2 model_validate
    return ThemeConfig.model_validate(data)


def save_theme(config: ThemeConfig, path: Union[str, Path]) -> None:
    """
    Save theme configuration to a JSON file.
    
    Args:
        config: ThemeConfig object to save
        path: Path where to save the theme JSON file
        
    The JSON is pretty-printed with 2-space indentation for human readability.
    """
    path = Path(path)
    
    # Ensure parent directory exists
    path.parent.mkdir(parents=True, exist_ok=True)
    
    # Use Pydantic v2 model_dump
    data = config.model_dump(mode="json")
    
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        # Add trailing newline for better git diffs
        f.write("\n")
