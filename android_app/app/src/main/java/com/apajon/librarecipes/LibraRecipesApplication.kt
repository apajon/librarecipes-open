package com.apajon.librarecipes

import android.app.Application
import com.apajon.librarecipes.data.local.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LibraRecipesApplication : Application() {
    
    @Inject
    lateinit var databaseInitializer: DatabaseInitializer
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with sample data in background
        CoroutineScope(Dispatchers.IO).launch {
            databaseInitializer.initializeWithSampleData()
        }
    }
}