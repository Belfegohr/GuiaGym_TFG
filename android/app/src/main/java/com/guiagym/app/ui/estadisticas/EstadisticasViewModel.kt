package com.guiagym.app.ui.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.guiagym.app.data.network.models.EntrenamientoDetailResponse
import com.guiagym.app.data.network.models.EjercicioResponse
import com.guiagym.app.data.repository.EjercicioRepository
import com.guiagym.app.data.repository.EntrenamientoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val SHORT_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM")

data class ChartPoint(val label: String, val maxWeight: Float)

data class EstadisticasUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val ejercicios: List<EjercicioResponse> = emptyList(),
    val selectedEjercicioId: Int? = null,
    val entrenamientos: List<EntrenamientoDetailResponse> = emptyList(),
) {
    val selectedEjercicio: EjercicioResponse?
        get() = ejercicios.firstOrNull { it.id == selectedEjercicioId }

    val chartPoints: List<ChartPoint>
        get() {
            val id = selectedEjercicioId ?: return emptyList()
            return entrenamientos
                .sortedBy { it.fechaInicio }
                .mapNotNull { session ->
                    val maxW = session.registros
                        .filter { it.ejercicioId == id && it.peso != null }
                        .maxOfOrNull { it.peso!! }
                        ?: return@mapNotNull null
                    val label = runCatching {
                        LocalDate.parse(session.fechaInicio.take(10)).format(SHORT_DATE_FMT)
                    }.getOrDefault(session.fechaInicio.take(5))
                    ChartPoint(label, maxW.toFloat())
                }
        }

    val totalEntrenamientos: Int
        get() = entrenamientos.count { it.fechaFin != null }

    // Consecutive days ending at the most recent training date
    val rachaActual: Int
        get() {
            val days = entrenamientos
                .mapNotNull { runCatching { LocalDate.parse(it.fechaInicio.take(10)) }.getOrNull() }
                .toSortedSet(reverseOrder())
                .toList()
            if (days.isEmpty()) return 0
            var streak = 1
            for (i in 0 until days.size - 1) {
                if (days[i].minusDays(1) == days[i + 1]) streak++ else break
            }
            return streak
        }

    val entrenamientoMasLargo: String
        get() {
            val maxMin = entrenamientos
                .filter { it.fechaFin != null }
                .mapNotNull { t ->
                    runCatching {
                        java.time.Duration.between(
                            LocalDateTime.parse(t.fechaInicio),
                            LocalDateTime.parse(t.fechaFin!!),
                        ).toMinutes()
                    }.getOrNull()
                }
                .maxOrNull() ?: return "—"
            val h = maxMin / 60
            val m = maxMin % 60
            return if (h > 0) "${h}h ${m}min" else "${m}min"
        }

    val musculoMasTrabajado: String
        get() = entrenamientos
            .flatMap { it.registros }
            .mapNotNull { it.ejercicio.grupoMuscular }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "—"
}

class EstadisticasViewModel(
    private val ejercicioRepo: EjercicioRepository,
    private val entrenamientoRepo: EntrenamientoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EstadisticasUiState())
    val state: StateFlow<EstadisticasUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val ejerciciosDeferred = async { ejercicioRepo.getEjercicios() }
            val listaDeferred      = async { entrenamientoRepo.getEntrenamientos() }

            val ejercicios = ejerciciosDeferred.await().getOrElse { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
                return@launch
            }
            val lista = listaDeferred.await().getOrElse { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
                return@launch
            }

            // Fetch all training details concurrently to get registros
            val detalles = lista
                .map { t -> async { entrenamientoRepo.getDetail(t.id).getOrNull() } }
                .awaitAll()
                .filterNotNull()

            _state.update {
                it.copy(
                    ejercicios     = ejercicios,
                    entrenamientos = detalles,
                    isLoading      = false,
                )
            }
        }
    }

    fun selectEjercicio(id: Int) = _state.update { it.copy(selectedEjercicioId = id) }
    fun clearError()              = _state.update { it.copy(error = null) }

    class Factory(
        private val ejercicioRepo: EjercicioRepository,
        private val entrenamientoRepo: EntrenamientoRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EstadisticasViewModel(ejercicioRepo, entrenamientoRepo) as T
    }
}
