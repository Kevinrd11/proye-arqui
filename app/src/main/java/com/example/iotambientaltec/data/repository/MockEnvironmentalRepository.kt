package com.example.iotambientaltec.data.repository

import com.example.iotambientaltec.data.mock.MockDataGenerator
import com.example.iotambientaltec.data.model.EnvironmentalData
import com.example.iotambientaltec.data.model.QueryResult
import com.example.iotambientaltec.utils.EnvironmentalStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockEnvironmentalRepository : EnvironmentalRepository {
    private val data = MockDataGenerator.generateEnvironmentalData()
    override fun getAllData(): Flow<List<EnvironmentalData>> = flowOf(data)
    override fun getLatestData(): Flow<List<EnvironmentalData>> = flowOf(data.groupBy { it.variable }.mapNotNull { it.value.maxByOrNull(EnvironmentalData::timestamp) })
    override fun getDataByDateRange(startDate: String, endDate: String): Flow<List<EnvironmentalData>> = flowOf(EnvironmentalStats.filterByDateRange(data, startDate, endDate))
    override fun getAverageByHour(date: String, variable: String): Flow<List<QueryResult>> = flowOf(EnvironmentalStats.averageByHour(data, date, variable))
    override fun getAverageByDay(startDate: String, endDate: String, variable: String): Flow<List<QueryResult>> = flowOf(EnvironmentalStats.averageByDay(data, startDate, endDate, variable))
    override fun getHistoricalMax(variable: String): Flow<QueryResult?> = flowOf(EnvironmentalStats.historicalMax(data, variable))
    override fun getHistoricalMin(variable: String): Flow<QueryResult?> = flowOf(EnvironmentalStats.historicalMin(data, variable))
}
