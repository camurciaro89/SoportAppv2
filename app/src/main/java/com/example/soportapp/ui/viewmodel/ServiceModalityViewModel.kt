package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State
sealed interface ServiceModalityUiState {
    object Initial : ServiceModalityUiState
    object Loading : ServiceModalityUiState
    data class Success(val supportRequestId: Long) : ServiceModalityUiState
    data class Error(val message: String) : ServiceModalityUiState
}

class ServiceModalityViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceModalityUiState>(ServiceModalityUiState.Initial)
    val uiState: StateFlow<ServiceModalityUiState> = _uiState

    fun updateModality(supportRequestId: Long, modality: String, branch: String? = null) {
        viewModelScope.launch {
            _uiState.value = ServiceModalityUiState.Loading
            try {
                // 1. Get the current request
                val request = repository.getSupportRequest(supportRequestId)
                if (request == null) {
                    _uiState.value = ServiceModalityUiState.Error("Request not found")
                    return@launch
                }

                // 2. Modify the object
                val updatedRequest = request.copy(
                    suggestedModality = modality,
                    assignedBranch = branch
                )

                // 3. Update the database
                repository.updateSupportRequest(updatedRequest)

                _uiState.value = ServiceModalityUiState.Success(supportRequestId)

            } catch (e: Exception) {
                _uiState.value = ServiceModalityUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

// ViewModel Factory
class ServiceModalityViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceModalityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceModalityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
