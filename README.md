# LibraRecipes 🍽️

LibraRecipes est une application de gestion de recettes de cuisine qui vous permet de créer, organiser et rechercher vos recettes préférées avec photos, ingrédients, étapes détaillées et métadonnées.

## 🏗️ Architecture

LibraRecipes est composé de trois éléments principaux :

- **Backend FastAPI** : API REST qui sert de source de vérité canonique pour les données
- **Interface Web Streamlit** : Application web moderne avec navigation intuitive
- **Application Android Native** : Application mobile pour smartphones et tablettes (Kotlin + Jetpack Compose)

## 📱 Interfaces disponibles

Vous pouvez accéder à vos recettes via :

- **Interface Web** : Application Streamlit moderne avec navigation intuitive
- **Application Android** : Application native pour smartphones et tablettes Android

## ✨ Fonctionnalités principales

- 🔍 **Recherche avancée** : Trouvez vos recettes par nom, ingrédients, catégories ou tags
- 📝 **Gestion complète** : Ajoutez et modifiez vos recettes avec tous les détails
- 📷 **Photos** : Ajoutez plusieurs photos à chaque recette
- 🏷️ **Organisation** : Classez vos recettes par catégories et tags personnalisables
- ⏱️ **Temps de préparation** : Suivez les temps de préparation et de cuisson
- 📚 **Sources** : Gardez une trace des sources (recettes maison, sites web, livres)
- 🔤 **Index** : Parcourez vos recettes par ordre alphabétique ou par ingrédients
- 💡 **Suggestions** : Découvrez quoi cuisiner selon vos envies

## 🚀 Installation et démarrage

### Prérequis

- Python 3.12 ou supérieur
- Poetry (gestionnaire de dépendances Python)

### Installation

```bash
# Cloner le dépôt
git clone https://github.com/apajon/librarecipes-open.git
cd librarecipes-open

# Installer les dépendances avec Poetry
poetry install --no-root

# Initialiser la base de données
PYTHONPATH=. poetry run python scripts/init_db.py

# (Optionnel) Ajouter des données d'exemple
PYTHONPATH=. poetry run python scripts/add_sample_data.py
```

### Lancement de l'interface Web (Streamlit)

```bash
# Méthode 1 : Via le script de lancement
./run.sh

# Méthode 2 : Directement avec Streamlit
PYTHONPATH=. poetry run streamlit run streamlit_app/Home.py
```

L'application s'ouvrira automatiquement dans votre navigateur à l'adresse `http://localhost:8501`.

### Lancement du backend API (optionnel)

Le backend FastAPI est nécessaire si vous utilisez l'application Android ou souhaitez accéder à l'API REST.

```bash
# Démarrer le serveur API
PYTHONPATH=. poetry run uvicorn backend.main:app --host 0.0.0.0 --port 8000

# Accéder à la documentation API
# Ouvrez http://localhost:8000/docs dans votre navigateur
```

### Lancement de l'application Android

Pour utiliser l'application Android native :

1. **Démarrez le backend API** (voir ci-dessus)

2. **Configurez l'application Android** :
   - Consultez le fichier `android_app/README.md` pour les instructions détaillées
   - Consultez `android_app/BACKEND_SETUP.md` pour la configuration de la connexion au backend

3. **Compilez et installez** l'application sur votre appareil Android ou émulateur

## 📖 Utilisation

### Ajouter une recette

1. Cliquez sur "➕ Ajouter une recette" depuis la page d'accueil
2. Remplissez les informations : nom, temps, portions, catégories, tags
3. Ajoutez les ingrédients avec quantités et unités
4. Décrivez les étapes de préparation
5. Ajoutez des photos (optionnel)
6. Enregistrez votre recette

### Rechercher une recette

1. Utilisez la barre de recherche pour trouver par nom
2. Accédez à "🔍 Recherche avancée" pour des critères multiples
3. Parcourez l'index alphabétique ou l'index des ingrédients
4. Découvrez les suggestions "Que cuisiner ?"

### Modifier une recette

1. Ouvrez la fiche détaillée d'une recette
2. Cliquez sur "✏️ Modifier cette recette"
3. Modifiez les informations souhaitées
4. Enregistrez les modifications

## 🗄️ Structure des données

Vos données sont stockées localement dans :
- **Base de données** : `data/recettes.db` (SQLite)
- **Photos** : `data/photos/`

## 🆘 Aide et support

Si vous rencontrez des problèmes :

1. Vérifiez que toutes les dépendances sont installées : `poetry install --no-root`
2. Assurez-vous que la base de données est initialisée : `PYTHONPATH=. python scripts/init_db.py`
3. Consultez le fichier `README_dev.md` pour plus de détails techniques
4. Pour l'application Android, consultez `android_app/TROUBLESHOOTING.md`

## 📄 Licence

Ce projet est distribué sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 🤝 Contribution

Les contributions sont les bienvenues ! Pour plus d'informations sur l'architecture du projet et les conventions de développement, consultez le fichier `README_dev.md`.

## 📜 Historique

**Note sur l'architecture** : LibraRecipes utilisait auparavant une application mobile Kivy/KivyMD qui a été remplacée par une application Android native moderne (Kotlin + Jetpack Compose) pour une meilleure performance et une expérience utilisateur améliorée.
