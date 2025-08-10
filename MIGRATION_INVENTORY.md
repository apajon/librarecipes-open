# MIGRATION_INVENTORY.md

## 1. Objectif global
Migrer une application Python (Streamlit + SQLAlchemy) vers une application Android native (Kotlin ou équivalent recommandé par l’agent), tout en conservant les fonctionnalités métier et en assurant une intégration backend/front sécurisée.

---

## 2. Contexte actuel
- **Backend** : Python 3.12, SQLAlchemy ORM
- **Frontend actuel** : Streamlit
- **Gestion des dépendances** : Passage à Poetry (`pyproject.toml`, `poetry.lock`)
- **CI/CD** : GitHub Actions (tests, lint, bumpversion)
- **Repo Git** : branch `feat/android-flutter-setup`
- **Tests** : Pytest
- **Versioning** : bump2version

---

## 3. Tâches de migration

### 3.1 Architecture cible
- Décider du framework Android :
  - Kotlin natif (Jetpack Compose)
  - Ou Flutter (Dart) si plus adapté (à discuter avec l’agent)
- Définir API backend → REST ou GraphQL
- Gérer l’authentification (JWT, OAuth2, etc.)

### 3.2 Backend (Python)
- Extraire la logique métier de Streamlit → API REST avec FastAPI ou Flask
- Adapter SQLAlchemy au nouveau contexte
- Créer endpoints pour toutes les actions utilisateur
- Ajouter tests unitaires + tests d’intégration backend

### 3.3 Frontend mobile (Android)
- Implémenter écrans natifs Kotlin / Jetpack Compose
- Consommer API backend
- Gestion d’état et navigation
- Support offline si nécessaire

### 3.4 CI/CD
- Création de pipelines GitHub Actions :
  - Tests backend avec Poetry
  - Lint backend
  - Build Android app
  - Tests instrumentés Android
  - bump2version automatique
- Push sur Play Store (optionnel, à configurer)

---

## 4. Contraintes & règles
- **Branche** : créer `feature/android-migration` à partir de la branche indiquée par l’utilisateur
- **Commits** : petits, fréquents, explicites
- **PRs** : automatiques avec résumé clair
- **Poetry** pour tout Python
- **Tests** obligatoires avant merge
- **Bump de version** à chaque release

---

## 5. Points d’attention
- Cohérence modèle de données (ORM ↔ API ↔ App mobile)
- Performance backend pour mobile
- Sécurité API (CORS, auth)
- UX mobile adaptée (navigation, responsive)
- Gestion erreurs réseau côté app

---

## 6. Étapes recommandées
1. **Setup** : créer branche + installer Poetry + setup Actions
2. **Refactor backend** pour exposer API
3. **Dev mobile** (UI + intégration API)
4. **Tests & QA**
5. **Release** sur branche cible + tag version
