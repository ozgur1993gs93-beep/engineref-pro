package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDark by mainViewModel.isDarkTheme.collectAsState()
            val language by mainViewModel.appLanguage.collectAsState()
            val isInitialized by mainViewModel.isInitialized.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isInitialized) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "EngineRef Pro\nInitializing SQLite Technical Database...",
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        AppMainNavigationContainer(mainViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppMainNavigationContainer(viewModel: MainViewModel) {
    var activeTab by remember { mutableStateOf("home") } // "home", "library", "calculator", "chat", "settings"
    var librarySubTab by remember { mutableStateOf("drill") } // "drill", "welding", "electrode"

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_bottom_navigation_bar")
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = viewModel.getString("nav_home")) },
                    label = { Text(viewModel.getString("nav_home"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_item_home")
                )
                
                // Tab 2: Library
                NavigationBarItem(
                    selected = activeTab == "library",
                    onClick = { activeTab = "library" },
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = viewModel.getString("nav_library")) },
                    label = { Text(viewModel.getString("nav_library"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_item_library")
                )

                // Tab 3: Calculator
                NavigationBarItem(
                    selected = activeTab == "calculator",
                    onClick = { activeTab = "calculator" },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = viewModel.getString("nav_calc")) },
                    label = { Text(viewModel.getString("nav_calc"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_item_calc")
                )

                // Tab 4: AI Chat
                NavigationBarItem(
                    selected = activeTab == "chat",
                    onClick = { activeTab = "chat" },
                    icon = { Icon(Icons.Filled.TipsAndUpdates, contentDescription = viewModel.getString("nav_ai")) },
                    label = { Text(viewModel.getString("nav_ai"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_item_chat")
                )

                // Tab 5: Settings
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { activeTab = "settings" },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = viewModel.getString("nav_settings")) },
                    label = { Text(viewModel.getString("nav_settings"), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { tab -> activeTab = tab },
                    onOpenModule = { module ->
                        if (module == "calculator") {
                            activeTab = "calculator"
                        } else {
                            activeTab = "library"
                            librarySubTab = module
                        }
                    }
                )
                "library" -> {
                    // Inside Library, support modular sub viewports
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = when (librarySubTab) {
                                "drill" -> 0
                                "welding" -> 1
                                else -> 2
                            },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Tab(
                                selected = librarySubTab == "drill",
                                onClick = { librarySubTab = "drill" },
                                text = { Text(viewModel.getString("drill_delik"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("library_sub_tab_drill")
                            )
                            Tab(
                                selected = librarySubTab == "welding",
                                onClick = { librarySubTab = "welding" },
                                text = { Text(viewModel.getString("welding_compare"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("library_sub_tab_weld")
                            )
                            Tab(
                                selected = librarySubTab == "electrode",
                                onClick = { librarySubTab = "electrode" },
                                text = { Text(viewModel.getString("electrode_db"), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("library_sub_tab_electrode")
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            when (librarySubTab) {
                                "drill" -> TapDrillScreen(viewModel = viewModel)
                                "welding" -> WeldingScreen(viewModel = viewModel, initialTab = "compare")
                                "electrode" -> WeldingScreen(viewModel = viewModel, initialTab = "electrodes")
                            }
                        }
                    }
                }
                "calculator" -> CalculatorScreen(viewModel = viewModel)
                "chat" -> ChatScreen(viewModel = viewModel)
                "settings" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
