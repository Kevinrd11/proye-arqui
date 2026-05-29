package com.example.iotambientaltec.domain.usecase

import com.example.iotambientaltec.data.repository.EnvironmentalRepository

class EnvironmentalUseCases(private val repository: EnvironmentalRepository) {
    fun allData() = repository.getAllData()
    fun latestData() = repository.getLatestData()
    fun dataByRange(start: String, end: String) = repository.getDataByDateRange(start, end)
    fun averageByHour(date: String, variable: String) = repository.getAverageByHour(date, variable)
    fun averageByDay(start: String, end: String, variable: String) = repository.getAverageByDay(start, end, variable)
    fun max(variable: String) = repository.getHistoricalMax(variable)
    fun min(variable: String) = repository.getHistoricalMin(variable)
}
