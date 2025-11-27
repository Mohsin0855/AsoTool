package com.example.asotool.modelclass

data class AppData(
    val appInfo: AppInfo,
    val ratings: Ratings,
    val keywords: Keywords,
    val asoScore: ASOScore,
    val competition: Competition,
    val cloneFeasibility: CloneFeasibility,
    val recommendations: List<String>
)

