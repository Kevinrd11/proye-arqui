package com.example.iotambientaltec.data.model

data class EnvironmentalData(
    val id: String = "",
    val timestamp: Long = 0L,
    val date: String = "",
    val time: String = "",
    val variable: String = "",
    val value: Double = 0.0,
    val unit: String = "",
    val location: String = "",
    val sensorId: String = ""
)

data class QueryResult(
    val label: String,
    val variable: String,
    val value: Double,
    val unit: String,
    val date: String = "",
    val time: String = "",
    val sensorId: String = ""
)
