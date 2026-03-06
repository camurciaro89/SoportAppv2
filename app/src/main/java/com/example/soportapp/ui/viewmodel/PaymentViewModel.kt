package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.Payment
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State
sealed interface PaymentUiState {
    object Initial : PaymentUiState
    object Loading : PaymentUiState
    data class Success(val supportRequestId: Long) : PaymentUiState
    data class Error(val message: String) : PaymentUiState
}

class PaymentViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Initial)
    val uiState: StateFlow<PaymentUiState> = _uiState

    fun processPayment(supportRequestId: Long, amount: Double, method: String) {
        viewModelScope.launch {
            _uiState.value = PaymentUiState.Loading
            try {
                // 1. Get the current request
                val request = repository.getSupportRequest(supportRequestId)
                if (request == null) {
                    _uiState.value = PaymentUiState.Error("Request not found")
                    return@launch
                }

                // 2. Create the payment record
                val newPayment = Payment(
                    supportRequestId = supportRequestId.toInt(),
                    monto = amount,
                    paymentMethod = method,
                    paymentStatus = "Éxito", // Simulate success
                    transactionState = "Exitosa",
                    bankReference = "TXN_${System.currentTimeMillis()}",
                    paymentDate = ""
                )
                repository.insertPayment(newPayment)

                // 3. Update the support request status
                val updatedRequest = request.copy(pagado = true, estado = "Pagado")
                repository.updateSupportRequest(updatedRequest)

                _uiState.value = PaymentUiState.Success(supportRequestId)

            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

// ViewModel Factory
class PaymentViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
