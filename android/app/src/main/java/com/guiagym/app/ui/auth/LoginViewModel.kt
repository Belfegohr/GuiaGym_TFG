package com.guiagym.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    data object Idle    : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class  Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            repository.login(email.trim(), password)
                .onSuccess { _uiState.value = LoginUiState.Success }
                .onFailure { _uiState.value = LoginUiState.Error(it.message ?: "Error al iniciar sesión") }
        }
    }

    fun resetState() { _uiState.value = LoginUiState.Idle }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(repository) as T
    }
}
