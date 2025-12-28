package com.example.asotool.domain.repository

import com.example.asotool.domain.model.AppData

interface ASORepository {
    suspend fun analyzeApp(packageName: String): Result<AppData>
}
