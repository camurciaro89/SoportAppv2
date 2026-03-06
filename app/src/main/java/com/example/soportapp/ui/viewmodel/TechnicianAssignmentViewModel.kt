package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State
sealed interface TechnicianAssignmentUiState {
    object Initial : TechnicianAssignmentUiState
    object Loading : TechnicianAssignmentUiState
    data class Success(val supportRequestId: Long) : TechnicianAssignmentUiState
    data class Error(val message: String) : TechnicianAssignmentUiState
}

class TechnicianAssignmentViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TechnicianAssignmentUiState>(TechnicianAssignmentUiState.Initial)
    val uiState: StateFlow<TechnicianAssignmentUiState> = _uiState

    fun assignTechnician(supportRequestId: Long, technicianId: Int) {
        viewModelScope.launch {
            _uiState.value = TechnicianAssignmentUiState.Loading
            try {
                // 1. Assign the technician
                repository.assignTechnicianToRequest(supportRequestId, technicianId)

                // 2. Update the request status to 'Asignado'
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    val updatedRequest = request.copy(estado = "Asignado")
                    repository.updateSupportRequest(updatedRequest)
                    _uiState.value = TechnicianAssignmentUiState.Success(supportRequestId)
                } else {
                    _uiState.value = TechnicianAssignmentUiState.Error("Request not found after assignment")
                }

            } catch (e: Exception) {
                _uiState.value = TechnicianAssignmentUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

// ViewModel Factory
class TechnicianAssignmentViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TechnicianAssignmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TechnicianAssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
