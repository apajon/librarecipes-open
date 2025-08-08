# LibraRecipes – Instructions pour agents IA

Objectif: aider rapidement à coder dans ce dépôt Streamlit/SQLAlchemy avec une app mobile Kivy optionnelle.

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
- Init DB: `PYTHONPATH=. python scripts/init_db.py`
- Lancer UI: `PYTHONPATH=. streamlit run streamlit_app/Home.py`
- Bump version: `poetry run bump2version patch` (si Poetry configuré)

Questions ouvertes à clarifier
- Convention d’emplacement définitif des photos (dossier exact) et stratégies de renommage.
- Portée de la Kivy app dans ce dépôt (doit-on tester/packager ici?).
- Données de démonstration officielles (scripts de seed) et jeu de tags/catégories canonique.
