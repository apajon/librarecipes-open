# LibraRecipes 🍽️

LibraRecipes est une application de gestion de recettes de cuisine développée avec Streamlit et SQLAlchemy. Elle permet de créer, rechercher, et organiser vos recettes avec photos, ingrédients, étapes et métadonnées.

## 🚀 Fonctionnalités

### ✅ Implémentées

- **Page d'accueil** : Navigation principale de l'application
- **Recherche de recettes** : Recherche avancée par nom, ingrédients, tags et catégories
- **Ajout de recettes complètes** :
  - Informations générales (nom, temps de préparation/cuisson, portions)
  - Gestion des ingrédients avec quantités, unités et alternatives
  - Étapes de préparation ordonnées et modifiables
  - Upload et catégorisation de photos
  - Classification par catégories et tags
  - Sources (maison, URL, livre)
- **Affichage détaillé des recettes** : Visualisation complète avec photos et historique
- **Gestion des photos** : Upload, catégorisation et organisation
- **Gestion des convives** : Suivi des retours lors des réalisations
- **Base de données SQLite** avec modèle relationnel complet

### 📋 Architecture

```text
├── app/                    # Interface Streamlit
│   ├── Home.py            # Page d'accueil
│   ├── assets/            # Images et ressources
│   └── pages/             # Pages de l'application
│       ├── add_recette.py     # ✨ Ajout de recettes (NOUVELLE)
│       ├── card_recette.py    # Détail d'une recette
│       ├── photos_recette.py  # Gestion des photos
│       └── recherche_recette.py # Recherche
├── src/                   # Logique métier
│   ├── model.py          # Modèles SQLAlchemy
│   ├── db.py             # Configuration base de données
│   └── crud/             # Opérations CRUD
├── data/                 # Données et photos
└── tests/                # Tests unitaires
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

## 🔧 Technologies

- **Frontend** : Streamlit + streamlit-tags
- **Backend** : SQLAlchemy + SQLite
- **Images** : Pillow (PIL)
- **Dev Tools** : Poetry, pre-commit, pylint, black, pyright
