package com.example.asotool.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asotool.domain.model.AppData
import com.example.asotool.domain.repository.ASORepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ASOViewModel(private val repository: ASORepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ASOUiState())
    val uiState: StateFlow<ASOUiState> = _uiState.asStateFlow()

    fun updatePackageName(name: String) {
        _uiState.update { it.copy(packageName = name) }
    }

    fun analyzeApp() {
        val packageName = _uiState.value.packageName
        if (packageName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            repository.analyzeApp(packageName)
                .onSuccess { data ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            appData = data,
                            errorMessage = null
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error.message ?: "Analysis failed"
                        ) 
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class ASOUiState(
    val packageName: String = "",
    val isLoading: Boolean = false,
    val appData: AppData? = null,
    val errorMessage: String? = null
)
