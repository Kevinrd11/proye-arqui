package com.example.iotambientaltec.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onDashboard: () -> Unit, onQueries: () -> Unit, onCharts: () -> Unit, onMap: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("IoT Ambiental TEC", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Consulta académica de sensores ambientales del TEC San Carlos.", style = MaterialTheme.typography.bodyLarge)
        HomeCard("Dashboard ambiental", "Resumen de últimos valores, promedios y extremos.", Icons.Default.Dashboard, onDashboard)
        HomeCard("Consultas estadísticas", "Promedios por hora, por día, máximos y mínimos.", Icons.Default.Search, onQueries)
        HomeCard("Gráficos de evolución", "Líneas y comparación entre variables.", Icons.Default.ShowChart, onCharts)
        HomeCard("Mapa interactivo", "Puntos de interés del TEC San Carlos con sensores.", Icons.Default.Map, onMap)
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
