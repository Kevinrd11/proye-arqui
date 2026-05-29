package com.example.iotambientaltec.data.mock

import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.LocationPoint
import com.example.iotambientaltec.utils.DateUtils
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object MockDataGenerator {
    fun generateEnvironmentalData(days: Int = 10): List<EnvironmentalData> {
        val random = Random(42)
        val data = mutableListOf<EnvironmentalData>()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(days - 1))
        }
        repeat(days) { dayIndex ->
            repeat(48) { slot ->
                val hour = slot / 2
                val minute = if (slot % 2 == 0) 0 else 30
                calendar.set(Calendar.HOUR_OF_DAY, hour); calendar.set(Calendar.MINUTE, minute)
                val date = DateUtils.formatDate(calendar)
                val time = DateUtils.formatTime(calendar)
                val daytimeCurve = sin(((hour - 6).coerceAtLeast(0) / 14.0).coerceIn(0.0, 1.0) * PI)
                
                // Temperaturas realistas para San Carlos (22°C - 32°C aprox)
                val temp = (23.0 + 8.0 * daytimeCurve + random.nextDouble(-0.5, 0.5)).coerceIn(22.0, 33.0)
                
                // Humedad inversa a la temperatura (alta en la noche, baja en el día)
                val humidity = (90.0 - 25.0 * daytimeCurve + random.nextDouble(-2.0, 2.0)).coerceIn(60.0, 95.0)
                
                // Viento con ráfagas por la tarde
                val windCurve = if (hour in 10..18) 1.0 else 0.2
                val wind = (5.0 + 10.0 * windCurve * random.nextDouble(0.5, 1.5)).coerceIn(2.0, 25.0)
                val baseId = "D${dayIndex}_T${slot}"
                data += EnvironmentalData("TEMP_$baseId", calendar.timeInMillis, date, time, "temperature", temp, "°C", "TEC San Carlos", "SENSOR_TEMP_001")
                data += EnvironmentalData("HUM_$baseId", calendar.timeInMillis, date, time, "humidity", humidity, "%", "TEC San Carlos", "SENSOR_HUM_002")
                data += EnvironmentalData("WIND_$baseId", calendar.timeInMillis, date, time, "wind", wind, "km/h", "TEC San Carlos", "SENSOR_WIND_003")
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return data.sortedBy { it.timestamp }
    }

    fun locationPoints() = listOf(
        LocationPoint("dir", "Dirección de Sede", "Administración principal del campus.", .44f, .22f, 25.1, 87.0, 7.2, "SENSOR_DIR"),
        LocationPoint("devesa", "DEVESA", "Vida estudiantil y servicios de apoyo.", .31f, .36f, 24.8, 88.4, 6.8, "SENSOR_DEVESA"),
        LocationPoint("comp", "Oficinas de Computación", "Coordinación y oficinas académicas.", .58f, .39f, 25.6, 86.3, 8.1, "SENSOR_COMP"),
        LocationPoint("exactas", "Ciencias Exactas", "Área de cursos básicos y laboratorios.", .68f, .28f, 26.0, 85.9, 7.7, "SENSOR_EXA"),
        LocationPoint("comedor", "Comedor Institucional", "Zona de alimentación estudiantil.", .48f, .55f, 25.3, 89.2, 6.5, "SENSOR_COM"),
        LocationPoint("labs", "Laboratorios de Computación", "Laboratorios para cursos y proyectos.", .63f, .61f, 24.9, 88.1, 7.9, "SENSOR_LAB"),
        LocationPoint("biblio", "Biblioteca", "Espacio de estudio y recursos.", .39f, .67f, 24.6, 90.0, 6.2, "SENSOR_BIB"),
        LocationPoint("entrada", "Entrada Principal", "Acceso principal al TEC San Carlos.", .18f, .82f, 26.4, 84.8, 9.0, "SENSOR_ENT"),
        LocationPoint("ctec", "CTEC", "Centro de Transferencia y Educación Continua.", .78f, .76f, 25.7, 86.8, 8.5, "SENSOR_CTEC")
    )
}
