package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.Equipment
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface EquipmentUiState {
    object Loading : EquipmentUiState
    data class Success(val equipments: List<Equipment>) : EquipmentUiState
    data class Error(val message: String) : EquipmentUiState
}

class EquipmentViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<EquipmentUiState>(EquipmentUiState.Loading)
    val uiState: StateFlow<EquipmentUiState> = _uiState

    fun loadEquipments(userId: Int) {
        viewModelScope.launch {
            _uiState.value = EquipmentUiState.Loading
            try {
                repository.getEquipmentsByUserId(userId).collectLatest { list ->
                    _uiState.value = EquipmentUiState.Success(list)
                }
            } catch (e: Exception) {
                _uiState.value = EquipmentUiState.Error("Fallo al cargar equipos: ${e.message}")
            }
        }
    }

    fun registerEquipment(equipment: Equipment) {
        viewModelScope.launch {
            try {
                repository.insertEquipment(equipment)
                // loadEquipments se actualizará automáticamente por el Flow
            } catch (e: Exception) {
                // Manejar error de inserción si es necesario
            }
        }
    }
}

class EquipmentViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EquipmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
