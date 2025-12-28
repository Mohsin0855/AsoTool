package com.example.asotool.di

import com.example.asotool.data.remote.OpenAIService
import com.example.asotool.data.remote.PlayStoreScraper
import com.example.asotool.data.repository.ASORepositoryImpl
import com.example.asotool.domain.repository.ASORepository
import com.example.asotool.presentation.viewmodel.ASOViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    
    single { PlayStoreScraper() }
    
    single { 
        OpenAIService(
            // in this api key need to add for the open ai
            apiKey = "sk-proj-"
        )
    }
    
    single<ASORepository> { 
        ASORepositoryImpl(
            scraper = get(),
            openAIService = get()
        )
    }
    
    viewModelOf(::ASOViewModel)
}
