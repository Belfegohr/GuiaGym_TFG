package com.guiagym.app.ui.seguimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.SeguimientoPesoResponse
import com.guiagym.app.data.repository.SeguimientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeguimientoUiState(
    val registros: List<SeguimientoPesoResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
)

class SeguimientoPesoViewModel(private val repository: SeguimientoRepository) : ViewModel() {

    private val _state = MutableStateFlow(SeguimientoUiState())
    val state: StateFlow<SeguimientoUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getSeguimiento()
                .onSuccess { list -> _state.update { it.copy(registros = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun registrarPeso(peso: Double, notas: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            repository.registrarPeso(peso, notas?.ifBlank { null })
                .onSuccess { load(); onDone() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    class Factory(private val repository: SeguimientoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SeguimientoPesoViewModel(repository) as T
    }
}
