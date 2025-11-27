package com.example.asotool

import com.example.asotool.modelclass.ASOScore
import com.example.asotool.modelclass.AppData
import com.example.asotool.modelclass.AppInfo
import com.example.asotool.modelclass.CloneFeasibility
import com.example.asotool.modelclass.Competition
import com.example.asotool.modelclass.Factors
import com.example.asotool.modelclass.Keywords
import com.example.asotool.modelclass.Ratings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PlayStoreScraper {

    companion object {
        private const val PLAY_STORE_BASE_URL = "https://play.google.com/store/apps/details"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    }

    suspend fun scrapeAppData(packageName: String): AppData = withContext(Dispatchers.IO) {
        try {
            val url = "$PLAY_STORE_BASE_URL?id=$packageName&hl=en"
            val doc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10000)
                .get()

            // Extract App Title
            val title = doc.select("h1[itemprop=name]").text()
                .ifEmpty { doc.select("h1.Fd93Bb").text() }

            // Extract Developer
            val developer = doc.select("div.Vbfug a").text()
                .ifEmpty { doc.select("a.Si6A0c").text() }

            // Extract Category
            val category = doc.select("a[itemprop=genre]").text()
                .ifEmpty { "Unknown" }

            // Extract Rating
            val ratingText = doc.select("div.TT9eCd").text()
            val rating = ratingText.replace(",", ".").toDoubleOrNull() ?: 0.0

            // Extract Review Count
            val reviewsText = doc.select("div.g1rdde").text()
            val reviews = extractNumber(reviewsText)

            // Extract Installs
            val installs = doc.select("div.ClM7O:contains(Downloads)").next().text()
                .ifEmpty { doc.select("div:contains(Downloads)").next().text() }

            // Extract Version
            val version = doc.select("div.ClM7O:contains(Version)").next().text()
                .ifEmpty { "Unknown" }

            // Extract Size
            val size = doc.select("div.ClM7O:contains(Size)").next().text()
                .ifEmpty { "Varies" }

            // Extract Content Rating
            val contentRating = doc.select("div.ClM7O:contains(Rated for)").next().text()
                .ifEmpty { "Everyone" }

            // Extract Description
            val description = doc.select("div[data-g-id=description]").text()
                .ifEmpty { doc.select("div.W4P4ne").text() }

            // Extract Keywords from title and description
            val keywords = extractKeywords(title, description)

            // Extract Rating Distribution
            val ratingDistribution = extractRatingDistribution(doc)

            // Calculate ASO Score
            val asoScore = calculateASOScore(title, description, rating, reviews)

            // Analyze Competition
            val competition = analyzeCompetition(category, rating, installs)

            // Analyze Clone Feasibility
            val cloneFeasibility = analyzeCloneFeasibility(
                category, rating, reviews, installs, keywords
            )

            // Generate Recommendations
            val recommendations = generateRecommendations(title, description, keywords, rating)

            return@withContext AppData(
                appInfo = AppInfo(
                    title = title,
                    developer = developer,
                    category = category,
                    installs = installs,
                    version = version,
                    size = size,
                    contentRating = contentRating
                ),
                ratings = Ratings(
                    overall = rating,
                    totalReviews = reviews,
                    distribution = ratingDistribution
                ),
                keywords = keywords,
                asoScore = asoScore,
                competition = competition,
                cloneFeasibility = cloneFeasibility,
                recommendations = recommendations
            )

        } catch (e: Exception) {
            throw Exception("Failed to scrape app data: ${e.message}")
        }
    }

    private fun extractNumber(text: String): Int {
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return when {
            text.contains("M", ignoreCase = true) -> (cleaned.toDoubleOrNull() ?: 0.0).times(1_000_000).toInt()
            text.contains("K", ignoreCase = true) -> (cleaned.toDoubleOrNull() ?: 0.0).times(1_000).toInt()
            text.contains("B", ignoreCase = true) -> (cleaned.toDoubleOrNull() ?: 0.0).times(1_000_000_000).toInt()
            else -> cleaned.toIntOrNull() ?: 0
        }
    }

    private fun extractKeywords(title: String, description: String): Keywords {
        val allText = "$title $description".lowercase()
        val words = allText.split(Regex("\\W+"))
            .filter { it.length > 3 }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .map { it.first }

        val primary = words.take(4)
        val secondary = words.drop(4).take(5)

        // Simulate keyword positions (in real scenario, you'd search Play Store)
        val positions = primary.mapIndexed { index, keyword ->
            keyword to (5 + index * 3)
        }.toMap()

        return Keywords(
            primary = primary,
            secondary = secondary,
            positions = positions
        )
    }

    private fun extractRatingDistribution(doc: org.jsoup.nodes.Document): Map<Int, Int> {
        val distribution = mutableMapOf<Int, Int>()

        try {
            // Try to extract rating bars
            val ratingBars = doc.select("div.VEF2C")

            if (ratingBars.isEmpty()) {
                // Return default distribution if not found
                return mapOf(5 to 60, 4 to 20, 3 to 10, 2 to 5, 1 to 5)
            }

            ratingBars.forEachIndexed { index, element ->
                val star = 5 - index
                val widthAttr = element.attr("style")
                val width = widthAttr.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                distribution[star] = width
            }
        } catch (e: Exception) {
            // Return default distribution on error
            return mapOf(5 to 60, 4 to 20, 3 to 10, 2 to 5, 1 to 5)
        }

        return distribution.ifEmpty {
            mapOf(5 to 60, 4 to 20, 3 to 10, 2 to 5, 1 to 5)
        }
    }

    private fun calculateASOScore(
        title: String,
        description: String,
        rating: Double,
        reviews: Int
    ): ASOScore {
        val titleScore = when {
            title.length in 30..50 -> 90
            title.length in 20..30 -> 75
            else -> 60
        }

        val descriptionScore = when {
            description.length > 3000 -> 85
            description.length > 1000 -> 70
            else -> 55
        }

        val keywordScore = if (description.split(" ").size > 200) 75 else 60

        val visualScore = 75 // Placeholder (would check screenshots in real scenario)

        val engagementScore = when {
            rating > 4.5 && reviews > 100000 -> 90
            rating > 4.0 && reviews > 10000 -> 75
            else -> 60
        }

        val overall = (titleScore + descriptionScore + keywordScore + visualScore + engagementScore) / 5

        return ASOScore(
            overall = overall,
            breakdown = mapOf(
                "titleOptimization" to titleScore,
                "descriptionQuality" to descriptionScore,
                "keywordDensity" to keywordScore,
                "visualAssets" to visualScore,
                "userEngagement" to engagementScore
            )
        )
    }

    private fun analyzeCompetition(category: String, rating: Double, installs: String): Competition {
        val installCount = extractNumber(installs)

        val ranking = when {
            installCount > 50_000_000 -> "Top 10"
            installCount > 10_000_000 -> "Top 30"
            installCount > 1_000_000 -> "Top 100"
            else -> "Top 500+"
        }

        val level = when {
            rating > 4.5 && installCount > 10_000_000 -> "Very High"
            rating > 4.0 && installCount > 1_000_000 -> "High"
            else -> "Medium"
        }

        val saturation = when (category.lowercase()) {
            "games", "social", "entertainment" -> "90%"
            "productivity", "tools", "business" -> "75%"
            else -> "65%"
        }

        val opportunities = listOf(
            "Focus on niche audience within $category",
            "Improve user retention strategies",
            "Enhance unique value proposition",
            "Optimize for emerging markets"
        )

        return Competition(
            ranking = ranking,
            level = level,
            saturation = saturation,
            opportunities = opportunities
        )
    }

    private fun analyzeCloneFeasibility(
        category: String,
        rating: Double,
        reviews: Int,
        installs: String,
        keywords: Keywords
    ): CloneFeasibility {
        val installCount = extractNumber(installs)

        // Score calculation (1-10)
        val demandScore = when {
            installCount > 10_000_000 -> 3
            installCount > 1_000_000 -> 2
            else -> 1
        }

        val competitionScore = when {
            rating < 4.0 -> 3
            rating < 4.5 -> 2
            else -> 1
        }

        val categoryScore = when (category.lowercase()) {
            "games" -> 1 // Very hard
            "social" -> 2 // Hard
            "productivity", "tools" -> 3 // Feasible
            else -> 2
        }

        val totalScore = demandScore + competitionScore + categoryScore

        val difficulty = when {
            totalScore >= 8 -> "Easy"
            totalScore >= 5 -> "Medium"
            else -> "Hard"
        }

        val recommendation = when {
            totalScore >= 7 -> "Highly Feasible - Good market opportunity with manageable competition"
            totalScore >= 5 -> "Feasible - Focus on unique features and specific niche"
            else -> "Challenging - Requires significant innovation and resources"
        }

        return CloneFeasibility(
            score = totalScore,
            difficulty = difficulty,
            recommendation = recommendation,
            factors = Factors(
                technical = "Medium - Standard mobile app architecture",
                demand = if (installCount > 1_000_000) "High" else "Medium",
                barrier = if (rating > 4.5) "High - Strong incumbent" else "Medium",
                uniqueFeatures = keywords.primary.take(3)
            )
        )
    }

    private fun generateRecommendations(
        title: String,
        description: String,
        keywords: Keywords,
        rating: Double
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (title.length < 30) {
            recommendations.add("Expand title to include more relevant keywords (aim for 30-50 characters)")
        }

        if (description.length < 2000) {
            recommendations.add("Expand description with more detailed feature explanations and use cases")
        }

        if (keywords.primary.size < 4) {
            recommendations.add("Identify and incorporate more high-traffic keywords in your description")
        }

        if (rating < 4.5) {
            recommendations.add("Focus on improving user experience to boost ratings")
            recommendations.add("Implement in-app feedback system to resolve issues quickly")
        }

        recommendations.addAll(listOf(
            "Localize app content for top 5 markets",
            "Update screenshots every quarter to showcase new features",
            "A/B test different icon designs to improve CTR",
            "Encourage satisfied users to leave reviews",
            "Monitor competitor keyword strategies weekly",
            "Optimize first 160 characters of description for preview text"
        ))

        return recommendations.take(8)
    }
}