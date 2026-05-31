package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tap_drills")
data class TapDrillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val size: String,           // e.g., "M4", "M16"
    val pitch: Double,          // Thread pitch in mm
    val drillSize: Double,      // Recommended drill hole diameter in mm
    val torque88: Double,       // Grade 8.8 recommended torque in Nm
    val torque109: Double,      // Grade 10.9 recommended torque in Nm
    val torque129: Double,      // Grade 12.9 recommended torque in Nm
    val materialType: String,   // "Çelik / Steel", "Alüminyum / Alu", "Paslanmaz / Stainless", "Döküm / Cast Iron"
    val standard: String        // "DIN 76 / ISO 724"
)

@Entity(tableName = "welding_methods")
data class WeldingMethodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codeName: String,       // e.g., "GTAW (TIG)", "GMAW (MIG/MAG)"
    val fullNameTr: String,
    val fullNameEn: String,
    val fullNameDe: String,
    val principleTr: String,
    val principleEn: String,
    val principleDe: String,
    val advantagesTr: String,   // comma or newline separated
    val advantagesEn: String,
    val advantagesDe: String,
    val disadvantagesTr: String,
    val disadvantagesEn: String,
    val disadvantagesDe: String,
    val recommendedMaterialsTr: String,
    val recommendedMaterialsEn: String,
    val recommendedMaterialsDe: String,
    val positionCompatibilityTr: String,
    val positionCompatibilityEn: String,
    val positionCompatibilityDe: String,
    val costTr: String,         // "Yüksek", "Orta", "Düşük"
    val costEn: String,         // "High", "Medium", "Low"
    val costDe: String,
    val efficiencyPercent: Int, // e.g., 90
    val qualityRatingTr: String, // e.g., "Mükemmel", "İyi"
    val qualityRatingEn: String,
    val qualityRatingDe: String,
    val thicknessRangeTr: String,
    val thicknessRangeEn: String,
    val thicknessRangeDe: String
)

@Entity(tableName = "electrodes")
data class ElectrodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val awsCode: String,        // e.g., "E6013", "E7018"
    val isoCode: String,        // e.g., "E 38 0 RC 11", "E 42 5 B 42 H5"
    val typeTr: String,         // e.g., "Rutil", "Bazik"
    val typeEn: String,         // e.g., "Rutile", "Basic"
    val typeDe: String,         // e.g., "Rutil", "Basisch"
    val applicationTr: String,
    val applicationEn: String,
    val applicationDe: String,
    val tensileStrengthTr: String, // e.g., "430-510 MPa"
    val tensileStrengthEn: String,
    val tensileStrengthDe: String
)

@Entity(tableName = "calculation_history")
data class CalculationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val calculatorType: String,  // "Torque", "HeatInput", "Stress", "UnitConverter"
    val inputs: String,          // formatted string of inputs used
    val result: String,          // result value and unit
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,        // "drill", "welding", "electrode"
    val referenceId: Int,        // references drill or welding methods id
    val title: String,
    val subtitle: String
)

@Entity(tableName = "user_feedbacks")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleName: String,
    val comment: String,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)
