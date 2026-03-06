package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.SupportRequest
import com.example.soportapp.data.database.User
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State
sealed interface ContactInfoUiState {
    object Loading : ContactInfoUiState
    data class Loaded(val request: SupportRequest) : ContactInfoUiState // State when data is ready
    data class Success(val supportRequestId: Long) : ContactInfoUiState
    data class Error(val message: String) : ContactInfoUiState
}

class ContactInfoViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactInfoUiState>(ContactInfoUiState.Loading)
    val uiState: StateFlow<ContactInfoUiState> = _uiState

    fun loadRequestDetails(supportRequestId: Long) {
        viewModelScope.launch {
            _uiState.value = ContactInfoUiState.Loading
            try {
                val request = repository.getSupportRequest(supportRequestId)
                if (request != null) {
                    _uiState.value = ContactInfoUiState.Loaded(request)
                } else {
                    _uiState.value = ContactInfoUiState.Error("Support request not found")
                }
            } catch (e: Exception) {
                _uiState.value = ContactInfoUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveUserAndAssociate(supportRequestId: Long, name: String, phone: String, userType: String) {
        viewModelScope.launch {
            // No need to show loading here again, as the user is already on the screen
            try {
                var user = repository.getUserByPhone(phone)
                if (user == null) {
                    val newUser = User(nombre = name, telefono = phone, userType = userType, createdAt = "")
                    repository.insertUser(newUser)
                    user = repository.getUserByPhone(phone)
                }

                if (user?.id != null) {
                    repository.associateUserToRequest(supportRequestId, user.id)
                    _uiState.value = ContactInfoUiState.Success(supportRequestId)
                } else {
                    _uiState.value = ContactInfoUiState.Error("Could not create or find user")
                }

            } catch (e: Exception) {
                _uiState.value = ContactInfoUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

// ViewModel Factory
class ContactInfoViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactInfoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
