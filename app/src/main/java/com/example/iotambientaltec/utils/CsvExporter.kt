package com.example.iotambientaltec.utils

import com.example.iotambientaltec.data.model.QueryResult

object CsvExporter {
    fun queryResultsToCsv(results: List<QueryResult>): String = buildString {
        appendLine("etiqueta,variable,valor,unidad,fecha,hora,sensor")
        results.forEach { appendLine("${it.label},${it.variable},${"%.2f".format(it.value)},${it.unit},${it.date},${it.time},${it.sensorId}") }
    }
}
