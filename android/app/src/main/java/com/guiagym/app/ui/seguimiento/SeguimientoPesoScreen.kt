package com.guiagym.app.ui.seguimiento

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiagym.app.data.network.models.SeguimientoPesoResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoPesoScreen(
    viewModel: SeguimientoPesoViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // Historial ordenado de más antiguo a más reciente para la gráfica
    val cronologico = remember(state.registros) {
        state.registros.sortedBy { it.fecha }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Peso") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Registrar peso")
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // ── Resumen ───────────────────────────────────────────────────
                if (state.registros.isNotEmpty()) {
                    item {
                        ResumenCard(cronologico)
                    }
                    // ── Gráfica ───────────────────────────────────────────────
                    item {
                        WeightChart(
                            data     = cronologico,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(vertical = 4.dp),
                        )
                    }
                    item {
                        Text("Historial", style = MaterialTheme.typography.titleLarge)
                    }
                } else {
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Añade tu primer registro pulsando +", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // ── Lista ─────────────────────────────────────────────────────
                items(state.registros, key = { it.id }) { registro ->
                    PesoCard(registro, previous = cronologico.getOrNull(cronologico.indexOf(registro) - 1))
                }

                if (state.error != null) {
                    item {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showDialog) {
        RegistrarPesoDialog(
            isSaving  = state.isSaving,
            onConfirm = { peso, notas ->
                viewModel.registrarPeso(peso, notas) { showDialog = false }
            },
            onDismiss = { showDialog = false },
        )
    }
}

// ── Resumen ───────────────────────────────────────────────────────────────────

@Composable
private fun ResumenCard(data: List<SeguimientoPesoResponse>) {
    val ultimo  = data.last().peso
    val minPeso = data.minOf { it.peso }
    val maxPeso = data.maxOf { it.peso }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem("Actual", "${"%.1f".format(ultimo)} kg")
            StatItem("Mínimo", "${"%.1f".format(minPeso)} kg")
            StatItem("Máximo", "${"%.1f".format(maxPeso)} kg")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Gráfica ───────────────────────────────────────────────────────────────────

@Composable
private fun WeightChart(
    data: List<SeguimientoPesoResponse>,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor    = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val pad    = 12.dp.toPx()
        val w      = size.width  - 2 * pad
        val h      = size.height - 2 * pad
        val weights = data.map { it.peso.toFloat() }
        val minW    = weights.min()
        val maxW    = weights.max()
        val range   = (maxW - minW).coerceAtLeast(0.5f)
        val n       = data.size

        fun xFor(i: Int)  = pad + i.toFloat() / (n - 1) * w
        fun yFor(v: Float) = pad + h - (v - minW) / range * h

        // Líneas de cuadrícula horizontales
        repeat(4) { i ->
            val y = pad + i * h / 3
            drawLine(gridColor, Offset(pad, y), Offset(pad + w, y), strokeWidth = 0.5.dp.toPx())
        }

        // Área bajo la curva
        val area = Path().apply {
            moveTo(xFor(0), pad + h)
            data.forEachIndexed { i, e -> lineTo(xFor(i), yFor(e.peso.toFloat())) }
            lineTo(xFor(n - 1), pad + h)
            close()
        }
        drawPath(area, primaryColor.copy(alpha = 0.12f))

        // Línea
        val line = Path().apply {
            data.forEachIndexed { i, e ->
                val x = xFor(i); val y = yFor(e.peso.toFloat())
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(line, primaryColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Puntos
        data.forEachIndexed { i, e ->
            val c      = Offset(xFor(i), yFor(e.peso.toFloat()))
            val isLast = i == n - 1
            drawCircle(primaryColor, if (isLast) 6.dp.toPx() else 3.5.dp.toPx(), c)
            if (isLast) drawCircle(Color.White, 3.dp.toPx(), c)
        }
    }
}

// ── Tarjeta de registro ───────────────────────────────────────────────────────

@Composable
private fun PesoCard(registro: SeguimientoPesoResponse, previous: SeguimientoPesoResponse?) {
    val fecha = runCatching { LocalDateTime.parse(registro.fecha).format(dateFormatter) }.getOrDefault(registro.fecha)
    val delta = previous?.let { registro.peso - it.peso }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text("${"%.1f".format(registro.peso)} kg", style = MaterialTheme.typography.titleLarge)
                Text(fecha, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!registro.notas.isNullOrBlank()) {
                    Text(registro.notas, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (delta != null) {
                val sign  = if (delta >= 0) "+" else ""
                val color = when {
                    delta > 0 -> MaterialTheme.colorScheme.error
                    delta < 0 -> Color(0xFF2E7D32)
                    else      -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text("$sign${"%.1f".format(delta)}", style = MaterialTheme.typography.titleLarge, color = color)
            }
        }
    }
}

// ── Diálogo ───────────────────────────────────────────────────────────────────

@Composable
private fun RegistrarPesoDialog(
    isSaving: Boolean,
    onConfirm: (Double, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var pesoText by rememberSaveable { mutableStateOf("") }
    var notas    by rememberSaveable { mutableStateOf("") }
    val pesoDouble = pesoText.replace(",", ".").toDoubleOrNull()
    val isValid    = pesoDouble != null && pesoDouble > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Registrar peso") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = pesoText,
                    onValueChange   = { pesoText = it },
                    label           = { Text("Peso (kg) *") },
                    isError         = pesoText.isNotEmpty() && !isValid,
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier        = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value         = notas,
                    onValueChange = { notas = it },
                    label         = { Text("Notas (opcional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 2,
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { pesoDouble?.let { onConfirm(it, notas) } },
                enabled  = isValid && !isSaving,
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
