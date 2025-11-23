# LibraRecipes – Règles pour agents IA

But: vous rendre productif immédiatement sur cette app Streamlit + FastAPI + SQLAlchemy, avec une app Android native (Kotlin/Compose).

## Vue d’ensemble architecture
- UI Streamlit: `streamlit_app/` (entrée `streamlit_app/Home.py`). Navigation avec `st.Page` + `st.navigation` (voir `config/app.py`, `config/pages.py`).
- Backend API: FastAPI dans `backend/` (entrée `backend/main.py`, schémas Pydantic `backend/schemas.py`). Utilisé par les tests d’API.
- Métiers & Données: SQLAlchemy dans `src/` (`model.py`, `db.py`, `crud/`). SQLite en local: `data/recettes.db` (+ photos sous `data/photos/`). Android: chemin via `android.storage.app_storage_path()`.

## Workflows dev essentiels
- Initialiser la base: `PYTHONPATH=. python scripts/init_db.py` (crée `data/recettes.db`).
- Lancer l’UI: `PYTHONPATH=. streamlit run streamlit_app/Home.py` (⚠️ la tâche VS Code “Lancer LibraRecipes” pointe parfois vers `app/Home.py`; utilisez le chemin ci‑dessus si échec).
- Lancer l’API: `PYTHONPATH=. uvicorn backend.main:app --reload` (API versionnée, CORS ouvert pour dev).
- Tests: `PYTHONPATH=. pytest -q` (tests CRUD + tests d’API via `fastapi.testclient`).

## Navigation & état Streamlit
- Créez les pages via `st.Page` dans `config/pages.py` et sauvegardez les objets dans `st.session_state` avec `setup_session_state_pages`.
- Pour changer de page, utilisez `streamlit_app/utils/navigation.py`:
  - `navigate_to_recipe_detail/modify/photos(recette_id)` définit `st.session_state.selected_recette_id` et `st.query_params.recette_id`, puis `st.switch_page(...)`.
- La sidebar est gérée par `utils/sidebar.py` et le sélecteur par `utils/recipe_selector.py`.

## Modèle & CRUD: conventions clés
- PK en UUID string; castez toujours en `str` avant d’écrire dans `session_state`/`query_params`.
- Ouvrez une session DB locale avec `with get_db_session():` (ne partagez pas la session entre callbacks Streamlit).
- `crud/recettes.py`:
  - `create_recette(session, data)` attend `etapes: list[str]` (l’ordre est calculé); idem pour `update_recette`.
  - `update_recette` remplace entièrement enfants si la clé est présente et non `None` (ingredients/etapes/categories/tags/photos). Omettre la clé pour conserver; passer `[]` pour vider.
  - Pour `Source`, l’existant est supprimé puis recréé (flush requis avant insert).
- Recherche (`crud/recherche.py`): `IngredientsMode` ANY/ALL; ALL utilise `HAVING COUNT(DISTINCT Ingredient.nom)` pour exiger tous les ingrédients.

## UI: réutiliser les helpers
- Nom affiché uniforme: `utils/recipe_display.format_recette_display_name(recette)` (type de source via `utils/constants.SOURCE_TYPES`, date d’ajout, dernière exécution).
- Validation/Préparation: `utils/recipe_validator.py` (validez avant création). Note: fournissez des étapes en `list[str]` côté CRUD/API; évitez des dicts `{description, ordre}` côté payload brut.

## Points d’attention (pièges)
- Streamlit ≥ 1.46: APIs `st.Page`, `st.navigation`, `st.switch_page` utilisées.
- SQLite: `check_same_thread=False` actif; ouvrez/fermez vos sessions dans le scope du callback.
- Photos: le modèle `Photo` n’a pas de champ `description` (les schémas API en définissent un: il est ignoré actuellement).
- API Source: `SourceCreate.valeur` est optionnel et peut ne pas se refléter en base; pour persister URL/livre, le modèle attend `url`/`book_*` (gap connu).

## Ajouter une page ou une feature
- Page: créez `streamlit_app/pages_functions/ma_page.py`, référencez-la dans `config/pages.get_pages_config()`, et exposez un `st.Page` si navigation directe.
- Filtre de recherche: étendez `crud/recherche.rechercher_recettes()` puis l’UI dans `pages_functions/recherche_recette.py`.

Feedback demandé
- Indiquez si vous souhaitez standardiser l’API Source (`valeur` vs `url/book_*`) et si la tâche VS Code doit être corrigée vers `streamlit_app/Home.py`.
