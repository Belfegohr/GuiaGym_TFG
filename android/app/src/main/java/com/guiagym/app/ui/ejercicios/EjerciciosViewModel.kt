package com.guiagym.app.ui.ejercicios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.EjercicioResponse
import com.guiagym.app.data.repository.EjercicioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjerciciosUiState(
    val all: List<EjercicioResponse> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filterGrupo: String? = null,
    val filterDificultad: String? = null,
    val query: String = "",
    val detail: EjercicioResponse? = null,
    val isLoadingDetail: Boolean = false,
) {
    val filtered: List<EjercicioResponse>
        get() = all.filter { e ->
            (filterGrupo == null      || e.grupoMuscular == filterGrupo) &&
            (filterDificultad == null || e.dificultad == filterDificultad) &&
            (query.isBlank()          || e.nombre.contains(query, ignoreCase = true))
        }
}

class EjerciciosViewModel(private val repository: EjercicioRepository) : ViewModel() {

    private val _state = MutableStateFlow(EjerciciosUiState())
    val state: StateFlow<EjerciciosUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getEjercicios()
                .onSuccess { list -> _state.update { it.copy(all = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun loadDetail(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetail = true, detail = null) }
            repository.getEjercicio(id)
                .onSuccess { e -> _state.update { it.copy(detail = e, isLoadingDetail = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoadingDetail = false) } }
        }
    }

    fun setGrupo(g: String?)        = _state.update { it.copy(filterGrupo = g) }
    fun setDificultad(d: String?)   = _state.update { it.copy(filterDificultad = d) }
    fun setQuery(q: String)         = _state.update { it.copy(query = q) }
    fun clearDetail()               = _state.update { it.copy(detail = null) }
    fun clearError()                = _state.update { it.copy(error = null) }

    class Factory(private val repo: EjercicioRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EjerciciosViewModel(repo) as T
    }
}
