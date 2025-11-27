package com.example.asotool.modelclass

data class ASOScore(
    val overall: Int,
    val breakdown: Map<String, Int>
)
