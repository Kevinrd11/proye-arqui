package com.example.iotambientaltec.data.model

data class LocationPoint(
    val id: String,
    val name: String,
    val description: String,
    val x: Float,
    val y: Float,
    val temperature: Double,
    val humidity: Double,
    val windSpeed: Double,
    val sensorId: String
)
