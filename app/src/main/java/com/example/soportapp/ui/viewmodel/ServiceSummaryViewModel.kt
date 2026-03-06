package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State
sealed interface ServiceSummaryUiState {
    object Loading : ServiceSummaryUiState
    data class Success(val request: SupportRequest) : ServiceSummaryUiState
    data class Error(val message: String) : ServiceSummaryUiState
}

class ServiceSummaryViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceSummaryUiState>(ServiceSummaryUiState.Loading)
    val uiState: StateFlow<ServiceSummaryUiState> = _uiState

    fun loadRequestDetails(supportRequestId: Long) {
        viewModelScope.launch {
            _uiState.value = ServiceSummaryUiState.Loading
            try {
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    _uiState.value = ServiceSummaryUiState.Success(request)
                } else {
                    _uiState.value = ServiceSummaryUiState.Error("Support request not found")
                }
            } catch (e: Exception) {
                _uiState.value = ServiceSummaryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

// ViewModel Factory
class ServiceSummaryViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceSummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceSummaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
