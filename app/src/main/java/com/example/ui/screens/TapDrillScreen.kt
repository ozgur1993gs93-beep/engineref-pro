package com.example.ui.screens

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
import com.example.data.TapDrillEntity
import com.example.viewmodel.MainViewModel

@Composable
fun TapDrillScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tapDrills by viewModel.tapDrills.collectAsState()
    val searchQuery by viewModel.drillSearchQuery.collectAsState()
    val materialFilter by viewModel.drillMaterialFilter.collectAsState()
    val language by viewModel.appLanguage.collectAsState()

    var selectedDrill by remember { mutableStateOf<TapDrillEntity?>(null) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        FeedbackDialog(
            moduleName = "Tap & Drill Sizes Table",
            viewModel = viewModel,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    if (selectedDrill != null) {
        TapDrillDetailDialog(
            drill = selectedDrill!!,
            viewModel = viewModel,
            onDismiss = { selectedDrill = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top header banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary
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
                        text = viewModel.getString("tap_drill_title"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.testTag("tap_feedback_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Feed,
                            contentDescription = "Feedback Form",
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = viewModel.getString("tap_drill_sub"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // Search inputs
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setDrillSearch(it) },
                placeholder = { Text(when (language) {
                    "TR" -> "Kılavuz çapı ara (Örn: M8)..."
                    "DE" -> "Suchen (z. B. M8)..."
                    else -> "Search tap (e.g., M8)..."
                }) },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setDrillSearch("") }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tap_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Material Filter chips matching Screen 2
            Text(
                text = viewModel.getString("material_filter"),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            val materials = listOf(
                "Tümü" to ("All" to "Alle"),
                "Çelik" to ("Steel" to "Stahl"),
                "Alüminyum" to ("Aluminum" to "Aluminium"),
                "Paslanmaz" to ("Stainless" to "Edelstahl"),
                "Döküm" to ("Cast Iron" to "Gusseisen")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                materials.forEach { (tr, foreign) ->
                    val (en, de) = foreign
                    val label = when (language) {
                        "TR" -> tr
                        "DE" -> de
                        else -> en
                    }
                    val valueToFilter = tr // our database model has Turkish names or matches tr

                    val isSelected = materialFilter == valueToFilter || 
                                     (materialFilter == "Tümü" && valueToFilter == "Tümü")

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setDrillMaterial(if (valueToFilter == "Tümü") "Tümü" else valueToFilter)
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_filter_$tr")
                    )
                }
            }
        }

        // Table Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Kılavuz", Modifier.weight(1.2f))
                TableHeaderCell(when (language) {
                    "TR" -> "Adım (mm)"
                    "DE" -> "Steigung"
                    else -> "Pitch (mm)"
                }, Modifier.weight(1.2f))
                TableHeaderCell(when (language) {
                    "TR" -> "Ön Delik"
                    "DE" -> "Bohrer"
                    else -> "Pre-Drill"
                }, Modifier.weight(1.2f))
                TableHeaderCell(when (language) {
                    "TR" -> "Malzeme"
                    "DE" -> "Material"
                    else -> "Material"
                }, Modifier.weight(1.5f))
                TableHeaderCell("", Modifier.weight(0.5f)) // bookmark column
            }
        }

        // Table Data list
        Box(modifier = Modifier.weight(1f)) {
            if (tapDrills.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = "No items",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aranan ölçü bulunamadı.\nNo results matches your searching query.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(tapDrills) { drill ->
                        val isBookmarked = viewModel.isItemBookmarked("drill", drill.id)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDrill = drill }
                                .padding(vertical = 12.dp, horizontal = 12.dp)
                                .testTag("drill_row_${drill.size}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = drill.size,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "${drill.pitch} mm",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.2f)
                            )
                            Text(
                                text = "Ø ${drill.drillSize} mm",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1.2f)
                            )
                            
                            // Localised material tag
                            val matLabel = when {
                                drill.materialType.contains("Çelik") -> viewModel.getString("steel")
                                drill.materialType.contains("Paslanmaz") -> viewModel.getString("stainless")
                                drill.materialType.contains("Alüminyum") -> viewModel.getString("aluminum")
                                drill.materialType.contains("Döküm") -> viewModel.getString("cast_iron")
                                else -> drill.materialType
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when {
                                    drill.materialType.contains("Çelik") -> Color(0xFFE2EFFC)
                                    drill.materialType.contains("Paslanmaz") -> Color(0xFFF0E5FC)
                                    drill.materialType.contains("Alüminyum") -> Color(0xFFE3F7EB)
                                    else -> Color(0xFFFDECEE)
                                },
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text(
                                    text = matLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )
                            }

                            // Bookmark toggle inside the row
                            IconButton(
                                onClick = {
                                    viewModel.toggleBookmark(
                                        category = "drill",
                                        id = drill.id,
                                        title = "${drill.size} Thread - Ø ${drill.drillSize} mm",
                                        subtitle = "Pitch: ${drill.pitch} mm"
                                    )
                                },
                                modifier = Modifier
                                    .weight(0.5f)
                                    .testTag("bookmark_toggle_${drill.size}")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun TapDrillDetailDialog(
    drill: TapDrillEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val language by viewModel.appLanguage.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${drill.size} Thread Specification Details",
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
                    DetailRow("Nominal Size:", drill.size)
                    DetailRow(viewModel.getString("torque_grade") + " / Standard:", drill.standard)
                    DetailRow("Thread Pitch (h):", "${drill.pitch} mm")
                    DetailRow(
                        label = when (language) {
                            "TR" -> "Tavsiye Edilen Matkap Ucu:"
                            "DE" -> "Empfohlener Bohrer:"
                            else -> "Recommended Drill Hole:"
                        },
                        value = "Ø ${drill.drillSize} mm",
                        valueColor = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = "Tightening Torque values (Metric - Nm)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    DetailRow("Grade 8.8 Torque:", "${drill.torque88} Nm")
                    DetailRow("Grade 10.9 Torque:", "${drill.torque109} Nm")
                    DetailRow("Grade 12.9 Torque:", "${drill.torque129} Nm")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("dialog_dismiss_button")) {
                Text("Kapat / Close")
            }
        },
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = fontWeight), color = valueColor)
    }
}
