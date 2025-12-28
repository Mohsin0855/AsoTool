package com.example.asotool.domain.model

data class AppData(
    val appInfo: AppInfo,
    val ratings: Ratings,
    val keywords: Keywords,
    val asoScore: ASOScore,
    val competition: Competition,
    val cloneFeasibility: CloneFeasibility,
    val recommendations: List<String>
)

data class AppInfo(
    val title: String,
    val developer: String,
    val category: String,
    val installs: String,
    val version: String,
    val size: String,
    val contentRating: String
)

data class Ratings(
    val overall: Double,
    val totalReviews: Int,
    val distribution: Map<Int, Int>
)

data class Keywords(
    val primary: List<String>,
    val secondary: List<String>,
    val positions: Map<String, Int>
)

data class ASOScore(
    val overall: Int,
    val breakdown: Map<String, Int>
)

data class Competition(
    val ranking: String,
    val level: String,
    val saturation: String,
    val opportunities: List<String>
)

data class CloneFeasibility(
    val score: Int,
    val difficulty: String,
    val recommendation: String,
    val factors: Factors
)

data class Factors(
    val technical: String,
    val demand: String,
    val barrier: String,
    val uniqueFeatures: List<String>
)
