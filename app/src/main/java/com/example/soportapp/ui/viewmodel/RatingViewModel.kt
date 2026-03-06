package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.Rating
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface RatingUiState {
    object Initial : RatingUiState
    object Loading : RatingUiState
    object Success : RatingUiState
    data class Error(val message: String) : RatingUiState
}

class RatingViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RatingUiState>(RatingUiState.Initial)
    val uiState: StateFlow<RatingUiState> = _uiState

    fun saveRating(supportRequestId: Long, score: Int, comment: String) {
        viewModelScope.launch {
            _uiState.value = RatingUiState.Loading
            try {
                val rating = Rating(
                    supportRequestId = supportRequestId.toInt(),
                    puntuacion = score,
                    comentario = comment,
                    ratingDate = System.currentTimeMillis().toString()
                )
                repository.insertRating(rating)
                
                // También podríamos marcar la solicitud como 'Finalizada' aquí
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    repository.updateSupportRequest(request.copy(estado = "Finalizado"))
                }

                _uiState.value = RatingUiState.Success
            } catch (e: Exception) {
                _uiState.value = RatingUiState.Error(e.message ?: "Error al guardar calificación")
            }
        }
    }
}

class RatingViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
