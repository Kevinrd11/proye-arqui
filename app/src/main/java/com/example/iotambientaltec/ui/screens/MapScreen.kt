package com.example.iotambientaltec.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.iotambientaltec.data.model.LocationPoint

@Composable
fun MapScreen(factory: AppViewModelFactory) {
    val vm: MapViewModel = viewModel(factory = factory)
    var selected by remember { mutableStateOf<LocationPoint?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Mapa interactivo TEC San Carlos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        BoxWithConstraints(Modifier.fillMaxWidth().height(360.dp)) {
            Canvas(Modifier.matchParentSize().clickable { }) {
                drawRoundRect(Color(0xFFE8F5E9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f, 28f))
                drawLine(Color(0xFF90A4AE), Offset(size.width * .15f, size.height * .85f), Offset(size.width * .85f, size.height * .18f), 8f)
                vm.points.forEach { p -> drawCircle(Color(0xFF0B6E4F), 16f, Offset(size.width * p.x, size.height * p.y)); drawCircle(Color.White, 6f, Offset(size.width * p.x, size.height * p.y)) }
            }
            vm.points.forEach { p -> Box(Modifier.offset(maxWidth * p.x - 24.dp, maxHeight * p.y - 24.dp).size(48.dp).clickable { selected = p }) }
        }
        selected?.let { p -> ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(p.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(p.description); Text("Temperatura: ${p.temperature} °C"); Text("Humedad: ${p.humidity} %"); Text("Viento: ${p.windSpeed} km/h"); Text("Sensor asociado: ${p.sensorId}") } } } ?: Text("Toque un punto del mapa para ver el sensor asociado.")
    }
}
