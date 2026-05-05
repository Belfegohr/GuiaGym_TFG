package com.guiagym.app.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar("Perfil actualizado")
            viewModel.clearSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null && state.usuario == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.clearError(); viewModel.load() }) { Text("Reintentar") }
            }

            else -> PerfilForm(
                state    = state,
                onSave   = { nombre, peso, altura -> viewModel.save(nombre, peso, altura) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun PerfilForm(
    state: PerfilUiState,
    onSave: (String, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val u = state.usuario

    var nombre by rememberSaveable(u?.nombre)      { mutableStateOf(u?.nombre ?: "") }
    var peso   by rememberSaveable(u?.pesoInicial) { mutableStateOf(u?.pesoInicial?.toString() ?: "") }
    var altura by rememberSaveable(u?.altura)      { mutableStateOf(u?.altura?.toString() ?: "") }

    Column(
        modifier            = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Cuenta", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(u?.email ?: "", style = MaterialTheme.typography.bodyLarge)
            }
        }

        OutlinedTextField(
            value           = nombre,
            onValueChange   = { nombre = it },
            label           = { Text("Nombre") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier        = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value           = peso,
            onValueChange   = { peso = it },
            label           = { Text("Peso inicial (kg)") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier        = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value           = altura,
            onValueChange   = { altura = it },
            label           = { Text("Altura (cm)") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier        = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Text(
                state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Button(
            onClick  = { onSave(nombre, peso.toDoubleOrNull(), altura.toDoubleOrNull()) },
            enabled  = !state.isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text("Guardar cambios")
            }
        }
    }
}
