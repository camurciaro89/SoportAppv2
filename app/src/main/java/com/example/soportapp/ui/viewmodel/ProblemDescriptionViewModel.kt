package com.example.soportapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.EvidencePhoto
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State for ProblemDescriptionScreen
sealed interface ProblemDescriptionUiState {
    object Initial : ProblemDescriptionUiState
    object Loading : ProblemDescriptionUiState
    data class Success(val supportRequestId: Long) : ProblemDescriptionUiState
    data class Error(val message: String) : ProblemDescriptionUiState
}

class ProblemDescriptionViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProblemDescriptionUiState>(ProblemDescriptionUiState.Initial)
    val uiState: StateFlow<ProblemDescriptionUiState> = _uiState

    fun saveProblemDescription(request: SupportRequest, photos: List<String>) {
        viewModelScope.launch {
            _uiState.value = ProblemDescriptionUiState.Loading
            try {
                // 1. Obtener nombre del servicio (Opcional, no bloquea)
                val service = repository.getServiceById(request.serviceCatalogId)
                val serviceName = service?.visibleName ?: "Servicio General"
                
                // 2. Crear una solicitud "limpia" para evitar errores de FK
                val finalRequest = request.copy(
                    serviceNameSnapshot = serviceName,
                    createdAt = System.currentTimeMillis().toString(), // Fecha real
                    estado = "Pendiente",
                    requestStatus = "POR_PAGAR"
                )

                Log.d("SoportApp", "Intentando insertar solicitud...")

                // 3. Insert the request
                val supportRequestId = repository.insertSupportRequest(finalRequest)
                
                Log.d("SoportApp", "Inserción exitosa. ID: $supportRequestId")

                // 4. Save photos (Opcional, no bloquea el flujo principal)
                if (photos.isNotEmpty()) {
                    try {
                        val photoEntities = photos.map {
                            EvidencePhoto(
                                supportRequestId = supportRequestId.toInt(),
                                storageUrl = it,
                                uploadDate = System.currentTimeMillis().toString()
                            )
                        }
                        repository.insertEvidencePhotos(photoEntities)
                    } catch (e: Exception) {
                        Log.e("SoportApp", "Error al guardar fotos, pero continuamos: ${e.message}")
                    }
                }

                _uiState.value = ProblemDescriptionUiState.Success(supportRequestId)

            } catch (e: Exception) {
                // LOG DETALLADO DEL ERROR
                Log.e("SoportApp", "FALLO CRÍTICO AL GUARDAR: ${e.message}", e)
                _uiState.value = ProblemDescriptionUiState.Error("Error técnico: ${e.localizedMessage}")
            }
        }
    }
}

// ViewModel Factory
class ProblemDescriptionViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProblemDescriptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProblemDescriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
