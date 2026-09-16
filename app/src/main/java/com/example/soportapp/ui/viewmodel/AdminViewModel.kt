package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.database.Technician
import com.example.soportapp.data.database.User
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminDashboardData(
    val totalTickets: Int = 0,
    val totalUsers: Int = 0,
    val totalTechnicians: Int = 0,
    val pendingTickets: Int = 0,
    val recentRequests: List<SupportRequest> = emptyList(),
    val technicians: List<Technician> = emptyList()
)

sealed interface AdminUiState {
    object Loading : AdminUiState
    data class Success(val data: AdminDashboardData) : AdminUiState
    data class Error(val message: String) : AdminUiState
}

class AdminViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = AdminUiState.Loading
            try {
                // Asumiendo que el repositorio tiene estos métodos o los implementaré
                val requests = repository.getAllLocalRequests()
                val users = repository.getAllUsers()
                val technicians = repository.getAllTechnicians()

                val data = AdminDashboardData(
                    totalTickets = requests.size,
                    totalUsers = users.size,
                    totalTechnicians = technicians.size,
                    pendingTickets = requests.count { it.estado.lowercase() == "pendiente" },
                    recentRequests = requests.take(10),
                    technicians = technicians
                )
                _uiState.value = AdminUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = AdminUiState.Error("Fallo al cargar indicadores: ${e.message}")
            }
        }
    }
}

class AdminViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
