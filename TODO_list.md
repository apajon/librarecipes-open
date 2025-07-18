# LibraRecipes - Android Port TODO List

- [1. 📱 Phase 1 : Préparation et Architecture](#1--phase-1--préparation-et-architecture)
  - [1.1. Configuration du projet](#11-configuration-du-projet)
  - [1.2. Scripts de configuration](#12-scripts-de-configuration)
- [2. 🔧 Phase 2 : Adaptation du Code Existant](#2--phase-2--adaptation-du-code-existant)
  - [2.1. Base de données](#21-base-de-données)
  - [2.2. Gestion des photos](#22-gestion-des-photos)
  - [2.3. Configuration Streamlit](#23-configuration-streamlit)
- [3. 🚀 Phase 3 : Développement Android](#3--phase-3--développement-android)
  - [3.1. MainActivity et WebView](#31-mainactivity-et-webview)
  - [3.2. Bridge Python-Android](#32-bridge-python-android)
  - [3.3. Configuration Gradle](#33-configuration-gradle)
- [4. 🧪 Phase 4 : Tests et Optimisation](#4--phase-4--tests-et-optimisation)
  - [4.1. Tests fonctionnels](#41-tests-fonctionnels)
  - [4.2. Tests de base de données](#42-tests-de-base-de-données)
  - [4.3. Optimisation mobile](#43-optimisation-mobile)
- [5. 📦 Phase 5 : Build et Déploiement](#5--phase-5--build-et-déploiement)
  - [5.1. Configuration de build](#51-configuration-de-build)
  - [5.2. Documentation](#52-documentation)
  - [5.3. Tests finaux](#53-tests-finaux)
- [6. 🎯 Phase 6 : Améliorations Futures](#6--phase-6--améliorations-futures)
  - [6.1. Fonctionnalités avancées](#61-fonctionnalités-avancées)
  - [6.2. Maintenance](#62-maintenance)
- [7. 📋 Notes et Considérations](#7--notes-et-considérations)
  - [7.1. Dépendances Python à vérifier](#71-dépendances-python-à-vérifier)
  - [7.2. Limitations connues](#72-limitations-connues)
  - [7.3. Alternatives à considérer](#73-alternatives-à-considérer)

## 1. 📱 Phase 1 : Préparation et Architecture

### 1.1. Configuration du projet
- [X] Créer la structure de dossiers Android
  - [X] Créer `/android/` pour le projet Android Studio
  - [X] Créer `/config/` pour les configurations Android
  - [X] Créer `/scripts/` pour les scripts d'automatisation
- [X] Créer le projet Android Studio
  - [X] Initialiser le projet avec Kotlin
  - [X] Configurer Chaquopy dans `build.gradle`
  - [X] Ajouter les permissions nécessaires dans `AndroidManifest.xml`

### 1.2. Scripts de configuration
- [X] Créer `scripts/android_setup.py`
  - [X] Fonction de copie du code Streamlit vers Android
  - [X] Fonction de copie de la logique métier
  - [X] Création du bridge Android-Python
- [X] Créer `scripts/sync_to_android.py`
  - [X] Synchronisation automatique du code
  - [X] Mode watch pour le développement
- [X] Créer `config/android_config.py`
  - [X] Configuration des chemins de stockage Android
  - [X] Gestion de la base de données SQLite

## 2. 🔧 Phase 2 : Adaptation du Code Existant

### 2.1. Base de données
- [X] Modifier `src/db.py`
  - [X] Adapter la fonction `get_database_url()` pour Android
  - [X] Configurer le chemin SQLite pour le stockage Android
  - [X] Tester la création de base avec `os.path.join(os.getcwd(), "my_local_db.db")`
- [X] Adapter les modèles SQLAlchemy
  - [X] Vérifier la compatibilité des modèles avec Android
  - [X] Tester les migrations si nécessaires

### 2.2. Gestion des photos
- [X] Adapter `app/utils/photos_manager.py`
  - [X] Configurer le stockage des photos sur Android
  - [X] Modifier les chemins vers le stockage interne Android
  - [X] Tester l'upload et la gestion des images

### 2.3. Configuration Streamlit
- [X] Créer `.streamlit/config.toml` pour Android
  - [X] Configuration headless
  - [X] Désactiver CORS et XSRF
  - [X] Configuration du port local
- [X] Adapter les styles CSS pour mobile
  - [X] Responsive design pour les formulaires
  - [X] Ajuster les boutons et composants pour mobile

## 3. 🚀 Phase 3 : Développement Android

### 3.1. MainActivity et WebView
- [X] Créer `android/app/src/main/java/com/librarecipes/MainActivity.kt`
  - [X] Initialisation de Chaquopy
  - [X] Configuration de la WebView
  - [X] Gestion du serveur Streamlit local
  - [X] Écran de chargement avec feedback utilisateur
  - [X] Gestion d'erreur améliorée avec retry
  - [X] Optimisations WebView pour mobile
- [X] Configurer les permissions Android
  - [X] Permission de stockage
  - [X] Permission Internet
  - [X] Permission d'accès aux fichiers
- [X] Améliorer l'interface utilisateur
  - [X] Layout avec écran de chargement
  - [X] Gestion des états (loading, error, success)
  - [X] Thème personnalisé LibraRecipes
  - [X] Configuration réseau sécurisée

### 3.2. Bridge Python-Android

- [X] Créer `android_bridge.py`
  - [X] Fonction de démarrage du serveur Streamlit
  - [X] Initialisation de la base de données Android
  - [X] Configuration des chemins de stockage
  - [X] Gestion des erreurs et logs
  - [X] Interface singleton pour la gestion du serveur
  - [X] Configuration Streamlit optimisée pour Android
  - [X] Threading pour serveur non-bloquant
- [X] Intégration avec Chaquopy
  - [X] Point d'entrée Python accessible depuis Kotlin
  - [X] Configuration du module Python (__init__.py)
  - [X] Tests de validation du bridge
- [X] Configuration Streamlit pour Android
  - [X] Fichier config.toml adapté mobile
  - [X] Désactivation CORS et XSRF
  - [X] Optimisations performance mobile

### 3.3. Configuration Gradle
- [X] Configurer `android/app/build.gradle`
  - [X] Optimiser les build types (debug/release)
  - [X] Configuration ProGuard pour production
  - [X] Gestion des signatures de release
  - [X] Mise à jour des dépendances AndroidX
  - [X] Configuration du packaging et lint
- [X] Configurer les assets Python
  - [X] Inclusion automatique du code Streamlit
  - [X] Configuration des requirements.txt
  - [X] Scripts de synchronisation automatique
- [X] Créer les outils de build
  - [X] Scripts Gradle personnalisés
  - [X] Script de build automatisé
  - [X] Configuration .gitignore Android
  - [X] Support pour signature de release

## 4. 🧪 Phase 4 : Tests et Optimisation

### 4.1. Tests fonctionnels
- [X] Tester le démarrage de l'application
  - [X] Vérifier l'initialisation de Chaquopy
  - [X] Tester le démarrage du serveur Streamlit
  - [X] Vérifier le chargement dans la WebView
- [X] Tester les fonctionnalités principales
  - [X] Création de recettes
  - [X] Modification de recettes
  - [X] Upload et affichage des photos
  - [X] Recherche et filtres
  - [X] Navigation entre les pages
- [X] Tests d'intégration WebView-Streamlit
  - [X] Configuration Streamlit pour Android
  - [X] Styles CSS mobiles optimisés
  - [X] Composants UI adaptés
  - [X] Persistance des données Android
  - [X] Performance et optimisation mémoire

### 4.2. Tests de base de données
- [X] Tester la création de la base SQLite
- [X] Vérifier les opérations CRUD
- [X] Tester la persistance des données
- [X] Vérifier la gestion des photos

### 4.3. Optimisation mobile
- [X] Optimiser l'interface pour mobile
  - [X] Ajuster les tailles de police
  - [X] Optimiser les formulaires pour tactile
  - [X] Améliorer la navigation mobile
- [X] Optimiser les performances
  - [X] Temps de démarrage du serveur
  - [X] Réactivité de la WebView
  - [X] Gestion mémoire

## 5. 📦 Phase 5 : Build et Déploiement

### 5.1. Configuration de build
- [X] Configurer le build de release
- [X] Optimiser la taille de l'APK
- [X] Configurer ProGuard si nécessaire
- [X] Tester le build de production

### 5.2. Documentation
- [X] Documenter l'installation Android Studio
- [X] Documenter la configuration Chaquopy
- [X] Créer un guide de développement
- [X] Documenter le processus de build

### 5.3. Tests finaux
- [ ] Tests sur différents appareils Android
- [ ] Tests de performance
- [ ] Tests de stockage et persistance
- [ ] Validation de l'expérience utilisateur

## 6. 🎯 Phase 6 : Améliorations Futures

### 6.1. Fonctionnalités avancées
- [ ] Notifications push (optionnel)
- [ ] Mode hors ligne amélioré
- [ ] Synchronisation cloud (optionnel)
- [ ] Partage de recettes

### 6.2. Maintenance
- [ ] Scripts de mise à jour automatique
- [ ] Monitoring des erreurs
- [ ] Système de logs
- [ ] Tests automatisés

---

## 7. 📋 Notes et Considérations

### 7.1. Dépendances Python à vérifier
- [ ] Streamlit (compatibilité Chaquopy)
- [ ] SQLAlchemy (compatibilité Android)
- [ ] Pillow (gestion des images)
- [ ] Autres dépendances du projet

### 7.2. Limitations connues
- [ ] Performance WebView vs app native
- [ ] Taille de l'APK avec Python embedded
- [ ] Temps de démarrage initial

### 7.3. Alternatives à considérer
- [ ] Flutter avec base SQLite (backup plan)
- [ ] React Native (backup plan)
- [ ] App native Kotlin (backup plan)

---

**Status général** : 🟡 En préparation
**Priorité** : 📱 Android MVP d'abord
**Timeline estimée** : 2-3 semaines de développement
