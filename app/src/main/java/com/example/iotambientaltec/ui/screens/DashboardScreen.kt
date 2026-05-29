package com.example.iotambientaltec.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iotambientaltec.ui.components.EmptyState
import com.example.iotambientaltec.ui.components.MetricCard

@Composable
fun DashboardScreen(factory: AppViewModelFactory) {
    val vm: DashboardViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    if (state.loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } else LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Dashboard ambiental", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        state.error?.let { item { EmptyState(it) } }
        items(state.latest.size) { index ->
            val item = state.latest[index]
            MetricCard("Última ${variableLabels[item.variable] ?: item.variable}", "${"%.1f".format(item.value)} ${item.unit}", "${item.date} ${item.time} · ${item.sensorId}")
        }
        item { MetricCard("Máximo histórico (temperatura)", state.max?.let { "${"%.1f".format(it.value)} ${it.unit}" } ?: "Sin datos", state.max?.let { "${it.date} ${it.time}" } ?: "") }
        item { MetricCard("Mínimo histórico (temperatura)", state.min?.let { "${"%.1f".format(it.value)} ${it.unit}" } ?: "Sin datos", state.min?.let { "${it.date} ${it.time}" } ?: "") }
    }
}
