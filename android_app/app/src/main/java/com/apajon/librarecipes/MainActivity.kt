package com.apajon.librarecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.apajon.librarecipes.preferences.ThemeMode
import com.apajon.librarecipes.ui.navigation.LibraRecipesNavigation
import com.apajon.librarecipes.ui.theme.LibraRecipesTheme
import com.apajon.librarecipes.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsState()

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LibraRecipesTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraRecipesNavigation(
                        useSystemDarkTheme = themeMode == ThemeMode.SYSTEM,
                        forceDarkTheme = themeMode == ThemeMode.DARK,
                        dynamicColor = dynamicColor,
                        onUseSystemDarkThemeChange = { use ->
                            settingsViewModel.setThemeMode(if (use) ThemeMode.SYSTEM else ThemeMode.LIGHT)
                        },
                        onForceDarkThemeChange = { force ->
                            settingsViewModel.setThemeMode(if (force) ThemeMode.DARK else ThemeMode.LIGHT)
                        },
                        onDynamicColorChange = { settingsViewModel.setDynamicColor(it) }
                    )
                }
            }
        }
    }
}