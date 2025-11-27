package com.example.asotool.modelclass

data class Keywords(
    val primary: List<String>,
    val secondary: List<String>,
    val positions: Map<String, Int>
)

