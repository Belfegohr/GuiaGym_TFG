package com.guiagym.app.ui.entrenamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.EntrenamientoListResponse
import com.guiagym.app.data.repository.EntrenamientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntrenamientosUiState(
    val entrenamientos: List<EntrenamientoListResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isActioning: Boolean = false,   // true mientras inicia/finaliza
    val iniciadoId: Int? = null,        // ID del entrenamiento recién creado para navegar
)

class EntrenamientosViewModel(private val repository: EntrenamientoRepository) : ViewModel() {

    private val _state = MutableStateFlow(EntrenamientosUiState())
    val state: StateFlow<EntrenamientosUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getEntrenamientos()
                .onSuccess { list -> _state.update { it.copy(entrenamientos = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun iniciar() {
        viewModelScope.launch {
            _state.update { it.copy(isActioning = true, error = null) }
            repository.iniciarEntrenamiento()
                .onSuccess { e -> _state.update { it.copy(isActioning = false, iniciadoId = e.id) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isActioning = false) } }
        }
    }

    fun clearIniciadoId() = _state.update { it.copy(iniciadoId = null) }

    fun finalizar(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isActioning = true, error = null) }
            repository.finalizarEntrenamiento(id)
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isActioning = false) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    class Factory(private val repository: EntrenamientoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EntrenamientosViewModel(repository) as T
    }
}
