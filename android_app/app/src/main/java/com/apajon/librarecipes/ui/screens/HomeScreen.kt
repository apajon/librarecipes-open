package com.apajon.librarecipes.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.apajon.librarecipes.R
import com.apajon.librarecipes.ui.components.ActionCard
import com.apajon.librarecipes.ui.icon.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    useSystemDarkTheme: Boolean = true,
    forceDarkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    onUseSystemDarkThemeChange: (Boolean) -> Unit = {},
    onForceDarkThemeChange: (Boolean) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit = {}
) {
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.home_theme_settings_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            // Primary Add Recipe FAB only
            FloatingActionButton(
                onClick = { 
                    navController.navigate("create_new_recipe")
                },
                modifier = Modifier.size(64.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = stringResource(R.string.home_fab_add_recipe_cd),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu_book),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.home_hero_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_hero_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_section_main_actions),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.home_action_view_recipes_title),
                    description = stringResource(R.string.home_action_view_recipes_desc),
                    icon = AppIcons.List,
                    onClick = { navController.navigate("recipes") }
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.home_action_add_recipe_title),
                    description = stringResource(R.string.home_action_add_recipe_desc),
                    icon = AppIcons.Add,
                    onClick = { navController.navigate("create_recipe") }
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.home_action_search_title),
                    description = stringResource(R.string.home_action_search_desc),
                    icon = Icons.Default.Search,
                    onClick = { navController.navigate("search") }
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.home_action_suggestions_title),
                    description = stringResource(R.string.home_action_suggestions_desc),
                    icon = Icons.Default.Star,
                    onClick = { navController.navigate("que_choisir") }
                )
            }

            item {
                ActionCard(
                    title = stringResource(R.string.home_action_import_title),
                    description = stringResource(R.string.home_action_import_desc),
                    icon = Icons.Default.ContentPaste,
                    onClick = { navController.navigate("import_recipe") }
                )
            }
        }
    }

    if (showThemeDialog) {
        ThemeSettingsDialog(
            useSystemDarkTheme = useSystemDarkTheme,
            forceDarkTheme = forceDarkTheme,
            dynamicColor = dynamicColor,
            onUseSystemDarkThemeChange = onUseSystemDarkThemeChange,
            onForceDarkThemeChange = onForceDarkThemeChange,
            onDynamicColorChange = onDynamicColorChange,
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun ThemeSettingsDialog(
    useSystemDarkTheme: Boolean,
    forceDarkTheme: Boolean,
    dynamicColor: Boolean,
    onUseSystemDarkThemeChange: (Boolean) -> Unit,
    onForceDarkThemeChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(R.string.theme_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Dark theme section
                Text(
                    text = stringResource(R.string.theme_dark_mode),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.theme_follow_system),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = useSystemDarkTheme,
                        onCheckedChange = onUseSystemDarkThemeChange
                    )
                }
                if (!useSystemDarkTheme) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.theme_dark_mode),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = forceDarkTheme,
                            onCheckedChange = onForceDarkThemeChange
                        )
                    }
                }

                HorizontalDivider()

                // Dynamic colors section
                Text(
                    text = stringResource(R.string.theme_colors),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.theme_dynamic_colors),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                                stringResource(R.string.theme_dynamic_colors_desc)
                            else
                                stringResource(R.string.theme_dynamic_colors_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColor,
                        onCheckedChange = onDynamicColorChange,
                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
