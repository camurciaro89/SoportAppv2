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

sealed interface ProblemDescriptionUiState {
    object Initial : ProblemDescriptionUiState
    object Loading : ProblemDescriptionUiState
    data class Success(val supportRequestId: Long) : ProblemDescriptionUiState
    data class Error(val message: String) : ProblemDescriptionUiState
}

class ProblemDescriptionViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ProblemDescriptionUiState>(ProblemDescriptionUiState.Initial)
    val uiState: StateFlow<ProblemDescriptionUiState> = _uiState

    private fun sanitizeInput(input: String): String {
        return input.replace(Regex("<[^>]*>"), "")
                    .replace(Regex("[{};]"), "")
                    .trim()
    }

    fun saveProblemDescription(request: SupportRequest, photos: List<String>) {
        viewModelScope.launch {
            _uiState.value = ProblemDescriptionUiState.Loading
            try {
                // 1. Obtener nombre del servicio con fallback
                var serviceName = "Soporte Técnico"
                try {
                    val service = repository.getServiceById(request.serviceCatalogId)
                    if (service != null) serviceName = service.visibleName
                } catch (e: Exception) {
                    Log.w("SoportApp", "Catálogo no listo, usando genérico")
                }
                
                // 2. Preparar datos
                val finalRequest = request.copy(
                    problemDescription = sanitizeInput(request.problemDescription),
                    serviceAddress = sanitizeInput(request.serviceAddress),
                    serviceNameSnapshot = serviceName,
                    createdAt = System.currentTimeMillis().toString(),
                    estado = "Pendiente",
                    requestStatus = "POR_PAGAR"
                )

                // 3. Insertar solicitud
                Log.d("SoportApp", "Intentando guardado local seguro...")
                val supportRequestId = repository.insertSupportRequest(finalRequest)
                
                // 4. Fotos
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
                        Log.e("SoportApp", "Error fotos: ${e.message}")
                    }
                }

                _uiState.value = ProblemDescriptionUiState.Success(supportRequestId)

            } catch (e: Exception) {
                // LOG DETALLADO PARA NOSOTROS
                Log.e("SoportApp", "FALLO CRÍTICO EN GUARDADO: ${e.message}", e)
                
                // MENSAJE PARA EL USUARIO
                val errorMsg = when {
                    e.message?.contains("passphrase", ignoreCase = true) == true -> "Error de llave de seguridad. Reinstala la app."
                    e.message?.contains("database", ignoreCase = true) == true -> "Error de base de datos segura."
                    else -> "No se pudo guardar la solicitud. Reintenta."
                }
                _uiState.value = ProblemDescriptionUiState.Error(errorMsg)
            }
        }
    }
}

class ProblemDescriptionViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProblemDescriptionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProblemDescriptionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
