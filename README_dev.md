# LibraRecipes - Documentation Développeur

Ce document contient toutes les informations techniques nécessaires pour développer et maintenir LibraRecipes.

## 📋 Table des matières

- [Architecture globale](#architecture-globale)
- [Structure du projet](#structure-du-projet)
- [Technologies et dépendances](#technologies-et-dépendances)
- [Conventions de développement](#conventions-de-développement)
- [Backend et API](#backend-et-api)
- [Frontend Streamlit](#frontend-streamlit)
- [Application Android](#application-android)
- [Base de données](#base-de-données)
- [Tests](#tests)
- [CI/CD](#cicd)
- [Ajout de nouvelles fonctionnalités](#ajout-de-nouvelles-fonctionnalités)
- [Bonnes pratiques](#bonnes-pratiques)

## Architecture globale

LibraRecipes est composé de trois composants principaux :

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Streamlit                    │
│              (streamlit_app/Home.py)                     │
│          Interface web pour utilisateurs                 │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ Direct DB Access
                 │
┌────────────────▼────────────────────────────────────────┐
│             Backend & Business Logic                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │  FastAPI REST API (backend/main.py)              │   │
│  │  - Endpoints RESTful                             │   │
│  │  - Validation Pydantic                           │   │
│  └────────────┬─────────────────────────────────────┘   │
│               │                                          │
│  ┌────────────▼─────────────────────────────────────┐   │
│  │  Business Logic (src/)                           │   │
│  │  - Models SQLAlchemy (model.py)                  │   │
│  │  - CRUD Operations (crud/)                       │   │
│  │  - Database Setup (db.py)                        │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────┬────────────────────────────────────────┘
                 │
                 ▼
       ┌──────────────────┐
       │  SQLite Database │
       │ (data/recettes.db)│
       └──────────────────┘
                 ▲
                 │ HTTP REST API
                 │
┌────────────────┴────────────────────────────────────────┐
│              Application Android                         │
│         (android_app/ - Kotlin/Compose)                  │
│         Application mobile native                        │
└─────────────────────────────────────────────────────────┘
```

### Flux de données

- **Streamlit** : Accès direct à SQLAlchemy ORM → SQLite
- **Android** : HTTP → FastAPI → SQLAlchemy ORM → SQLite
- **Photos** : Stockage local dans `data/photos/`

## Structure du projet

```
librarecipes-open/
├── streamlit_app/              # Interface web Streamlit
│   ├── Home.py                 # Point d'entrée principal
│   ├── config/                 # Configuration et pages
│   │   ├── app.py              # Configuration Streamlit
│   │   └── pages.py            # Définition des pages
│   ├── pages_functions/        # Logique des pages
│   │   ├── home.py             # Page d'accueil
│   │   ├── recherche_recette.py # Recherche avancée
│   │   ├── add_recette.py      # Ajout de recettes
│   │   ├── card_recette.py     # Détails recette
│   │   ├── modify_recette.py   # Modification
│   │   ├── index_recettes.py   # Index A-Z
│   │   ├── index_ingredients.py # Index ingrédients
│   │   └── que_cuisiner.py     # Suggestions
│   ├── utils/                  # Utilitaires UI
│   │   ├── navigation.py       # Navigation entre pages
│   │   ├── sidebar.py          # Gestion sidebar
│   │   ├── recipe_display.py   # Formatage affichage
│   │   ├── recipe_validator.py # Validation formulaires
│   │   ├── recipe_selector.py  # Sélecteur recettes
│   │   └── constants.py        # Constantes UI
│   └── assets/                 # Images et ressources
│
├── backend/                    # API REST FastAPI
│   ├── main.py                 # Point d'entrée API
│   └── schemas.py              # Schémas Pydantic
│
├── src/                        # Logique métier et données
│   ├── model.py                # Modèles SQLAlchemy
│   ├── db.py                   # Configuration base de données
│   ├── crud/                   # Opérations CRUD
│   │   ├── recettes.py         # CRUD recettes
│   │   ├── recherche.py        # Recherche avancée
│   │   ├── ingredients.py      # Gestion ingrédients
│   │   └── metadata.py         # Catégories, tags
│   └── utils/                  # Utilitaires backend
│
├── android_app/                # Application Android native
│   ├── app/src/main/java/com/apajon/librarecipes/
│   │   ├── data/               # Couche données
│   │   │   ├── api/            # Services API Retrofit
│   │   │   ├── model/          # Modèles de données
│   │   │   └── repository/     # Repositories
│   │   ├── di/                 # Injection dépendances (Hilt)
│   │   ├── ui/                 # Interface utilisateur
│   │   │   ├── components/     # Composants réutilisables
│   │   │   ├── navigation/     # Navigation
│   │   │   ├── screens/        # Écrans Compose
│   │   │   └── theme/          # Thème Material 3
│   │   └── viewmodel/          # ViewModels
│   ├── README.md               # Documentation Android
│   ├── BACKEND_SETUP.md        # Configuration backend
│   └── TROUBLESHOOTING.md      # Résolution problèmes
│
├── scripts/                    # Scripts utilitaires
│   ├── init_db.py              # Initialisation base
│   ├── add_sample_data.py      # Données de test
│   └── get_version.py          # Gestion version
│
├── tests/                      # Tests unitaires et intégration
│   ├── test_crud.py            # Tests CRUD
│   ├── test_recherche.py       # Tests recherche
│   └── test_api.py             # Tests API
│
├── data/                       # Données locales
│   ├── recettes.db             # Base de données SQLite
│   └── photos/                 # Photos des recettes
│
├── .github/                    # Configuration GitHub
│   ├── workflows/              # CI/CD GitHub Actions
│   └── copilot-instructions.md # Instructions pour Copilot
│
├── pyproject.toml              # Configuration Poetry
├── poetry.lock                 # Lock des dépendances
├── requirements.txt            # Requirements pip (legacy)
├── .pylintrc                   # Configuration pylint
├── pyrightconfig.json          # Configuration Pyright
├── .pre-commit-config.yaml     # Hooks pre-commit
└── README.md                   # Documentation utilisateur
```

## Technologies et dépendances

### Python Backend

**Version Python** : 3.12+

**Gestionnaire de dépendances** : Poetry

**Dépendances principales** :
```toml
streamlit = ">=1.46.1"          # Interface web
fastapi = ">=0.115.0"           # API REST
uvicorn = ">=0.25.0"            # Serveur ASGI
sqlalchemy = ">=2.0.41"         # ORM
pydantic = ">=2.5.0"            # Validation données
pillow = ">=10.0.0"             # Manipulation images
streamlit-tags = ">=1.2.8"      # Tags input Streamlit
```

**Dépendances de développement** :
```toml
pytest = ">=7.4.0"              # Tests
black = ">=23.0.0"              # Formatage code
pylint = ">=3.0.0"              # Linting
pyright = ">=1.1.0"             # Type checking
pre-commit = ">=3.5.0"          # Git hooks
```

### Android

**Langage** : Kotlin 1.9.22

**Framework UI** : Jetpack Compose + Material 3

**Architecture** : MVVM avec Repository pattern

**Dépendances principales** :
- Android SDK 24+ (minimum), 34 (target)
- Jetpack Compose BOM 2024.02.00
- Hilt 2.48 (injection de dépendances)
- Retrofit 2.9.0 (client HTTP)
- Navigation Compose 2.7.6
- Lifecycle ViewModel Compose 2.7.0

### Base de données

**Type** : SQLite

**ORM** : SQLAlchemy 2.0+

**Fichier** : `data/recettes.db`

## Conventions de développement

### Conventions Python

#### Nommage

- **Fichiers** : snake_case (ex: `recipe_validator.py`)
- **Classes** : PascalCase (ex: `RecetteCreate`)
- **Fonctions/Variables** : snake_case (ex: `create_recette()`)
- **Constantes** : UPPER_SNAKE_CASE (ex: `SOURCE_TYPES`)
- **Modules** : snake_case, noms courts et descriptifs

#### Structure du code

- **Imports** : Regroupés en 3 sections (stdlib, third-party, local)
- **Docstrings** : Style Google (avec Args, Returns, Raises)
- **Type hints** : Obligatoires pour fonctions publiques
- **Ligne max** : 100 caractères (configuré dans .pylintrc)

#### Example

```python
from typing import Optional
from sqlalchemy.orm import Session

def create_recette(
    session: Session,
    data: dict,
) -> Recette:
    """Crée une nouvelle recette dans la base de données.
    
    Args:
        session: Session SQLAlchemy active
        data: Dictionnaire contenant les données de la recette
        
    Returns:
        L'objet Recette créé
        
    Raises:
        ValueError: Si les données sont invalides
    """
    # Implementation...
```

### Conventions Kotlin (Android)

- **Fichiers** : PascalCase.kt (ex: `RecipeListScreen.kt`)
- **Classes** : PascalCase (ex: `RecipeRepository`)
- **Fonctions** : camelCase (ex: `loadRecipes()`)
- **Constantes** : UPPER_SNAKE_CASE (ex: `BASE_URL`)
- **Composables** : PascalCase (ex: `RecipeCard()`)

### Gestion de version

**Outil** : bump2version

**Format** : Semantic Versioning (MAJOR.MINOR.PATCH)

**Fichiers versionnés** :
- `pyproject.toml`
- `.bumpversion.cfg`

**Commandes** :
```bash
# Increment patch version (bug fixes)
bump2version patch

# Increment minor version (new features)
bump2version minor

# Increment major version (breaking changes)
bump2version major
```

### Git

**Branches** :
- `main` : Branche principale stable
- `feature/*` : Nouvelles fonctionnalités
- `fix/*` : Corrections de bugs
- `copilot/*` : Branches créées par GitHub Copilot

**Messages de commit** :
- Format : `<type>: <description courte>`
- Types : feat, fix, docs, style, refactor, test, chore
- Exemple : `feat: Add ingredient search functionality`

## Backend et API

### FastAPI (backend/main.py)

**Point d'entrée** : `backend/main.py`

**Port par défaut** : 8000

**Démarrage** :
```bash
PYTHONPATH=. uvicorn backend.main:app --reload --host 0.0.0.0 --port 8000
```

**Documentation API** :
- Swagger UI : `http://localhost:8000/docs`
- ReDoc : `http://localhost:8000/redoc`
- OpenAPI JSON : `http://localhost:8000/openapi.json`

### Endpoints principaux

```python
# Informations API
GET /                          # Info API et version
GET /health                    # Health check

# Recettes
GET /recipes                   # Liste (pagination: skip, limit)
POST /recipes                  # Créer
GET /recipes/{id}              # Détails
PUT /recipes/{id}              # Modifier
DELETE /recipes/{id}           # Supprimer
POST /recipes/search           # Recherche avancée

# Métadonnées
GET /ingredients               # Liste ingrédients
GET /categories                # Liste catégories
GET /tags                      # Liste tags
```

### Schémas Pydantic (backend/schemas.py)

Les schémas définissent la structure des données API :

```python
# Input schemas
RecetteCreate                  # Création recette
RecetteUpdate                  # Modification recette
RecipeSearchParams             # Paramètres recherche

# Output schemas
RecetteResponse                # Réponse recette
RecetteListResponse            # Liste recettes
```

**Validation** : Automatique via Pydantic

**Sérialisation** : JSON automatique

### Modèles SQLAlchemy (src/model.py)

#### Entités principales

```python
Recette                        # Recette principale
├── Ingredient                 # Ingrédients (N-N)
├── Etape                      # Étapes ordonnées (1-N)
├── Photo                      # Photos (1-N)
├── Source                     # Source (1-1)
├── CategorieRecette           # Catégories (N-N)
├── TagRecette                 # Tags (N-N)
├── Execution                  # Historique (1-N)
└── Convive                    # Convives (N-N via Execution)
```

#### Relations importantes

- **Recette → Ingredients** : Many-to-Many via table `recette_ingredient`
  - Champs : `quantite`, `unite`, `essentiel`, `alternatives`
  
- **Recette → Etapes** : One-to-Many
  - Champs : `description`, `ordre`
  - Ordre géré automatiquement par CRUD
  
- **Recette → Photos** : One-to-Many
  - Champs : `chemin`, `type_photo`, `date_ajout`
  
- **Recette → Source** : One-to-One
  - Types : `maison`, `url`, `livre`
  - Champs conditionnels selon type

#### Conventions modèle

- **IDs** : UUID stockés comme strings
- **Dates** : `datetime` avec timezone aware si nécessaire
- **Cascade** : `delete-orphan` pour enfants dépendants
- **Lazy loading** : `joined` pour relations fréquentes

### CRUD Operations (src/crud/)

#### Structure

```
crud/
├── recettes.py         # CRUD recettes
├── recherche.py        # Recherche avancée
├── ingredients.py      # Gestion ingrédients
└── metadata.py         # Catégories, tags
```

#### Conventions CRUD

**Sessions** : Toujours utiliser context manager
```python
from src.db import get_db_session

with get_db_session() as session:
    recette = create_recette(session, data)
    # Session fermée automatiquement
```

**Création de recettes** :
```python
# crud/recettes.py : create_recette()
data = {
    "nom": "Tarte aux pommes",
    "temps_preparation": 30,
    "temps_cuisson": 45,
    "nombre_personnes": 6,
    "ingredients": [
        {
            "nom": "Pommes",
            "quantite": 4,
            "unite": "pièce(s)",
            "essentiel": True
        }
    ],
    "etapes": [
        "Éplucher les pommes",
        "Préparer la pâte",
        "Cuire au four"
    ],  # Ordre calculé automatiquement
    "categories": ["Dessert"],
    "tags": ["Facile", "Rapide"],
    "source": {
        "type_source": "maison"
    }
}
```

**Modification de recettes** :
```python
# crud/recettes.py : update_recette()
# Remplacement TOTAL si clé présente et non None
update_data = {
    "nom": "Nouveau nom",
    "ingredients": [],  # VIDE la liste
    # "etapes" omis = conservé tel quel
}
```

**Recherche** :
```python
# crud/recherche.py : rechercher_recettes()
from src.crud.recherche import IngredientsMode

results = rechercher_recettes(
    session,
    ingredients=["pommes", "sucre"],
    mode_ingredients=IngredientsMode.ALL  # Tous requis
)
```

## Frontend Streamlit

### Architecture de navigation

**Streamlit ≥ 1.46** : Utilise les nouvelles APIs `st.Page`, `st.navigation`, `st.switch_page`

#### Configuration des pages (streamlit_app/config/pages.py)

```python
def get_pages_config() -> list:
    """Retourne la configuration de toutes les pages."""
    return [
        st.Page(home_page, title="Accueil", icon="🏠"),
        st.Page(recherche_page, title="Recherche", icon="🔍"),
        # ... autres pages
    ]
```

#### Initialisation (streamlit_app/config/app.py)

```python
def setup_session_state_pages(pages_config):
    """Configure st.session_state avec les pages."""
    if "pages" not in st.session_state:
        st.session_state.pages = pages_config
```

#### Point d'entrée (streamlit_app/Home.py)

```python
# Charge config et pages
pages = get_pages_config()
setup_session_state_pages(pages)

# Navigation
nav = st.navigation(pages)
nav.run()
```

### Navigation entre pages

**Module** : `streamlit_app/utils/navigation.py`

**Fonctions** :
```python
navigate_to_recipe_detail(recette_id: str)  # Vers détails
navigate_to_modify_recipe(recette_id: str)  # Vers modification
navigate_to_photos_recipe(recette_id: str)  # Vers photos
```

**Mécanisme** :
1. Stocke `recette_id` dans `st.session_state.selected_recette_id`
2. Met à jour `st.query_params.recette_id`
3. Appelle `st.switch_page(target_page)`

### État de l'application (Session State)

**Clés importantes** :
```python
st.session_state.pages              # Liste des pages
st.session_state.selected_recette_id # ID recette sélectionnée (str)
```

**⚠️ Attention** : Toujours caster les IDs en `str` avant stockage

### Composants réutilisables

#### Sidebar (utils/sidebar.py)

Gestion de la barre latérale avec navigation et filtres

#### Recipe Display (utils/recipe_display.py)

```python
format_recette_display_name(recette: Recette) -> str
```
Nom unifié : inclut source, date ajout, dernière exécution

#### Recipe Validator (utils/recipe_validator.py)

Validation des formulaires avant création/modification

#### Recipe Selector (utils/recipe_selector.py)

Composant de sélection de recettes avec autocomplétion

#### Constants (utils/constants.py)

```python
SOURCE_TYPES = {
    "maison": "🏠 Maison",
    "url": "🌐 Internet",
    "livre": "📚 Livre"
}
```

### Ajouter une nouvelle page

1. **Créer la fonction page** :
```python
# streamlit_app/pages_functions/ma_page.py
import streamlit as st

def ma_page():
    """Ma nouvelle page."""
    st.title("Ma Page")
    st.write("Contenu...")
```

2. **Référencer dans config** :
```python
# streamlit_app/config/pages.py
from streamlit_app.pages_functions.ma_page import ma_page

def get_pages_config():
    return [
        # ... pages existantes
        st.Page(ma_page, title="Ma Page", icon="🎯"),
    ]
```

3. **Optionnel : Navigation directe** :
```python
# streamlit_app/utils/navigation.py
def navigate_to_ma_page():
    st.switch_page(st.session_state.pages[INDEX_MA_PAGE])
```

### Bonnes pratiques Streamlit

1. **Sessions DB** : Toujours dans le scope du callback
```python
# ❌ MAU VAIS
session = get_db_session()  # En dehors callback
if st.button("Save"):
    save_data(session)

# ✅ BON
if st.button("Save"):
    with get_db_session() as session:
        save_data(session)
```

2. **IDs dans state** : Toujours en string
```python
# ✅ BON
st.session_state.selected_recette_id = str(recette.id)
```

3. **Réexécutions** : Gérer avec `st.session_state` et callbacks

## Application Android

Consultez les fichiers spécifiques :
- `android_app/README.md` : Architecture et structure
- `android_app/BACKEND_SETUP.md` : Configuration backend
- `android_app/TROUBLESHOOTING.md` : Résolution problèmes

### Architecture MVVM

```
UI (Compose) → ViewModel → Repository → API Service → Backend
```

### Points d'intégration

**Configuration réseau** :
- Émulateur : `http://10.0.2.2:8000/`
- Appareil réel : `http://[IP_ORDINATEUR]:8000/`

**Fichier** : `android_app/app/src/main/java/com/apajon/librarecipes/di/NetworkModule.kt`

## Base de données

### Configuration (src/db.py)

```python
DATABASE_URL = "sqlite:///data/recettes.db"
engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False}  # SQLite + multithread
)
```

### Migrations

**⚠️ Pas de migrations automatiques pour le moment**

Modifications du schéma :
1. Modifier `src/model.py`
2. Supprimer `data/recettes.db`
3. Réinitialiser : `PYTHONPATH=. python scripts/init_db.py`

**TODO** : Implémenter Alembic pour migrations

### Initialisation

```bash
# Créer la base vide
PYTHONPATH=. python scripts/init_db.py

# Ajouter des données de test
PYTHONPATH=. python scripts/add_sample_data.py
```

### Backup

```bash
# Backup manuel
cp data/recettes.db data/recettes.db.backup

# Backup photos
cp -r data/photos data/photos.backup
```

## Tests

### Structure des tests

```
tests/
├── test_crud.py           # Tests CRUD operations
├── test_recherche.py      # Tests recherche
├── test_api.py            # Tests API FastAPI
└── conftest.py            # Fixtures pytest
```

### Exécution des tests

```bash
# Tous les tests
PYTHONPATH=. pytest

# Tests spécifiques
PYTHONPATH=. pytest tests/test_crud.py

# Avec couverture
PYTHONPATH=. pytest --cov=src --cov=backend

# Mode verbeux
PYTHONPATH=. pytest -v

# Mode quiet
PYTHONPATH=. pytest -q
```

### Écrire un test

```python
import pytest
from src.db import get_db_session
from src.crud.recettes import create_recette

def test_create_recette():
    """Test création d'une recette."""
    with get_db_session() as session:
        data = {
            "nom": "Test Recipe",
            "temps_preparation": 10,
            # ...
        }
        recette = create_recette(session, data)
        assert recette.nom == "Test Recipe"
        assert recette.id is not None
```

### Tests API

```python
from fastapi.testclient import TestClient
from backend.main import app

client = TestClient(app)

def test_read_recipes():
    """Test GET /recipes."""
    response = client.get("/recipes")
    assert response.status_code == 200
    assert "recipes" in response.json()
```

## CI/CD

### GitHub Actions

**Workflows** : `.github/workflows/`

1. **Backend CI** (`backend-ci.yml`)
   - Tests Python avec pytest
   - Linting (pylint, black, mypy)
   - Coverage report

2. **Android CI** (`android-ci.yml`)
   - Build APK
   - Tests unitaires
   - Tests instrumentés

3. **Release** (`release.yml`)
   - Version bump automatique
   - Création de release GitHub
   - Build et publication artifacts

### Pre-commit hooks

**Configuration** : `.pre-commit-config.yaml`

**Hooks actifs** :
- `trailing-whitespace` : Supprime espaces en fin de ligne
- `end-of-file-fixer` : Assure nouvelle ligne en fin de fichier
- `check-yaml` : Vérifie syntaxe YAML
- `check-added-large-files` : Prévient gros fichiers
- `black` : Formatage Python
- `pylint` : Linting Python

**Installation** :
```bash
poetry run pre-commit install
```

**Exécution manuelle** :
```bash
poetry run pre-commit run --all-files
```

## Ajout de nouvelles fonctionnalités

### Workflow général

1. **Créer une branche** : `git checkout -b feature/ma-feature`
2. **Implémenter** :
   - Backend : Modèle → CRUD → API → Tests
   - Frontend : Page function → Config → UI
3. **Tests** : Écrire et exécuter les tests
4. **Linting** : `pre-commit run --all-files`
5. **Commit** : Messages clairs et descriptifs
6. **PR** : Pull Request avec description détaillée

### Ajouter un endpoint API

1. **Définir le schéma** (backend/schemas.py) :
```python
class MonSchemaRequest(BaseModel):
    field1: str
    field2: int

class MonSchemaResponse(BaseModel):
    id: str
    result: str
```

2. **Implémenter CRUD** (src/crud/) :
```python
def ma_operation(session: Session, data: dict):
    # Implementation
    pass
```

3. **Créer l'endpoint** (backend/main.py) :
```python
@app.post("/mon-endpoint", response_model=MonSchemaResponse)
def mon_endpoint(data: MonSchemaRequest):
    with get_db_session() as session:
        result = ma_operation(session, data.dict())
        return result
```

4. **Tester** :
```python
def test_mon_endpoint():
    response = client.post(
        "/mon-endpoint",
        json={"field1": "test", "field2": 42}
    )
    assert response.status_code == 200
```

### Ajouter un filtre de recherche

1. **Étendre CRUD** (src/crud/recherche.py) :
```python
def rechercher_recettes(
    session: Session,
    # ... paramètres existants
    mon_filtre: Optional[str] = None,
):
    query = session.query(Recette)
    # ... filtres existants
    
    if mon_filtre:
        query = query.filter(Recette.mon_champ == mon_filtre)
    
    return query.all()
```

2. **Mettre à jour UI** (streamlit_app/pages_functions/recherche_recette.py) :
```python
# Ajouter widget
mon_filtre = st.text_input("Mon filtre")

# Utiliser dans recherche
results = rechercher_recettes(
    session,
    # ... autres paramètres
    mon_filtre=mon_filtre if mon_filtre else None
)
```

### Modifier le modèle de données

1. **Modifier modèle** (src/model.py)
2. **⚠️ Supprimer la base** : `rm data/recettes.db`
3. **Réinitialiser** : `PYTHONPATH=. python scripts/init_db.py`
4. **Mettre à jour CRUD** si nécessaire
5. **Mettre à jour schémas API** si nécessaire
6. **Mettre à jour tests**

## Bonnes pratiques

### Performance

1. **Requêtes DB** :
   - Utiliser `joinedload()` pour éviter N+1 queries
   - Paginer les résultats (skip/limit)
   - Indexer les colonnes fréquemment cherchées

2. **Streamlit** :
   - Utiliser `@st.cache_data` pour données statiques
   - Éviter requêtes DB à chaque réexécution
   - Gérer l'état avec `st.session_state`

3. **API** :
   - Implémenter rate limiting si exposition publique
   - Utiliser des réponses paginées
   - Valider les inputs avec Pydantic

### Sécurité

1. **Injection SQL** :
   - ✅ Utiliser SQLAlchemy ORM (paramétrisé automatiquement)
   - ❌ Ne jamais construire des requêtes SQL manuelles avec f-strings

2. **Validation** :
   - ✅ Valider tous les inputs (Pydantic côté API)
   - ✅ Utiliser les validators Streamlit

3. **Secrets** :
   - ❌ Ne jamais commiter de secrets dans le code
   - ✅ Utiliser variables d'environnement ou `.streamlit/secrets.toml`

### Code quality

1. **Documentation** :
   - Docstrings pour toutes les fonctions publiques
   - Commentaires pour logique complexe
   - README à jour

2. **Types** :
   - Type hints pour toutes les fonctions
   - Utiliser `Optional[T]` pour valeurs nullables
   - Utiliser `Union`, `Literal` quand approprié

3. **Tests** :
   - Coverage minimum : 80%
   - Tests unitaires pour toute nouvelle fonction
   - Tests d'intégration pour endpoints API

4. **Linting** :
   - Exécuter pre-commit avant chaque commit
   - Corriger les warnings pylint
   - Respecter black formatting

### Maintenance

1. **Dépendances** :
   - Mettre à jour régulièrement : `poetry update`
   - Vérifier vulnérabilités : `poetry audit` (via plugin)
   - Tester après mise à jour

2. **Backups** :
   - Backup régulier de `data/recettes.db`
   - Backup des photos `data/photos/`
   - Versionner les migrations (futur Alembic)

3. **Monitoring** :
   - Logs pour erreurs importantes
   - Surveiller performances API
   - Monitorer espace disque (photos)

## Ressources utiles

### Documentation officielle

- [Streamlit](https://docs.streamlit.io/)
- [FastAPI](https://fastapi.tiangolo.com/)
- [SQLAlchemy](https://docs.sqlalchemy.org/)
- [Pydantic](https://docs.pydantic.dev/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)

### Outils de développement

- [Poetry](https://python-poetry.org/)
- [pytest](https://docs.pytest.org/)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [Retrofit](https://square.github.io/retrofit/)

### GitHub Copilot

- Instructions pour Copilot : `.github/copilot-instructions.md`
- Guide complet des conventions et workflows pour AI assistants

## Support

Pour toute question ou problème :

1. Consulter cette documentation
2. Vérifier les issues GitHub existantes
3. Consulter les fichiers TROUBLESHOOTING spécifiques
4. Créer une nouvelle issue avec détails et contexte

---

**Dernière mise à jour** : Décembre 2024
**Version** : 2.3.18+
