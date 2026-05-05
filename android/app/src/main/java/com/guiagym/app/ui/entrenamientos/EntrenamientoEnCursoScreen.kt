package com.guiagym.app.ui.entrenamientos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientoEnCursoScreen(
    viewModel: EntrenamientoEnCursoViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val items = remember(state.entrenamiento, state.rutina) { viewModel.workoutItems() }
    var logDialogItem by remember { mutableStateOf<EjercicioWorkoutItem?>(null) }

    LaunchedEffect(state.finalizado) {
        if (state.finalizado) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.rutina?.nombre ?: "Entrenamiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.finalizar() }) {
                        Text("Finalizar", color = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError(); viewModel.load() }) { Text("Reintentar") }
                }

                items.isEmpty() -> Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Entrenamiento libre", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay rutina asociada a este entrenamiento.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick  = { viewModel.finalizar() },
                        colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Finalizar entrenamiento")
                    }
                }

                else -> LazyColumn(
                    contentPadding      = PaddingValues(
                        start   = 16.dp, end = 16.dp,
                        top     = 8.dp,
                        bottom  = if (state.restSeconds > 0) 120.dp else 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.re.id }) { item ->
                        EjercicioWorkoutCard(
                            item     = item,
                            isSaving = state.isSaving,
                            onLogSerie = { logDialogItem = item },
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            AnimatedVisibility(
                visible  = state.restSeconds > 0,
                enter    = slideInVertically(initialOffsetY = { it }),
                exit     = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                RestTimerCard(
                    seconds = state.restSeconds,
                    total   = state.restTotalSeconds,
                    onSkip  = { viewModel.skipTimer() },
                )
            }
        }
    }

    logDialogItem?.let { item ->
        LogSerieDialog(
            ejercicioNombre = item.re.ejercicio.nombre,
            serieNumero     = item.seriesLogueadas + 1,
            repsSugeridas   = item.re.repeticiones,
            pesoSugerido    = item.re.pesoSugerido,
            isSaving        = state.isSaving,
            onConfirm       = { reps, peso ->
                viewModel.logSerie(item.re.ejercicioId, reps, peso, item.re.descansoSegundos)
                logDialogItem = null
            },
            onDismiss = { logDialogItem = null },
        )
    }
}

@Composable
private fun EjercicioWorkoutCard(
    item: EjercicioWorkoutItem,
    isSaving: Boolean,
    onLogSerie: () -> Unit,
) {
    val done = item.seriesLogueadas >= item.re.series
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = if (done)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        else
            CardDefaults.cardColors(),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    item.re.ejercicio.nombre,
                    style    = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${item.seriesLogueadas}/${item.re.series}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                "${item.re.repeticiones} reps  •  ${item.re.descansoSegundos}s descanso",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.re.pesoSugerido?.let {
                Text(
                    "Peso sugerido: ${it}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (done) {
                Text(
                    "Completado",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Button(
                    onClick  = onLogSerie,
                    enabled  = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Registrar serie ${item.seriesLogueadas + 1}")
                }
            }
        }
    }
}

@Composable
private fun RestTimerCard(
    seconds: Int,
    total: Int,
    onSkip: () -> Unit,
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Descanso",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    "${seconds}s",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { seconds.toFloat() / total.coerceAtLeast(1).toFloat() },
                    modifier = Modifier.size(56.dp),
                    color    = MaterialTheme.colorScheme.secondary,
                )
            }
            OutlinedButton(onClick = onSkip) { Text("Saltar") }
        }
    }
}

@Composable
private fun LogSerieDialog(
    ejercicioNombre: String,
    serieNumero: Int,
    repsSugeridas: Int,
    pesoSugerido: Double?,
    isSaving: Boolean,
    onConfirm: (reps: Int?, peso: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var reps by rememberSaveable { mutableStateOf(repsSugeridas.toString()) }
    var peso by rememberSaveable { mutableStateOf(pesoSugerido?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Serie $serieNumero · $ejercicioNombre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = reps,
                    onValueChange   = { reps = it },
                    label           = { Text("Repeticiones") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value           = peso,
                    onValueChange   = { peso = it },
                    label           = { Text("Peso (kg, opcional)") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reps.toIntOrNull(), peso.toDoubleOrNull()) },
                enabled = !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        },
    )
}
