package com.guiagym.app.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.RutinaListResponse
import com.guiagym.app.data.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MisRutinasUiState(
    val rutinas: List<RutinaListResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaving: Boolean = false,
)

class MisRutinasViewModel(private val repository: RutinaRepository) : ViewModel() {

    private val _state = MutableStateFlow(MisRutinasUiState())
    val state: StateFlow<MisRutinasUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRutinas()
                .onSuccess { rutinas -> _state.update { it.copy(rutinas = rutinas, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun createRutina(nombre: String, descripcion: String?, publica: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.createRutina(nombre, descripcion?.ifBlank { null }, publica)
                .onSuccess { load(); onDone() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    class Factory(private val repository: RutinaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MisRutinasViewModel(repository) as T
    }
}
