package com.example.asotool.util

import com.example.asotool.modelclass.ASOScore
import com.example.asotool.modelclass.AppData
import com.example.asotool.modelclass.AppInfo
import com.example.asotool.modelclass.CloneFeasibility
import com.example.asotool.modelclass.Competition
import com.example.asotool.modelclass.Factors
import com.example.asotool.modelclass.Keywords
import com.example.asotool.modelclass.Ratings

suspend fun analyzeApp(packageName: String): AppData {
    // In production, use Google Play Scraper API or similar
    // For now, return demo data
    return getDemoData(packageName)
}

fun getDemoData(packageName: String): AppData {
    return AppData(
        appInfo = AppInfo(
            title = "Demo App Analysis",
            developer = "Demo Developer",
            category = "Productivity",
            installs = "1M+",
            version = "2.5.0",
            size = "45MB",
            contentRating = "Everyone"
        ),
        ratings = Ratings(
            overall = 4.3,
            totalReviews = 125000,
            distribution = mapOf(5 to 65, 4 to 20, 3 to 8, 2 to 4, 1 to 3)
        ),
        keywords = Keywords(
            primary = listOf("task manager", "productivity", "todo list", "organizer"),
            secondary = listOf("calendar", "reminder", "notes", "planner", "schedule"),
            positions = mapOf(
                "task manager" to 8,
                "productivity" to 15,
                "todo list" to 5
            )
        ),
        asoScore = ASOScore(
            overall = 72,
            breakdown = mapOf(
                "titleOptimization" to 75,
                "descriptionQuality" to 68,
                "keywordDensity" to 70,
                "visualAssets" to 80,
                "userEngagement" to 72
            )
        ),
        competition = Competition(
            ranking = "Top 30",
            level = "High",
            saturation = "82%",
            opportunities = listOf(
                "Focus on niche workflows",
                "AI-powered suggestions",
                "Better collaboration",
                "Offline-first approach"
            )
        ),
        cloneFeasibility = CloneFeasibility(
            score = 7,
            difficulty = "Medium",
            recommendation = "Feasible - Focus on unique features and niche audience",
            factors = Factors(
                technical = "Medium - Standard CRUD with sync",
                demand = "High - Always in demand",
                barrier = "Medium - Large market",
                uniqueFeatures = listOf(
                    "Custom task templates",
                    "Smart prioritization",
                    "Cross-platform sync"
                )
            )
        ),
        recommendations = listOf(
            "Optimize title with high-volume keywords",
            "Expand description with long-tail keywords",
            "Update screenshots to highlight features",
            "Encourage user reviews",
            "Localize content for top markets",
            "Monitor competitor strategies",
            "A/B test different icons"
        )
    )
}
