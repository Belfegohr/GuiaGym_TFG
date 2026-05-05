package com.guiagym.app.ui.ejercicios

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiagym.app.data.network.models.EjercicioResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EjerciciosScreen(
    viewModel: EjerciciosViewModel,
    pickerMode: Boolean = false,
    onBack: () -> Unit,
    onPick: ((Int) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedEjercicio by remember { mutableStateOf<EjercicioResponse?>(null) }

    val grupos = remember(state.all) {
        state.all.mapNotNull { it.grupoMuscular }.distinct().sorted()
    }
    val dificultades = remember(state.all) {
        state.all.mapNotNull { it.dificultad }.distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (pickerMode) "Seleccionar ejercicio" else "Ejercicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value         = state.query,
                onValueChange = { viewModel.setQuery(it) },
                placeholder   = { Text("Buscar ejercicio...") },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon  = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (grupos.isNotEmpty()) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    item {
                        FilterChip(
                            selected = state.filterGrupo == null,
                            onClick  = { viewModel.setGrupo(null) },
                            label    = { Text("Todos") },
                        )
                    }
                    items(grupos) { grupo ->
                        FilterChip(
                            selected = state.filterGrupo == grupo,
                            onClick  = { viewModel.setGrupo(if (state.filterGrupo == grupo) null else grupo) },
                            label    = { Text(grupo) },
                        )
                    }
                }
            }

            if (dificultades.isNotEmpty()) {
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth(),
                ) {
                    items(dificultades) { dif ->
                        FilterChip(
                            selected = state.filterDificultad == dif,
                            onClick  = { viewModel.setDificultad(if (state.filterDificultad == dif) null else dif) },
                            label    = { Text(dif) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            HorizontalDivider()

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Column(
                    Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError(); viewModel.load() }) { Text("Reintentar") }
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin resultados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.filtered, key = { it.id }) { ej ->
                        EjercicioCard(
                            ejercicio  = ej,
                            pickerMode = pickerMode,
                            onClick    = {
                                if (pickerMode) onPick?.invoke(ej.id)
                                else selectedEjercicio = ej
                            },
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    selectedEjercicio?.let { ej ->
        EjercicioDetailDialog(
            ejercicio = ej,
            onDismiss = { selectedEjercicio = null },
        )
    }
}

@Composable
private fun EjercicioCard(
    ejercicio: EjercicioResponse,
    pickerMode: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(ejercicio.nombre, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ejercicio.grupoMuscular?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                ejercicio.dificultad?.let    { AssistChip(onClick = {}, label = { Text(it) }) }
            }
            if (!ejercicio.descripcion.isNullOrBlank()) {
                Text(
                    ejercicio.descripcion,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (pickerMode) 1 else 2,
                )
            }
        }
    }
}

@Composable
private fun EjercicioDetailDialog(
    ejercicio: EjercicioResponse,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(ejercicio.nombre) },
        text    = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ejercicio.grupoMuscular?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                    ejercicio.dificultad?.let    { AssistChip(onClick = {}, label = { Text(it) }) }
                    ejercicio.tipo?.let          { AssistChip(onClick = {}, label = { Text(it) }) }
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    MuscleGroupDiagram(
                        grupoMuscular = ejercicio.grupoMuscular,
                        modifier      = Modifier.size(width = 100.dp, height = 150.dp),
                    )
                }
                if (!ejercicio.descripcion.isNullOrBlank()) {
                    Text(ejercicio.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
    )
}

@Composable
private fun MuscleGroupDiagram(
    grupoMuscular: String?,
    modifier: Modifier = Modifier,
) {
    val grupo     = grupoMuscular?.lowercase()?.trim()
    val highlight = when (grupo) {
        "pecho"   -> Color(0xFFE53935)
        "espalda" -> Color(0xFF1E88E5)
        "piernas" -> Color(0xFF43A047)
        "hombros" -> Color(0xFFFF8F00)
        "brazos"  -> Color(0xFF8E24AA)
        "abdomen" -> Color(0xFFF4511E)
        "cardio"  -> Color(0xFFD81B60)
        else      -> Color(0xFF9E9E9E)
    }
    val base     = Color(0xFFCFD8DC)
    val isCardio = grupo == "cardio"

    fun active(condition: Boolean) = if (condition || isCardio) highlight else base

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cr = CornerRadius(w * 0.06f)

        drawCircle(color = base, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.09f))

        drawCircle(color = active(grupo == "hombros"), radius = w * 0.09f, center = Offset(w * 0.30f, h * 0.24f))
        drawCircle(color = active(grupo == "hombros"), radius = w * 0.09f, center = Offset(w * 0.70f, h * 0.24f))

        drawRoundRect(
            color        = active(grupo == "brazos" || grupo == "hombros"),
            topLeft      = Offset(w * 0.18f, h * 0.24f),
            size         = Size(w * 0.15f, h * 0.37f),
            cornerRadius = cr,
        )
        drawRoundRect(
            color        = active(grupo == "brazos" || grupo == "hombros"),
            topLeft      = Offset(w * 0.67f, h * 0.24f),
            size         = Size(w * 0.15f, h * 0.37f),
            cornerRadius = cr,
        )

        drawRoundRect(
            color        = active(grupo == "pecho" || grupo == "espalda"),
            topLeft      = Offset(w * 0.35f, h * 0.20f),
            size         = Size(w * 0.30f, h * 0.26f),
            cornerRadius = cr,
        )

        drawRoundRect(
            color        = active(grupo == "abdomen"),
            topLeft      = Offset(w * 0.37f, h * 0.46f),
            size         = Size(w * 0.26f, h * 0.17f),
            cornerRadius = cr,
        )

        drawRoundRect(
            color        = active(grupo == "piernas"),
            topLeft      = Offset(w * 0.35f, h * 0.63f),
            size         = Size(w * 0.13f, h * 0.35f),
            cornerRadius = cr,
        )
        drawRoundRect(
            color        = active(grupo == "piernas"),
            topLeft      = Offset(w * 0.52f, h * 0.63f),
            size         = Size(w * 0.13f, h * 0.35f),
            cornerRadius = cr,
        )
    }
}
