# LibraRecipes Mobile App

A beautiful, offline Android recipe notebook app built with KivyMD and Python.

## 🎨 Design Features

- **App Name**: LibraRecipes
- **Slogan**: "The recipe notebook you will never lose."
- **Color Palette**:
  - Primary: Teal Blue (#006D77)
  - Secondary: Pastel Peach (#FFE5CC)
  - Dark Curry Red (#91372C)
  - Deep Navy Blue (#090C6B)
  - Accent colors: #FFD447, #4E342E, #8A4699, #A5E079
- **Typography**: Merienda for logo, Nunito for UI

## 🚀 Features

### ✅ Core Recipe Management

- Add new recipes with ingredients and preparation steps
- View detailed recipe information with beautiful Material Design UI
- Edit and delete recipes
- Ingredient management with quantities and units
- Step-by-step preparation instructions

### ✅ Recipe Organization

- Categories and tags for organization
- Recipe statistics and quick overview
- Chronological recipe list

### ✅ Search & Discovery

- Advanced search by name, ingredients, categories, tags
- Flexible ingredient matching (any/all ingredients)
- Real-time search results

### 🔄 Coming Soon

- Photo capture and gallery integration
- Recipe sharing and export
- Offline sync and backup
- Recipe ratings and reviews

## 📱 Installation & Development

### Desktop Development

1. **Install Dependencies**:

```bash
pip install -r mobile_requirements.txt
   ```

2. **Run the App**:

```bash
python run_mobile.py
```

### Android APK Build

1. **Install Buildozer**

```bash
pip install buildozer
```

1. **Initialize Buildozer** (first time only):

```bash
buildozer init
```

2. **Build APK**:

```bash
buildozer android debug
```

3. **Install on Device**:

```bash
buildozer android deploy
```

## 🗄️ Database

The app uses SQLite database with the same schema as the original Streamlit app:

- **Recipes**: Core recipe information
- **Ingredients**: Recipe ingredients with quantities
- **Steps**: Preparation instructions
- **Categories & Tags**: Organization metadata
- **Photos**: Recipe images (file paths)
- **Sources**: Recipe origins

## 📂 Project Structure

```text
mobile_app/
├── main.py                 # Main application entry point
├── screens/                # UI screens
│   ├── home_screen.py      # Dashboard and navigation
│   ├── recipe_list_screen.py # Browse all recipes
│   ├── add_recipe_screen.py  # Add new recipes
│   ├── recipe_detail_screen.py # View recipe details
│   └── search_screen.py    # Search and filters
├── utils/                  # Utilities
│   └── photo_manager.py    # Photo handling
└── assets/                 # Resources
    └── fonts/              # Custom fonts

src/                        # Shared backend (from original app)
├── model.py               # SQLAlchemy models
├── db.py                  # Database configuration
└── crud/                  # Database operations
```

## 🔧 Technical Details

- **Framework**: KivyMD (Material Design for Kivy)
- **Database**: SQLite with SQLAlchemy ORM
- **Platform**: Android (API 21+)
- **Language**: Python 3.8+
- **UI**: Material Design components

## 🎯 Offline First

LibraRecipes is designed to work completely offline:

- Local SQLite database
- Local photo storage
- No internet connection required
- All data stored on device

## 🔐 Permissions

The app requests minimal permissions:

- **Camera**: For recipe photo capture
- **Storage**: For saving photos and database

## 📸 Photo Management

- Capture photos directly from camera
- Select photos from device gallery
- Organize photos by recipe
- Local storage in app directory

## 🎨 UI Components

- **Material Design 3** styling
- **Custom color theming** based on brand colors
- **Responsive layouts** for different screen sizes
- **Smooth animations** and transitions
- **Intuitive navigation** with bottom sheets and cards

## 🚀 Performance

- **Fast startup** with optimized loading
- **Smooth scrolling** with recycled list views
- **Efficient database** queries with proper indexing
- **Memory management** for photo handling

---

*LibraRecipes Mobile - The recipe notebook you will never lose.*
