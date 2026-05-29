package com.example.iotambientaltec.utils

import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.QueryResult

object EnvironmentalStats {
    fun filterByVariable(data: List<EnvironmentalData>, variable: String) = data.filter { it.variable.trim() == variable }
    fun filterByDateRange(data: List<EnvironmentalData>, start: String, end: String) = data.filter { 
        val cleanDate = it.date.trim().removeSuffix(".")
        cleanDate in start..end 
    }

    fun averageByHour(data: List<EnvironmentalData>, date: String, variable: String): List<QueryResult> =
        data.filter { 
            val cleanDate = it.date.trim().removeSuffix(".")
            cleanDate == date && it.variable.trim() == variable 
        }
            .groupBy { it.time.trim().substringBefore(":") + ":00" }
            .toSortedMap()
            .map { (hour, values) -> values.toAverageResult(hour, variable, date = date, time = hour) }

    fun averageByDay(data: List<EnvironmentalData>, start: String, end: String, variable: String): List<QueryResult> =
        data.filter { 
            val cleanDate = it.date.trim().removeSuffix(".")
            it.variable.trim() == variable && cleanDate in start..end 
        }
            .groupBy { it.date.trim().removeSuffix(".") }
            .toSortedMap()
            .map { (day, values) -> values.toAverageResult(day, variable, date = day) }

    fun historicalMax(data: List<EnvironmentalData>, variable: String): QueryResult? =
        data.filter { it.variable == variable }.maxByOrNull { it.value }?.toQueryResult("Máximo histórico")

    fun historicalMin(data: List<EnvironmentalData>, variable: String): QueryResult? =
        data.filter { it.variable == variable }.minByOrNull { it.value }?.toQueryResult("Mínimo histórico")

    fun detectOutliers(data: List<EnvironmentalData>): List<EnvironmentalData> {
        if (data.isEmpty()) return emptyList()
        val avg = data.map { it.value }.average()
        val sd = kotlin.math.sqrt(data.map { (it.value - avg) * (it.value - avg) }.average())
        return data.filter { kotlin.math.abs(it.value - avg) > 2 * sd }
    }

    private fun List<EnvironmentalData>.toAverageResult(label: String, variable: String, date: String = "", time: String = "") =
        QueryResult(label, variable, map { it.value }.average(), first().unit, date, time, first().sensorId)

    private fun EnvironmentalData.toQueryResult(label: String) = QueryResult(label, variable, value, unit, date, time, sensorId)
}
