package com.example.iotambientaltec.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iotambientaltec.data.model.EnvironmentalData
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChartsScreen(factory: AppViewModelFactory) {
    val vm: ChartsViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsState()
    
    // Debug para ver si llegan datos al estado de la UI
    LaunchedEffect(state.data) {
        android.util.Log.d("CHARTS_UI_DEBUG", "Datos en UI: ${state.data.size} items para ${state.variable}")
    }

    val selected = state.data
        .filter { it.variable == state.variable }
        .sortedBy { it.timestamp }
        .takeLast(192) // 4 días (48 slots/día)

    val comparison = state.data
        .sortedBy { it.timestamp }
        .takeLast(576) // 4 días x 3 variables
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { 
            Column {
                Text("Gráficos Ambientales", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Fuente: Firestore Real-Time v2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        item { 
            val variablesEncontradas = state.data.map { it.variable }.distinct().filter { it != "sin_variable" }.joinToString(", ")
            val sinVariableCount = state.data.count { it.variable == "sin_variable" }
            
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(
                    "Conectado a Firebase: ${state.data.size} registros encontrados.", 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.data.isNotEmpty()) Color(0xFF0B6E4F) else Color.Red
                )
                if (variablesEncontradas.isNotEmpty()) {
                    Text("Variables: $variablesEncontradas", style = MaterialTheme.typography.labelSmall)
                }
                if (sinVariableCount > 0) {
                    Text("⚠️ $sinVariableCount documentos tienen mal el nombre del campo 'variable'", 
                         color = Color.Red, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { VariableDropdown(state.variable, vm::setVariable) }
        item { ChartCard("Evolución temporal - ${variableLabels[state.variable]}") { LineChart(selected) } }
        item { ChartCard("Comparación entre variables (líneas traslapadas)") { ComparisonChart(comparison) } }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) = ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(12.dp)); content() } }

@Composable
private fun LineChart(data: List<EnvironmentalData>) {
    val stats = remember(data) {
        val values = data.map { it.value }
        val rawMin = values.minOrNull() ?: 0.0
        val rawMax = values.maxOrNull() ?: 1.0
        val avg = if (values.isNotEmpty()) values.average() else 0.0

        val padding = ((rawMax - rawMin) * 0.1).coerceAtLeast(1.0)
        val min = rawMin - padding
        val max = rawMax + padding
        Triple(min, max, avg)
    }
    val min = stats.first
    val max = stats.second
    val avg = stats.third
    val range = (max - min).takeIf { it > 0 } ?: 1.0
    val axisDates = remember(data) { data.map { it.date } }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            // EJE Y a la izquierda
            YAxisLabels(min = min, max = max)
            
            Canvas(Modifier.weight(1f).height(220.dp).background(Color(0xFFF1F3F4).copy(alpha = 0.3f))) {
                if (data.size < 2) return@Canvas

                // Líneas de cuadrícula horizontales
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = i * size.height / gridLines
                    drawLine(Color.Gray.copy(alpha = 0.1f), Offset(0f, y), Offset(size.width, y), 1f)
                }

                val timestamps = data.map { it.timestamp }
                val minTs = timestamps.minOrNull() ?: 0L
                val maxTs = timestamps.maxOrNull() ?: 1L
                val tsRange = (maxTs - minTs).coerceAtLeast(1L)

                val yAvg = size.height - (((avg - min) / range).toFloat() * size.height)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, yAvg),
                    end = Offset(size.width, yAvg),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                )

                val path = Path()
                data.forEachIndexed { i, d ->
                    val x = ((d.timestamp - minTs).toFloat() / tsRange) * size.width
                    val y = size.height - (((d.value - min) / range).toFloat() * size.height)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(Color(0xFF0B6E4F), radius = 5f, center = Offset(x, y))
                }
                drawPath(path, Color(0xFF0B6E4F), style = Stroke(4f))
                drawLine(Color.Black.copy(alpha = 0.5f), Offset(0f, size.height), Offset(size.width, size.height), 2f)
            }
        }
        
        if (data.isNotEmpty()) {
            // Ajustamos las fechas para que se alineen con el canvas (considerando el ancho del eje Y)
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(45.dp))
                DateAxisLabels(dates = axisDates, modifier = Modifier.weight(1f))
            }

            val last = data.last()
            val first = data.first()
            Spacer(Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊 Análisis de Evolución", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rango detectado: ${"%.1f".format(data.map { it.value }.minOrNull() ?: 0.0)} a ${"%.1f".format(data.map { it.value }.maxOrNull() ?: 0.0)} ${last.unit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    val trend = when {
                        last.value > first.value + 0.5 -> "📈 Tendencia al alza"
                        last.value < first.value - 0.5 -> "📉 Tendencia a la baja"
                        else -> "➡️ Estado estable"
                    }
                    Text(trend, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}



@Composable
private fun DateAxisLabels(
    dates: List<String>,
    modifier: Modifier = Modifier,
    maxLabels: Int = 4
) {
    val outputDateFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }
    val inputDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val labels = remember(dates, maxLabels) {
        val validDates = dates.filter { it.isNotBlank() }
        if (validDates.isEmpty()) emptyList()
        else {
            val unique = validDates.distinct().map { rawDate ->
                try {
                    inputDateFormat.parse(rawDate)?.let(outputDateFormat::format) ?: rawDate
                } catch (e: Exception) {
                    "Dato" // Texto genérico si la fecha está corrupta
                }
            }
            if (unique.size <= maxLabels && unique.size >= 4) unique
            else if (unique.size < 4) {
                unique
            }
            else {
                val steps = (maxLabels - 1).coerceAtLeast(1)
                (0..steps).map { step ->
                    val idx = (step * (unique.lastIndex.toFloat() / steps)).toInt()
                    unique[idx]
                }.distinct()
            }
        }
    }

    if (labels.isNotEmpty()) {
        Row(modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ComparisonChart(data: List<EnvironmentalData>) {
    val colors = mapOf(
        "temperature" to Color(0xFFE53935),
        "humidity" to Color(0xFF1E88E5),
        "wind" to Color(0xFF43A047)
    )
    val groupedByVariable = data
        .groupBy { it.variable }
        .mapValues { (_, values) -> values.sortedBy { it.timestamp }.takeLast(192) }
    val allValues = groupedByVariable.values.flatten().map(EnvironmentalData::value)
    val rawMin = allValues.minOrNull() ?: 0.0
    val rawMax = allValues.maxOrNull() ?: 1.0
    val padding = ((rawMax - rawMin) * 0.1).coerceAtLeast(1.0)
    val min = rawMin - padding
    val max = rawMax + padding
    val range = (max - min).takeIf { it > 0 } ?: 1.0
    val timelineDates = remember(groupedByVariable) {
        groupedByVariable.values
            .flatten()
            .sortedBy { it.timestamp }
            .map { it.date }
    }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            YAxisLabels(min = min, max = max)
            
            Canvas(Modifier.weight(1f).height(220.dp).background(Color(0xFFF8F9FA))) {
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = i * size.height / gridLines
                    drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(0f, y), Offset(size.width, y), 1f)
                }

                groupedByVariable.entries.forEach { (variable, points) ->
                    if (points.size < 2) return@forEach
                    val color = colors[variable] ?: Color.DarkGray
                    val path = Path()
                    
                    val pointCount = points.size
                    points.forEachIndexed { i, point ->
                        val x = i * size.width / (pointCount - 1).coerceAtLeast(1)
                        val y = size.height - (((point.value - min) / range).toFloat() * size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path = path, color = color.copy(alpha = 0.8f), style = Stroke(width = 3.5f))
                }
                drawLine(Color.Gray, Offset(0f, size.height), Offset(size.width, size.height), 1.5f)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(45.dp))
            DateAxisLabels(dates = timelineDates, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            groupedByVariable.keys.forEach { variable ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(colors[variable] ?: Color.DarkGray))
                    Spacer(Modifier.width(4.dp))
                    Text(variableLabels[variable] ?: variable, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Conclusión comparativa:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(
                    "Esta gráfica permite observar la correlación entre variables. Por ejemplo, en San Carlos es usual que al subir la temperatura, la humedad descienda significativamente.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                val latestValuesText = groupedByVariable.entries
                    .sortedBy { it.key }
                    .joinToString(" | ") { (variable, points) ->
                        val latest = points.lastOrNull()?.value ?: 0.0
                        "${variableLabels[variable] ?: variable}: ${"%.1f".format(latest)}"
                    }
                Text("Lectura actual: $latestValuesText", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun YAxisLabels(min: Double, max: Double) {
    val mid = (min + max) / 2.0
    Column(
        Modifier.height(220.dp).width(45.dp).padding(end = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Text("%.1f".format(max), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text("%.1f".format(mid), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Text("%.1f".format(min), style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}

@Composable
private fun ValueScale(min: Double, max: Double) {
    // Componente obsoleto, ahora usamos YAxisLabels a la izquierda
}
