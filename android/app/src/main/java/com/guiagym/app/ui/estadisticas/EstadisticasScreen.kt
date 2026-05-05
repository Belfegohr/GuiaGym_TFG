package com.guiagym.app.ui.estadisticas

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.clearError(); viewModel.load() }) { Text("Reintentar") }
            }

            else -> LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { ResumenCard(state) }
                item {
                    ExerciseProgressSection(
                        state              = state,
                        onSelectEjercicio  = { viewModel.selectEjercicio(it) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Resumen general ───────────────────────────────────────────────────────────

@Composable
private fun ResumenCard(state: EstadisticasUiState) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Resumen general", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("Entrenamientos",      "${state.totalEntrenamientos}",      Modifier.weight(1f))
                StatItem("Racha actual",        "${state.rachaActual} días",         Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                StatItem("Más largo",           state.entrenamientoMasLargo,         Modifier.weight(1f))
                StatItem("Músculo top",         state.musculoMasTrabajado,           Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Progreso por ejercicio ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseProgressSection(
    state: EstadisticasUiState,
    onSelectEjercicio: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Progreso por ejercicio", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded          = expanded,
                onExpandedChange  = { expanded = it },
            ) {
                OutlinedTextField(
                    value         = state.selectedEjercicio?.nombre ?: "Selecciona un ejercicio",
                    onValueChange = {},
                    readOnly      = true,
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier      = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded          = expanded,
                    onDismissRequest  = { expanded = false },
                ) {
                    state.ejercicios.sortedBy { it.nombre }.forEach { ej ->
                        DropdownMenuItem(
                            text    = { Text(ej.nombre) },
                            onClick = { onSelectEjercicio(ej.id); expanded = false },
                        )
                    }
                }
            }

            when {
                state.selectedEjercicioId == null ->
                    EmptyChartBox("Selecciona un ejercicio para ver su evolución")

                state.chartPoints.isEmpty() ->
                    EmptyChartBox("Sin datos registrados para este ejercicio")

                state.chartPoints.size == 1 -> {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${"%.1f".format(state.chartPoints[0].maxWeight)} kg",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                "1 sesión registrada",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> ExerciseChart(
                    data     = state.chartPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyChartBox(message: String) {
    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// ── Gráfica de líneas ─────────────────────────────────────────────────────────

@Composable
private fun ExerciseChart(
    data: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelColor   = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val gridColor    = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle   = TextStyle(color = labelColor, fontSize = 9.sp)

    Canvas(modifier = modifier) {
        val padL = 44.dp.toPx()
        val padR = 8.dp.toPx()
        val padT = 8.dp.toPx()
        val padB = 24.dp.toPx()
        val cW   = size.width  - padL - padR
        val cH   = size.height - padT - padB
        val n    = data.size

        val weights = data.map { it.maxWeight }
        val minW    = weights.min()
        val maxW    = weights.max()
        val range   = (maxW - minW).coerceAtLeast(5f)

        fun xFor(i: Int)   = padL + i.toFloat() / (n - 1) * cW
        fun yFor(v: Float) = padT + cH - (v - minW) / range * cH

        // Grid lines + Y axis labels (right-aligned before the chart area)
        repeat(4) { i ->
            val v      = minW + i * range / 3f
            val y      = yFor(v)
            drawLine(gridColor, Offset(padL, y), Offset(padL + cW, y), strokeWidth = 0.5.dp.toPx())
            val layout = textMeasurer.measure("${"%.0f".format(v)}kg", style = labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft          = Offset(
                    x = padL - 4.dp.toPx() - layout.size.width,
                    y = y - layout.size.height / 2f,
                ),
            )
        }

        // Area under the line
        val area = Path().apply {
            moveTo(xFor(0), padT + cH)
            data.forEachIndexed { i, p -> lineTo(xFor(i), yFor(p.maxWeight)) }
            lineTo(xFor(n - 1), padT + cH)
            close()
        }
        drawPath(area, primaryColor.copy(alpha = 0.12f))

        // Line
        val line = Path().apply {
            data.forEachIndexed { i, p ->
                val x = xFor(i); val y = yFor(p.maxWeight)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(line, primaryColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Dots — last point highlighted
        data.forEachIndexed { i, p ->
            val center = Offset(xFor(i), yFor(p.maxWeight))
            val isLast = i == n - 1
            drawCircle(primaryColor, if (isLast) 6.dp.toPx() else 3.5.dp.toPx(), center)
            if (isLast) drawCircle(surfaceColor, 3.dp.toPx(), center)
        }

        // X axis labels (centered below each point): first, middle, last
        val labelIdx = if (n <= 4) (0 until n).toList() else listOf(0, n / 2, n - 1)
        labelIdx.forEach { i ->
            val layout = textMeasurer.measure(data[i].label, style = labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft          = Offset(
                    x = xFor(i) - layout.size.width / 2f,
                    y = padT + cH + 8.dp.toPx(),
                ),
            )
        }
    }
}
