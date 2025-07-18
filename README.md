# LibraRecipes 🍽️

LibraRecipes est une application de gestion de recettes de cuisine développée avec Streamlit et SQLAlchemy. Elle permet de créer, rechercher, et organiser vos recettes avec photos, ingrédients, étapes et métadonnées.

**✨ Nouvelle version avec `st.Page` et `st.navigation` !**
Cette application utilise maintenant la nouvelle API de navigation de Streamlit pour une expérience utilisateur améliorée.

## 🚀 Fonctionnalités

### ✅ Implémentées

- **🏠 Page d'accueil** : Vue d'ensemble et navigation principale
- **🔍 Recherche & Exploration** :
  - Recherche avancée par nom, ingrédients, tags et catégories
  - Index alphabétique des recettes
  - Index des ingrédients
  - Suggestions "Que cuisiner ?"
- **➕ Gestion des recettes** :
  - Ajout de recettes complètes avec ingrédients et étapes
  - Affichage détaillé des recettes
  - Modification et gestion des photos (en développement)
- **📊 Organisation** :
  - Classification par catégories et tags
  - Gestion des temps de préparation et cuisson
  - Support des sources (maison, URL, livre)

### 📋 Architecture Moderne

```text
├── app/                           # Interface Streamlit
│   ├── Home.py                   # Point d'entrée avec st.navigation 🆕
│   ├── pages_functions/          # Fonctions des pages 🆕
│   │   ├── home.py              # Page d'accueil
│   │   ├── recherche_recette.py # Recherche avancée
│   │   ├── add_recette.py       # Ajout de recettes
│   │   ├── card_recette.py      # Détail d'une recette
│   │   ├── index_recettes.py    # Index A-Z des recettes
│   │   ├── index_ingredients.py # Index des ingrédients
│   │   ├── que_cuisiner.py      # Suggestions de recettes
│   │   ├── modify_recette.py    # Modification (placeholder)
│   │   └── photos_recette.py    # Gestion photos (placeholder)
│   └── assets/                   # Images et ressources
├── src/                          # Logique métier
│   ├── model.py                 # Modèles SQLAlchemy
│   ├── db.py                    # Configuration base de données
│   └── crud/                    # Opérations CRUD
├── data/                        # Données et photos
└── tests/                       # Tests unitaires
```

## 🛠️ Installation et Lancement

### Prérequis

- Python 3.12+
- Poetry (gestionnaire de dépendances)

### Installation

```bash
# Cloner le projet
git clone <repository-url>
cd librarecipes-open

# Installer les dépendances
poetry install --no-root

# Initialiser la base de données
PYTHONPATH=. python scripts/init_db.py
```

### Lancement

```bash
# Via le script
./run.sh

# Ou directement
PYTHONPATH=. streamlit run app/Home.py
```

## 📝 Utilisation

### Ajouter une nouvelle recette

1. **Accédez à la page d'ajout** : Depuis l'accueil, cliquez sur "➕ Ajouter une recette"

2. **Remplissez les informations générales** :
   - Nom de la recette (obligatoire)
   - Temps de préparation et cuisson
   - Nombre de portions
   - Catégories et tags (suggestions automatiques)
   - Source (maison, URL, ou livre)

3. **Ajoutez les ingrédients** :
   - Nom, quantité, unité
   - Marquez comme indispensable ou optionnel
   - Ajoutez des alternatives si nécessaire

4. **Décrivez les étapes** :
   - Ajoutez chaque étape de préparation
   - Réorganisez l'ordre si nécessaire

5. **Ajoutez des photos** (optionnel) :
   - Upload multiple de photos
   - Catégorisez chaque photo (final, cuisson, ingrédient, etc.)

6. **Enregistrez** : La recette est créée en base avec toutes ses données

### Modifier une recette existante

1. **Accédez à la page de modification** : Depuis la page de détail d'une recette, cliquez sur "✏️ Modifier cette recette"

2. **Modifiez les informations** :
   - Tous les champs sont pré-remplis avec les données actuelles
   - Ajustez le nom, temps, portions selon vos besoins
   - Modifiez les catégories et tags

3. **Gérez les ingrédients** :
   - Les ingrédients existants sont affichés
   - Ajoutez de nouveaux ingrédients
   - Supprimez ceux que vous ne voulez plus

4. **Modifiez les étapes** :
   - Les étapes actuelles sont listées
   - Ajoutez, supprimez ou réorganisez les étapes
   - L'ordre est automatiquement mis à jour

5. **Ajoutez de nouvelles photos** (optionnel) :
   - Les photos existantes restent accessibles via "Gérer les photos existantes"
   - Vous pouvez ajouter de nouvelles photos qui s'ajouteront aux existantes

6. **Enregistrez** : Les modifications sont appliquées immédiatement

### Fonctionnalités avancées

- **Validation** : Vérification des champs obligatoires
- **Gestion d'erreurs** : Messages d'erreur explicites
- **Auto-suggestions** : Catégories et tags existants proposés
- **Réorganisation** : Déplacement des étapes, suppression d'éléments
- **Lien direct** : Accès immédiat à la recette créée

## 🗄️ Modèle de données

- **Recette** : Entité principale avec métadonnées
- **Ingrédients** : Avec quantités, unités et alternatives
- **Étapes** : Ordonnées avec descriptions
- **Photos** : Catégorisées et stockées localement
- **Sources** : Multiples types (maison/URL/livre)
- **Catégories et Tags** : Pour classification
- **Convives et Exécutions** : Suivi des réalisations


## 📱 Développement Android

### Prérequis

- **Android Studio** (version 2023.1 ou supérieure recommandée)
- **SDK Android** (API 34 minimum)
- **Chaquopy** (géré via build.gradle)
- **Java 8+** et **Kotlin**

### Installation Android Studio

1. Télécharger et installer [Android Studio](https://developer.android.com/studio)
2. Ouvrir le dossier `android/` comme projet Android Studio
3. Laisser Android Studio télécharger les dépendances nécessaires (Gradle, SDK, etc.)

### Configuration Chaquopy

- Chaquopy est déjà configuré dans `android/app/build.gradle` (voir le bloc `python { ... }`)
- Les dépendances Python sont listées dans `android/app/requirements.txt`
- Les assets Python sont synchronisés via le script `scripts/sync_to_android.py`

### Build et Signature APK/AAB

#### Build debug
```bash
cd android
./gradlew assembleDebug
# APK généré : android/app/build/outputs/apk/debug/app-debug.apk
```

#### Build release (signé)
```bash
cd android
# Assurez-vous d'avoir un fichier keystore.properties et un keystore valide
./gradlew assembleRelease
# APK généré : android/app/build/outputs/apk/release/app-release.apk
```

#### Script de build automatisé
```bash
./scripts/build-android.sh [debug|release|clean|all]
```

#### ProGuard & Optimisation
- ProGuard est activé pour le build release (`proguard-rules.pro`)
- Les ressources inutilisées sont supprimées (`shrinkResources true`)
- Les architectures supportées sont filtrées (`abiFilters`)

#### Bonnes pratiques
- Toujours synchroniser les assets Python avant un build (`python3 scripts/sync_to_android.py`)
- Tester l’APK sur un vrai appareil Android
- Utiliser le build release pour la publication/distribution

### Dépannage
- Si le build échoue, vérifier les logs Gradle dans Android Studio
- Pour les erreurs Chaquopy, vérifier la version de Python et les dépendances dans `requirements.txt`

---
## 🔧 Technologies

- **Frontend** : Streamlit + streamlit-tags
- **Backend** : SQLAlchemy + SQLite
- **Images** : Pillow (PIL)
- **Dev Tools** : Poetry, pre-commit, pylint, black, pyright
