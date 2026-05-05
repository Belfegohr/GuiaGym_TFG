package com.guiagym.app.ui.rutinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.RutinaDetailResponse
import com.guiagym.app.data.network.models.RutinaEjercicioCreateRequest
import com.guiagym.app.data.repository.EntrenamientoRepository
import com.guiagym.app.data.repository.RutinaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RutinaDetailUiState(
    val rutina: RutinaDetailResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    // flujo añadir ejercicio
    val pickedEjercicioId: Int = -1,   // -1 = ninguno
    val isSaving: Boolean = false,
    // flujo iniciar entrenamiento
    val entrenamientoIniciadoId: Int? = null,
)

class RutinaDetailViewModel(
    private val rutinaId: Int,
    private val repository: RutinaRepository,
    private val entrenamientoRepository: EntrenamientoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RutinaDetailUiState())
    val state: StateFlow<RutinaDetailUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getRutinaDetail(rutinaId)
                .onSuccess { r -> _state.update { it.copy(rutina = r, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun setPickedEjercicio(id: Int) = _state.update { it.copy(pickedEjercicioId = id) }
    fun clearPickedEjercicio()      = _state.update { it.copy(pickedEjercicioId = -1) }

    fun addEjercicio(
        ejercicioId: Int,
        series: Int,
        repeticiones: Int,
        descansoSegundos: Int,
        diaSemana: String?,
        onDone: () -> Unit,
    ) {
        val orden = (_state.value.rutina?.ejercicios?.size ?: 0)
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            repository.addEjercicio(
                rutinaId,
                RutinaEjercicioCreateRequest(
                    ejercicioId      = ejercicioId,
                    series           = series,
                    repeticiones     = repeticiones,
                    descansoSegundos = descansoSegundos,
                    orden            = orden,
                    diaSemana        = diaSemana,
                ),
            )
                .onSuccess { load(); _state.update { it.copy(isSaving = false, pickedEjercicioId = -1) }; onDone() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun removeEjercicio(reId: Int) {
        viewModelScope.launch {
            repository.removeEjercicio(rutinaId, reId)
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun iniciarEntrenamiento() {
        val rid = _state.value.rutina?.id ?: return
        viewModelScope.launch {
            entrenamientoRepository.iniciarEntrenamiento(rid)
                .onSuccess { e -> _state.update { it.copy(entrenamientoIniciadoId = e.id) } }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun clearEntrenamientoIniciadoId() = _state.update { it.copy(entrenamientoIniciadoId = null) }

    fun clearError() = _state.update { it.copy(error = null) }

    class Factory(
        private val rutinaId: Int,
        private val repo: RutinaRepository,
        private val entrenamientoRepo: EntrenamientoRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RutinaDetailViewModel(rutinaId, repo, entrenamientoRepo) as T
    }
}
