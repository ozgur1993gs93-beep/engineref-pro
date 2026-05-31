package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    suspend fun askAssistant(
        prompt: String,
        history: List<Pair<String, String>> = emptyList() // User message -> Model message
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Hata: Gemini API Anahtarı eksik. Lütfen AI Studio'da bulunan 'Secrets' panelinden GEMINI_API_KEY anahtarınızı ekleyin.\n\nError: Gemini API Key is missing. Please add your GEMINI_API_KEY in the Secrets panel."
        }

        val systemPrompt = "Sen deneyimli bir makine ve kaynak mühendisisin. Kullanıcıların teknik sorularını kısa, net ve standartlara uygun şekilde yanıtlıyorsun. Her yanıtta ilgili ISO/DIN/AWS standardını belirt. Türkçe, Almanca ve İngilizce destekle."

        // Construct contents list with history
        val contents = mutableListOf<GeminiContent>()
        
        for ((userMsg, modelMsg) in history) {
            contents.add(GeminiContent(listOf(GeminiPart(text = userMsg))))
            contents.add(GeminiContent(listOf(GeminiPart(text = modelMsg))))
        }
        
        // Add current user prompt
        contents.add(GeminiContent(listOf(GeminiPart(text = prompt))))

        val systemInstruction = GeminiContent(listOf(GeminiPart(text = systemPrompt)))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Cevap üretilemedi. Lütfen tekrar deneyin.\n\nCould not generate content. Please try again."
        } catch (e: Exception) {
            "Bağlantı Hatası: ${e.localizedMessage ?: e.message}\nLütfen internet bağlantınızı kontrol edin."
        }
    }
}
