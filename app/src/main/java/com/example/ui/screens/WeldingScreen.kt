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
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ElectrodeEntity
import com.example.data.WeldingMethodEntity
import com.example.viewmodel.MainViewModel

@Composable
fun WeldingScreen(
    viewModel: MainViewModel,
    initialTab: String = "compare", // "compare" or "electrode"
    modifier: Modifier = Modifier
) {
    val weldingMethods by viewModel.weldingMethods.collectAsState()
    val electrodes by viewModel.electrodes.collectAsState()
    val electrodeQuery by viewModel.electrodeSearchQuery.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    var activeSubTab by remember { mutableStateOf(initialTab) } // "compare" or "electrodes"
    var selectedMethod by remember { mutableStateOf<WeldingMethodEntity?>(null) }
    var selectedElectrode by remember { mutableStateOf<ElectrodeEntity?>(null) }
    var showFeedback by remember { mutableStateOf(false) }

    // Filters for compare table
    var selectedMaterialFilter by remember { mutableStateOf("All") } // "All", "Steel", "Stainless", "Aluminum"
    var selectedThicknessFilter by remember { mutableStateOf("All") } // "All", "Thin", "Thick"

    if (showFeedback) {
        FeedbackDialog(
            moduleName = "Welding Module (${activeSubTab.uppercase()})",
            viewModel = viewModel,
            onDismiss = { showFeedback = false }
        )
    }

    if (selectedMethod != null) {
        WeldingMethodDetailDialog(
            method = selectedMethod!!,
            viewModel = viewModel,
            onDismiss = { selectedMethod = null }
        )
    }

    if (selectedElectrode != null) {
        ElectrodeDetailDialog(
            electrode = selectedElectrode!!,
            viewModel = viewModel,
            onDismiss = { selectedElectrode = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper banner matching welding theme
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFA24B1B) // Brand Rust Brown Color
            ),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeSubTab == "compare") viewModel.getString("welding_compare") else viewModel.getString("electrode_db"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showFeedback = true },
                        modifier = Modifier.testTag("welding_feedback_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Feed,
                            contentDescription = "Feedback form",
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = if (activeSubTab == "compare") viewModel.getString("weld_sub") else viewModel.getString("electrode_sub"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Sub Tab row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activeSubTab = "compare" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSubTab == "compare") Color.White else Color.White.copy(alpha = 0.15f),
                            contentColor = if (activeSubTab == "compare") Color(0xFFA24B1B) else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sub_tab_compare"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (language == "TR") "Yöntem Kıyasla" else "Compare Methods",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { activeSubTab = "electrodes" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activeSubTab == "electrodes") Color.White else Color.White.copy(alpha = 0.15f),
                            contentColor = if (activeSubTab == "electrodes") Color(0xFFA24B1B) else Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sub_tab_electrodes"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (language == "TR") "Elektrot Kataloğu" else "Electrode Catalog",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // CONTENT REGION
        if (activeSubTab == "compare") {
            // VIEW A: Welding Methods Comparison with material & thickness filters
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MATERIAL FILTERS matching Screen 4
                Column {
                    Text(
                        text = viewModel.getString("material_filter"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Steel", "Stainless", "Aluminum").forEach { mat ->
                            val label = when (mat) {
                                "Steel" -> viewModel.getString("steel")
                                "Stainless" -> viewModel.getString("stainless")
                                "Aluminum" -> viewModel.getString("aluminum")
                                else -> viewModel.getString("all")
                            }
                            FilterChip(
                                selected = selectedMaterialFilter == mat,
                                onClick = { selectedMaterialFilter = mat },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFA24B1B),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("weld_mat_chip_$mat")
                            )
                        }
                    }
                }

                // DATA LIST
                val filteredMethods = weldingMethods.filter { method ->
                    val matchesMaterial = when (selectedMaterialFilter) {
                        "Steel" -> method.recommendedMaterialsEn.contains("steel", ignoreCase = true) || method.recommendedMaterialsTr.contains("çelik", ignoreCase = true)
                        "Stainless" -> method.recommendedMaterialsEn.contains("stainless", ignoreCase = true) || method.recommendedMaterialsTr.contains("paslanmaz", ignoreCase = true)
                        "Aluminum" -> method.recommendedMaterialsEn.contains("aluminum", ignoreCase = true) || method.recommendedMaterialsTr.contains("alüminyum", ignoreCase = true)
                        else -> true
                    }
                    matchesMaterial
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredMethods) { method ->
                        val isBookmarked = viewModel.isItemBookmarked("welding", method.id)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMethod = method }
                                .testTag("method_row_${method.codeName.replace(" ", "_")}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(end = 10.dp)
                                        ) {
                                            Text(
                                                text = method.codeName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = if (language == "TR") method.fullNameTr else method.fullNameEn,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            fontSize = 14.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleBookmark(
                                                category = "welding",
                                                id = method.id,
                                                title = method.codeName,
                                                subtitle = if (language == "TR") method.fullNameTr else method.fullNameEn
                                            )
                                        },
                                        modifier = Modifier
                                            .testTag("weld_bookmark_${method.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = viewModel.getString("speed") + ":", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = when (method.codeName) {
                                                "GTAW (TIG)" -> if (language == "TR") "Düşük" else "Slow"
                                                "GMAW (MIG/MAG)" -> if (language == "TR") "Çok yüksek" else "Very Fast"
                                                "SAW (Tozaltı)" -> if (language == "TR") "Aşırı yüksek" else "Extreme Fast"
                                                else -> if (language == "TR") "Orta" else "Medium"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = viewModel.getString("cost") + ":", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text(
                                            text = if (language == "TR") method.costTr else method.costEn,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Efficiency indicator progress bar matching Screen 4
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${viewModel.getString("efficiency")}: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                    LinearProgressIndicator(
                                        progress = { method.efficiencyPercent / 100f },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .padding(horizontal = 8.dp),
                                        color = Color(0xFFA24B1B),
                                        trackColor = Color.LightGray.copy(alpha = 0.4f),
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    Text(
                                        text = "%${method.efficiencyPercent}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFA24B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // VIEW B: Electrodes Database with live search filter
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = electrodeQuery,
                    onValueChange = { viewModel.setElectrodeSearch(it) },
                    placeholder = { Text(if (language == "TR") "Elektrot kodu ara (Örn: E7018)..." else "Search electrode code (e.g., E7018)...") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("electrode_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(electrodes) { electrode ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedElectrode = electrode }
                                .testTag("electrode_row_${electrode.awsCode}"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = electrode.awsCode,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (language == "TR") electrode.typeTr else electrode.typeEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = electrode.isoCode,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = "Go details",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeldingMethodDetailDialog(
    method: WeldingMethodEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${method.codeName} Process Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "WORKING PRINCIPLE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (language) {
                                "TR" -> method.principleTr
                                "DE" -> method.principleDe
                                else -> method.principleEn
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = viewModel.getString("advantages").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0F6E56)
                        )
                        Text(
                            text = when (language) {
                                "TR" -> method.advantagesTr
                                "DE" -> method.advantagesDe
                                else -> method.advantagesEn
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = viewModel.getString("disadvantages").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red.copy(alpha = 0.8f)
                        )
                        Text(
                            text = when (language) {
                                "TR" -> method.disadvantagesTr
                                "DE" -> method.disadvantagesDe
                                else -> method.disadvantagesEn
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = viewModel.getString("recommended_materials").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (language) {
                                "TR" -> method.recommendedMaterialsTr
                                "DE" -> method.recommendedMaterialsDe
                                else -> method.recommendedMaterialsEn
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = viewModel.getString("positions").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = when (language) {
                                "TR" -> method.positionCompatibilityTr
                                "DE" -> method.positionCompatibilityDe
                                else -> method.positionCompatibilityEn
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("weld_dialog_dismiss")) {
                Text("Kapat / Close")
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun ElectrodeDetailDialog(
    electrode: ElectrodeEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "AWS ${electrode.awsCode} Catalog Specifications",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow("AWS Classification:", electrode.awsCode)
                    DetailRow("EN ISO Standard:", electrode.isoCode)
                    DetailRow("Coating Type:", when (language) {
                        "TR" -> electrode.typeTr
                        "DE" -> electrode.typeDe
                        else -> electrode.typeEn
                    })
                    DetailRow("Tensile Strength (Rm):", when (language) {
                        "TR" -> electrode.tensileStrengthTr
                        "DE" -> electrode.tensileStrengthDe
                        else -> electrode.tensileStrengthEn
                    }, valueColor = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = viewModel.getString("usage_application").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (language) {
                            "TR" -> electrode.applicationTr
                            "DE" -> electrode.applicationDe
                            else -> electrode.applicationEn
                        },
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("electrode_dialog_dismiss")) {
                Text("Kapat")
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}
