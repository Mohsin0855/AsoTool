package com.example.asotool.data.remote

import android.util.Log
import com.example.asotool.domain.model.ASOScore
import com.example.asotool.domain.model.CloneFeasibility
import com.example.asotool.domain.model.Factors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateASORecommendations(
        title: String,
        developer: String,
        category: String,
        description: String,
        rating: Double,
        reviewCount: Int,
        installs: String,
        keywords: List<String>
    ): List<String> = withContext(Dispatchers.IO) {
        val prompt = buildRecommendationsPrompt(title, developer, category, description, rating, reviewCount, installs, keywords)
        
        try {
            val response = callOpenAI(prompt)
            parseRecommendationsResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "AI recommendations failed: ${e.message}")
            getDefaultRecommendations(title, rating, description.length)
        }
    }

    suspend fun generateCloneFeasibilityAnalysis(
        title: String,
        category: String,
        description: String,
        rating: Double,
        reviewCount: Int,
        installs: String,
        keywords: List<String>
    ): CloneFeasibility = withContext(Dispatchers.IO) {
        val prompt = buildCloneFeasibilityPrompt(title, category, description, rating, reviewCount, installs, keywords)
        
        try {
            val response = callOpenAI(prompt)
            parseCloneFeasibilityResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "AI clone feasibility failed: ${e.message}")
            getDefaultCloneFeasibility(category, rating, installs, keywords)
        }
    }

    suspend fun generateASOScore(
        title: String,
        description: String,
        rating: Double,
        reviewCount: Int,
        installs: String
    ): ASOScore = withContext(Dispatchers.IO) {
        val prompt = buildASOScorePrompt(title, description, rating, reviewCount, installs)
        
        try {
            val response = callOpenAI(prompt)
            parseASOScoreResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "AI ASO score failed: ${e.message}")
            getDefaultASOScore(title, description, rating, reviewCount)
        }
    }

    private fun buildRecommendationsPrompt(
        title: String, developer: String, category: String, description: String,
        rating: Double, reviewCount: Int, installs: String, keywords: List<String>
    ) = """
You are an expert ASO consultant. Analyze this Android app and provide 8 specific, actionable recommendations.

App: $title by $developer
Category: $category | Rating: $rating | Reviews: $reviewCount | Installs: $installs
Keywords: ${keywords.joinToString(", ")}
Description: ${description.length} chars

Provide exactly 8 specific ASO recommendations as a JSON array:
["Recommendation 1", "Recommendation 2", ...]
Only output JSON.
    """.trimIndent()

    private fun buildCloneFeasibilityPrompt(
        title: String, category: String, description: String,
        rating: Double, reviewCount: Int, installs: String, keywords: List<String>
    ) = """
Analyze clone feasibility for: $title
Category: $category | Rating: $rating | Reviews: $reviewCount | Installs: $installs
Keywords: ${keywords.joinToString(", ")}

Provide JSON:
{"score": 1-10, "difficulty": "Easy/Medium/Hard", "recommendation": "...", "technical": "...", "demand": "...", "barrier": "...", "uniqueFeatures": ["f1","f2","f3"]}
Only output JSON.
    """.trimIndent()

    private fun buildASOScorePrompt(
        title: String, description: String, rating: Double, reviewCount: Int, installs: String
    ) = """
Score ASO performance for: "$title" (${title.length} chars)
Description: ${description.length} chars | Rating: $rating | Reviews: $reviewCount | Installs: $installs

Score 0-100: titleOptimization, descriptionQuality, keywordDensity, visualAssets, userEngagement
{"overall": X, "breakdown": {"titleOptimization": X, "descriptionQuality": X, "keywordDensity": X, "visualAssets": X, "userEngagement": X}}
Only output JSON.
    """.trimIndent()

    private fun callOpenAI(prompt: String): String {
        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().put("role", "system").put("content", "You are an ASO analyst. Respond with valid JSON only."))
                put(JSONObject().put("role", "user").put("content", prompt))
            })
            put("temperature", 0.7)
            put("max_tokens", 800)
        }

        val request = Request.Builder()
            .url(API_URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API error: ${response.code}")
            
            val content = JSONObject(response.body?.string() ?: "")
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
            
            return content.replace("```json", "").replace("```", "").trim()
        }
    }

    private fun parseRecommendationsResponse(response: String): List<String> {
        return try {
            val arr = JSONArray(response)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) { emptyList() }
    }

    private fun parseCloneFeasibilityResponse(response: String): CloneFeasibility {
        val json = JSONObject(response)
        val features = json.optJSONArray("uniqueFeatures")?.let { arr ->
            (0 until arr.length()).map { arr.getString(it) }
        } ?: listOf("Feature 1", "Feature 2", "Feature 3")
        
        return CloneFeasibility(
            score = json.optInt("score", 5),
            difficulty = json.optString("difficulty", "Medium"),
            recommendation = json.optString("recommendation", "Analysis unavailable"),
            factors = Factors(
                technical = json.optString("technical", "Medium complexity"),
                demand = json.optString("demand", "Moderate demand"),
                barrier = json.optString("barrier", "Moderate barrier"),
                uniqueFeatures = features
            )
        )
    }

    private fun parseASOScoreResponse(response: String): ASOScore {
        val json = JSONObject(response)
        val breakdown = json.optJSONObject("breakdown") ?: JSONObject()
        
        return ASOScore(
            overall = json.optInt("overall", 70),
            breakdown = mapOf(
                "titleOptimization" to breakdown.optInt("titleOptimization", 70),
                "descriptionQuality" to breakdown.optInt("descriptionQuality", 70),
                "keywordDensity" to breakdown.optInt("keywordDensity", 70),
                "visualAssets" to breakdown.optInt("visualAssets", 75),
                "userEngagement" to breakdown.optInt("userEngagement", 70)
            )
        )
    }

    private fun getDefaultRecommendations(title: String, rating: Double, descLength: Int): List<String> = listOf(
        if (title.length < 30) "Expand title to 30-50 characters with keywords" else "Title length is optimal",
        if (descLength < 2500) "Increase description to 2500+ characters" else "Description length is good",
        if (rating < 4.5) "Focus on improving ratings above 4.5 stars" else "Maintain high ratings",
        "Localize for top markets (US, India, Brazil)",
        "Update screenshots quarterly",
        "A/B test icon designs",
        "Implement smart review prompts",
        "Monitor competitor keywords"
    )

    private fun getDefaultCloneFeasibility(
        category: String, rating: Double, installs: String, keywords: List<String>
    ): CloneFeasibility {
        val installNum = installs.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        val score = when { installNum > 100000000 -> 3; installNum > 10000000 -> 5; else -> 7 }
        
        return CloneFeasibility(
            score = score,
            difficulty = if (score >= 7) "Easy" else if (score >= 5) "Medium" else "Hard",
            recommendation = "Based on market analysis, this represents a ${if (score >= 7) "good" else "challenging"} opportunity.",
            factors = Factors(
                technical = "Standard mobile development",
                demand = if (installNum > 1000000) "High demand" else "Moderate demand",
                barrier = if (rating > 4.5) "Strong incumbent" else "Moderate competition",
                uniqueFeatures = keywords.take(3).map { "${it.replaceFirstChar { c -> c.uppercase() }} feature" }
            )
        )
    }

    private fun getDefaultASOScore(title: String, description: String, rating: Double, reviews: Int): ASOScore {
        val titleScore = if (title.length in 30..50) 85 else 65
        val descScore = if (description.length > 2500) 85 else if (description.length > 1500) 70 else 55
        val engScore = if (rating >= 4.5 && reviews > 10000) 90 else if (rating >= 4.0) 75 else 60
        
        return ASOScore(
            overall = (titleScore + descScore + 70 + 75 + engScore) / 5,
            breakdown = mapOf(
                "titleOptimization" to titleScore,
                "descriptionQuality" to descScore,
                "keywordDensity" to 70,
                "visualAssets" to 75,
                "userEngagement" to engScore
            )
        )
    }

    companion object {
        private const val TAG = "OpenAIService"
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
    }
}
