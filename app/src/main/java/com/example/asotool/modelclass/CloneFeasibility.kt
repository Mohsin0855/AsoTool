package com.example.asotool.modelclass

data class CloneFeasibility(
    val score: Int,
    val difficulty: String,
    val recommendation: String,
    val factors: Factors
)
