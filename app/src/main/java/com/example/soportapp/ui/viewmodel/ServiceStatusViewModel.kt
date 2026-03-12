package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ServiceStatusUiState {
    object Loading : ServiceStatusUiState
    data class Success(val request: SupportRequest) : ServiceStatusUiState
    data class Error(val message: String) : ServiceStatusUiState
}

class ServiceStatusViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceStatusUiState>(ServiceStatusUiState.Loading)
    val uiState: StateFlow<ServiceStatusUiState> = _uiState

    fun loadRequest(supportRequestId: Long) {
        viewModelScope.launch {
            _uiState.value = ServiceStatusUiState.Loading
            try {
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    _uiState.value = ServiceStatusUiState.Success(request)
                } else {
                    _uiState.value = ServiceStatusUiState.Error("Solicitud no encontrada")
                }
            } catch (e: Exception) {
                _uiState.value = ServiceStatusUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /**
     * SIMULACIÓN PERSISTENTE: Guarda en la BD que se pagó la recogida o se eligió entrega personal.
     */
    fun updateRequestStatus(supportRequestId: Long, isPaid: Boolean, isSelfDelivery: Boolean) {
        viewModelScope.launch {
            try {
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    val updated = request.copy(
                        pagado = isPaid,
                        // Usamos un campo existente para simular la elección
                        modalidad = if (isSelfDelivery) "ENTREGA_PERSONAL" else if (isPaid) "RECOGIDA_PAGADA" else request.modalidad
                    )
                    repository.updateSupportRequest(updated)
                    // Recargamos el estado para que la UI se actualice
                    loadRequest(supportRequestId)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
}

class ServiceStatusViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceStatusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceStatusViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
