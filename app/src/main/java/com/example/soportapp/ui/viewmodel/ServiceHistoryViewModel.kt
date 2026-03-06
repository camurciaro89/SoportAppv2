package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ServiceHistoryUiState {
    object Loading : ServiceHistoryUiState
    data class Success(val requests: List<SupportRequest>) : ServiceHistoryUiState
    data class Error(val message: String) : ServiceHistoryUiState
}

class ServiceHistoryViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceHistoryUiState>(ServiceHistoryUiState.Loading)
    val uiState: StateFlow<ServiceHistoryUiState> = _uiState

    /**
     * Carga todas las solicitudes registradas en la web.
     * Ideal para la vista del técnico o administrador.
     */
    fun loadAllRequests() {
        viewModelScope.launch {
            _uiState.value = ServiceHistoryUiState.Loading
            try {
                val requests = repository.getAllRequestsFromWeb()
                _uiState.value = ServiceHistoryUiState.Success(requests)
            } catch (e: Exception) {
                _uiState.value = ServiceHistoryUiState.Error(e.message ?: "Error al cargar historial")
            }
        }
    }

    /**
     * Carga solo las solicitudes de un usuario específico.
     */
    fun loadUserRequests(userId: Int) {
        viewModelScope.launch {
            _uiState.value = ServiceHistoryUiState.Loading
            try {
                val requests = repository.getUserRequestsFromWeb(userId)
                _uiState.value = ServiceHistoryUiState.Success(requests)
            } catch (e: Exception) {
                _uiState.value = ServiceHistoryUiState.Error(e.message ?: "Error al cargar tus servicios")
            }
        }
    }
}

class ServiceHistoryViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
