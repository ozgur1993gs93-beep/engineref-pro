package com.example.repository

import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class EngineRepository(private val db: AppDatabase) {

    private val tapDrillDao = db.tapDrillDao()
    private val weldingDao = db.weldingMethodDao()
    private val electrodeDao = db.electrodeDao()
    val calculationHistoryDao = db.calculationHistoryDao()
    val bookmarkDao = db.bookmarkDao()
    val feedbackDao = db.feedbackDao()

    // Read queries
    fun getTapDrills(): Flow<List<TapDrillEntity>> = tapDrillDao.getAllDrills()
    fun searchTapDrills(query: String): Flow<List<TapDrillEntity>> = tapDrillDao.searchDrills(query)
    fun getWeldingMethods(): Flow<List<WeldingMethodEntity>> = weldingDao.getAllMethods()
    fun getElectrodes(): Flow<List<ElectrodeEntity>> = electrodeDao.getAllElectrodes()
    fun searchElectrodes(query: String): Flow<List<ElectrodeEntity>> = electrodeDao.searchElectrodes(query)
    fun getCalculationHistory(): Flow<List<CalculationHistoryEntity>> = calculationHistoryDao.getHistory()
    fun getBookmarks(): Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    // Write queries
    suspend fun addCalculationHistory(historyItem: CalculationHistoryEntity) = withContext(Dispatchers.IO) {
        calculationHistoryDao.insertHistory(historyItem)
    }

    suspend fun clearCalculationHistory() = withContext(Dispatchers.IO) {
        calculationHistoryDao.clearHistory()
    }

    suspend fun addBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(category: String, referenceId: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(category, referenceId)
    }

    fun isBookmarked(category: String, referenceId: Int): Flow<Boolean> {
        return bookmarkDao.isBookmarked(category, referenceId)
    }

    suspend fun addFeedback(feedback: FeedbackEntity) = withContext(Dispatchers.IO) {
        feedbackDao.insertFeedback(feedback)
    }

    // Prefill the database on initial start (offline-first sync)
    suspend fun ensureDataInitialized() = withContext(Dispatchers.IO) {
        try {
            val drillsCount = tapDrillDao.getDrillsCount()
        if (drillsCount == 0) {
            val tapDrillList = listOf(
                TapDrillEntity(
                    size = "M3", pitch = 0.5, drillSize = 2.5,
                    torque88 = 1.3, torque109 = 1.9, torque129 = 2.2,
                    materialType = "Çelik / Paslanmaz", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M4", pitch = 0.7, drillSize = 3.3,
                    torque88 = 3.0, torque109 = 4.4, torque129 = 5.1,
                    materialType = "Çelik / Paslanmaz", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M5", pitch = 0.8, drillSize = 4.2,
                    torque88 = 6.0, torque109 = 8.7, torque129 = 10.0,
                    materialType = "Çelik / Paslanmaz", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M6", pitch = 1.0, drillSize = 5.0,
                    torque88 = 10.3, torque109 = 15.0, torque129 = 18.0,
                    materialType = "Çelik / Döküm", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M8", pitch = 1.25, drillSize = 6.8,
                    torque88 = 25.0, torque109 = 36.0, torque129 = 43.0,
                    materialType = "Çelik / Döküm", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M10", pitch = 1.5, drillSize = 8.5,
                    torque88 = 49.0, torque109 = 72.0, torque129 = 84.0,
                    materialType = "Döküm / Alüminyum", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M12", pitch = 1.75, drillSize = 10.2,
                    torque88 = 85.0, torque109 = 125.0, torque129 = 145.0,
                    materialType = "Alüminyum / Döküm", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M14", pitch = 2.0, drillSize = 12.0,
                    torque88 = 135.0, torque109 = 200.0, torque129 = 235.0,
                    materialType = "Çelik / Paslanmaz", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M16", pitch = 2.0, drillSize = 14.0,
                    torque88 = 210.0, torque109 = 310.0, torque129 = 365.0,
                    materialType = "Paslanmaz / Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M20", pitch = 2.5, drillSize = 17.5,
                    torque88 = 410.0, torque109 = 610.0, torque129 = 715.0,
                    materialType = "Paslanmaz / Alüminyum", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M24", pitch = 3.0, drillSize = 21.0,
                    torque88 = 710.0, torque109 = 1050.0, torque129 = 1220.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M30", pitch = 3.5, drillSize = 26.5,
                    torque88 = 1450.0, torque109 = 2100.0, torque129 = 2450.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M36", pitch = 4.0, drillSize = 32.0,
                    torque88 = 2500.0, torque109 = 3700.0, torque129 = 4300.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M42", pitch = 4.5, drillSize = 37.5,
                    torque88 = 4000.0, torque109 = 5800.0, torque129 = 6800.0,
                    materialType = "Döküm / Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M48", pitch = 5.0, drillSize = 43.0,
                    torque88 = 6000.0, torque109 = 8900.0, torque129 = 10400.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M56", pitch = 5.5, drillSize = 50.5,
                    torque88 = 9500.0, torque109 = 13900.0, torque129 = 16300.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                ),
                TapDrillEntity(
                    size = "M64", pitch = 6.0, drillSize = 58.0,
                    torque88 = 14000.0, torque109 = 20500.0, torque129 = 24000.0,
                    materialType = "Çelik", standard = "DIN 13-1"
                )
            )
            tapDrillDao.insertDrills(tapDrillList)
        }

        val methodsCount = weldingDao.getMethodsCount()
        if (methodsCount == 0) {
            val weldingMethodsList = listOf(
                WeldingMethodEntity(
                    codeName = "GTAW (TIG)",
                    fullNameTr = "Tungsten Inert Gaz Kaynağı (TIG / Argon)",
                    fullNameEn = "Gas Tungsten Arc Welding (TIG)",
                    fullNameDe = "Wolfram-Inertgasschweißen (WIG)",
                    principleTr = "Erimeyen tungsten elektrot ile koruyucu inert gaz altında yapılan pürüzsüz ve yüksek nüfuziyetli ark kaynağı yöntemidir.",
                    principleEn = "An arc welding process that uses a non-consumable tungsten electrode to produce the weld with an inert shielding gas like Argon.",
                    principleDe = "Ein Lichtbogenschweißverfahren mit nicht abschmelzender Wolframelektrode unter inerten Schutzgasen.",
                    advantagesTr = "Üstün kalite, ince saclarda mükemmel kontrol, sıçramasız ve cürufsuz pürüzsüz dikişler.",
                    advantagesEn = "Extremely high quality, precise heat control on thin sheets, slag-free clean weld profile.",
                    advantagesDe = "Hervorragende Qualität, präzise Wärmeeinbringung, schlackenfreie und saubere Schweißnähte.",
                    disadvantagesTr = "Yavaş kaynak hızı, yüksek operatör tecrübesi gereksinimi, açık rüzgarlı alanlara uygunsuzluk.",
                    disadvantagesEn = "Slow welding speed, requires high operator skill level, sensitive to drafts / wind.",
                    disadvantagesDe = "Niedrige Schweißgeschwindigkeit, erfordert hohe Qualifikation, windempfindlich.",
                    recommendedMaterialsTr = "Paslanmaz çelik, Alüminyum alaşımları, Alaşımlı ince çelikler, Titanyum.",
                    recommendedMaterialsEn = "Stainless steel, Aluminum alloys, Nickel alloys, Thin steel sheets, Titanium.",
                    recommendedMaterialsDe = "Edelstahl, Aluminiumlegierungen, Nickellegierungen, Dünnbleche, Titan.",
                    positionCompatibilityTr = "Tüm pozisyonlar (PA, PB, PC, PD, PE, PF)",
                    positionCompatibilityEn = "All positions (Flat, Horizontal, Vertical Up, Overhead)",
                    positionCompatibilityDe = "Alle Schweißpositionen (PA, PB, PC, PD, PE, PF)",
                    costTr = "Yüksek (Ekipman ve koruyucu gaz maliyeti yüksek)",
                    costEn = "High (Expensive equipment, consumables, and shielding gas)",
                    costDe = "Hoch (Hohe Gerätekosten und teure Gase)",
                    efficiencyPercent = 60,
                    qualityRatingTr = "Mükemmel",
                    qualityRatingEn = "Excellent",
                    qualityRatingDe = "Exzellent",
                    thicknessRangeTr = "0.5 mm - 6 mm arası şampiyon",
                    thicknessRangeEn = "0.5 mm to 6 mm (Ideal for thin metals)",
                    thicknessRangeDe = "0.5 mm bis 6 mm (ideal für Dünnblech)"
                ),
                WeldingMethodEntity(
                    codeName = "GMAW (MIG/MAG)",
                    fullNameTr = "Gazaltı Metal Ark Kaynağı (MIG / MAG)",
                    fullNameEn = "Gas Metal Arc Welding (MIG / MAG)",
                    fullNameDe = "Metall-Schutzgasschweißen (MSG)",
                    principleTr = "Otomatik sürülen eriyen çıplak telin aktif veya soy gaz koruması altında sürekli beslenerek yapıldığı yüksek verimli kaynak türü.",
                    principleEn = "An arc welding process that continuously feeds a consumable solid wire through a welding gun, protected by active or inert shielding gases.",
                    principleDe = "Lichtbogenschweißen mit kontinuierlich zugeführtem Schweißdraht unter inertem (MIG) oder aktivem (MAG) Schutzgas.",
                    advantagesTr = "Çok yüksek hız ve üretim verimliliği, kolay öğrenilebilirlik, cüruf temizliği gerektirmez, derin nüfuziyet.",
                    advantagesEn = "Very high speed and productivity, easy to learn, no slag cleanup required, excellent penetration control.",
                    advantagesDe = "Sehr hohe Schweißgeschwindigkeit, leicht erlernbar, keine Schlackenbeseitigung nötig, tiefes Einbringen.",
                    disadvantagesTr = "Rüzgardan kolay etkilenir, kaynak ağzında pas/yağ varsa hata riskini artırır, ağır şantiye ortamında zor taşınır.",
                    disadvantagesEn = "Sensitive to welding drafts, potential wire feeding issues, requires clean base material surfaces.",
                    disadvantagesDe = "Windempfindlich, korrosionsanfällig bei unsauberen Werkstücken, begrenzte Werkstattmobilität.",
                    recommendedMaterialsTr = "Karbon çelikleri, Alüminyum alaşımları, Paslanmaz çelikler.",
                    recommendedMaterialsEn = "Carbon steels, Structural steels, Stainless steels, Aluminum alloys.",
                    recommendedMaterialsDe = "Baustähle, Kohlenstoffstähle, Edelstähle, Aluminiumlegierungen.",
                    positionCompatibilityTr = "Tüm pozisyonlar (Sprey ark hariç)",
                    positionCompatibilityEn = "All positions (excluding raw spray transfer vertical)",
                    positionCompatibilityDe = "Alle Positionen (außer Spraylichtbogen fallend)",
                    costTr = "Orta (Yüksek tüketim malzemesi debisi)",
                    costEn = "Medium (Fast wire consumption and gas bottle rentals)",
                    costDe = "Mittel (Hoher Drahtverbrauch und Gasflaschenmiete)",
                    efficiencyPercent = 90,
                    qualityRatingTr = "İyi / Çok İyi",
                    qualityRatingEn = "Good / Very Good",
                    qualityRatingDe = "Gut / Sehr Gut",
                    thicknessRangeTr = "1.2 mm - 20 mm ve üzeri",
                    thicknessRangeEn = "1.2 mm to 20 mm or thicker",
                    thicknessRangeDe = "1.2 mm bis über 20 mm"
                ),
                WeldingMethodEntity(
                    codeName = "SMAW (Elektrot)",
                    fullNameTr = "Örtülü Elektrot Ark Kaynağı (SMAW / MMA)",
                    fullNameEn = "Shielded Metal Arc Welding (SMAW / Stick)",
                    fullNameDe = "Lichtbogenhandschweißen (E-Hand)",
                    principleTr = "Flux (örtü) ile kaplı eriyen elektrot yardımıyla yapılan, cüruf koruması oluşturan en klasik ark kaynağı.",
                    principleEn = "A manual arc welding process that uses a consumable electrode covered with a flux to lay the weld, yielding solid slag protection.",
                    principleDe = "Klassisches Handschweißverfahren mit abschmelzender, umhüllter Stabelektrode.",
                    advantagesTr = "Açık havada / rüzgarda rahat kaynak yapabilme, yüksek mobilite, gaz tüpü gerektirmez, ucuz ekipman.",
                    advantagesEn = "Excellent for outdoor use and high wind, supreme portability without heavy gas cylinders, inexpensive setup.",
                    advantagesDe = "Sehr gut im Freien und bei Wind einsetzbar, hohe Mobilität ohne Gasflaschen, günstige Anschaffung.",
                    disadvantagesTr = "Sürekli dur-kalk (elektrot değiştirme), yoğun cüruf temizliği gerekir, düşük ark süresi verimliliği.",
                    disadvantagesEn = "Low productivity due to changing rod stubs, requires meticulous slag removal and grinding, high spatter.",
                    disadvantagesDe = "Geringe Produktivität durch Elektrodenwechsel, aufwendige Schlackenentfernung, hoher Spritzeranteil.",
                    recommendedMaterialsTr = "Karbon çelikleri, Düşük alaşımlı yapılar, Dökme demir imalatları.",
                    recommendedMaterialsEn = "Mild carbon steels, Structural steels, Cast irons, Maintenance alloys.",
                    recommendedMaterialsDe = "Baustähle, Kohlenstoffstähle, Gusseisen, Legierungsstähle für Reparaturen.",
                    positionCompatibilityTr = "Tüm pozisyonlar (Elektrot tipine bağlı)",
                    positionCompatibilityEn = "All positions (depending on electrode flux coatings)",
                    positionCompatibilityDe = "Alle Positionen (abhängig von der Elektrodenumhüllung)",
                    costTr = "Düşük (En ekonomik kaynak yatırım maliyeti)",
                    costEn = "Low (Highly affordable machinery and consumables)",
                    costDe = "Gering (Sehr günstige Maschinenschaffung)",
                    efficiencyPercent = 65,
                    qualityRatingTr = "Orta / İyi",
                    qualityRatingEn = "Medium / Good",
                    qualityRatingDe = "Mittel / Gut",
                    thicknessRangeTr = "2 mm - 30 mm ve üzeri kalın kesitler",
                    thicknessRangeEn = "2 mm to 30 mm (Popular in field constructions)",
                    thicknessRangeDe = "2 mm bis 30 mm (Standard im Rohrleitungsbau)"
                ),
                WeldingMethodEntity(
                    codeName = "FCAW (Özlü Tel)",
                    fullNameTr = "Özlü Tel Ark Kaynağı (FCW)",
                    fullNameEn = "Flux-Cored Arc Welding (FCAW)",
                    fullNameDe = "Fülldrahtschweißen",
                    principleTr = "İçi koruyucu ark tozu ile doldurulmuş özel telin erimesi esnasıyla gazlı veya gazsız yapılan kaynak çeşididir.",
                    principleEn = "An automatic or semi-automatic process with a tubular wire containing flux, optionally requiring external gas, for rapid deposition rates.",
                    principleDe = "Ein Schweißverfahren unter Verwendung eines schlackenbildenden Fülldrahts mit oder ohne externes Schutzgas.",
                    advantagesTr = "Aşırı yüksek dolgu metal hızı (MIG'den de hızlı), kalın yapı metallerinde mukavemetli dolgular sağlar.",
                    advantagesEn = "Extreme metal deposition rates, excellent sidewall fusion, suitable for heavy structural thick steels.",
                    advantagesDe = "Extrem hohe Abschmelzleistung, hervorragende Nahtverschmelzung, perfekt für dicken Stahlbau.",
                    disadvantagesTr = "Ark kararlılığı zayıftır, duman emisyonu çok yüksektir, yüzey cürufunun temizlenmesi gerekir.",
                    disadvantagesEn = "Produces high amount of welding fumes and smoke, requires slag chipping, expensive wire consumables.",
                    disadvantagesDe = "Erzeugt viel Rauch und Schweißgase, Schlackenreinigung erforderlich, teurerer Fülldraht.",
                    recommendedMaterialsTr = "Kalın karbon çelikleri, Yapısal çelik konstrüksiyonlar, Sert dolgular.",
                    recommendedMaterialsEn = "Thick carbon steels, Offshore structural steel, Hardfacing operations.",
                    recommendedMaterialsDe = "Dicke Kohlenstoffstähle, Offshore-Stahlkonstruktionen, Auftragschweißen.",
                    positionCompatibilityTr = "Tüm pozisyonlar (Esnek)",
                    positionCompatibilityEn = "All positions (Flexible)",
                    positionCompatibilityDe = "Alle Positionen (Flexibel)",
                    costTr = "Orta / Yüksek (Özel tel sarf maliyeti)",
                    costEn = "Medium / High (Fluxed core wire is costly to manufacture)",
                    costDe = "Mittel / Hoch (Fülldrähte sind teurer in der Herstellung)",
                    efficiencyPercent = 85,
                    qualityRatingTr = "İyi",
                    qualityRatingEn = "Good",
                    qualityRatingDe = "Gut",
                    thicknessRangeTr = "4 mm - 50 mm arası kritik montajlar",
                    thicknessRangeEn = "4 mm to 50 mm (Heavy duty machinery joints)",
                    thicknessRangeDe = "4 mm bis 50 mm (Schwerer Stahlbau)"
                ),
                WeldingMethodEntity(
                    codeName = "SAW (Tozaltı)",
                    fullNameTr = "Tozaltı Ark Kaynağı (SAW)",
                    fullNameEn = "Submerged Arc Welding (SAW)",
                    fullNameDe = "Unterpulverschweißen (UP)",
                    principleTr = "Arkın dökme toz tabakası altında tamamen gizlenerek oluştuğu, sıçramasız, dumansız tam otomatik endüstriyel proses.",
                    principleEn = "The welding arc is completely submerged under a granular blanket of fusible flux, resulting in zero visible spatter or radiation.",
                    principleDe = "Der Lichtbogen brennt unsichtbar unter einer körnigen Pulverschicht. Spritzerfreier, vollautomatischer Prozess.",
                    advantagesTr = "En yüksek kaynak penetrasyon derinliği ve temiz dikiş, ışık/radyasyon sızdırmaz, sıfır sıçrama, mükemmel dikiş.",
                    advantagesEn = "Highest deposition rates and deepest penetration, zero visible arc light / radiation, superb mechanics.",
                    advantagesDe = "Höchste Abschmelzleistung, kein Schweißlicht sichtbar, spritzerfrei, hervorragende mechanische Gütewerte.",
                    disadvantagesTr = "Sadece düz ve yatay pozisyonlarda yapılabilir (toz döküleceği için), toz kurtarma üniteleri gereksinimi.",
                    disadvantagesEn = "Strictly limited to flat and horizontal fillet positions (due to gravity flux spill), requires flux recovery systems.",
                    disadvantagesDe = "Nur in flacher und horizontaler Position anwendbar (Schweißpulver rieselt ab), Pulverabsauganlage erforderlich.",
                    recommendedMaterialsTr = "Kalın kazan sacları, Basınçlı boru imalatları, Ağır köprü profilleri.",
                    recommendedMaterialsEn = "Heavy steel plates, Pressure vessels, Ship hulls, Thick pipe manufacturing.",
                    recommendedMaterialsDe = "Schwere Stahlplatten, Druckbehälter, Schiffbau, Großrohrherstellung.",
                    positionCompatibilityTr = "Sadece Düz ve Yatay Pozisyon (PA, PB)",
                    positionCompatibilityEn = "Flat and Horizontal Fillet positions only (PA, PB)",
                    positionCompatibilityDe = "Nur Flach- und Horizontalpositionen (PA, PB)",
                    costTr = "Yüksek (Kurulum otomasyon yatırımı gereklidir)",
                    costEn = "High (Requires robust automation gantry systems)",
                    costDe = "Hoch (Erfordert stabile Portalanlagen und Automatisierung)",
                    efficiencyPercent = 98,
                    qualityRatingTr = "Mükemmel / Üstün",
                    qualityRatingEn = "Outstanding / X-Ray Clear",
                    qualityRatingDe = "Hervorragend / Röntgensicher",
                    thicknessRangeTr = "6 mm - 100 mm ve üzeri ağır plakalar",
                    thicknessRangeEn = "6 mm to over 100 mm (Heavy structural columns)",
                    thicknessRangeDe = "6 mm bis über 100 mm (Dickbleche)"
                )
            )
            weldingDao.insertMethods(weldingMethodsList)
        }

        val electrodesCount = electrodeDao.getElectrodesCount()
        if (electrodesCount == 0) {
            val electrodeList = listOf(
                ElectrodeEntity(
                    awsCode = "E6013",
                    isoCode = "EN ISO 2560-A: E 38 0 RC 11",
                    typeTr = "Rutil Örtülü Elektrot",
                    typeEn = "Rutile Coated Electrode",
                    typeDe = "Rutilumhüllte Stabelektrode",
                    applicationTr = "Genel çelik konstrüksiyon, boru imalatı ve şasi montajı. Kolay ark başlangıcı ve pürüzsüz kaynak dumanı ile bilinir.",
                    applicationEn = "General steel fabrication, sheet metal work, structural joints. Highly popular for easy arc re-striking and smooth arc stability.",
                    applicationDe = "Allgemeiner Stahlbau, Blecharbeiten, Rohrleitungen. Sehr leicht zündbar, stabiler Lichtbogen, einfache Schlackeentfernung.",
                    tensileStrengthTr = "430 - 510 MPa",
                    tensileStrengthEn = "430 - 510 MPa",
                    tensileStrengthDe = "430 - 510 MPa"
                ),
                ElectrodeEntity(
                    awsCode = "E7018",
                    isoCode = "EN ISO 2560-A: E 42 5 B 42 H10",
                    typeTr = "Bazik Örtülü Elektrot (Düşük Hidrojen)",
                    typeEn = "Basic Coated Electrode (Low Hydrogen)",
                    typeDe = "Basisch umhüllte Stabelektrode (Wasserstoffarm)",
                    applicationTr = "Dinamik yük binen ağır çelik yapılar, basınçlı tanklar, köprüler. Çatlamaya karşı yüksek mukavemet ve tokluk sunar.",
                    applicationEn = "Highly dynamic loaded structural assembly, bridges, pressure vessels. Excellent fracture toughness and low diffusible hydrogen.",
                    applicationDe = "Dynamisch beanspruchte Konstruktionen, Brücken, Druckbehälter. Hohe Zähigkeit, rissfeste Schweißnähte.",
                    tensileStrengthTr = "490 - 590 MPa",
                    tensileStrengthEn = "490 - 590 MPa",
                    tensileStrengthDe = "490 - 590 MPa"
                ),
                ElectrodeEntity(
                    awsCode = "ER70S-6",
                    isoCode = "EN ISO 14341-A: G 42 4 M21 3Si1",
                    typeTr = "Gazaltı Masif Kaynak Teli",
                    typeEn = "Solid Gas Metal Arc Wire (MIG/MAG)",
                    typeDe = "Massivdrahtelektrode für MSG-Schweißen",
                    applicationTr = "MAG kaynağı için karbon çelik telleri. Çelik kaplar, tır kasaları ve seri imalat montaj hatlarında %100 Argon / CO2 koruma ile kullanılır.",
                    applicationEn = "Carbon steel solid wire for general MAG welding. Used with Argon/CO2 shielding gas mixtures for vehicles, sheet fabrications, and robots.",
                    applicationDe = "Massivdraht für das Schweißen von unlegierten Stählen. Bestens geeignet für Karosseriebau, Behälterbau unter Mischgas.",
                    tensileStrengthTr = "480 - 580 MPa",
                    tensileStrengthEn = "480 - 580 MPa",
                    tensileStrengthDe = "480 - 580 MPa"
                ),
                ElectrodeEntity(
                    awsCode = "E308L-16",
                    isoCode = "EN ISO 3581-A: E 19 9 L R 12",
                    typeTr = "Paslanmaz Çelik Örtülü Elektrot",
                    typeEn = "Stainless Steel Rutile Coated Electrode",
                    typeDe = "CrNi-Stabelektrode für Edelstahl",
                    applicationTr = "AISI 304, 304L, 321 ve 347 tipi paslanmaz çeliklerin korozyona dayanıklı dolgu kaynakları ve birleştirmeleri.",
                    applicationEn = "Joining and overlaying of low-carbon AISI 304, 304L stainless steels. Imparts exceptional high temperature scaling resistance.",
                    applicationDe = "Schweißen von korrosionsbeständigen CrNi-Stählen wie 1.4301 (AISI 304) und 1.4306 (304L). Hochglanzglatte Nähte.",
                    tensileStrengthTr = "520 - 620 MPa",
                    tensileStrengthEn = "520 - 620 MPa",
                    tensileStrengthDe = "520 - 620 MPa"
                )
            )
            electrodeDao.insertElectrodes(electrodeList)
        }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
