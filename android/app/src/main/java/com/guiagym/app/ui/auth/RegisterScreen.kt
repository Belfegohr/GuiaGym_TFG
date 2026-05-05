package com.guiagym.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var nombre   by rememberSaveable { mutableStateOf("") }
    var email    by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    val isLoading = uiState is RegisterUiState.Loading
    val errorMsg  = (uiState as? RegisterUiState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("GuiaGym", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value         = nombre,
                onValueChange = { nombre = it; viewModel.resetState() },
                label         = { Text("Nombre") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier      = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it; viewModel.resetState() },
                label         = { Text("Email") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier      = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value           = password,
                onValueChange   = { password = it; viewModel.resetState() },
                label           = { Text("Contraseña (mín. 8 caracteres)") },
                singleLine      = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    viewModel.register(nombre, email, password)
                }),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            if (errorMsg != null) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = { focusManager.clearFocus(); viewModel.register(nombre, email, password) },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Registrarse")
                }
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
