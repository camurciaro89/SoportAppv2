package com.example.soportapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.soportapp.data.database.User
import com.example.soportapp.data.repository.SoportAppRepository
import com.example.soportapp.utils.InputValidator
import com.example.soportapp.utils.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

/**
 * Gestiona la lógica de autenticación de la aplicación, incluyendo el registro de nuevos usuarios
 * y el inicio de sesión.
 *
 * Implementa medidas de seguridad no funcionales como:
 * - Hashing de contraseñas con Sal (SHA-256).
 * - Validación de fortaleza de contraseñas.
 * - Validación de formatos de email y teléfono.
 */
class AuthViewModel(private val repository: SoportAppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(nombre: String, email: String, telefono: String, contrasena: String, userType: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            
            // Validaciones de entrada
            if (!InputValidator.isValidName(nombre)) {
                _uiState.value = AuthUiState.Error("El nombre debe tener al menos 3 caracteres")
                return@launch
            }
            if (!InputValidator.isValidEmail(email)) {
                _uiState.value = AuthUiState.Error("Correo electrónico inválido")
                return@launch
            }
            if (!InputValidator.isValidPhone(telefono)) {
                _uiState.value = AuthUiState.Error("Teléfono inválido (mínimo 7 dígitos)")
                return@launch
            }
            if (!InputValidator.isStrongPassword(contrasena)) {
                _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres, incluir una letra y un número")
                return@launch
            }

            try {
                val existingUser = repository.getUserByEmail(email)
                if (existingUser != null) {
                    _uiState.value = AuthUiState.Error("El correo ya está registrado")
                    return@launch
                }

                // Hashing de contraseña
                val salt = SecurityUtils.generateSalt()
                val hashedPass = SecurityUtils.hashPassword(contrasena, salt)

                val newUser = User(
                    nombre = nombre,
                    email = email,
                    telefono = telefono,
                    contrasena = hashedPass,
                    salt = salt,
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
                if (user != null) {
                    val isValid = SecurityUtils.verifyPassword(contrasena, user.salt, user.contrasena)
                    if (isValid) {
                        _uiState.value = AuthUiState.Success(user)
                    } else {
                        _uiState.value = AuthUiState.Error("Correo o contraseña incorrectos")
                    }
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
