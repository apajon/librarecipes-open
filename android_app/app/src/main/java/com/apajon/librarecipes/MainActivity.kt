package com.apajon.librarecipes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.apajon.librarecipes.ui.theme.LibraRecipesTheme
import com.apajon.librarecipes.ui.navigation.LibraRecipesNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var useSystemDarkTheme by rememberSaveable { mutableStateOf(true) }
            var forceDarkTheme by rememberSaveable { mutableStateOf(false) }
            var dynamicColor by rememberSaveable { mutableStateOf(true) }

            val darkTheme = if (useSystemDarkTheme) null else forceDarkTheme

            LibraRecipesTheme(
                darkTheme = darkTheme
                    ?: androidx.compose.foundation.isSystemInDarkTheme(),
                dynamicColor = dynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraRecipesNavigation(
                        useSystemDarkTheme = useSystemDarkTheme,
                        forceDarkTheme = forceDarkTheme,
                        dynamicColor = dynamicColor,
                        onUseSystemDarkThemeChange = { useSystemDarkTheme = it },
                        onForceDarkThemeChange = { forceDarkTheme = it },
                        onDynamicColorChange = { dynamicColor = it }
                    )
                }
            }
        }
    }
}