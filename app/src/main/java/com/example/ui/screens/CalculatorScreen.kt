package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

private fun String.toDoubleOrNullSafe(): Double? {
    return this.replace(',', '.').trim().toDoubleOrNull()
}

@Composable
fun CalculatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Torque, 1: Heat, 2: Stress, 3: Unit
    var showFeedback by remember { mutableStateOf(false) }

    if (showFeedback) {
        FeedbackDialog(
            moduleName = "Calculators Module",
            viewModel = viewModel,
            onDismiss = { showFeedback = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Banner Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
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
                        text = viewModel.getString("calculations"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    IconButton(
                        onClick = { showFeedback = true },
                        modifier = Modifier.testTag("calculator_feedback_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Feed,
                            contentDescription = "Submit calculation feedback",
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = viewModel.getString("calculations_sub"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable tab headers
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    val tabTitles = listOf(
                        viewModel.getString("torque_calc_title"),
                        viewModel.getString("heat_input_title"),
                        viewModel.getString("stress_title"),
                        viewModel.getString("unit_converter")
                    )
                    tabTitles.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            unselectedContentColor = Color.White.copy(alpha = 0.6f),
                            selectedContentColor = Color.White,
                            modifier = Modifier.testTag("calculator_tab_$idx")
                        )
                    }
                }
            }
        }

        // TAB DISPLAYS
        Box(modifier = Modifier.weight(1f).padding(16.dp)) {
            when (selectedTab) {
                0 -> TorqueCalculatorView(viewModel)
                1 -> HeatInputCalculatorView(viewModel)
                2 -> StressCalculatorView(viewModel)
                3 -> UnitConverterView(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TorqueCalculatorView(viewModel: MainViewModel) {
    val language by viewModel.appLanguage.collectAsState()
    
    // States
    var selectedBoltSize by remember { mutableStateOf("M12") }
    var selectedGrade by remember { mutableStateOf("8.8") }
    var selectedFrictionType by remember { mutableStateOf("Dry") } // "Dry", "Lubricated", "Zinc", "Alu"
    var tensionInput by remember { mutableStateOf("38") } // in kN (preload tension force)
    var calculationResult by remember { mutableStateOf<Double?>(null) }
    var showingDetailsResult by remember { mutableStateOf(false) }

    // Bolt diameter lookup map
    val boltDiameters = mapOf(
        "M3" to 3.0, "M4" to 4.0, "M5" to 5.0, "M6" to 6.0, "M8" to 8.0,
        "M10" to 10.0, "M12" to 12.0, "M14" to 14.0, "M16" to 16.0, "M20" to 20.0,
        "M24" to 24.0, "M30" to 30.0, "M36" to 36.0, "M42" to 42.0, "M48" to 48.0,
        "M56" to 56.0, "M64" to 64.0
    )

    // Friction K coefficients
    val frictionCoefficients = mapOf(
        "Dry" to 0.20,
        "Lubricated" to 0.15,
        "Zinc" to 0.18,
        "Alu" to 0.16
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("torque_calc_descr") + "\nFormula: T = K * d * F",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Bolt Size Row selection
                    Text(
                        text = "Bolt nominal size D (mm)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("M6", "M8", "M10", "M12", "M16", "M20", "M24").forEach { size ->
                            FilterChip(
                                selected = selectedBoltSize == size,
                                onClick = {
                                    selectedBoltSize = size
                                    // Set a typical proof tension value as suggestion
                                    tensionInput = when (size) {
                                        "M6" -> "10"
                                        "M8" -> "18"
                                        "M10" -> "29"
                                        "M12" -> "41"
                                        "M16" -> "75"
                                        "M20" -> "120"
                                        else -> "180"
                                    }
                                },
                                label = { Text(size, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("chip_bolt_$size")
                            )
                        }
                    }

                    // Bolt grade row
                    Text(
                        text = viewModel.getString("torque_grade"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("8.8", "10.9", "12.9").forEach { grade ->
                            FilterChip(
                                selected = selectedGrade == grade,
                                onClick = { selectedGrade = grade },
                                label = { Text("Grade $grade", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("chip_grade_$grade")
                            )
                        }
                    }

                    // Preload tension field (F - kN)
                    OutlinedTextField(
                        value = tensionInput,
                        onValueChange = { tensionInput = it },
                        label = { Text(viewModel.getString("axial_tension")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tension_input_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Friction select
                    Text(
                        text = viewModel.getString("lubrication"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "Dry" to viewModel.getString("dry"),
                            "Lubricated" to viewModel.getString("lubed"),
                            "Zinc" to viewModel.getString("galvanized"),
                            "Alu" to viewModel.getString("al_lubed")
                        ).forEach { (key, desc) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFrictionType = key }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFrictionType == key,
                                    onClick = { selectedFrictionType = key },
                                    modifier = Modifier.testTag("radio_friction_$key")
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Calculate trigger button
                    Button(
                        onClick = {
                            val nominalD = boltDiameters[selectedBoltSize] ?: 12.0
                            val tensionKN = tensionInput.toDoubleOrNullSafe() ?: 38.0
                            val frictionK = frictionCoefficients[selectedFrictionType] ?: 0.20

                            // Physics metric equation: Torque (Nm) = K * D(mm) * F(kN)
                            val finalTorque = frictionK * nominalD * tensionKN
                            calculationResult = finalTorque
                            showingDetailsResult = true

                            // Inject history record
                            viewModel.addHistory(
                                type = "Torque",
                                inputs = "$selectedBoltSize Grade $selectedGrade ($selectedFrictionType K=$frictionK), Force $tensionKN kN",
                                result = "${String.format("%.1f", finalTorque)} Nm"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculate_torque_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(viewModel.getString("calculate"))
                    }
                }
            }
        }

        // Result displays card
        if (showingDetailsResult && calculationResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.getString("result"),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${String.format("%.1f", calculationResult ?: 0.0)} Nm",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = viewModel.getString("recommended_torque") + " ($selectedBoltSize / Grade $selectedGrade)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )

                        // Safety indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("VDI 2230 Yield Factor", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("90%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Safety Coefficient", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("SF ~ 1.8", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0F6E56))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatInputCalculatorView(viewModel: MainViewModel) {
    var voltageInput by remember { mutableStateOf("24") }
    var currentInput by remember { mutableStateOf("180") }
    var travelSpeedInput by remember { mutableStateOf("250") } // mm/min
    var selectEfficiency by remember { mutableStateOf(0.8f) } // default GMAW / MIG = 0.8
    var efficiencyLabel by remember { mutableStateOf("MIG / MAG (η = 0.8)") }
    var calculatedHeat by remember { mutableStateOf<Double?>(null) }
    var isDone by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("heat_input_descr"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Thermal Voltage
                    OutlinedTextField(
                        value = voltageInput,
                        onValueChange = { voltageInput = it },
                        label = { Text(viewModel.getString("voltage")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voltage_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Thermal Current Amps
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        label = { Text(viewModel.getString("current")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Welding Travel Speed mm/min
                    OutlinedTextField(
                        value = travelSpeedInput,
                        onValueChange = { travelSpeedInput = it },
                        label = { Text(viewModel.getString("travel_speed")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("speed_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Methods efficiency
                    Text(
                        text = viewModel.getString("welding_process"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(0.6f, "TIG (0.6)", "GTAW"),
                            Triple(0.8f, "MIG (0.8)", "GMAW"),
                            Triple(0.8f, "Rod (0.8)", "SMAW"),
                            Triple(1.0f, "SAW (1.0)", "SAW")
                        ).forEach { (eff, chipLabel, codeName) ->
                            FilterChip(
                                selected = selectEfficiency == eff && efficiencyLabel.contains(codeName),
                                onClick = {
                                    selectEfficiency = eff
                                    efficiencyLabel = "$codeName (η = $eff)"
                                },
                                label = { Text(chipLabel, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("eff_chip_$codeName")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val u = voltageInput.toDoubleOrNullSafe() ?: 24.0
                            val i = currentInput.toDoubleOrNullSafe() ?: 180.0
                            val s = travelSpeedInput.toDoubleOrNullSafe() ?: 250.0

                            // Formula Q = (η * U * I * 60) / (1000 * S) (kJ/mm)
                            if (s > 0) {
                                val qValue = (selectEfficiency * u * i * 60) / (1000 * s)
                                calculatedHeat = qValue
                                isDone = true

                                viewModel.addHistory(
                                    type = "HeatInput",
                                    inputs = "U=$u V, I=$i A, Speed=$s mm/min, η=$selectEfficiency",
                                    result = "${String.format("%.3f", qValue)} kJ/mm"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculate_heat_button")
                    ) {
                        Text(viewModel.getString("calculate"))
                    }
                }
            }
        }

        if (isDone && calculatedHeat != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.getString("result"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${String.format("%.3f", calculatedHeat ?: 0.0)} kJ/mm",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Net Heat Input (EN ISO 15614-1 / ASME IX)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StressCalculatorView(viewModel: MainViewModel) {
    var forceInput by remember { mutableStateOf("50") } // in kN
    var diameterInput by remember { mutableStateOf("20") } // in mm
    var yieldLimitInput by remember { mutableStateOf("250") } // in MPa (e.g. typical construction structural steel)
    var calculatedStress by remember { mutableStateOf<Double?>(null) }
    var safetyFactorResult by remember { mutableStateOf<Double?>(null) }
    var isDone by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("stress_descr") + "\nFormula: σ = Force / Area",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Tension force in kN
                    OutlinedTextField(
                        value = forceInput,
                        onValueChange = { forceInput = it },
                        label = { Text(viewModel.getString("stress_force")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stress_force_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Shaft diameter in mm
                    OutlinedTextField(
                        value = diameterInput,
                        onValueChange = { diameterInput = it },
                        label = { Text(viewModel.getString("bar_radius")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stress_diameter_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Yield limit in MPa
                    OutlinedTextField(
                        value = yieldLimitInput,
                        onValueChange = { yieldLimitInput = it },
                        label = { Text(viewModel.getString("yield_strength")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stress_yield_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val forceKN = forceInput.toDoubleOrNullSafe() ?: 50.0
                            val diameter = diameterInput.toDoubleOrNullSafe() ?: 20.0
                            val yieldLimit = yieldLimitInput.toDoubleOrNullSafe() ?: 250.0

                            if (diameter > 0) {
                                // Area of round bar profile in mm² = (pi * d²) / 4
                                val area = (Math.PI * diameter * diameter) / 4.0
                                // Stress σ in MPa (or N/mm²) = (Force in Newtons) / Area
                                val forceN = forceKN * 1000.0
                                val stressValue = forceN / area
                                calculatedStress = stressValue

                                // Safety coefficient = Yield limit / Stress
                                safetyFactorResult = if (stressValue > 0) yieldLimit / stressValue else 0.0
                                isDone = true

                                viewModel.addHistory(
                                    type = "Stress",
                                    inputs = "Force $forceKN kN, Bar d=$diameter mm, Yield Limit $yieldLimit MPa",
                                    result = "${String.format("%.1f", stressValue)} MPa"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calculate_stress_button")
                    ) {
                        Text(viewModel.getString("calculate"))
                    }
                }
            }
        }

        if (isDone && calculatedStress != null) {
            item {
                val yieldWarning = (safetyFactorResult ?: 2.0) < 1.0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (yieldWarning) Color(0xFFFDEDEC) else Color(0xFFEAF9F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.getString("result"),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (yieldWarning) Color.Red else Color(0xFF0F6E56)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${String.format("%.1f", calculatedStress ?: 0.0)} MPa",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (yieldWarning) Color.Red else Color(0xFF0F6E56)
                        )
                        Text(
                            text = viewModel.getString("calculated_stress"),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color.Black
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = if (yieldWarning) Color.Red.copy(alpha = 0.2f) else Color(0xFF0F6E56).copy(alpha = 0.2f)
                        )

                        Text(
                            text = "Safety Coefficient: ${String.format("%.2f", safetyFactorResult ?: 0.0)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (yieldWarning) Color.Red else Color(0xFF004D3C)
                        )
                        Text(
                            text = if (yieldWarning) "CRITICAL WARNING: Mechanical bar will YIELD / plastically deform." else "Joint is structurally SAFE according to mechanical thresholds.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConverterView(viewModel: MainViewModel) {
    var rawInput by remember { mutableStateOf("10") }
    var convertType by remember { mutableStateOf(0) } // 0: mm->in, 1: in->mm, 2: Nm->lb-ft, 3: lb-ft->Nm, 4: MPa->psi, 5: psi->MPa, 6: C->F, 7: F->C
    var convertedOutput by remember { mutableStateOf("") }
    var inUnitLabel by remember { mutableStateOf("mm") }
    var outUnitLabel by remember { mutableStateOf("inch") }

    fun runConvert() {
        val originalVal = rawInput.toDoubleOrNullSafe() ?: 0.0
        val finalVal = when (convertType) {
            0 -> { inUnitLabel="mm"; outUnitLabel="inch"; originalVal * 0.03937 } // mm -> inch
            1 -> { inUnitLabel="inch"; outUnitLabel="mm"; originalVal / 0.03937 } // inch -> mm
            2 -> { inUnitLabel="Nm"; outUnitLabel="lb-ft"; originalVal * 0.73756 }  // Nm -> lb-ft
            3 -> { inUnitLabel="lb-ft"; outUnitLabel="Nm"; originalVal / 0.73756 }  // lb-ft -> Nm
            4 -> { inUnitLabel="MPa"; outUnitLabel="psi"; originalVal * 145.038 }  // MPa -> psi
            5 -> { inUnitLabel="psi"; outUnitLabel="MPa"; originalVal / 145.038 }  // psi -> MPa
            6 -> { inUnitLabel="°C"; outUnitLabel="°F"; (originalVal * 9/5) + 32 } // C -> F
            else -> { inUnitLabel="°F"; outUnitLabel="°C"; (originalVal - 32) * 5/9 } // F -> C
        }
        convertedOutput = String.format("%.3f", finalVal)
    }

    // Run trigger when values change
    LaunchedEffect(rawInput, convertType) {
        runConvert()
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = viewModel.getString("unit_descr"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Options selector
                    val conversions = listOf(
                        "mm ➙ inch", "inch ➙ mm",
                        "Nm ➙ lb-ft", "lb-ft ➙ Nm",
                        "MPa ➙ psi", "psi ➙ MPa",
                        "°C ➙ °F", "°F ➙ °C"
                    )

                    Text("Conversion Metrics Type:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        conversions.chunked(2).forEachIndexed { i, pair ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                pair.forEachIndexed { j, opt ->
                                    val index = (i * 2) + j
                                    FilterChip(
                                        selected = convertType == index,
                                        onClick = { convertType = index },
                                        label = { Text(opt, fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f).testTag("chip_conversion_$index")
                                    )
                                }
                            }
                        }
                    }

                    // Original numeric input
                    OutlinedTextField(
                        value = rawInput,
                        onValueChange = { rawInput = it },
                        label = { Text(viewModel.getString("convert_val") + " ($inUnitLabel)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_input_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.getString("converted").uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$convertedOutput $outUnitLabel",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
