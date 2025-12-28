package com.example.asotool.data.repository

import com.example.asotool.data.remote.OpenAIService
import com.example.asotool.data.remote.PlayStoreScraper
import com.example.asotool.domain.model.*
import com.example.asotool.domain.repository.ASORepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ASORepositoryImpl(
    private val scraper: PlayStoreScraper,
    private val openAIService: OpenAIService
) : ASORepository {

    override suspend fun analyzeApp(packageName: String): Result<AppData> = runCatching {
        coroutineScope {
            val doc = scraper.fetchDocument(packageName)
            val details = scraper.getScrapedDetails(packageName, doc)
            
            val asoScoreDeferred = async {
                openAIService.generateASOScore(
                    details.appDetails.title,
                    details.description,
                    details.appDetails.rating,
                    details.appDetails.reviewCount,
                    details.appDetails.installs
                )
            }
            
            val cloneFeasibilityDeferred = async {
                openAIService.generateCloneFeasibilityAnalysis(
                    details.appDetails.title,
                    details.appDetails.category,
                    details.description,
                    details.appDetails.rating,
                    details.appDetails.reviewCount,
                    details.appDetails.installs,
                    details.keywords.primary
                )
            }
            
            val recommendationsDeferred = async {
                openAIService.generateASORecommendations(
                    details.appDetails.title,
                    details.appDetails.developer,
                    details.appDetails.category,
                    details.description,
                    details.appDetails.rating,
                    details.appDetails.reviewCount,
                    details.appDetails.installs,
                    details.keywords.primary + details.keywords.secondary
                )
            }

            val competition = analyzeCompetition(
                details.appDetails.category,
                details.appDetails.rating,
                details.appDetails.installs
            )

            AppData(
                appInfo = AppInfo(
                    title = details.appDetails.title,
                    developer = details.appDetails.developer,
                    category = details.appDetails.category,
                    installs = details.appDetails.installs,
                    version = details.appDetails.version,
                    size = details.appDetails.size,
                    contentRating = details.appDetails.contentRating
                ),
                ratings = Ratings(
                    overall = details.appDetails.rating,
                    totalReviews = details.appDetails.reviewCount,
                    distribution = details.appDetails.ratingDistribution
                ),
                keywords = details.keywords,
                asoScore = asoScoreDeferred.await(),
                competition = competition,
                cloneFeasibility = cloneFeasibilityDeferred.await(),
                recommendations = recommendationsDeferred.await()
            )
        }
    }

    private fun analyzeCompetition(category: String, rating: Double, installs: String): Competition {
        val installCount = extractNumber(installs)
        
        return Competition(
            ranking = when {
                installCount > 100_000_000 -> "Top 5"
                installCount > 50_000_000 -> "Top 10"
                installCount > 10_000_000 -> "Top 30"
                installCount > 1_000_000 -> "Top 100"
                else -> "Top 500+"
            },
            level = when {
                rating >= 4.5 && installCount > 50_000_000 -> "Very High"
                rating >= 4.3 && installCount > 10_000_000 -> "High"
                rating >= 4.0 && installCount > 1_000_000 -> "Medium-High"
                else -> "Medium"
            },
            saturation = when (category.lowercase()) {
                "games", "game" -> "92%"
                "social" -> "88%"
                "entertainment" -> "85%"
                "productivity", "tools" -> "75%"
                else -> "70%"
            },
            opportunities = listOf(
                "Target underserved niches within $category",
                "Focus on user retention strategies",
                "Optimize for emerging markets",
                "Consider lite version for low-end devices"
            )
        )
    }

    private fun extractNumber(text: String): Int {
        val cleaned = text.replace(Regex("[^0-9.KMB]", RegexOption.IGNORE_CASE), "")
        return when {
            text.contains("M", true) -> (cleaned.replace(Regex("[BMK]", RegexOption.IGNORE_CASE), "").toDoubleOrNull() ?: 0.0).times(1_000_000).toInt()
            text.contains("K", true) -> (cleaned.replace(Regex("[BMK]", RegexOption.IGNORE_CASE), "").toDoubleOrNull() ?: 0.0).times(1_000).toInt()
            else -> cleaned.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        }
    }
}
