import tomllib
from pathlib import Path


def get_version():
    pyproject = Path(__file__).parent.parent / "pyproject.toml"
    with pyproject.open("rb") as f:
        data = tomllib.load(f)
    return data["project"]["version"]


__version__ = get_version()
