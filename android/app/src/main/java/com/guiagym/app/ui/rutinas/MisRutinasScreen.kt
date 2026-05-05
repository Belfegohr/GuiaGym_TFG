package com.guiagym.app.ui.rutinas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guiagym.app.data.network.models.RutinaListResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisRutinasScreen(
    viewModel: MisRutinasViewModel,
    onBack: () -> Unit,
    onNavigateToRutina: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Rutinas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nueva rutina")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> ErrorState(state.error!!, onRetry = { viewModel.clearError(); viewModel.load() })
                state.rutinas.isEmpty() -> EmptyState("No tienes rutinas aún.\nPulsa + para crear una.")
                else -> RutinasList(state.rutinas, onNavigateToRutina)
            }
        }
    }

    if (showDialog) {
        CreateRutinaDialog(
            isSaving  = state.isSaving,
            onConfirm = { nombre, descripcion, publica ->
                viewModel.createRutina(nombre, descripcion, publica) { showDialog = false }
            },
            onDismiss = { showDialog = false },
        )
    }
}

@Composable
private fun RutinasList(rutinas: List<RutinaListResponse>, onNavigateToRutina: (Int) -> Unit) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(rutinas, key = { it.id }) { rutina ->
            RutinaCard(rutina, onClick = { onNavigateToRutina(rutina.id) })
        }
    }
}

@Composable
private fun RutinaCard(rutina: RutinaListResponse, onClick: () -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(rutina.nombre, style = MaterialTheme.typography.titleLarge)
            if (!rutina.descripcion.isNullOrBlank()) {
                Text(rutina.descripcion, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(
                onClick  = {},
                label    = { Text(if (rutina.publica) "Pública" else "Privada") },
            )
        }
    }
}

@Composable
private fun CreateRutinaDialog(
    isSaving: Boolean,
    onConfirm: (nombre: String, descripcion: String, publica: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var nombre      by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var publica     by rememberSaveable { mutableStateOf(false) }
    val nombreError = nombre.isBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Nueva rutina") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    isError       = nombreError && nombre.isNotEmpty(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier      = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción (opcional)") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3,
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text("Pública", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = publica, onCheckedChange = { publica = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { if (!nombreError) onConfirm(nombre, descripcion, publica) },
                enabled  = !isSaving && !nombreError,
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        },
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
