---
# Optional YAML frontmatter for scoping (uncomment if needed)
# applies-to: ["**/*.py", "**/*.md"]
# excludeAgent: "code-review"  # Exclude from code review agent if needed
---

# LibraRecipes – Instructions pour agents IA

Objectif: aider rapidement à coder dans ce dépôt Streamlit/SQLAlchemy avec une app mobile Kivy optionnelle.

## Principes généraux
- **Modifications minimales**: Ne modifier que le strict nécessaire pour résoudre le problème
- **Tests systématiques**: Toujours tester les modifications avant de commit
- **Validation des builds**: Vérifier que le code compile et passe les linters
- **Pas de secrets**: Ne jamais commiter de secrets, tokens ou credentials
- **Revue de code**: Toujours demander une revue avant de finaliser

## Vue d’ensemble
- Deux interfaces:
  - Desktop Web: Streamlit dans `streamlit_app/` (point d’entrée `streamlit_app/Home.py`). Navigation via `st.Page` et `st.navigation`.
  - Mobile Android: Kivy dans `mobile_app/` (point d’entrée `mobile_app/main.py`), non prioritaire ici.
- Backend: SQLite via SQLAlchemy. Modèles dans `src/model.py`, session DB depuis `src/db.py` (get_db_session()).
- Couche CRUD dans `src/crud/` (ex: `recettes.py`, `recherche.py`, `metadata.py`).
- Données locales: `data/recettes.db` et photos sous `data/photos/`.

## Démarrage & workflows dev
- Installer dépendances (Poetry désactivé en package-mode; dépendances listées dans `[project]` de `pyproject.toml`).
- Init DB: `PYTHONPATH=. python scripts/init_db.py` crée `data/recettes.db` à partir des modèles.
- Lancer l’app Streamlit:
  - Script: `./run.sh` (utilise `PYTHONPATH=.` et démarre `streamlit_app/Home.py`).
  - Ou: `PYTHONPATH=. streamlit run streamlit_app/Home.py`.
  - Tâche VS Code disponible: “Lancer LibraRecipes”.
- Tests rapides manuels dans `tests/` (scripts d’intégration simples utilisant `get_db_session()`). Pas de framework spécifique câblé; si vous ajoutez des tests, utilisez pytest.

## Architecture Streamlit
- Point d’entrée `Home.py`:
  - Configure la page (`config/app.py`), crée les `Page` et la navigation (`config/pages.py`).
  - Sidebar via `utils/sidebar.py`.
- Pages:
  - Fonctions de pages dans `streamlit_app/pages_functions/*`. Exemples:
    - `add_recette.py`: création; compose les formulaires avec `utils/*_manager.py`, valide puis appelle `src.crud.recettes.create_recette`.
    - `recherche_recette.py`: recherches via `src.crud.recherche.rechercher_recettes` et navigate vers le détail avec `utils/navigation.py`.
- Navigation avancée:
  - `utils/navigation.py` stocke l’ID sélectionné dans `st.session_state.selected_recette_id` + `st.query_params.recette_id`, puis `st.switch_page()` avec des objets `st.Page` conservés dans `st.session_state.*_page_obj` (placés par `config/pages.setup_session_state_pages`).

## Modèle & Accès Données
- Modèles SQLAlchemy dans `src/model.py` (Recette, Ingredient, Etape, Photo, Categorie, Tag, Convive, Execution, FeedbackExecution, Source). UUID string comme PK; cascade "all, delete-orphan" sur relations enfants.
- DB path:
  - Desktop: `data/recettes.db` (créé au besoin).
  - Android: via `android.storage.app_storage_path()`.
- Session:
  - `src/db.py` expose `get_db_session()` (context manager). Toujours ouvrir une session localement dans les pages/CRUD.
- CRUD:
  - `src/crud/recettes.py` crée/charge/liste/maj/supprime recettes. Les updates font un clear+recreate des enfants (ingredients/etapes/categories/tags/photos) puis réaffectent `Source` (suppression explicite + flush avant réinsertion).
  - `src/crud/recherche.py` prend en charge les filtres nom/ingrédients/tags/catégories. `IngredientsMode` ANY vs ALL géré par HAVING COUNT(DISTINCT Ingredient.nom).

## Conventions importantes
- Tous les appels DB dans des with get_db_session(): context manager ferme proprement.
- Les IDs recette sont des strings UUID; toujours caster en `str()` quand vous les mettez dans `st.session_state`/`st.query_params`.
- Affichage nom recette uniformisé via `utils/recipe_display.format_recette_display_name()` (inclut source, date d’ajout, dernière exécution). Réutiliser cette fonction pour lister.
- Navigation: utiliser les helpers `navigate_to_recipe_detail/modify/...` pour gérer état + redirection.
- Validation d’entrée pour création/modification via `utils/recipe_validator.py` et préparation de payload via `prepare_recipe_data`.

## Points d’attention
- Streamlit 1.46: API `st.Page`, `st.navigation`, `st.switch_page` utilisées. Garder la compatibilité si vous touchez la config de pages.
- Concurrence SQLite: `connect_args={"check_same_thread": False}` déjà activé. Éviter de partager la même session entre callbacks.
- Photos: chemins stockés en base (`Photo.chemin`), gestion UI en cours; ne déplacez pas les fichiers sans mettre à jour la DB.
- Les tests existants ne seedent pas la DB; exécutez `scripts/add_sample_data.py` si nécessaire pour démo.

## Exemples d’extension
- Ajouter une page:
  1) Créer la fonction de page dans `streamlit_app/pages_functions/ma_page.py`.
  2) L’enregistrer dans `config/pages.get_pages_config()` sous une section.
  3) Si navigation programmatique: exposer `st.Page` et sauvegarder l’objet dans `setup_session_state_pages`.
- Ajouter un filtre de recherche: étendre `src/crud/recherche.rechercher_recettes` et propager l’UI dans `pages_functions/recherche_recette.py`.

## Commandes utiles

### Gestion de la base de données
- Init DB: `PYTHONPATH=. python scripts/init_db.py`
- Ajouter des données de démonstration: `PYTHONPATH=. python scripts/add_sample_data.py`

### Lancement de l'application
- Lancer UI: `PYTHONPATH=. streamlit run streamlit_app/Home.py`
- Ou avec le script: `./run.sh`

### Linting et formatage (OBLIGATOIRE avant commit)
- Formater le code: `poetry run black --line-length=119 .`
- Vérifier style: `poetry run flake8 --max-line-length=119 --ignore=E203,W503 .`
- Vérifier types: `poetry run pyright`
- Linter complet: `poetry run pylint src/ streamlit_app/ --max-line-length=119`
- Tous les hooks pre-commit: `poetry run pre-commit run --all-files`

### Tests
- Exécuter tous les tests: `PYTHONPATH=. poetry run pytest tests/ -v`
- Test spécifique: `PYTHONPATH=. poetry run pytest tests/test_create_recette.py -v`
- Note: Les tests sont simples et n'utilisent pas de fixtures complexes

### Gestion des versions
- Bump version patch: `poetry run bump2version patch`
- Bump version minor: `poetry run bump2version minor`
- Bump version major: `poetry run bump2version major`

### Installation des dépendances
- Installer toutes les dépendances: `poetry install --no-root`
- Installer uniquement prod: `poetry install --no-root --only main`
- Installer avec mobile: `poetry install --no-root --with mobile_app`

Questions ouvertes à clarifier
- Convention d’emplacement définitif des photos (dossier exact) et stratégies de renommage.
- Portée de la Kivy app dans ce dépôt (doit-on tester/packager ici?).
- Données de démonstration officielles (scripts de seed) et jeu de tags/catégories canonique.

## Workflow de développement recommandé

### Avant toute modification
1. Créer une branche depuis `main`: `git checkout -b feature/ma-fonctionnalite`
2. S'assurer que les dépendances sont à jour: `poetry install --no-root`
3. Initialiser la DB si nécessaire: `PYTHONPATH=. python scripts/init_db.py`
4. Lancer l'app pour vérifier l'état initial: `./run.sh`

### Pendant le développement
1. Faire des modifications ciblées et minimales
2. Tester fréquemment: relancer l'app et vérifier les changements
3. Exécuter les linters après chaque changement significatif
4. Commit régulièrement avec des messages clairs: `git commit -m "feat: description"`

### Avant de commit
1. **OBLIGATOIRE**: Formater le code avec black: `poetry run black --line-length=119 .`
2. **OBLIGATOIRE**: Vérifier flake8: `poetry run flake8 --max-line-length=119 --ignore=E203,W503 .`
3. **RECOMMANDÉ**: Vérifier pyright: `poetry run pyright`
4. **RECOMMANDÉ**: Exécuter les tests: `PYTHONPATH=. poetry run pytest tests/ -v`
5. Vérifier les fichiers modifiés: `git status` et `git diff`
6. Stage et commit: `git add .` puis `git commit`

### Avant de push
1. Vérifier que l'app démarre correctement: `./run.sh`
2. Tester les fonctionnalités modifiées manuellement
3. S'assurer qu'aucun secret n'est présent: `git diff origin/main`
4. Push: `git push origin feature/ma-fonctionnalite`

## Règles de sécurité (CRITIQUE)

### Interdictions absolues
- ❌ Ne JAMAIS commiter de mots de passe, tokens, API keys, ou credentials
- ❌ Ne JAMAIS commiter de fichiers contenant des données personnelles sensibles
- ❌ Ne JAMAIS désactiver les validations de sécurité sans justification
- ❌ Ne JAMAIS utiliser `eval()` ou `exec()` sur des entrées utilisateur
- ❌ Ne JAMAIS stocker de secrets dans le code source ou les configs

### Bonnes pratiques de sécurité
- ✅ Utiliser des variables d'environnement pour les secrets
- ✅ Valider toutes les entrées utilisateur
- ✅ Utiliser des requêtes SQL paramétrées (SQLAlchemy le fait automatiquement)
- ✅ Limiter les permissions des fichiers uploadés
- ✅ Vérifier les types et tailles des données avant traitement
- ✅ Logger les erreurs sans exposer d'informations sensibles

### Vulnérabilités à surveiller
- SQL injection: Utiliser toujours les ORM SQLAlchemy, jamais de requêtes raw
- XSS: Streamlit échappe automatiquement les données, ne pas utiliser `unsafe_allow_html` avec des données utilisateur
- Path traversal: Valider les chemins de fichiers avant accès
- Upload de fichiers: Vérifier extensions et types MIME des photos

## Conventions de code

### Style Python
- Ligne max: 119 caractères
- Formatage: black avec `--line-length=119`
- Imports: groupés (stdlib, third-party, local) et triés alphabétiquement
- Docstrings: style Google (voir exemples dans le code existant)
- Type hints: utiliser autant que possible (Python 3.12+)

### Nommage
- Fonctions/variables: `snake_case`
- Classes: `PascalCase`
- Constantes: `UPPER_SNAKE_CASE`
- Fichiers Python: `snake_case.py`
- Modules privés: préfixe `_` (ex: `_internal.py`)

### Structure des commits
- Format: `<type>: <description courte>`
- Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- Exemples:
  - `feat: ajout recherche par tags multiples`
  - `fix: correction affichage photos dans card_recette`
  - `docs: mise à jour README avec nouvelles commandes`
  - `refactor: extraction logique de validation dans validator.py`

## Gestion des erreurs

### Principes
- Toujours utiliser des context managers pour la DB: `with get_db_session() as session:`
- Catcher les exceptions spécifiques, pas `except Exception:` générique
- Logger les erreurs avec contexte suffisant
- Afficher des messages utilisateur clairs dans Streamlit: `st.error()`, `st.warning()`

### Exemples
```python
# BON
try:
    with get_db_session() as session:
        recette = session.query(Recette).filter_by(id=recette_id).first()
        if not recette:
            st.error(f"Recette {recette_id} non trouvée")
            return None
except SQLAlchemyError as e:
    st.error(f"Erreur base de données: {e}")
    return None

# MAUVAIS
try:
    session = get_db_session()
    recette = session.query(Recette).filter_by(id=recette_id).first()
except Exception:  # Trop générique
    pass  # Erreur silencieuse
```

## Patterns courants

### Créer une nouvelle page Streamlit
1. Créer le fichier dans `streamlit_app/pages_functions/ma_nouvelle_page.py`
2. Définir la fonction principale: `def ma_nouvelle_page():`
3. L'enregistrer dans `streamlit_app/config/pages.py` -> `get_pages_config()`
4. Si navigation programmatique nécessaire: ajouter l'objet Page dans `setup_session_state_pages()`

### Ajouter un champ au modèle
1. Modifier la classe dans `src/model.py`
2. Recréer la DB: `rm data/recettes.db && PYTHONPATH=. python scripts/init_db.py`
3. Mettre à jour les CRUD dans `src/crud/recettes.py`
4. Ajouter les champs dans les formulaires (`streamlit_app/utils/*_manager.py`)
5. Tester création et modification de recette

### Ajouter un filtre de recherche
1. Étendre la fonction dans `src/crud/recherche.py` -> `rechercher_recettes()`
2. Ajouter le paramètre dans la signature
3. Construire la requête SQL avec les filtres
4. Mettre à jour l'UI dans `streamlit_app/pages_functions/recherche_recette.py`
5. Tester avec différentes combinaisons de filtres

## Troubleshooting courant

### "ModuleNotFoundError" au lancement
- Solution: Toujours utiliser `PYTHONPATH=.` avant les commandes Python
- Exemple: `PYTHONPATH=. python scripts/init_db.py`

### "database is locked"
- Cause: Plusieurs sessions DB ouvertes simultanément
- Solution: S'assurer d'utiliser le context manager `with get_db_session()`

### Photos ne s'affichent pas
- Vérifier que `data/photos/` existe
- Vérifier les chemins dans la table Photo (relatifs à `data/photos/`)
- Vérifier les permissions des fichiers

### Streamlit "script ran successfully" mais rien ne s'affiche
- Vérifier qu'une fonction de page retourne du contenu
- S'assurer que `st.navigation()` est appelé dans `Home.py`
- Vérifier les logs dans le terminal

### Pre-commit échoue
- Exécuter manuellement: `poetry run black . && poetry run flake8 .`
- Si pyright échoue: vérifier les type hints, corriger ou ajouter `# type: ignore`
- Vérifier les warnings et corriger le code

## Exemples de tâches types

### Tâche: Ajouter un champ "difficulté" aux recettes
1. Modifier `src/model.py`: ajouter `difficulte = Column(String)` dans classe Recette
2. Recréer DB: `rm data/recettes.db && PYTHONPATH=. python scripts/init_db.py`
3. Modifier `src/crud/recettes.py`: ajouter `difficulte` dans `create_recette()` et `update_recette()`
4. Modifier `streamlit_app/utils/metadata_manager.py`: ajouter input difficulté
5. Modifier `streamlit_app/pages_functions/card_recette.py`: afficher difficulté
6. Tester: créer une recette, vérifier affichage
7. Linter: `poetry run black . && poetry run flake8 .`
8. Commit: `git commit -m "feat: ajout champ difficulté aux recettes"`

### Tâche: Corriger un bug d'affichage
1. Identifier la page/composant concerné
2. Reproduire le bug localement: `./run.sh`
3. Localiser le code dans `streamlit_app/pages_functions/`
4. Corriger le bug avec modification minimale
5. Tester la correction: relancer l'app et vérifier
6. Linter: `poetry run black . && poetry run flake8 .`
7. Commit: `git commit -m "fix: correction affichage [description]"`

### Tâche: Améliorer les performances d'une requête
1. Identifier la requête lente dans `src/crud/`
2. Analyser avec des prints/logs pour mesurer le temps
3. Optimiser: ajouter indexes, utiliser joinedload, limiter résultats
4. Tester: vérifier que les résultats sont identiques
5. Mesurer l'amélioration
6. Commit: `git commit -m "perf: optimisation requête [description]"`

## Ressources utiles

### Documentation
- Streamlit: https://docs.streamlit.io/
- SQLAlchemy 2.0: https://docs.sqlalchemy.org/en/20/
- Python type hints: https://docs.python.org/3/library/typing.html

### Fichiers importants à connaître
- `src/model.py`: Tous les modèles SQLAlchemy
- `src/db.py`: Configuration DB et session factory
- `src/crud/recettes.py`: Opérations CRUD principales
- `streamlit_app/Home.py`: Point d'entrée et navigation
- `streamlit_app/config/pages.py`: Configuration des pages
- `streamlit_app/utils/navigation.py`: Helpers de navigation
- `pyproject.toml`: Dépendances et config Poetry
- `.pre-commit-config.yaml`: Hooks de validation
