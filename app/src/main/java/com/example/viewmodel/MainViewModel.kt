package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiApiClient
import com.example.repository.EngineRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Chat Message UI holder
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EngineRepository(db)

    // General app states
    private val _appLanguage = MutableStateFlow("TR") // "TR", "EN", "DE"
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Start in cool modern dark theme
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // 1. Mechanical library state
    private val _drillSearchQuery = MutableStateFlow("")
    val drillSearchQuery: StateFlow<String> = _drillSearchQuery.asStateFlow()

    private val _drillMaterialFilter = MutableStateFlow("Tümü") // "Tümü", "Çelik", "Alüminyum", "Paslanmaz", "Döküm"
    val drillMaterialFilter: StateFlow<String> = _drillMaterialFilter.asStateFlow()

    val tapDrills: StateFlow<List<TapDrillEntity>> = combine(
        repository.getTapDrills(),
        _drillSearchQuery,
        _drillMaterialFilter,
        _appLanguage
    ) { drillList, query, materialFilter, lang ->
        var filtered = if (query.isBlank()) {
            drillList
        } else {
            drillList.filter { it.size.contains(query, ignoreCase = true) }
        }

        if (materialFilter != "Tümü" && materialFilter != "All" && materialFilter != "Alle") {
            filtered = filtered.filter { drill ->
                // Check if drill's material tag matches the filter in any way
                val localizedMaterial = getMaterialLabelTr(materialFilter, lang)
                drill.materialType.contains(materialFilter, ignoreCase = true) || 
                drill.materialType.contains(localizedMaterial, ignoreCase = true)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Welding technology state
    val weldingMethods: StateFlow<List<WeldingMethodEntity>> = repository.getWeldingMethods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _electrodeSearchQuery = MutableStateFlow("")
    val electrodeSearchQuery: StateFlow<String> = _electrodeSearchQuery.asStateFlow()

    val electrodes: StateFlow<List<ElectrodeEntity>> = combine(
        repository.getElectrodes(),
        _electrodeSearchQuery
    ) { electrodeList, query ->
        if (query.isBlank()) {
            electrodeList
        } else {
            electrodeList.filter { 
                it.awsCode.contains(query, ignoreCase = true) || 
                it.isoCode.contains(query, ignoreCase = true) 
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Calculation lists / History state
    val calculationHistory: StateFlow<List<CalculationHistoryEntity>> = repository.getCalculationHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Bookmark list
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. AI Chat state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiTyping = MutableStateFlow(false)
    val isAiTyping: StateFlow<Boolean> = _isAiTyping.asStateFlow()

    // 6. User feedback items
    val userFeedbacks: StateFlow<List<FeedbackEntity>> = repository.feedbackDao.getAllFeedbacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    init {
        // Run database automatic initial population
        viewModelScope.launch {
            try {
                repository.ensureDataInitialized()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInitialized.value = true
                // Populate welcome chat assistant introduction
                resetAiConversation()
            }
        }
    }

    // Language Toggle
    fun setLanguage(lang: String) {
        _appLanguage.value = lang
        resetAiConversation() // update welcome message in target language
    }

    // Theme Toggle
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    // Drills filter
    fun setDrillSearch(query: String) {
        _drillSearchQuery.value = query
    }

    fun setDrillMaterial(filter: String) {
        _drillMaterialFilter.value = filter
    }

    // Electrodes search
    fun setElectrodeSearch(query: String) {
        _electrodeSearchQuery.value = query
    }

    // Bookmarking helper
    fun toggleBookmark(category: String, id: Int, title: String, subtitle: String) {
        viewModelScope.launch {
            try {
                val exists = bookmarks.value.any { it.category == category && it.referenceId == id }
                if (exists) {
                    repository.removeBookmark(category, id)
                } else {
                    repository.addBookmark(BookmarkEntity(category = category, referenceId = id, title = title, subtitle = subtitle))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun isItemBookmarked(category: String, id: Int): Boolean {
        return bookmarks.value.any { it.category == category && it.referenceId == id }
    }

    // Calculations Helpers
    fun addHistory(type: String, inputs: String, result: String) {
        viewModelScope.launch {
            try {
                repository.addCalculationHistory(
                    CalculationHistoryEntity(calculatorType = type, inputs = inputs, result = result)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                repository.clearCalculationHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Feedback Submit Helper
    fun submitFeedback(moduleName: String, comment: String, rating: Int) {
        viewModelScope.launch {
            try {
                repository.addFeedback(
                    FeedbackEntity(moduleName = moduleName, comment = comment, rating = rating)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // AI Chat Interaction
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val currentMessages = _chatMessages.value.toMutableList()
        currentMessages.add(ChatMessage(content = text, isUser = true))
        _chatMessages.value = currentMessages

        _isAiTyping.value = true

        // Form chat history inside memory to feed to Gemini
        val apiHistory = currentMessages
            .drop(1) // skip the initial ai greeting
            .filter { it.content.isNotEmpty() }
            .chunked(2)
            .mapNotNull { chunk ->
                if (chunk.size >= 2) {
                    Pair(chunk[0].content, chunk[1].content)
                } else null
            }

        viewModelScope.launch {
            val response = GeminiApiClient.askAssistant(text, apiHistory)
            val updatedMessages = _chatMessages.value.toMutableList()
            updatedMessages.add(ChatMessage(content = response, isUser = false))
            _chatMessages.value = updatedMessages
            _isAiTyping.value = false
        }
    }

    fun resetAiConversation() {
        val welcomeMsg = when (_appLanguage.value) {
            "TR" -> "Merhaba! Ben EngineRef Pro AI Asistanıyım. Mekanik toleranslar, tap kılavuz çapları, tork limitleri, kaynak parametreleri veya AWS/ISO standartları hakkındaki tüm teknik sorularınızı yanıtlayabilirim. Bugün size nasıl yardımcı olabilirim?"
            "DE" -> "Hallo! Ich bin Ihr EngineRef Pro KI-Assistent. Ich kann alle Ihre technischen Fragen zu mechanischen Toleranzen, Gewindebohrtabellen, Drehmomentgrenzen, Schweißparametern oder AWS/ISO-Normen beantworten. Wie kann ich Ihnen heute helfen?"
            else -> "Hello! I am your EngineRef Pro AI Assistant. I can answer all your technical questions about mechanical tolerances, tap drill tables, torque limits, welding parameters, or AWS/ISO standards. How can I help you today?"
        }
        _chatMessages.value = listOf(ChatMessage(content = welcomeMsg, isUser = false))
    }

    // Language mapper for material filters lookup
    private fun getMaterialLabelTr(filter: String, lang: String): String {
        return when (filter) {
            "Steel", "Stahl" -> "Çelik"
            "Aluminum", "Aluminium" -> "Alüminyum"
            "Stainless", "Edelstahl" -> "Paslanmaz"
            "Cast Iron", "Gusseisen" -> "Döküm"
            else -> filter
        }
    }

    // Localization Database inside ViewModel (Highly performant, instantly switches UI languages)
    fun getString(key: String): String {
        val strings = mapOf(
            "TR" to mapOf(
                "app_title" to "EngineRef Pro",
                "subtitle" to "Mühendislik El Kitabı",
                "nav_home" to "Ana Sayfa",
                "nav_library" to "Kitaplık",
                "nav_calc" to "Hesap",
                "nav_ai" to "AI Chat",
                "nav_settings" to "Ayarlar",
                "search_hint" to "Ara (kılavuz çapı, kaynak kodu...)",
                "ai_headline" to "AI Mühendislik Asistanı",
                "ai_sub" to "Gemini tabanlı · Çevrimiçi",
                "ai_input_hint" to "Teknik sorunuzu yazın...",
                "ai_typing" to "Asistan yazıyor...",
                "modules" to "MODÜLLER",
                "drill_delik" to "Kılavuz & Delik",
                "drill_sub" to "Metrik/inç ön delik çapları",
                "welding_methods" to "Kaynak Teknolojileri",
                "welding_sub" to "MIG, TIG, MMA karşılaştırma",
                "bolt_torque" to "Civata & Tork",
                "bolt_sub" to "Çap, adım ve tork sınırları",
                "calculations" to "Hesaplama Araçları",
                "calculations_sub" to "Tork, ısı girdisi, gerilme",
                "welding_codes" to "Kaynak Kodları",
                "welding_codes_sub" to "AWS, EN/ISO elektrot veri tabanı",
                "materials" to "Malzeme Kitaplığı",
                "materials_sub" to "Çelik, paslanmaz, döküm, alü",
                "tap_drill_title" to "Metrik Kılavuz — Delik Çapları",
                "tap_drill_sub" to "DIN 76 / ISO 724 standardına göre",
                "material_filter" to "MALZEME FİLTRELE",
                "torque_grade" to "Civata Sınıfı",
                "lubrication" to "Yağlama Durumu",
                "lubed" to "Yağlanmış (K=0.15)",
                "dry" to "Kuru / Standart (K=0.20)",
                "galvanized" to "Galvaniz Kaplama (K=0.18)",
                "al_lubed" to "Alüminyum Yağlı (K=0.16)",
                "axial_tension" to "Sıkma Kuvveti (Eksenel - kN)",
                "calculate" to "Hesapla",
                "result" to "HESAPLAMA SONUCU",
                "recommended_torque" to "Önerilen Tork Değeri",
                "safety_factor" to "Güvenlik Katsayısı",
                "welding_compare" to "Kaynak Yöntemi Karşılaştır",
                "weld_sub" to "Malzeme ve pozisyona göre yöntem seçimi",
                "method" to "Yöntem",
                "quality" to "Kalite",
                "speed" to "Hız",
                "cost" to "Maliyet",
                "difficulty" to "Zorluk",
                "thickness_range" to "Levha Kalınlığı",
                "advantages" to "Avantajlar",
                "disadvantages" to "Dezavantajlar",
                "recommended_materials" to "Önerilen Malzemeler",
                "positions" to "Pozisyon Uyumluluğu",
                "efficiency" to "Verimlilik",
                "electrode_db" to "Elektrot Veri Tabanı",
                "electrode_sub" to "AWS A5.1 / ISO Özellikleri",
                "tensile_strength" to "Çekme Dayanımı",
                "usage_application" to "Uygulama Alanı",
                "torque_calc_title" to "Tork Hesaplama",
                "torque_calc_descr" to "Civata sınıfı ve sürtünme değerlerine göre sıkma torku.",
                "heat_input_title" to "Isı Girdisi Hesaplama",
                "heat_input_descr" to "Kaynak ısı girdisi (Q = η * U * I * 60 / (1000 * S))",
                "stress_title" to "Gerilme & Emniyet",
                "stress_descr" to "Basit çekme/basma gerilmesi ve emniyet katsayısı.",
                "unit_converter" to "Birim Dönüştürücü",
                "unit_descr" to "Metrik ve İmparatorluk mühendislik dönüşümleri.",
                "voltage" to "Kaynak Voltajı (V - Volt)",
                "current" to "Kaynak Akımı (I - Ampere)",
                "travel_speed" to "Kaynak Hızı (S - mm/min)",
                "welding_process" to "Kaynak Yöntemi Verimi",
                "heat_result" to "Isı Girdisi (Q)",
                "stress_force" to "Uygulanan Kuvvet (F - kN)",
                "bar_radius" to "Mil Çapı (d - mm)",
                "yield_strength" to "Malzeme Akma Sınırı (MPa)",
                "calculated_stress" to "Hesaplanan Gerilme",
                "convert_val" to "Dönüştürülecek Değer",
                "converted" to "Sonuç",
                "feedback_title" to "Geri Bildirim Paylaş",
                "feedback_hint" to "Bu modül hakkındaki teknik yorumlarınız...",
                "submit" to "Gönder",
                "settings_headline" to "Uygulama Ayarları",
                "language" to "Uygulama Dili (Language)",
                "theme" to "Karanlık Tema (Dark Theme)",
                "saved_bookmarks" to "Kayıtlı Referanslarım",
                "no_bookmarks" to "Henüz kaydedilmiş teknik kart yok.",
                "about_body" to "EngineRef Pro, saha mühendisleri ve teknisyenleri için tasarlanmış bağımsız bir teknik el kitabıdır.\nTüm hesaplama ve tablolar standartlara göredir. DIN 13-1, ISO 724 ve AWS standartları temel alınmıştır.",
                "all" to "Tümü",
                "steel" to "Çelik",
                "aluminum" to "Alüminyum",
                "stainless" to "Paslanmaz",
                "cast_iron" to "Döküm"
            ),
            "EN" to mapOf(
                "app_title" to "EngineRef Pro",
                "subtitle" to "Engineering Handbook",
                "nav_home" to "Home",
                "nav_library" to "Library",
                "nav_calc" to "Calc",
                "nav_ai" to "AI Chat",
                "nav_settings" to "Settings",
                "search_hint" to "Search (drill diameter, weld code...)",
                "ai_headline" to "AI Engineering Assistant",
                "ai_sub" to "Gemini powered · Online",
                "ai_input_hint" to "Ask your technical question...",
                "ai_typing" to "Assistant is typing...",
                "modules" to "MODULES",
                "drill_delik" to "Tap & Drill",
                "drill_sub" to "Metric/inch pre-drill sizes",
                "welding_methods" to "Welding Technologies",
                "welding_sub" to "MIG, TIG, MMA comparisons",
                "bolt_torque" to "Bolts & Torque",
                "bolt_sub" to "Diameter, pitch and torque limits",
                "calculations" to "Calculators",
                "calculations_sub" to "Torque, heat input, stress",
                "welding_codes" to "Welding Codes",
                "welding_codes_sub" to "AWS, EN/ISO electrode index",
                "materials" to "Material Reference",
                "materials_sub" to "Steel, stainless, cast iron, alu",
                "tap_drill_title" to "Metric Tap — Drill Diameters",
                "tap_drill_sub" to "According to DIN 76 / ISO 724",
                "material_filter" to "MATERIAL FILTER",
                "torque_grade" to "Bolt Grade",
                "lubrication" to "Lubrication State",
                "lubed" to "Lubricated Steel (K=0.15)",
                "dry" to "Dry Steel / Default (K=0.20)",
                "galvanized" to "Zinc-Plated / Galvanized (K=0.18)",
                "al_lubed" to "Aluminum Oiled (K=0.16)",
                "axial_tension" to "Preload Tension (Axial - kN)",
                "calculate" to "Calculate",
                "result" to "CALCULATION RESULT",
                "recommended_torque" to "Recommended Torque",
                "safety_factor" to "Safety Factor",
                "welding_compare" to "Compare Welding Methods",
                "weld_sub" to "Find method by metal and positions",
                "method" to "Method",
                "quality" to "Quality",
                "speed" to "Speed",
                "cost" to "Cost",
                "difficulty" to "Difficulty",
                "thickness_range" to "Plate Thickness",
                "advantages" to "Advantages",
                "disadvantages" to "Disadvantages",
                "recommended_materials" to "Recommended Metals",
                "positions" to "Position Suitability",
                "efficiency" to "Efficiency",
                "electrode_db" to "Electrode Database",
                "electrode_sub" to "AWS A5.1 / ISO Properties",
                "tensile_strength" to "Tensile Strength",
                "usage_application" to "Application Area",
                "torque_calc_title" to "Torque Calculator",
                "torque_calc_descr" to "Tightening torque by grade and lubricants.",
                "heat_input_title" to "Heat Input Calculator",
                "heat_input_descr" to "Welding heat input (Q = η * U * I * 60 / (1000 * S))",
                "stress_title" to "Stress & Safety Factor",
                "stress_descr" to "Simple tensile / compressive stress calculation.",
                "unit_converter" to "Unit Converter",
                "unit_descr" to "Metric and Imperial technical translations.",
                "voltage" to "Welding Voltage (V - Volts)",
                "current" to "Welding Current (I - Amperes)",
                "travel_speed" to "Travel Speed (S - mm/min)",
                "welding_process" to "Process Efficiency",
                "heat_result" to "Heat Input (Q)",
                "stress_force" to "Applied Force (F - kN)",
                "bar_radius" to "Bar Diameter (d - mm)",
                "yield_strength" to "Yield Strength limit (MPa)",
                "calculated_stress" to "Calculated Stress",
                "convert_val" to "Value to Convert",
                "converted" to "Result",
                "feedback_title" to "Submit Feedback",
                "feedback_hint" to "Your technical review message about this ...",
                "submit" to "Submit",
                "settings_headline" to "Application Settings",
                "language" to "App Language",
                "theme" to "Dark Theme",
                "saved_bookmarks" to "My Bookmarked Cards",
                "no_bookmarks" to "No technical cards saved yet.",
                "about_body" to "EngineRef Pro is an independent offline reference handbook designed for mechanical and welding engineers in the field.\nCalculations and technical values are based on DIN 13-1, ISO 724 and AWS standards.",
                "all" to "All",
                "steel" to "Steel",
                "aluminum" to "Aluminum",
                "stainless" to "Stainless",
                "cast_iron" to "Cast Iron"
            ),
            "DE" to mapOf(
                "app_title" to "EngineRef Pro",
                "subtitle" to "Technisches Handbuch",
                "nav_home" to "Start",
                "nav_library" to "Bibliothek",
                "nav_calc" to "Rechner",
                "nav_ai" to "KI Chat",
                "nav_settings" to "Einstell.",
                "search_hint" to "Suchen (Gewinde, Schweißcode...)",
                "ai_headline" to "KI Ingenieur-Assistent",
                "ai_sub" to "Gemini-basiert · Online",
                "ai_input_hint" to "Stellen Sie Ihre technische Frage...",
                "ai_typing" to "Assistent schreibt...",
                "modules" to "MODULE",
                "drill_delik" to "Gewindebohren",
                "drill_sub" to "Metrische Vorbohrdurchmesser",
                "welding_methods" to "Schweißtechnik",
                "welding_sub" to "Vergleich von MIG, WIG, E-Hand",
                "bolt_torque" to "Schrauben & Drehmoment",
                "bolt_sub" to "Durchmesser, Steigung & Grenzwerte",
                "calculations" to "Berechnungen",
                "calculations_sub" to "Drehmoment, Wärmeeintrag, Belastung",
                "welding_codes" to "Schweißnormen",
                "welding_codes_sub" to "AWS, EN/ISO Elektrodenindex",
                "materials" to "Werkstoffe",
                "materials_sub" to "Stahl, Edelstahl, Guss, Alu",
                "tap_drill_title" to "Metrische Gewindebohrtabellen",
                "tap_drill_sub" to "Nach DIN 76 / ISO 724 Standards",
                "material_filter" to "WERKSTOFF FILTER",
                "torque_grade" to "Schraubenklasse",
                "lubrication" to "Schmierung",
                "lubed" to "Stahl geschmiert (K=0.15)",
                "dry" to "Stahl trocken / Standard (K=0.20)",
                "galvanized" to "Verzinkt (K=0.18)",
                "al_lubed" to "Aluminium geölt (K=0.16)",
                "axial_tension" to "Vorspannkraft (Axial - kN)",
                "calculate" to "Berechnen",
                "result" to "BERECHNUNGERGEBNIS",
                "recommended_torque" to "Empfohlenes Drehmoment",
                "safety_factor" to "Sicherheitsbeiwert",
                "welding_compare" to "Schweißverfahren vergleichen",
                "weld_sub" to "Methode nach Werkstoff und Position wählen",
                "method" to "Methode",
                "quality" to "Qualität",
                "speed" to "Geschwind.",
                "cost" to "Kosten",
                "difficulty" to "Schwierigkeit",
                "thickness_range" to "Blechdicke",
                "advantages" to "Vorteile",
                "disadvantages" to "Nachteile",
                "recommended_materials" to "Empfohlene Metalle",
                "positions" to "Schweißpositionen",
                "efficiency" to "Wirkungsgrad",
                "electrode_db" to "Elektroden-Datenbank",
                "electrode_sub" to "AWS A5.1 / ISO Eigenschaften",
                "tensile_strength" to "Zugfestigkeit",
                "usage_application" to "Anwendungsbereiche",
                "torque_calc_title" to "Drehmoment-Rechner",
                "torque_calc_descr" to "Anzugsdrehmoment nach Klasse und Schmierung.",
                "heat_input_title" to "Wärmeeintrag-Rechner",
                "heat_input_descr" to "Wärmeeintrag (Q = η * U * I * 60 / (1000 * S))",
                "stress_title" to "Spannung & Sicherheit",
                "stress_descr" to "Einfache Zug-/Druckspannung berechnen.",
                "unit_converter" to "Einheitenumrechner",
                "unit_descr" to "Metrische und imperiale Konvertierung.",
                "voltage" to "Schweißspannung (V - Volt)",
                "current" to "Schweißstrom (I - Ampere)",
                "travel_speed" to "Schweißgeschwind. (S - mm/min)",
                "welding_process" to "Wirkungsgrad",
                "heat_result" to "Wärmeeintrag (Q)",
                "stress_force" to "Angewandte Kraft (F - kN)",
                "bar_radius" to "Stabdurchmesser (d - mm)",
                "yield_strength" to "Streckgrenze (MPa)",
                "calculated_stress" to "Berechnete Spannung",
                "convert_val" to "Wert zum Umrechnen",
                "converted" to "Ergebnis",
                "feedback_title" to "Feedback senden",
                "feedback_hint" to "Technische Kommentare zu diesem modul...",
                "submit" to "Senden",
                "settings_headline" to "Einstellungen",
                "language" to "Sprache",
                "theme" to "Dunkles Design",
                "saved_bookmarks" to "Gespeicherte technische Karten",
                "no_bookmarks" to "Noch keine Karten gespeichert.",
                "about_body" to "EngineRef Pro ist ein eigenständiges Offline-Referenzhandbuch für Maschinenbau- und Schweißingenieure.\nBerechnungen basieren auf DIN 13-1, ISO 724 und AWS-Normen.",
                "all" to "Alle",
                "steel" to "Stahl",
                "aluminum" to "Aluminium",
                "stainless" to "Edelstahl",
                "cast_iron" to "Guss"
            )
        )
        return strings[appLanguage.value]?.get(key) ?: strings["EN"]?.get(key) ?: key
    }
}
