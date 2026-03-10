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

    /**
     * Limpia el texto de posibles etiquetas HTML o scripts maliciosos.
     */
    private fun sanitizeInput(input: String): String {
        return input.replace(Regex("<[^>]*>"), "") // Elimina etiquetas HTML
                    .replace(Regex("[{};]"), "")   // Elimina caracteres de código comunes
                    .trim()
    }

    fun saveProblemDescription(request: SupportRequest, photos: List<String>) {
        viewModelScope.launch {
            _uiState.value = ProblemDescriptionUiState.Loading
            try {
                // 1. Obtener nombre del servicio (Opcional, no bloquea)
                val service = repository.getServiceById(request.serviceCatalogId)
                val serviceName = service?.visibleName ?: "Servicio General"
                
                // 2. Limpieza de seguridad y preparación de datos reales
                val finalRequest = request.copy(
                    problemDescription = sanitizeInput(request.problemDescription),
                    serviceAddress = sanitizeInput(request.serviceAddress),
                    serviceNameSnapshot = serviceName,
                    createdAt = System.currentTimeMillis().toString(), // Fecha inalterable
                    estado = "Pendiente",
                    requestStatus = "POR_PAGAR"
                )

                Log.d("SoportApp", "Seguridad verificada. Insertando solicitud...")

                // 3. Insertar solicitud
                val supportRequestId = repository.insertSupportRequest(finalRequest)
                
                // 4. Guardar fotos con blindaje (Si una falla, no detiene el proceso)
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
                        Log.e("SoportApp", "Error fotos (No crítico): ${e.message}")
                    }
                }

                _uiState.value = ProblemDescriptionUiState.Success(supportRequestId)

            } catch (e: Exception) {
                Log.e("SoportApp", "FALLO CRÍTICO: ${e.message}", e)
                _uiState.value = ProblemDescriptionUiState.Error("Error de guardado seguro. Intenta de nuevo.")
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
