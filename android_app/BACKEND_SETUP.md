# Backend Setup for Android App

The Android app requires the LibraRecipes backend server to be running to display recipes.

## Quick Setup

1. **Navigate to the project root directory**:
   ```bash
   cd /path/to/librarecipes-open
   ```

2. **Install Python dependencies** (if not already done):
   ```bash
   pip install -r requirements.txt
   pip install fastapi uvicorn
   ```

3. **Initialize the database** (if not already done):
   ```bash
   mkdir -p data
   PYTHONPATH=. python scripts/init_db.py
   ```

4. **Add sample data** (optional):
   ```bash
   PYTHONPATH=. python scripts/add_sample_data.py
   ```

5. **Start the backend server**:
   ```bash
   PYTHONPATH=. uvicorn backend.main:app --host 0.0.0.0 --port 8000
   ```

6. **Verify the server is running**:
   Open http://localhost:8000 in your browser. You should see:
   ```json
   {"message": "LibraRecipes API", "version": "2.3.18"}
   ```

7. **Test recipe endpoint**:
   Visit http://localhost:8000/recipes to see the list of recipes.

## Troubleshooting

- **Error "Erreur de connexion au serveur"** in Android app:
  - Make sure the backend server is running (step 5 above)
  - Check that the server is accessible at http://localhost:8000
  
- **"Aucune recette trouvée"** after successful connection:
  - Run the sample data script (step 4 above)
  - Check http://localhost:8000/recipes returns recipes

- **Android emulator can't connect**:
  - The Android app is configured to use `http://10.0.2.2:8000/` which maps to the host's localhost:8000
  - Ensure the backend server is started with `--host 0.0.0.0` (not just localhost)

## Network Configuration

The Android app is configured in `NetworkModule.kt` to connect to:
- **Android Emulator**: `http://10.0.2.2:8000/` (maps to host localhost:8000)
- **Real Device**: Update to `http://YOUR_COMPUTER_IP:8000/` in the code

Make sure the backend server is always running when testing the Android app!