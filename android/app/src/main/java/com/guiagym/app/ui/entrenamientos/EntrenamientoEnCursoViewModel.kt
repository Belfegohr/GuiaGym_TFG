package com.guiagym.app.ui.entrenamientos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.EntrenamientoDetailResponse
import com.guiagym.app.data.network.models.RegistroEjercicioCreateRequest
import com.guiagym.app.data.network.models.RutinaDetailResponse
import com.guiagym.app.data.network.models.RutinaEjercicioResponse
import com.guiagym.app.data.repository.EntrenamientoRepository
import com.guiagym.app.data.repository.RutinaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EjercicioWorkoutItem(
    val re: RutinaEjercicioResponse,   // configuración de la rutina
    val seriesLogueadas: Int,           // cuántas series ya se registraron
)

data class EntrenamientoEnCursoState(
    val entrenamiento: EntrenamientoDetailResponse? = null,
    val rutina: RutinaDetailResponse? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val restSeconds: Int = 0,
    val restTotalSeconds: Int = 0,
    val finalizado: Boolean = false,
)

class EntrenamientoEnCursoViewModel(
    private val entrenamientoId: Int,
    private val entrenamientoRepo: EntrenamientoRepository,
    private val rutinaRepo: RutinaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EntrenamientoEnCursoState())
    val state: StateFlow<EntrenamientoEnCursoState> = _state.asStateFlow()

    private var timerJob: Job? = null

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            entrenamientoRepo.getDetail(entrenamientoId)
                .onSuccess { detalle ->
                    val rutina = detalle.rutinaId?.let { rid ->
                        rutinaRepo.getRutinaDetail(rid).getOrNull()
                    }
                    _state.update { it.copy(entrenamiento = detalle, rutina = rutina, isLoading = false) }
                }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /** Items para mostrar en pantalla: ejercicios de la rutina + cuántas series se registraron ya. */
    fun workoutItems(): List<EjercicioWorkoutItem> {
        val s = _state.value
        val rutinaEjercicios = s.rutina?.ejercicios ?: return emptyList()
        val registros = s.entrenamiento?.registros ?: emptyList()
        return rutinaEjercicios.sortedBy { it.orden }.map { re ->
            val logueadas = registros.count { it.ejercicioId == re.ejercicioId }
            EjercicioWorkoutItem(re, logueadas)
        }
    }

    fun logSerie(ejercicioId: Int, reps: Int?, peso: Double?, descansoSegundos: Int) {
        val serieNumero = (_state.value.entrenamiento?.registros
            ?.count { it.ejercicioId == ejercicioId } ?: 0) + 1

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            entrenamientoRepo.addRegistro(
                entrenamientoId,
                RegistroEjercicioCreateRequest(ejercicioId, serieNumero, reps, peso),
            )
                .onSuccess {
                    load()
                    _state.update { it.copy(isSaving = false) }
                    startRestTimer(descansoSegundos)
                }
                .onFailure { e -> _state.update { it.copy(error = e.message, isSaving = false) } }
        }
    }

    fun finalizar() {
        viewModelScope.launch {
            timerJob?.cancel()
            entrenamientoRepo.finalizarEntrenamiento(entrenamientoId)
                .onSuccess { _state.update { it.copy(finalizado = true) } }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _state.update { it.copy(restSeconds = seconds, restTotalSeconds = seconds) }
        timerJob = viewModelScope.launch {
            for (remaining in seconds downTo 1) {
                _state.update { it.copy(restSeconds = remaining) }
                delay(1_000)
            }
            _state.update { it.copy(restSeconds = 0) }
        }
    }

    fun skipTimer() {
        timerJob?.cancel()
        _state.update { it.copy(restSeconds = 0) }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() { timerJob?.cancel() }

    class Factory(
        private val entrenamientoId: Int,
        private val entrenamientoRepo: EntrenamientoRepository,
        private val rutinaRepo: RutinaRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EntrenamientoEnCursoViewModel(entrenamientoId, entrenamientoRepo, rutinaRepo) as T
    }
}
