package com.guiagym.app.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.UsuarioResponse
import com.guiagym.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val usuario: UsuarioResponse? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

class PerfilViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(PerfilUiState())
    val state: StateFlow<PerfilUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getMe()
                .onSuccess { u -> _state.update { it.copy(usuario = u, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun save(nombre: String, pesoInicial: Double?, altura: Double?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null, saved = false) }
            repository.updateMe(nombre.ifBlank { null }, pesoInicial, altura)
                .onSuccess { u -> _state.update { it.copy(usuario = u, isSaving = false, saved = true) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSaved() = _state.update { it.copy(saved = false) }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PerfilViewModel(repo) as T
    }
}
