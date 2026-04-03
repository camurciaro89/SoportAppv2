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
import kotlin.random.Random

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
                var serviceName = "Soporte Técnico"
                try {
                    val service = repository.getServiceById(request.serviceCatalogId)
                    if (service != null) serviceName = service.visibleName
                } catch (e: Exception) {
                    Log.w("SoportApp", "Catálogo no listo")
                }
                
                // GENERACIÓN DE CÓDIGO OTP ALEATORIO (4 DÍGITOS)
                val randomCode = Random.nextInt(1000, 9999).toString()

                val finalRequest = request.copy(
                    problemDescription = sanitizeInput(request.problemDescription),
                    serviceAddress = sanitizeInput(request.serviceAddress),
                    serviceNameSnapshot = serviceName,
                    securityCode = randomCode, // Guardamos el código dinámico
                    createdAt = System.currentTimeMillis().toString(),
                    estado = "Pendiente",
                    requestStatus = "POR_PAGAR"
                )

                val supportRequestId = repository.insertSupportRequest(finalRequest)
                
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
                Log.e("SoportApp", "FALLO EN GUARDADO: ${e.message}", e)
                _uiState.value = ProblemDescriptionUiState.Error("No se pudo guardar la solicitud. Reinstala la app.")
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
