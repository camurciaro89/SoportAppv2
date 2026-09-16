package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface TechnicianUiState {
    object Loading : TechnicianUiState
    data class Success(val tasks: List<SupportRequest>) : TechnicianUiState
    data class Error(val message: String) : TechnicianUiState
}

class TechnicianViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TechnicianUiState>(TechnicianUiState.Loading)
    val uiState: StateFlow<TechnicianUiState> = _uiState

    fun loadTasks(technicianId: Int) {
        viewModelScope.launch {
            _uiState.value = TechnicianUiState.Loading
            try {
                // Necesitamos este método en el repositorio
                val tasks = repository.getRequestsByTechnician(technicianId)
                _uiState.value = TechnicianUiState.Success(tasks)
            } catch (e: Exception) {
                _uiState.value = TechnicianUiState.Error("Error al cargar tareas: ${e.message}")
            }
        }
    }

    fun updateStatus(requestId: Long, technicianId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                val request = repository.getSupportRequest(requestId)
                if (request != null) {
                    val updated = request.copy(estado = newStatus)
                    repository.updateSupportRequest(updated)
                    loadTasks(technicianId)
                }
            } catch (e: Exception) { }
        }
    }

    fun completeTask(
        requestId: Long,
        technicianId: Int,
        solution: String,
        parts: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val request = repository.getSupportRequest(requestId)
                if (request != null) {
                    val updated = request.copy(
                        technicalSolution = solution,
                        partsUsed = parts,
                        technicianNotes = notes,
                        estado = "Completado",
                        completedAt = System.currentTimeMillis().toString()
                    )
                    repository.updateSupportRequest(updated)
                    loadTasks(technicianId)
                }
            } catch (e: Exception) { }
        }
    }
}

class TechnicianViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TechnicianViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TechnicianViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
