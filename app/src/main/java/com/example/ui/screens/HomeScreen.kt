package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (String) -> Unit,
    onOpenModule: (String) -> Unit // "drill", "welding", "electrode", "calculator"
) {
    val language by viewModel.appLanguage.collectAsState()
    val historyList by viewModel.calculationHistory.collectAsState()
    var showFeedback by remember { mutableStateOf(false) }

    if (showFeedback) {
        FeedbackDialog(
            moduleName = "Home Dashboard",
            viewModel = viewModel,
            onDismiss = { showFeedback = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFeedback = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("home_feedback_fab")
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Feed,
                    contentDescription = "Feedback"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = viewModel.getString("app_title"),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = viewModel.getString("subtitle"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                        
                        // Icon indicators
                        Icon(
                            imageVector = Icons.Filled.Engineering,
                            contentDescription = "Industrial Settings logo",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Quick search row which suggests the user can navigate to search tap/weld
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Placeholder to trigger navigation */ },
                    enabled = true,
                    readOnly = true,
                    placeholder = { Text(viewModel.getString("search_hint")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab("library") }
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Active AI Assistant Banner Card
            item {
                val geminiGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F2027), // Cyber Blue Grey
                        Color(0xFF1E88E5), // Electric Cobalt Blue
                        Color(0xFF00ACC1)  // Precision Cyan
                    )
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab("chat") }
                        .testTag("dashboard_ai_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(geminiGradient)
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Forum,
                                    contentDescription = "AI active",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.getString("ai_headline") + " " + when (language) {
                                    "TR" -> "aktif"
                                    "DE" -> "aktiv"
                                    else -> "active"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = when (language) {
                                    "TR" -> "Teknik sorunuzu sorun, anında yanıt alın."
                                    "DE" -> "Stellen Sie Ihre Frage, erhalten Sie Antworten."
                                    else -> "Ask engineering questions, get instant calculations."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Arrow right icon",
                            tint = Color.White
                        )
                    }
                }
            }

            // MODULES Section Header
            item {
                Text(
                    text = viewModel.getString("modules"),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Grid of Modules
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuleItemCard(
                            title = viewModel.getString("drill_delik"),
                            subtitle = viewModel.getString("drill_sub"),
                            icon = Icons.Filled.Build,
                            modifier = Modifier.weight(1f),
                            badge = "DIN 76 / ISO",
                            colorTheme = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { onOpenModule("drill") }
                        )
                        ModuleItemCard(
                            title = viewModel.getString("welding_methods"),
                            subtitle = viewModel.getString("welding_sub"),
                            icon = Icons.Filled.Whatshot,
                            modifier = Modifier.weight(1f),
                            badge = "MIG / TIG / MMA",
                            colorTheme = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { onOpenModule("welding") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuleItemCard(
                            title = viewModel.getString("calculations"),
                            subtitle = viewModel.getString("calculations_sub"),
                            icon = Icons.Filled.HistoryEdu,
                            modifier = Modifier.weight(1f),
                            badge = "Q / T / σ",
                            colorTheme = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = { onOpenModule("calculator") }
                        )
                        ModuleItemCard(
                            title = viewModel.getString("welding_codes"),
                            subtitle = viewModel.getString("welding_codes_sub"),
                            icon = Icons.Filled.Shield,
                            modifier = Modifier.weight(1f),
                            badge = "AWS & ISO",
                            colorTheme = MaterialTheme.colorScheme.primaryContainer,
                            onClick = { onOpenModule("electrode") }
                        )
                    }
                }
            }

            // RECENT CALCULATIONS Section Header
            if (historyList.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GEÇMİŞ HESAPLAMALAR / RECENT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Temizle", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                items(historyList.take(3)) { historyItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (historyItem.calculatorType) {
                                    "Torque" -> Icons.Filled.Bolt
                                    "HeatInput" -> Icons.Filled.Whatshot
                                    "Stress" -> Icons.Filled.Anchor
                                    else -> Icons.Filled.SyncAlt
                                },
                                contentDescription = "Calc indicator icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (historyItem.calculatorType) {
                                        "Torque" -> viewModel.getString("torque_calc_title")
                                        "HeatInput" -> viewModel.getString("heat_input_title")
                                        "Stress" -> viewModel.getString("stress_title")
                                        else -> viewModel.getString("unit_converter")
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = historyItem.inputs,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = historyItem.result,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badge: String,
    colorTheme: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() }
            .testTag("module_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = colorTheme
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.size(38.dp),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp
                    ),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
        }
    }
}
