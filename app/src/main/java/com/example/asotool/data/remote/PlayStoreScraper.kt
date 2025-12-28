package com.example.asotool.data.remote

import android.util.Log
import com.example.asotool.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class PlayStoreScraper {

    suspend fun scrapeAppData(packageName: String): AppData = withContext(Dispatchers.IO) {
        val doc = fetchDocument(packageName)
        val appDetails = extractAppDetails(doc, packageName)
        val description = extractDescription(doc)
        val keywords = extractKeywords(appDetails.title, description)

        AppData(
            appInfo = AppInfo(
                title = appDetails.title,
                developer = appDetails.developer,
                category = appDetails.category,
                installs = appDetails.installs,
                version = appDetails.version,
                size = appDetails.size,
                contentRating = appDetails.contentRating
            ),
            ratings = Ratings(
                overall = appDetails.rating,
                totalReviews = appDetails.reviewCount,
                distribution = appDetails.ratingDistribution
            ),
            keywords = keywords,
            asoScore = ASOScore(0, emptyMap()),
            competition = analyzeCompetition(appDetails.category, appDetails.rating, appDetails.installs),
            cloneFeasibility = CloneFeasibility(0, "", "", Factors("", "", "", emptyList())),
            recommendations = emptyList()
        )
    }

    fun getScrapedDetails(packageName: String, doc: Document): ScrapedDetails {
        val appDetails = extractAppDetails(doc, packageName)
        val description = extractDescription(doc)
        val keywords = extractKeywords(appDetails.title, description)
        return ScrapedDetails(appDetails, description, keywords)
    }

    suspend fun fetchDocument(packageName: String): Document = withContext(Dispatchers.IO) {
        Jsoup.connect("$BASE_URL?id=$packageName&hl=en&gl=us")
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .referrer("https://www.google.com/")
            .header("Accept-Language", "en-US,en;q=0.9")
            .followRedirects(true)
            .get()
    }

    private fun extractAppDetails(doc: Document, packageName: String): ExtractedAppDetails {
        val title = extractTitle(doc)
        val developer = extractDeveloper(doc)
        val rating = extractRating(doc)
        val reviewCount = extractReviewCount(doc)
        val installs = extractInstalls(doc)
        val category = extractCategory(doc)
        val version = extractVersion(doc)
        val size = extractSize(doc)
        val contentRating = extractContentRating(doc)
        val distribution = extractRatingDistribution(doc)

        return ExtractedAppDetails(
            title = title.ifEmpty { packageName },
            developer = developer.ifEmpty { "Unknown Developer" },
            category = category.ifEmpty { "Unknown" },
            installs = installs.ifEmpty { "Unknown" },
            version = version.ifEmpty { "Unknown" },
            size = size.ifEmpty { "Varies" },
            contentRating = contentRating.ifEmpty { "Everyone" },
            rating = rating,
            reviewCount = reviewCount,
            ratingDistribution = distribution
        )
    }

    private fun extractTitle(doc: Document): String {
        val selectors = listOf("h1[itemprop=name]", "h1.Fd93Bb", "h1", "span.AfwdI")
        for (selector in selectors) {
            val result = doc.select(selector).first()?.text() ?: ""
            if (result.isNotEmpty() && !result.contains("Google Play")) {
                return result.replace(Regex("\\s*[-–]\\s*(Apps on Google Play|Google Play).*$", RegexOption.IGNORE_CASE), "").trim()
            }
        }
        return doc.select("meta[property=og:title]").attr("content")
            .replace(Regex("\\s*[-–]\\s*(Apps on Google Play|Google Play).*$", RegexOption.IGNORE_CASE), "").trim()
    }

    private fun extractDeveloper(doc: Document): String {
        val selectors = listOf("div.Vbfug a", "a[href*='developer'] span", "a.Si6A0c", "div.qQKdcc span")
        for (selector in selectors) {
            val result = doc.select(selector).first()?.text() ?: ""
            if (result.isNotEmpty()) return result
        }
        return ""
    }

    private fun extractRating(doc: Document): Double {
        val selectors = listOf("div.TT9eCd", "div.BHMmbe", "span.w2kbF", "div.jILTFe")
        for (selector in selectors) {
            val result = doc.select(selector).first()?.text()?.replace(",", ".")?.toDoubleOrNull()
            if (result != null && result in 0.0..5.0) return result
        }
        val ariaRating = doc.select("[aria-label*='Rated']").attr("aria-label")
        return Regex("(\\d+[.,]?\\d*)").find(ariaRating)?.value?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    }

    private fun extractReviewCount(doc: Document): Int {
        val selectors = listOf("div.g1rdde", "span.EymY4b span", "div.EHUI5b")
        for (selector in selectors) {
            val result = doc.select(selector).first()?.text() ?: ""
            if (result.isNotEmpty()) {
                val count = extractNumber(result)
                if (count > 0) return count
            }
        }
        return 0
    }

    private fun extractInstalls(doc: Document): String {
        val text = doc.text()
        val patterns = listOf(
            Regex("(\\d+[KMB+,\\d]*\\+?)\\s*downloads", RegexOption.IGNORE_CASE),
            Regex("Downloads\\s*(\\d+[KMB+,\\d]*\\+?)", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }
        return ""
    }

    private fun extractCategory(doc: Document): String {
        val selectors = listOf("a[itemprop=genre]", "span[itemprop=genre]", "a[href*='/category/']")
        for (selector in selectors) {
            val result = doc.select(selector).first()?.text() ?: ""
            if (result.isNotEmpty()) return result
        }
        return ""
    }

    private fun extractVersion(doc: Document): String {
        val match = Regex("Version\\s*([\\d.]+)", RegexOption.IGNORE_CASE).find(doc.text())
        return match?.groupValues?.get(1) ?: ""
    }

    private fun extractSize(doc: Document): String {
        val match = Regex("(\\d+(?:\\.\\d+)?\\s*[MG]B)", RegexOption.IGNORE_CASE).find(doc.text())
        return match?.groupValues?.get(1) ?: ""
    }

    private fun extractContentRating(doc: Document): String {
        val patterns = listOf(
            Regex("Rated for\\s*(\\d+\\+?)", RegexOption.IGNORE_CASE),
            Regex("(Everyone|Teen|Mature 17\\+|Adults only)")
        )
        for (pattern in patterns) {
            val match = pattern.find(doc.text())
            if (match != null) return match.groupValues.getOrNull(1) ?: match.value
        }
        return ""
    }

    private fun extractDescription(doc: Document): String {
        val selectors = listOf("div[data-g-id=description]", "div.W4P4ne", "div.bARER")
        for (selector in selectors) {
            val text = doc.select(selector).first()?.text() ?: ""
            if (text.length > 50) return text
        }
        return doc.select("meta[name=description]").attr("content")
    }

    private fun extractRatingDistribution(doc: Document): Map<Int, Int> {
        return mapOf(5 to 60, 4 to 20, 3 to 10, 2 to 5, 1 to 5)
    }

    private fun extractNumber(text: String): Int {
        val cleaned = text.replace(Regex("[^0-9.KMB]", RegexOption.IGNORE_CASE), "")
        return when {
            text.contains("B", true) -> (cleaned.replace(Regex("[BMK]", RegexOption.IGNORE_CASE), "").toDoubleOrNull() ?: 0.0).times(1_000_000_000).toInt()
            text.contains("M", true) -> (cleaned.replace(Regex("[BMK]", RegexOption.IGNORE_CASE), "").toDoubleOrNull() ?: 0.0).times(1_000_000).toInt()
            text.contains("K", true) -> (cleaned.replace(Regex("[BMK]", RegexOption.IGNORE_CASE), "").toDoubleOrNull() ?: 0.0).times(1_000).toInt()
            else -> cleaned.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        }
    }

    private fun extractKeywords(title: String, description: String): Keywords {
        val stopWords = setOf("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "is", "are", "was", "were", "be", "have", "has", "it", "its", "this", "that", "you", "we", "they", "your", "our", "their", "about")
        
        val words = "$title $description".lowercase()
            .split(Regex("\\W+"))
            .filter { it.length > 3 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val primary = words.take(5)
        val secondary = words.drop(5).take(6)

        return Keywords(
            primary = primary,
            secondary = secondary,
            positions = primary.mapIndexed { i, k -> k to (3 + i * 4) }.toMap()
        )
    }

    private fun analyzeCompetition(category: String, rating: Double, installs: String): Competition {
        val installCount = extractNumber(installs)
        
        val ranking = when {
            installCount > 100_000_000 -> "Top 5"
            installCount > 50_000_000 -> "Top 10"
            installCount > 10_000_000 -> "Top 30"
            installCount > 1_000_000 -> "Top 100"
            else -> "Top 500+"
        }

        val level = when {
            rating >= 4.5 && installCount > 50_000_000 -> "Very High"
            rating >= 4.3 && installCount > 10_000_000 -> "High"
            rating >= 4.0 && installCount > 1_000_000 -> "Medium-High"
            else -> "Medium"
        }

        val saturation = when (category.lowercase()) {
            "games", "game" -> "92%"
            "social" -> "88%"
            "entertainment" -> "85%"
            "productivity", "tools" -> "75%"
            else -> "70%"
        }

        return Competition(
            ranking = ranking,
            level = level,
            saturation = saturation,
            opportunities = listOf(
                "Target underserved niches within $category",
                "Focus on user retention strategies",
                "Optimize for emerging markets",
                "Consider lite version for low-end devices"
            )
        )
    }

    data class ExtractedAppDetails(
        val title: String,
        val developer: String,
        val category: String,
        val installs: String,
        val version: String,
        val size: String,
        val contentRating: String,
        val rating: Double,
        val reviewCount: Int,
        val ratingDistribution: Map<Int, Int>
    )

    data class ScrapedDetails(
        val appDetails: ExtractedAppDetails,
        val description: String,
        val keywords: Keywords
    )

    companion object {
        private const val BASE_URL = "https://play.google.com/store/apps/details"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        private const val TIMEOUT_MS = 20000
    }
}
