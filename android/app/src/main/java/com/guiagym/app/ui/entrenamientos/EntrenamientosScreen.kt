package com.guiagym.app.ui.entrenamientos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiagym.app.data.network.models.EntrenamientoListResponse
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientosScreen(
    viewModel: EntrenamientosViewModel,
    onBack: () -> Unit,
    onNavigateToEntrenamiento: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.iniciadoId) {
        state.iniciadoId?.let { id ->
            viewModel.clearIniciadoId()
            onNavigateToEntrenamiento(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenamientos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Botón iniciar ─────────────────────────────────────────────────
            Button(
                onClick  = { viewModel.iniciar() },
                enabled  = !state.isActioning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                if (state.isActioning) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("  Iniciar entrenamiento")
                }
            }

            if (state.error != null) {
                Text(
                    state.error!!,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            // ── Historial ─────────────────────────────────────────────────────
            Text(
                "Historial",
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.entrenamientos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes entrenamientos registrados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.entrenamientos, key = { it.id }) { entrenamiento ->
                        EntrenamientoCard(
                            entrenamiento = entrenamiento,
                            onFinalizar   = { viewModel.finalizar(entrenamiento.id) },
                            onContinuar   = { onNavigateToEntrenamiento(entrenamiento.id) },
                            isActioning   = state.isActioning,
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EntrenamientoCard(
    entrenamiento: EntrenamientoListResponse,
    onFinalizar: () -> Unit,
    onContinuar: () -> Unit,
    isActioning: Boolean,
) {
    val enCurso  = entrenamiento.fechaFin == null
    val inicio   = runCatching { LocalDateTime.parse(entrenamiento.fechaInicio) }.getOrNull()
    val fin      = entrenamiento.fechaFin?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
    val duracion = if (inicio != null && fin != null) {
        val mins = Duration.between(inicio, fin).toMinutes()
        if (mins >= 60) "${mins / 60}h ${mins % 60}min" else "${mins}min"
    } else null

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .then(if (enCurso) Modifier.clickable { onContinuar() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = if (enCurso)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else
            CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    inicio?.format(dateFormatter) ?: entrenamiento.fechaInicio,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (enCurso) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text("En curso", modifier = Modifier.padding(horizontal = 4.dp))
                    }
                } else if (duracion != null) {
                    Text(duracion, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (!entrenamiento.notas.isNullOrBlank()) {
                Text(entrenamiento.notas, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (enCurso) {
                Button(
                    onClick  = onContinuar,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Continuar entrenamiento")
                }
                OutlinedButton(
                    onClick  = onFinalizar,
                    enabled  = !isActioning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Finalizar")
                }
            }
        }
    }
}
