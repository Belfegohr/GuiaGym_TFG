package com.guiagym.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    data object Idle    : RegisterUiState()
    data object Loading : RegisterUiState()
    data object Success : RegisterUiState()
    data class  Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(nombre: String, email: String, password: String) {
        when {
            nombre.isBlank() || email.isBlank() || password.isBlank() ->
                _uiState.value = RegisterUiState.Error("Completa todos los campos")
            password.length < 8 ->
                _uiState.value = RegisterUiState.Error("La contraseña debe tener al menos 8 caracteres")
            else -> viewModelScope.launch {
                _uiState.value = RegisterUiState.Loading
                repository.register(nombre.trim(), email.trim(), password)
                    .onSuccess { _uiState.value = RegisterUiState.Success }
                    .onFailure { _uiState.value = RegisterUiState.Error(it.message ?: "Error al registrarse") }
            }
        }
    }

    fun resetState() { _uiState.value = RegisterUiState.Idle }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(repository) as T
    }
}
