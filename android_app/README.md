# LibraRecipes Android App

This is the native Android implementation of LibraRecipes built with Kotlin and Jetpack Compose.

## Architecture

- **UI**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **State Management**: StateFlow + Compose State
- **Navigation**: Navigation Compose

## Features

### Implemented
- Home screen with navigation
- Recipe list with pull-to-refresh
- Modern Material 3 design
- API integration with FastAPI backend
- Dependency injection setup
- Error handling and loading states

### To Be Implemented
- Recipe detail view
- Recipe creation/editing
- Advanced search functionality
- Photo management
- Offline support
- Recipe sharing

## API Integration

The app connects to the FastAPI backend at:
- Emulator: `http://10.0.2.2:8000/`
- Real device: `http://localhost:8000/` (when on same network)

## Building

1. Ensure Android SDK is installed
2. Open in Android Studio
3. Build and run on device/emulator

## Project Structure

```
app/src/main/java/com/apajon/librarecipes/
├── data/
│   ├── api/           # API service definitions
│   ├── model/         # Data models
│   └── repository/    # Repository layer
├── di/                # Dependency injection modules
├── ui/
│   ├── components/    # Reusable UI components
│   ├── navigation/    # Navigation setup
│   ├── screens/       # Screen composables
│   └── theme/         # Material 3 theming
├── viewmodel/         # ViewModels
├── LibraRecipesApplication.kt
└── MainActivity.kt
```