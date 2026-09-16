package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.User
import com.example.soportapp.data.repository.SoportAppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(nombre: String, email: String, telefono: String, contrasena: String, userType: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val existingUser = repository.getUserByEmail(email)
                if (existingUser != null) {
                    _uiState.value = AuthUiState.Error("El correo ya está registrado")
                    return@launch
                }

                val newUser = User(
                    nombre = nombre,
                    email = email,
                    telefono = telefono,
                    contrasena = contrasena,
                    userType = userType
                )
                repository.insertUser(newUser)
                _uiState.value = AuthUiState.Success(newUser)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error al registrar: ${e.message}")
            }
        }
    }

    fun login(email: String, contrasena: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val user = repository.getUserByEmail(email)
                if (user != null && user.contrasena == contrasena) {
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Correo o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error al iniciar sesión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

class AuthViewModelFactory(private val repository: SoportAppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
