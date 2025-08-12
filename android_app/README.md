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

### Quick Start
```bash
# Use the intelligent build wrapper (recommended)
./build_wrapper.sh

# Or traditional Gradle commands
./gradlew build
```

### Build Environment Requirements
1. Ensure Android SDK is installed
2. Java 17 or higher
3. Internet connectivity for dependency download (first build)

### Troubleshooting Build Issues

If you encounter build errors like:
```
> Task :app:kaptGenerateStubsDebugKotlin FAILED
e: Could not load module <Error module>
```

This is typically a network connectivity issue. Use our troubleshooting tools:

```bash
# Check connectivity and get solutions
./check_connectivity.sh

# Try intelligent build wrapper
./build_wrapper.sh

# For detailed troubleshooting
cat BUILD_TROUBLESHOOTING.md
```

### Build Variants
```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew assembleRelease

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Offline Building
If you're in a restricted network environment:
```bash
# Use offline mode (requires cached dependencies)
./gradlew build --offline

# Or use the wrapper which handles this automatically
./build_wrapper.sh
```

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