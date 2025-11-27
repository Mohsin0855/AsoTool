package com.example.asotool.modelclass

data class Ratings(
    val overall: Double,
    val totalReviews: Int,
    val distribution: Map<Int, Int>
)