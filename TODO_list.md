- [1. LibraRecipes - Android Port TODO List](#1-librarecipes---android-port-todo-list)
  - [1.1. 📱 Phase 1 : Préparation et Architecture](#11--phase-1--préparation-et-architecture)
    - [1.1.1. Configuration du projet](#111-configuration-du-projet)
    - [1.1.2. Scripts de configuration](#112-scripts-de-configuration)
  - [1.2. 🔧 Phase 2 : Adaptation du Code Existant](#12--phase-2--adaptation-du-code-existant)
    - [1.2.1. Base de données](#121-base-de-données)
    - [1.2.2. Gestion des photos](#122-gestion-des-photos)
    - [1.2.3. Configuration Streamlit](#123-configuration-streamlit)
  - [1.3. 🚀 Phase 3 : Développement Android](#13--phase-3--développement-android)
    - [1.3.1. MainActivity et WebView](#131-mainactivity-et-webview)
    - [1.3.2. Bridge Python-Android](#132-bridge-python-android)
    - [1.3.3. Configuration Gradle](#133-configuration-gradle)
  - [1.4. 🧪 Phase 4 : Tests et Optimisation](#14--phase-4--tests-et-optimisation)
    - [1.4.1. Tests fonctionnels](#141-tests-fonctionnels)
    - [1.4.2. Tests de base de données](#142-tests-de-base-de-données)
    - [1.4.3. Optimisation mobile](#143-optimisation-mobile)
  - [1.5. 📦 Phase 5 : Build et Déploiement](#15--phase-5--build-et-déploiement)
    - [1.5.1. Configuration de build](#151-configuration-de-build)
    - [1.5.2. Documentation](#152-documentation)
    - [1.5.3. Tests finaux](#153-tests-finaux)
  - [1.6. 🎯 Phase 6 : Améliorations Futures](#16--phase-6--améliorations-futures)
    - [1.6.1. Fonctionnalités avancées](#161-fonctionnalités-avancées)
    - [1.6.2. Maintenance](#162-maintenance)
  - [1.7. 📋 Notes et Considérations](#17--notes-et-considérations)
    - [1.7.1. Dépendances Python à vérifier](#171-dépendances-python-à-vérifier)
    - [1.7.2. Limitations connues](#172-limitations-connues)
    - [1.7.3. Alternatives à considérer](#173-alternatives-à-considérer)

# 1. LibraRecipes - Android Port TODO List

## 1.1. 📱 Phase 1 : Préparation et Architecture

### 1.1.1. Configuration du projet
- [X] Créer la structure de dossiers Android
  - [X] Créer `/android/` pour le projet Android Studio
  - [X] Créer `/config/` pour les configurations Android
  - [X] Créer `/scripts/` pour les scripts d'automatisation
- [X] Créer le projet Android Studio
  - [X] Initialiser le projet avec Kotlin
  - [X] Configurer Chaquopy dans `build.gradle`
  - [X] Ajouter les permissions nécessaires dans `AndroidManifest.xml`

### 1.1.2. Scripts de configuration
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

## 1.2. 🔧 Phase 2 : Adaptation du Code Existant

### 1.2.1. Base de données
- [X] Modifier `src/db.py`
  - [X] Adapter la fonction `get_database_url()` pour Android
  - [X] Configurer le chemin SQLite pour le stockage Android
  - [X] Tester la création de base avec `os.path.join(os.getcwd(), "my_local_db.db")`
- [X] Adapter les modèles SQLAlchemy
  - [X] Vérifier la compatibilité des modèles avec Android
  - [X] Tester les migrations si nécessaires

### 1.2.2. Gestion des photos
- [ ] Adapter `app/utils/photos_manager.py`
  - [ ] Configurer le stockage des photos sur Android
  - [ ] Modifier les chemins vers le stockage interne Android
  - [ ] Tester l'upload et la gestion des images

### 1.2.3. Configuration Streamlit
- [ ] Créer `.streamlit/config.toml` pour Android
  - [ ] Configuration headless
  - [ ] Désactiver CORS et XSRF
  - [ ] Configuration du port local
- [ ] Adapter les styles CSS pour mobile
  - [ ] Responsive design pour les formulaires
  - [ ] Ajuster les boutons et composants pour mobile

## 1.3. 🚀 Phase 3 : Développement Android

### 1.3.1. MainActivity et WebView
- [ ] Créer `android/app/src/main/java/com/librarecipes/MainActivity.kt`
  - [ ] Initialisation de Chaquopy
  - [ ] Configuration de la WebView
  - [ ] Gestion du serveur Streamlit local
- [ ] Configurer les permissions Android
  - [ ] Permission de stockage
  - [ ] Permission Internet
  - [ ] Permission d'accès aux fichiers

### 1.3.2. Bridge Python-Android
- [ ] Créer `android_bridge.py`
  - [ ] Fonction de démarrage du serveur Streamlit
  - [ ] Initialisation de la base de données Android
  - [ ] Configuration des chemins de stockage
  - [ ] Gestion des erreurs et logs

### 1.3.3. Configuration Gradle
- [ ] Configurer `android/app/build.gradle`
  - [ ] Dépendance Chaquopy
  - [ ] Configuration Python
  - [ ] Requirements.txt pour les packages Python
- [ ] Configurer les assets Python
  - [ ] Copie automatique du code Streamlit
  - [ ] Inclusion des dépendances Python

## 1.4. 🧪 Phase 4 : Tests et Optimisation

### 1.4.1. Tests fonctionnels
- [ ] Tester le démarrage de l'application
  - [ ] Vérifier l'initialisation de Chaquopy
  - [ ] Tester le démarrage du serveur Streamlit
  - [ ] Vérifier le chargement dans la WebView
- [ ] Tester les fonctionnalités principales
  - [ ] Création de recettes
  - [ ] Modification de recettes
  - [ ] Upload et affichage des photos
  - [ ] Recherche et filtres
  - [ ] Navigation entre les pages

### 1.4.2. Tests de base de données
- [ ] Tester la création de la base SQLite
- [ ] Vérifier les opérations CRUD
- [ ] Tester la persistance des données
- [ ] Vérifier la gestion des photos

### 1.4.3. Optimisation mobile
- [ ] Optimiser l'interface pour mobile
  - [ ] Ajuster les tailles de police
  - [ ] Optimiser les formulaires pour tactile
  - [ ] Améliorer la navigation mobile
- [ ] Optimiser les performances
  - [ ] Temps de démarrage du serveur
  - [ ] Réactivité de la WebView
  - [ ] Gestion mémoire

## 1.5. 📦 Phase 5 : Build et Déploiement

### 1.5.1. Configuration de build
- [ ] Configurer le build de release
- [ ] Optimiser la taille de l'APK
- [ ] Configurer ProGuard si nécessaire
- [ ] Tester le build de production

### 1.5.2. Documentation
- [ ] Documenter l'installation Android Studio
- [ ] Documenter la configuration Chaquopy
- [ ] Créer un guide de développement
- [ ] Documenter le processus de build

### 1.5.3. Tests finaux
- [ ] Tests sur différents appareils Android
- [ ] Tests de performance
- [ ] Tests de stockage et persistance
- [ ] Validation de l'expérience utilisateur

## 1.6. 🎯 Phase 6 : Améliorations Futures

### 1.6.1. Fonctionnalités avancées
- [ ] Notifications push (optionnel)
- [ ] Mode hors ligne amélioré
- [ ] Synchronisation cloud (optionnel)
- [ ] Partage de recettes

### 1.6.2. Maintenance
- [ ] Scripts de mise à jour automatique
- [ ] Monitoring des erreurs
- [ ] Système de logs
- [ ] Tests automatisés

---

## 1.7. 📋 Notes et Considérations

### 1.7.1. Dépendances Python à vérifier
- [ ] Streamlit (compatibilité Chaquopy)
- [ ] SQLAlchemy (compatibilité Android)
- [ ] Pillow (gestion des images)
- [ ] Autres dépendances du projet

### 1.7.2. Limitations connues
- [ ] Performance WebView vs app native
- [ ] Taille de l'APK avec Python embedded
- [ ] Temps de démarrage initial

### 1.7.3. Alternatives à considérer
- [ ] Flutter avec base SQLite (backup plan)
- [ ] React Native (backup plan)
- [ ] App native Kotlin (backup plan)

---

**Status général** : 🟡 En préparation
**Priorité** : 📱 Android MVP d'abord
**Timeline estimée** : 2-3 semaines de développement
