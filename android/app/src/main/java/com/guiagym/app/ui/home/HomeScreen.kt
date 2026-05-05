package com.guiagym.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Section(val emoji: String, val title: String, val subtitle: String, val onClick: () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateRutinas: () -> Unit,
    onNavigateEntrenam: () -> Unit,
    onNavigateSeguim: () -> Unit,
    onNavigateEjercicios: () -> Unit,
    onNavigateEstadisticas: () -> Unit,
    onNavigatePerfil: () -> Unit,
) {
    val sections = listOf(
        Section("💪", "Mis Rutinas",          "Crea y gestiona tus rutinas de entrenamiento",  onNavigateRutinas),
        Section("🏋️", "Entrenamientos",       "Registra tus sesiones y series en tiempo real", onNavigateEntrenam),
        Section("📚", "Ejercicios",           "Explora el catálogo completo de ejercicios",     onNavigateEjercicios),
        Section("⚖️", "Seguimiento de Peso",  "Controla tu evolución corporal con gráficas",   onNavigateSeguim),
        Section("📊", "Estadísticas",         "Analiza tu progreso y evolución por ejercicio",  onNavigateEstadisticas),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title  = { Text("GuiaGym", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor         = MaterialTheme.colorScheme.primary,
                    titleContentColor      = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = onNavigatePerfil) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Mi perfil")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("¡Bienvenido!", style = MaterialTheme.typography.titleLarge)
            Text("¿Qué quieres hacer hoy?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))

            sections.forEach { section ->
                SectionCard(section)
            }
        }
    }
}

@Composable
private fun SectionCard(section: Section) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { section.onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${section.emoji}  ${section.title}", style = MaterialTheme.typography.titleLarge)
                Text(section.subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier           = Modifier.size(24.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
