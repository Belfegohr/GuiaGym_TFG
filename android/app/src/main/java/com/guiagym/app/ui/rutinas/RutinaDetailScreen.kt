package com.guiagym.app.ui.rutinas

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiagym.app.data.network.models.RutinaEjercicioResponse

private val DIAS_SEMANA = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinaDetailScreen(
    viewModel: RutinaDetailViewModel,
    onBack: () -> Unit,
    onPickEjercicio: () -> Unit,
    onIniciarEntrenamiento: (rutinaId: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var expandedDays by remember { mutableStateOf((DIAS_SEMANA + "Sin asignar").toSet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.rutina?.nombre ?: "Rutina") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    state.rutina?.let { rutina ->
                        IconButton(onClick = { onIniciarEntrenamiento(rutina.id) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Iniciar entrenamiento")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPickEjercicio) {
                Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
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

                else -> {
                    val ejercicios = state.rutina?.ejercicios?.sortedBy { it.orden } ?: emptyList()
                    if (ejercicios.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Sin ejercicios.\nPulsa + para añadir.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        val byDay = ejercicios.groupBy { re ->
                            re.diaSemana?.trim()?.takeIf { it.isNotBlank() } ?: "Sin asignar"
                        }
                        val daysWithContent = (DIAS_SEMANA + "Sin asignar").filter { byDay.containsKey(it) }

                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            daysWithContent.forEach { day ->
                                val dayEjercicios = byDay[day] ?: return@forEach
                                val isExpanded = day in expandedDays

                                item(key = "header_$day") {
                                    DayHeader(
                                        day        = day,
                                        count      = dayEjercicios.size,
                                        isExpanded = isExpanded,
                                        onToggle   = {
                                            expandedDays = if (isExpanded)
                                                expandedDays - day
                                            else
                                                expandedDays + day
                                        },
                                    )
                                }

                                if (isExpanded) {
                                    items(dayEjercicios, key = { "ex_${it.id}" }) { re ->
                                        EjercicioRutinaCard(
                                            re       = re,
                                            onDelete = { viewModel.removeEjercicio(re.id) },
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(72.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (state.pickedEjercicioId != -1) {
        AddEjercicioDialog(
            isSaving  = state.isSaving,
            onConfirm = { series, reps, descanso, dia ->
                viewModel.addEjercicio(
                    ejercicioId      = state.pickedEjercicioId,
                    series           = series,
                    repeticiones     = reps,
                    descansoSegundos = descanso,
                    diaSemana        = dia,
                    onDone           = {},
                )
            },
            onDismiss = { viewModel.clearPickedEjercicio() },
        )
    }
}

@Composable
private fun DayHeader(
    day: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            day,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "$count ${if (count == 1) "ejercicio" else "ejercicios"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector        = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun EjercicioRutinaCard(
    re: RutinaEjercicioResponse,
    onDelete: () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(re.ejercicio.nombre, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${re.series} × ${re.repeticiones} reps  •  ${re.descansoSegundos}s descanso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                re.pesoSugerido?.let {
                    Text(
                        "Peso sugerido: ${it}kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEjercicioDialog(
    isSaving: Boolean,
    onConfirm: (series: Int, reps: Int, descanso: Int, diaSemana: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var series           by rememberSaveable { mutableStateOf("3") }
    var reps             by rememberSaveable { mutableStateOf("10") }
    var descanso         by rememberSaveable { mutableStateOf("60") }
    var dia              by rememberSaveable { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Configurar ejercicio") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value         = dia ?: "Sin asignar",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Día de la semana") },
                        trailingIcon  = {
                            Icon(
                                if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { dropdownExpanded = !dropdownExpanded })
                    DropdownMenu(
                        expanded         = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text    = { Text("Sin asignar") },
                            onClick = { dia = null; dropdownExpanded = false },
                        )
                        DIAS_SEMANA.forEach { d ->
                            DropdownMenuItem(
                                text    = { Text(d) },
                                onClick = { dia = d; dropdownExpanded = false },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value           = series,
                    onValueChange   = { series = it },
                    label           = { Text("Series") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value           = reps,
                    onValueChange   = { reps = it },
                    label           = { Text("Repeticiones") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value           = descanso,
                    onValueChange   = { descanso = it },
                    label           = { Text("Descanso (segundos)") },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier        = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        series.toIntOrNull() ?: 3,
                        reps.toIntOrNull() ?: 10,
                        descanso.toIntOrNull() ?: 60,
                        dia,
                    )
                },
                enabled = !isSaving,
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        },
    )
}
