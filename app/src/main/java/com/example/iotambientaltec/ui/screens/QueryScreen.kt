package com.example.iotambientaltec.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iotambientaltec.ui.components.EmptyState
import com.example.iotambientaltec.ui.components.MetricCard

@Composable
fun QueryScreen(factory: AppViewModelFactory) {
    val vm: QueryViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    val clipboard = LocalClipboardManager.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Consultas ambientales", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { VariableDropdown(state.variable, vm::setVariable) }
        item { QueryTypeDropdown(state.type, vm::setType) }
        item { OutlinedTextField(state.startDate, vm::setStartDate, Modifier.fillMaxWidth(), label = { Text("Fecha inicial o día específico (yyyy-MM-dd)") }, isError = !com.example.iotambientaltec.utils.DateUtils.isRangeValid(state.startDate, state.endDate)) }
        item { OutlinedTextField(state.endDate, vm::setEndDate, Modifier.fillMaxWidth(), label = { Text("Fecha final (yyyy-MM-dd)") }, isError = !com.example.iotambientaltec.utils.DateUtils.isRangeValid(state.startDate, state.endDate)) }
        item { Button(onClick = vm::query, enabled = state.isValid && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (state.loading) "Consultando..." else "Consultar") } }
        state.error?.let { item { EmptyState(it) } }
        items(state.results.size) { index -> val r = state.results[index]; MetricCard(r.label, "${"%.2f".format(r.value)} ${r.unit}", listOf(r.date, r.time, r.sensorId).filter { it.isNotBlank() }.joinToString(" · ")) }
        if (state.results.isNotEmpty()) item {
            OutlinedButton(onClick = { clipboard.setText(AnnotatedString(state.csv)) }, modifier = Modifier.fillMaxWidth()) { Text("Exportar CSV (copiar al portapapeles)") }
            Text(state.csv, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariableDropdown(value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(variableLabels[value] ?: "", {}, Modifier.menuAnchor().fillMaxWidth(), readOnly = true, label = { Text("Variable ambiental") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded, { expanded = false }) { variableLabels.forEach { (key, label) -> DropdownMenuItem(text = { Text(label) }, onClick = { onChange(key); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryTypeDropdown(value: QueryType?, onChange: (QueryType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(value?.label ?: "", {}, Modifier.menuAnchor().fillMaxWidth(), readOnly = true, label = { Text("Tipo de consulta") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded, { expanded = false }) { QueryType.entries.forEach { item -> DropdownMenuItem(text = { Text(item.label) }, onClick = { onChange(item); expanded = false }) } }
    }
}
